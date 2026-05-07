package com.nexussms.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import com.nexussms.data.models.Conversation
import com.nexussms.data.models.Message
import com.nexussms.data.repository.ConversationRepository
import com.nexussms.data.repository.MessageRepository
import com.nexussms.security.EncryptionManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SmsReceiver : BroadcastReceiver() {

    @Inject
    lateinit var messageRepository: MessageRepository

    @Inject
    lateinit var conversationRepository: ConversationRepository

    @Inject
    lateinit var encryptionManager: EncryptionManager

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
                    val rawBody = smsMessage.messageBody ?: ""
                    val timestamp = smsMessage.timestampMillis
                    val isEncryptedPayload = rawBody.startsWith("ENC:")
                    val messageBody = if (isEncryptedPayload) {
                        encryptionManager.decryptAES256(rawBody.removePrefix("ENC:"))
                    } else {
                        rawBody
                    }

                    // Look up or create the conversation.
                    val conversation = conversationRepository.findConversationWithParticipant(senderPhoneNumber)
                    val conversationId = if (conversation == null) {
                        val newConversation = Conversation(
                            participantPhoneNumbers = senderPhoneNumber,
                            displayName = senderPhoneNumber,
                            lastMessage = messageBody,
                            lastMessageTime = timestamp
                        )
                        conversationRepository.insertConversation(newConversation)
                        newConversation.id
                    } else {
                        conversation.id
                    }

                    // Save the inbound message.
                    val message = Message(
                        conversationId = conversationId,
                        senderPhoneNumber = senderPhoneNumber,
                        recipientPhoneNumber = "self",
                        content = messageBody,
                        timestamp = timestamp,
                        status = "DELIVERED",
                        type = "TEXT",
                        isEncrypted = isEncryptedPayload,
                        encryptionAlgorithm = if (isEncryptedPayload) "AES256" else null
                    )
                    messageRepository.insertMessage(message)

                    // Update conversation metadata.
                    val current = conversationRepository.getConversationById(conversationId)
                    if (current != null) {
                        conversationRepository.updateConversation(
                            current.copy(
                                lastMessage = messageBody,
                                lastMessageTime = timestamp,
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
