package com.nexusmedia.nexussms.services

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.telephony.SmsManager
import com.nexusmedia.nexussms.data.models.Message
import com.nexusmedia.nexussms.data.repository.MessageRepository
import com.nexusmedia.nexussms.features.messaging.SimSelector
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SmsSender @Inject constructor(
    @ApplicationContext private val context: Context,
    private val messageRepository: MessageRepository,
    private val simSelector: SimSelector
) {
    suspend fun sendTextMessage(
        conversationId: String,
        recipientPhone: String,
        content: String,
        existingMessageId: String? = null,
        persistToDb: Boolean = true,
        subscriptionId: Int? = null
    ): Result<String> = withContext(Dispatchers.IO) {
        val messageId = existingMessageId ?: UUID.randomUUID().toString()
        val message = Message(
            id = messageId,
            conversationId = conversationId,
            senderPhoneNumber = "self",
            recipientPhoneNumber = recipientPhone,
            content = content,
            timestamp = System.currentTimeMillis(),
            type = "TEXT",
            status = "SENDING",
            isEncrypted = false
        )

        if (persistToDb) {
            messageRepository.insertMessage(message)
        }

        try {
            val smsManager = resolveSmsManager(subscriptionId)
            val parts = smsManager.divideMessage(content)
            val sentIntents = ArrayList<PendingIntent>()
            val deliveredIntents = ArrayList<PendingIntent>()

            for (i in parts.indices) {
                sentIntents.add(
                    PendingIntent.getBroadcast(
                        context,
                        messageId.hashCode() + i,
                        Intent(ACTION_SMS_SENT)
                            .setPackage(context.packageName)
                            .putExtra(EXTRA_MESSAGE_ID, messageId),
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                )
                deliveredIntents.add(
                    PendingIntent.getBroadcast(
                        context,
                        messageId.hashCode() + i + 10_000,
                        Intent(ACTION_SMS_DELIVERED)
                            .setPackage(context.packageName)
                            .putExtra(EXTRA_MESSAGE_ID, messageId),
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                )
            }

            smsManager.sendMultipartTextMessage(
                recipientPhone,
                null,
                parts,
                sentIntents,
                deliveredIntents
            )
            Result.success(messageId)
        } catch (e: Exception) {
            Timber.e(e, "SMS send failed")
            if (persistToDb) {
                messageRepository.updateMessage(message.copy(status = "FAILED"))
            }
            Result.failure(e)
        }
    }

    suspend fun applySentResult(messageId: String, success: Boolean) {
        val message = messageRepository.getMessageById(messageId) ?: return
        messageRepository.updateMessage(
            message.copy(status = if (success) "SENT" else "FAILED")
        )
    }

    suspend fun applyDeliveredResult(messageId: String, delivered: Boolean) {
        val message = messageRepository.getMessageById(messageId) ?: return
        if (delivered && message.status != "FAILED") {
            messageRepository.updateMessage(message.copy(status = "DELIVERED"))
        }
    }

    private fun resolveSmsManager(subscriptionId: Int? = null): SmsManager {
        if (subscriptionId != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            return try {
                val simInfo = SimSelector.SimInfo(
                    slotIndex = 0,
                    displayName = "",
                    phoneNumber = null,
                    subscriptionId = subscriptionId,
                    carrierName = ""
                )
                simSelector.getSmsManagerForSim(simInfo)
            } catch (e: Exception) {
                Timber.w(e, "Failed to get SMS manager for SIM subscription %d, falling back to default", subscriptionId)
                getDefaultSmsManager()
            }
        }
        return getDefaultSmsManager()
    }

    private fun getDefaultSmsManager(): SmsManager {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            context.getSystemService(SmsManager::class.java)
        } else {
            @Suppress("DEPRECATION")
            SmsManager.getDefault()
        }
    }

    companion object {
        const val ACTION_SMS_SENT = "com.nexusmedia.nexussms.SMS_SENT"
        const val ACTION_SMS_DELIVERED = "com.nexusmedia.nexussms.SMS_DELIVERED"
        const val EXTRA_MESSAGE_ID = "message_id"
        const val EXTRA_CONVERSATION_ID = "conversation_id"
    }
}
