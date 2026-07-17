package com.nexusmedia.nexussms.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.ContactsContract
import android.provider.Telephony
import com.nexusmedia.nexussms.data.models.Conversation
import com.nexusmedia.nexussms.data.models.Message
import com.nexusmedia.nexussms.data.repository.ConversationRepository
import com.nexusmedia.nexussms.data.repository.MessageRepository
import com.nexusmedia.nexussms.features.notifications.PerContactNotificationSettings
import com.nexusmedia.nexussms.features.security.SpamBlocklistManager
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
import timber.log.Timber
import java.util.UUID
import javax.inject.Inject

@AndroidEntryPoint
class SmsReceiver : BroadcastReceiver() {

    @Inject lateinit var messageRepository: MessageRepository
    @Inject lateinit var conversationRepository: ConversationRepository
    @Inject lateinit var encryptionManager: EncryptionManager
    @Inject lateinit var smsNotificationHelper: SmsNotificationHelper
    @Inject lateinit var perContactNotificationSettings: PerContactNotificationSettings
    @Inject lateinit var spamBlocklistManager: SpamBlocklistManager
    @Inject lateinit var ruleEngine: RuleEngine
    @Inject lateinit var actionExecutor: ActionExecutor
    @Inject lateinit var automationDao: com.nexusmedia.nexussms.features.automation.AutomationDao

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return
        val rawSmsMessages = Telephony.Sms.Intents.getMessagesFromIntent(intent) ?: return
        if (context == null) return

        val pendingResult = goAsync()
        val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

        scope.launch {
            try {
                val grouped = groupMultiPartMessages(rawSmsMessages)

                for ((senderPhoneNumber, fullBody, timestamp) in grouped) {
                    processOneMessage(
                        context = context,
                        senderPhoneNumber = senderPhoneNumber,
                        rawBody = fullBody,
                        timestamp = timestamp
                    )
                }
            } catch (e: Exception) {
                Timber.e(e, "SmsReceiver: unexpected error processing batch")
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun groupMultiPartMessages(
        rawMessages: Array<android.telephony.SmsMessage>
    ): List<Triple<String, String, Long>> {
        data class MessageGroup(
            val address: String,
            val bodies: MutableList<String>,
            val timestamp: Long
        )

        val groups = LinkedHashMap<String, MessageGroup>()

        for (sms in rawMessages) {
            val address = sms.originatingAddress ?: continue
            val body = sms.messageBody ?: continue

            val existing = groups[address]
            if (existing != null) {
                existing.bodies.add(body)
            } else {
                groups[address] = MessageGroup(
                    address = address,
                    bodies = mutableListOf(body),
                    timestamp = sms.timestampMillis
                )
            }
        }

        return groups.values.map { group ->
            Triple(group.address, group.bodies.joinToString(""), group.timestamp)
        }
    }

    private suspend fun processOneMessage(
        context: Context,
        senderPhoneNumber: String,
        rawBody: String,
        timestamp: Long
    ) {
        try {
            val isEncryptedPayload = rawBody.startsWith("ENC:")
            val messageBody = if (isEncryptedPayload) {
                try {
                    encryptionManager.decryptAES256(rawBody.removePrefix("ENC:"))
                } catch (e: Exception) {
                    Timber.w(e, "Failed to decrypt ENC: message from %s, storing raw", senderPhoneNumber)
                    "[Encrypted message - unable to decrypt]"
                }
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

            val sourceSmsId = lookupSourceSmsId(context, senderPhoneNumber, timestamp)

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
                encryptionAlgorithm = if (isEncryptedPayload) "AES256" else null,
                sourceSmsId = sourceSmsId
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
            } catch (e: Exception) { Timber.w(e, "Automation rule evaluation failed") }

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
            if (perContactPrivacy != PerContactNotificationSettings.PRIVACY_NONE) {
                return
            }

            if (!skipNotification && !conversationForNotify.isBlocked && !conversationForNotify.isMuted) {
                smsNotificationHelper.showIncomingMessageNotification(
                    conversation = conversationForNotify,
                    senderPhone = senderPhoneNumber,
                    messageBody = messageBody,
                    messageId = messageId
                )
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to process SMS from %s", senderPhoneNumber)
        }
    }

    private fun lookupSourceSmsId(context: Context, address: String, timestamp: Long): Long? {
        val cursor = try {
            context.contentResolver.query(
                Telephony.Sms.CONTENT_URI,
                arrayOf(Telephony.Sms._ID),
                "${Telephony.Sms.ADDRESS} = ? AND ${Telephony.Sms.DATE} = ?",
                arrayOf(address, timestamp.toString()),
                null
            )
        } catch (e: SecurityException) {
            Timber.w(e, "Permission denied looking up source SMS ID")
            null
        }

        return cursor?.use {
            if (it.moveToFirst()) it.getLong(0) else null
        }
    }
}
