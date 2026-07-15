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
import com.nexusmedia.nexussms.features.security.SpamBlocklistManager
import com.nexusmedia.nexussms.features.security.SpamDetector
import com.nexusmedia.nexussms.features.security.SpamAction
import com.nexusmedia.nexussms.features.automation.RuleEngine
import com.nexusmedia.nexussms.features.automation.ActionExecutor
import com.nexusmedia.nexussms.features.automation.IncomingMessage
import com.nexusmedia.nexussms.security.EncryptionManager
import com.nexusmedia.nexussms.services.SmsNotificationHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
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
    @Inject lateinit var spamDetector: SpamDetector
    @Inject lateinit var spamBlocklistManager: SpamBlocklistManager
    @Inject lateinit var ruleEngine: RuleEngine
    @Inject lateinit var actionExecutor: ActionExecutor
    @Inject lateinit var automationDao: com.nexusmedia.nexussms.features.automation.AutomationDao

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

                    val spamAction = spamBlocklistManager.handleSpamDetection(
                        message = messageBody,
                        senderNumber = senderPhoneNumber,
                        conversationId = null
                    )

                    val isSpamBlocked = spamAction == SpamAction.BLOCKED
                    var skipNotification = isSpamBlocked

                    val existing = conversationRepository.findConversationWithParticipant(senderPhoneNumber)
                    val conversationId: String
                    val conversationForNotify: Conversation

                    if (existing == null) {
                        val newConversation = Conversation(
                            participantPhoneNumbers = senderPhoneNumber,
                            displayName = senderPhoneNumber,
                            lastMessage = if (isSpamBlocked) "" else messageBody,
                            lastMessageTime = timestamp,
                            unreadCount = if (isSpamBlocked) 0 else 1
                        )
                        conversationRepository.insertConversation(newConversation)
                        conversationId = newConversation.id
                        conversationForNotify = newConversation
                    } else {
                        conversationId = existing.id
                        if (existing.isBlocked) {
                            skipNotification = true
                        }
                        if (!existing.isBlocked) {
                            val updated = existing.copy(
                                lastMessage = messageBody,
                                lastMessageTime = timestamp,
                                unreadCount = existing.unreadCount + 1
                            )
                            conversationRepository.updateConversation(updated)
                            conversationForNotify = updated
                        } else {
                            conversationForNotify = existing
                        }
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

                    try {
                        val enabledEntities = automationDao.getEnabledRules().first()
                        val enabledRules = enabledEntities.map { entity ->
                            com.nexusmedia.nexussms.features.automation.MessageRule(
                                id = entity.id,
                                name = entity.name,
                                description = entity.description,
                                isEnabled = entity.isEnabled,
                                priority = entity.priority,
                                senderPattern = entity.senderPattern,
                                contentPattern = entity.contentPattern,
                                platform = entity.platform,
                                timeRangeStart = entity.timeRangeStart,
                                timeRangeEnd = entity.timeRangeEnd,
                                minMessageLength = entity.minMessageLength,
                                maxMessageLength = entity.maxMessageLength,
                                actions = emptyList(),
                                createdAt = entity.createdAt,
                                lastTriggered = entity.lastTriggered,
                                triggerCount = entity.triggerCount,
                                lastTriggeredMessage = entity.lastTriggeredMessage
                            )
                        }
                        if (enabledRules.isNotEmpty()) {
                            val incoming = IncomingMessage(
                                id = messageId,
                                senderNumber = senderPhoneNumber,
                                senderName = null,
                                content = messageBody,
                                platform = "SMS",
                                timestamp = timestamp,
                                conversationId = conversationId
                            )
                            ruleEngine.evaluateMessage(incoming, enabledRules) { action, msg ->
                                actionExecutor.execute(action, msg)
                            }
                        }
                    } catch (_: Exception) { }

                    if (spamAction == SpamAction.WARNING) {
                        val spamEnabled = spamBlocklistManager.spamNotificationEnabled.first()
                        if (spamEnabled) {
                            smsNotificationHelper.showSpamWarningNotification(
                                senderPhone = senderPhoneNumber,
                                messageBody = messageBody,
                                messageId = messageId
                            )
                        }
                    }

                    val perContactPrivacy = perContactNotificationSettings.getPrivacyLevel(conversationId)
                    if (perContactPrivacy == PerContactNotificationSettings.PRIVACY_NONE) {
                        continue
                    }

                    if (!skipNotification && !conversationForNotify.isBlocked && !conversationForNotify.isMuted) {
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
