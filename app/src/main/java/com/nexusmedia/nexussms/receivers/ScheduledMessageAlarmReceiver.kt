package com.nexusmedia.nexussms.receivers

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.nexusmedia.nexussms.data.repository.ConversationRepository
import com.nexusmedia.nexussms.data.repository.ScheduledMessageRepository
import com.nexusmedia.nexussms.services.SmsSender
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class ScheduledMessageAlarmReceiver : BroadcastReceiver() {

    @Inject lateinit var smsSender: SmsSender
    @Inject lateinit var conversationRepository: ConversationRepository
    @Inject lateinit var scheduledMessageRepository: ScheduledMessageRepository

    override fun onReceive(context: Context, intent: Intent) {
        val scheduledMsgId = intent.getStringExtra(EXTRA_SCHEDULED_MSG_ID) ?: return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val scheduled = scheduledMessageRepository.getScheduledMessageById(scheduledMsgId)
                if (scheduled == null) {
                    Timber.w("Scheduled message %s not found in DB, alarm is stale", scheduledMsgId)
                    return@launch
                }

                val conversationId = scheduled.conversationId
                val recipientPhone = scheduled.recipientPhoneNumber
                val content = scheduled.content
                val repeatType = scheduled.repeatType
                val repeatUntil = scheduled.repeatUntil ?: -1L

                val result = smsSender.sendTextMessage(
                    conversationId = conversationId,
                    recipientPhone = recipientPhone,
                    content = content,
                    persistToDb = true
                )

                if (result.isSuccess) {
                    val sentAt = System.currentTimeMillis()
                    val nextScheduledTime = computeNextScheduleTime(repeatType, scheduled.scheduledTime)
                    if (nextScheduledTime != null &&
                        (repeatUntil <= 0 || nextScheduledTime <= repeatUntil)
                    ) {
                        scheduledMessageRepository.updateScheduledMessage(
                            scheduled.copy(
                                scheduledTime = nextScheduledTime,
                                status = "PENDING",
                                sentAt = sentAt,
                                failureReason = null
                            )
                        )
                    } else {
                        scheduledMessageRepository.updateScheduledMessage(
                            scheduled.copy(status = "SENT", sentAt = sentAt, failureReason = null)
                        )
                    }

                    conversationRepository.getConversationById(conversationId)?.let { conv ->
                        conversationRepository.updateConversation(
                            conv.copy(lastMessage = content, lastMessageTime = sentAt)
                        )
                    }

                    scheduleNextAlarmIfRepeating(context, scheduledMsgId, repeatType, repeatUntil, System.currentTimeMillis())
                } else {
                    Timber.e(result.exceptionOrNull(), "Alarm send failed for message %s", scheduledMsgId)
                }
            } catch (e: Exception) {
                Timber.e(e, "ScheduledMessageAlarmReceiver failed for message %s", scheduledMsgId)
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun computeNextScheduleTime(
        repeatType: String?,
        currentScheduledTime: Long
    ): Long? {
        if (repeatType == null || repeatType == "NONE") return null
        val cal = java.util.Calendar.getInstance().apply { timeInMillis = currentScheduledTime }
        return when (repeatType) {
            "DAILY" -> { cal.add(java.util.Calendar.DAY_OF_YEAR, 1); cal.timeInMillis }
            "WEEKLY" -> { cal.add(java.util.Calendar.WEEK_OF_YEAR, 1); cal.timeInMillis }
            "MONTHLY" -> { cal.add(java.util.Calendar.MONTH, 1); cal.timeInMillis }
            else -> null
        }
    }

    private fun scheduleNextAlarmIfRepeating(
        context: Context,
        scheduledMsgId: String,
        repeatType: String?,
        repeatUntil: Long,
        currentScheduledTime: Long
    ) {
        val nextTime = computeNextScheduleTime(repeatType, currentScheduledTime) ?: return
        if (repeatUntil > 0 && nextTime > repeatUntil) return
        scheduleExactAlarm(context, scheduledMsgId, nextTime, repeatType, repeatUntil)
    }

    companion object {
        const val EXTRA_SCHEDULED_MSG_ID = "extra_scheduled_msg_id"

        suspend fun rescheduleAllPendingAlarms(
            context: Context,
            scheduledMessageRepository: ScheduledMessageRepository
        ) {
            val pendingMessages = scheduledMessageRepository.getAllPendingScheduledMessages()
            var rescheduled = 0
            for (msg in pendingMessages) {
                if (msg.scheduledTime > System.currentTimeMillis()) {
                    scheduleExactAlarm(
                        context = context,
                        scheduledMsgId = msg.id,
                        triggerAtMillis = msg.scheduledTime,
                        repeatType = msg.repeatType,
                        repeatUntil = msg.repeatUntil ?: -1L
                    )
                    rescheduled++
                } else {
                    Timber.w("Scheduled message %s is overdue (was %d), skipping", msg.id, msg.scheduledTime)
                }
            }
            Timber.d("Boot reschedule: %d of %d pending alarms re-registered", rescheduled, pendingMessages.size)
        }

        fun scheduleExactAlarm(
            context: Context,
            scheduledMsgId: String,
            triggerAtMillis: Long,
            repeatType: String? = null,
            repeatUntil: Long = -1L
        ) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, ScheduledMessageAlarmReceiver::class.java).apply {
                putExtra(EXTRA_SCHEDULED_MSG_ID, scheduledMsgId)
            }
            val requestCode = scheduledMsgId.hashCode()
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            try {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            } catch (e: SecurityException) {
                Timber.w(e, "SCHEDULE_EXACT_ALARM not granted, falling back to inexact")
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            }
        }
    }
}
