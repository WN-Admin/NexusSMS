package com.nexussms.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import com.nexussms.data.models.Conversation
import com.nexussms.data.models.Message
import com.nexussms.data.repository.ConversationRepository
import com.nexussms.data.repository.MessageRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

@AndroidEntryPoint
class SmsReceiver : BroadcastReceiver() {

    @Inject
    lateinit var messageRepository: MessageRepository

    @Inject
    lateinit var conversationRepository: ConversationRepository

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return
        val smsMessages = Telephony.Sms.Intents.getMessagesFromIntent(intent) ?: return

        // Use goAsync() so Android keeps us alive past onReceive() while we hit the DB.
        val pendingResult = goAsync()
        val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

        scope.launch {
            try {
                for (smsMessage in smsMessages) {
                    val senderPhoneNumber = smsMessage.originatingAddress ?: continue
                    val messageBody = smsMessage.messageBody ?: ""
                    val timestamp = smsMessage.timestampMillis

                    // Look up or create the conversation.
                    val existing = conversationRepository.getConversationByPhone(senderPhoneNumber).first()
                    val conversationId = existing?.id ?: conversationRepository.insertConversation(
                        Conversation(
                            participantPhone = senderPhoneNumber,
                            participantName = senderPhoneNumber,
                            lastMessage = messageBody,
                            lastMessageTime = Date(timestamp),
                            messageType = "SMS"
                        )
                    )

                    // Save the inbound message.
                    val message = Message(
                        conversationId = conversationId,
                        senderId = senderPhoneNumber,
                        recipientId = "self",
                        content = messageBody,
                        timestamp = Date(timestamp),
                        isIncoming = true,
                        isSent = true,
                        isDelivered = true,
                        messageType = "SMS"
                    )
                    messageRepository.insertMessage(message)

                    // Update conversation metadata.
                    val current = conversationRepository.getConversation(conversationId).first()
                    if (current != null) {
                        conversationRepository.updateConversation(
                            current.copy(
                                lastMessage = messageBody,
                                lastMessageTime = Date(timestamp),
                                unreadCount = current.unreadCount + 1
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                pendingResult.finish()
            }
        }
    }
}
