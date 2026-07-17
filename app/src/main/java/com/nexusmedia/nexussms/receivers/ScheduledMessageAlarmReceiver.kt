package com.nexusmedia.nexussms.receivers

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.nexusmedia.nexussms.data.database.NexusSMSDatabase
import com.nexusmedia.nexussms.data.repository.ConversationRepository
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

    override fun onReceive(context: Context, intent: Intent) {
        val messageId = intent.getLongExtra(EXTRA_MESSAGE_ID, -1L)
        val conversationId = intent.getStringExtra(EXTRA_CONVERSATION_ID) ?: return
        val recipientPhone = intent.getStringExtra(EXTRA_RECIPIENT) ?: return
        val content = intent.getStringExtra(EXTRA_CONTENT) ?: return
        val repeatType = intent.getStringExtra(EXTRA_REPEAT_TYPE)
        val repeatUntil = intent.getLongExtra(EXTRA_REPEAT_UNTIL, -1L)

        if (messageId == -1L) return

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
                    conversationRepository.getConversationById(conversationId)?.let { conv ->
                        conversationRepository.updateConversation(
                            conv.copy(lastMessage = content, lastMessageTime = sentAt)
                        )
                    }

                    scheduleNextAlarmIfRepeating(
                        context, messageId, conversationId, recipientPhone,
                        content, repeatType, repeatUntil, sentAt
                    )
                } else {
                    Timber.e(result.exceptionOrNull(), "Alarm send failed for message $messageId")
                }
            } catch (e: Exception) {
                Timber.e(e, "ScheduledMessageAlarmReceiver failed for message $messageId")
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun scheduleNextAlarmIfRepeating(
        context: Context,
        messageId: Long,
        conversationId: String,
        recipientPhone: String,
        content: String,
        repeatType: String?,
        repeatUntil: Long,
        currentScheduledTime: Long
    ) {
        if (repeatType == null || repeatType == "NONE") return

        val nextTime = when (repeatType) {
            "DAILY" -> currentScheduledTime + 24 * 60 * 60 * 1000L
            "WEEKLY" -> currentScheduledTime + 7 * 24 * 60 * 60 * 1000L
            "MONTHLY" -> {
                val cal = java.util.Calendar.getInstance().apply { timeInMillis = currentScheduledTime }
                cal.add(java.util.Calendar.MONTH, 1)
                cal.timeInMillis
            }
            else -> return
        }

        if (repeatUntil > 0 && nextTime > repeatUntil) return

        scheduleExactAlarm(context, messageId, conversationId, recipientPhone, content, nextTime, repeatType, repeatUntil)
    }

    companion object {
        const val EXTRA_MESSAGE_ID = "extra_message_id"
        const val EXTRA_CONVERSATION_ID = "extra_conversation_id"
        const val EXTRA_RECIPIENT = "extra_recipient"
        const val EXTRA_CONTENT = "extra_content"
        const val EXTRA_REPEAT_TYPE = "extra_repeat_type"
        const val EXTRA_REPEAT_UNTIL = "extra_repeat_until"

        fun scheduleExactAlarm(
            context: Context,
            messageId: Long,
            conversationId: String,
            recipientPhone: String,
            content: String,
            triggerAtMillis: Long,
            repeatType: String? = null,
            repeatUntil: Long = -1L
        ) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, ScheduledMessageAlarmReceiver::class.java).apply {
                putExtra(EXTRA_MESSAGE_ID, messageId)
                putExtra(EXTRA_CONVERSATION_ID, conversationId)
                putExtra(EXTRA_RECIPIENT, recipientPhone)
                putExtra(EXTRA_CONTENT, content)
                putExtra(EXTRA_REPEAT_TYPE, repeatType)
                putExtra(EXTRA_REPEAT_UNTIL, repeatUntil)
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                messageId.toInt(),
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
