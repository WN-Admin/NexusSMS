package com.nexusmedia.nexussms.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.RemoteInput
import com.nexusmedia.nexussms.data.repository.ConversationRepository
import com.nexusmedia.nexussms.services.SmsNotificationHelper
import com.nexusmedia.nexussms.services.SmsSender
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class NotificationActionReceiver : BroadcastReceiver() {

    @Inject lateinit var conversationRepository: ConversationRepository
    @Inject lateinit var smsSender: SmsSender
    @Inject lateinit var smsNotificationHelper: SmsNotificationHelper

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return
        val pendingResult = goAsync()
        val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

        scope.launch {
            try {
                when (intent.action) {
                    ACTION_REPLY -> handleReply(intent)
                    ACTION_MARK_READ -> handleMarkRead(intent)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    private suspend fun handleReply(intent: Intent) {
        val conversationId = intent.getStringExtra(EXTRA_CONVERSATION_ID) ?: return
        val senderPhone = intent.getStringExtra(EXTRA_SENDER_PHONE) ?: return
        val remoteInput = RemoteInput.getResultsFromIntent(intent)
        val replyText = remoteInput?.getCharSequence(KEY_REPLY_TEXT)?.toString()?.trim().orEmpty()
        if (replyText.isEmpty()) return

        smsSender.sendTextMessage(
            conversationId = conversationId,
            recipientPhone = senderPhone,
            content = replyText
        )
        conversationRepository.markConversationAsRead(conversationId)
        smsNotificationHelper.cancelNotification(conversationId)
    }

    private suspend fun handleMarkRead(intent: Intent) {
        val conversationId = intent.getStringExtra(EXTRA_CONVERSATION_ID) ?: return
        conversationRepository.markConversationAsRead(conversationId)
        smsNotificationHelper.cancelNotification(conversationId)
    }

    companion object {
        const val ACTION_REPLY = "com.nexusmedia.nexussms.NOTIFICATION_REPLY"
        const val ACTION_MARK_READ = "com.nexusmedia.nexussms.NOTIFICATION_MARK_READ"
        const val KEY_REPLY_TEXT = "reply_text"
        const val EXTRA_CONVERSATION_ID = "conversation_id"
        const val EXTRA_SENDER_PHONE = "sender_phone"
        const val EXTRA_NOTIFICATION_ID = "notification_id"
    }
}
