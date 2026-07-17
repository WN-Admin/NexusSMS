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
        val conversationId = intent.getStringExtra(EXTRA_CONVERSATION_ID) ?: return
        val recipientPhone = intent.getStringExtra(EXTRA_RECIPIENT) ?: return
        val content = intent.getStringExtra(EXTRA_CONTENT) ?: return
        val repeatType = intent.getStringExtra(EXTRA_REPEAT_TYPE)
        val repeatUntil = intent.getLongExtra(EXTRA_REPEAT_UNTIL, -1L)

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val result = smsSender.sendTextMessage(
                    conversationId = conversationId,
                    recipientPhone = recipientPhone,
                    content = content,
                    persistToDb = true
                )

                if (result.isSuccess) {
                    val sentAt = System.currentTimeMillis()
                    scheduledMessageRepository.getScheduledMessageById(scheduledMsgId)?.let { scheduled ->
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
                    } ?: Timber.w("ScheduledMessage row %s not found after alarm send", scheduledMsgId)

                    conversationRepository.getConversationById(conversationId)?.let { conv ->
                        conversationRepository.updateConversation(
                            conv.copy(lastMessage = content, lastMessageTime = sentAt)
                        )
                    }

                    scheduleNextAlarmIfRepeating(
                        context, scheduledMsgId, conversationId, recipientPhone,
                        content, repeatType, repeatUntil, System.currentTimeMillis()
                    )
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
        conversationId: String,
        recipientPhone: String,
        content: String,
        repeatType: String?,
        repeatUntil: Long,
        currentScheduledTime: Long
    ) {
        val nextTime = computeNextScheduleTime(repeatType, currentScheduledTime) ?: return
        if (repeatUntil > 0 && nextTime > repeatUntil) return
        scheduleExactAlarm(context, scheduledMsgId, conversationId, recipientPhone, content, nextTime, repeatType, repeatUntil)
    }

    companion object {
        const val EXTRA_SCHEDULED_MSG_ID = "extra_scheduled_msg_id"
        const val EXTRA_CONVERSATION_ID = "extra_conversation_id"
        const val EXTRA_RECIPIENT = "extra_recipient"
        const val EXTRA_CONTENT = "extra_content"
        const val EXTRA_REPEAT_TYPE = "extra_repeat_type"
        const val EXTRA_REPEAT_UNTIL = "extra_repeat_until"

        fun scheduleExactAlarm(
            context: Context,
            scheduledMsgId: String,
            conversationId: String,
            recipientPhone: String,
            content: String,
            triggerAtMillis: Long,
            repeatType: String? = null,
            repeatUntil: Long = -1L
        ) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, ScheduledMessageAlarmReceiver::class.java).apply {
                putExtra(EXTRA_SCHEDULED_MSG_ID, scheduledMsgId)
                putExtra(EXTRA_CONVERSATION_ID, conversationId)
                putExtra(EXTRA_RECIPIENT, recipientPhone)
                putExtra(EXTRA_CONTENT, content)
                putExtra(EXTRA_REPEAT_TYPE, repeatType)
                putExtra(EXTRA_REPEAT_UNTIL, repeatUntil)
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
