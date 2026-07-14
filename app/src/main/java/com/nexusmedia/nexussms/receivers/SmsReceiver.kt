package com.nexusmedia.nexussms.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import com.nexusmedia.nexussms.data.models.Conversation
import com.nexusmedia.nexussms.data.models.Message
import com.nexusmedia.nexussms.data.repository.ConversationRepository
import com.nexusmedia.nexussms.data.repository.MessageRepository
import com.nexusmedia.nexussms.features.notifications.PerContactNotificationSettings
import com.nexusmedia.nexussms.security.EncryptionManager
import com.nexusmedia.nexussms.services.SmsNotificationHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@AndroidEntryPoint
class SmsReceiver : BroadcastReceiver() {

    @Inject lateinit var messageRepository: MessageRepository
    @Inject lateinit var conversationRepository: ConversationRepository
    @Inject lateinit var encryptionManager: EncryptionManager
    @Inject lateinit var smsNotificationHelper: SmsNotificationHelper
    @Inject lateinit var perContactNotificationSettings: PerContactNotificationSettings

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return
        val smsMessages = Telephony.Sms.Intents.getMessagesFromIntent(intent) ?: return

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

                    val existing = conversationRepository.findConversationWithParticipant(senderPhoneNumber)
                    val conversationId: String
                    val conversationForNotify: Conversation

                    if (existing == null) {
                        val newConversation = Conversation(
                            participantPhoneNumbers = senderPhoneNumber,
                            displayName = senderPhoneNumber,
                            lastMessage = messageBody,
                            lastMessageTime = timestamp,
                            unreadCount = 1
                        )
                        conversationRepository.insertConversation(newConversation)
                        conversationId = newConversation.id
                        conversationForNotify = newConversation
                    } else {
                        conversationId = existing.id
                        if (existing.isBlocked) {
                            continue
                        }
                        val updated = existing.copy(
                            lastMessage = messageBody,
                            lastMessageTime = timestamp,
                            unreadCount = existing.unreadCount + 1
                        )
                        conversationRepository.updateConversation(updated)
                        conversationForNotify = updated
                    }

                    val messageId = UUID.randomUUID().toString()
                    val message = Message(
                        id = messageId,
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

                    val perContactPrivacy = perContactNotificationSettings.getPrivacyLevel(conversationId)
                    if (perContactPrivacy == PerContactNotificationSettings.PRIVACY_NONE) {
                        continue
                    }

                    if (!conversationForNotify.isBlocked && !conversationForNotify.isMuted) {
                        smsNotificationHelper.showIncomingMessageNotification(
                            conversation = conversationForNotify,
                            senderPhone = senderPhoneNumber,
                            messageBody = messageBody,
                            messageId = messageId
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
