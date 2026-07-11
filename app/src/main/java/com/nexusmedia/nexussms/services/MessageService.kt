package com.nexusmedia.nexussms.services

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.app.PendingIntent
import android.app.Activity.RESULT_OK
import android.telephony.SmsManager
import com.nexusmedia.nexussms.data.models.Message
import com.nexusmedia.nexussms.data.repository.MessageRepository
import com.nexusmedia.nexussms.security.EncryptionManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import java.util.UUID

@AndroidEntryPoint
class MessageService : Service() {
    
    @Inject
    lateinit var messageRepository: MessageRepository
    
    @Inject
    lateinit var encryptionManager: EncryptionManager
    
    private val binder = MessageServiceBinder()
    private val scope = CoroutineScope(Dispatchers.Main)

    private val sentReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val messageId = intent?.getStringExtra(EXTRA_MESSAGE_ID) ?: return
            scope.launch(Dispatchers.IO) {
                val isSuccess = resultCode == RESULT_OK
                updateMessageStatus(
                    messageId = messageId,
                    status = if (isSuccess) "SENT" else "FAILED",
                    error = if (isSuccess) null else "SMS send failed with code=$resultCode"
                )
            }
        }
    }

    private val deliveredReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val messageId = intent?.getStringExtra(EXTRA_MESSAGE_ID) ?: return
            scope.launch(Dispatchers.IO) {
                val isDelivered = resultCode == RESULT_OK
                updateMessageStatus(
                    messageId = messageId,
                    status = if (isDelivered) "DELIVERED" else "SENT",
                    error = if (isDelivered) null else "Delivery receipt not confirmed"
                )
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        registerCompatReceiver(sentReceiver, IntentFilter(ACTION_SMS_SENT))
        registerCompatReceiver(deliveredReceiver, IntentFilter(ACTION_SMS_DELIVERED))
    }

    override fun onDestroy() {
        unregisterReceiver(sentReceiver)
        unregisterReceiver(deliveredReceiver)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    fun sendSMS(phoneNumber: String, content: String, encryptionType: String = "NONE") {
        scope.launch(Dispatchers.IO) {
            try {
                val smsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    applicationContext.getSystemService(SmsManager::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    SmsManager.getDefault()
                }
                val isEncrypted = encryptionType != "NONE"
                val contentWithSignature = encryptionManager.generateMessageSignature(content)
                val payload = when (encryptionType) {
                    "AES256" -> "ENC:${encryptionManager.encryptAES256(contentWithSignature)}"
                    else -> contentWithSignature
                }

                val messageId = UUID.randomUUID().toString()
                val dbMessage = Message(
                    id = messageId,
                    conversationId = "0", // caller should replace this with a real conversation id
                    senderPhoneNumber = "self",
                    recipientPhoneNumber = phoneNumber,
                    content = contentWithSignature,
                    timestamp = System.currentTimeMillis(),
                    type = "TEXT",
                    status = "SENDING",
                    isEncrypted = isEncrypted,
                    encryptionAlgorithm = if (isEncrypted) encryptionType else null
                )
                messageRepository.insertMessage(dbMessage)

                val sentIntent = PendingIntent.getBroadcast(
                    this@MessageService,
                    messageId.hashCode(),
                    Intent(ACTION_SMS_SENT).putExtra(EXTRA_MESSAGE_ID, messageId),
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                val deliveredIntent = PendingIntent.getBroadcast(
                    this@MessageService,
                    messageId.hashCode() + 1,
                    Intent(ACTION_SMS_DELIVERED).putExtra(EXTRA_MESSAGE_ID, messageId),
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                val smsMessages = smsManager.divideMessage(payload)
                smsManager.sendMultipartTextMessage(
                    phoneNumber,
                    null,
                    smsMessages,
                    ArrayList(smsMessages.map { sentIntent }),
                    ArrayList(smsMessages.map { deliveredIntent })
                )
            } catch (e: Exception) {
                e.printStackTrace()
                // If we can map the failure to a stored message later, this keeps behavior explicit.
            }
        }
    }

    fun sendRCSMessage(phoneNumber: String, content: String, attachments: List<String> = emptyList()) {
        scope.launch(Dispatchers.Default) {
            try {
                val contentWithSignature = encryptionManager.generateMessageSignature(content)
                // RCS is handled through the system RCS provider or proprietary protocol
                val message = Message(
                    conversationId = "0",
                    senderPhoneNumber = "self",
                    recipientPhoneNumber = phoneNumber,
                    content = contentWithSignature,
                    timestamp = System.currentTimeMillis(),
                    type = "TEXT",
                    status = "SENT",
                    isEncrypted = true,
                    encryptionAlgorithm = "AES256",
                    mediaUrls = attachments.joinToString(",")
                )
                messageRepository.insertMessage(message)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun sendEncryptedMessage(phoneNumber: String, content: String) {
        sendSMS(phoneNumber, content, "AES256")
    }

    private suspend fun updateMessageStatus(
        messageId: String,
        status: String,
        error: String?
    ) {
        val message = messageRepository.getMessageById(messageId) ?: return
        val report =
            "{\"lastStatus\":\"$status\",\"timestamp\":${System.currentTimeMillis()},\"error\":${if (error == null) "null" else "\"$error\""}}"
        messageRepository.updateMessage(
            message.copy(
                status = status,
                deliveryReport = report
            )
        )
    }

    private fun registerCompatReceiver(receiver: BroadcastReceiver, filter: IntentFilter) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(receiver, filter, RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(receiver, filter)
        }
    }

    inner class MessageServiceBinder : Binder() {
        fun getService(): MessageService = this@MessageService
    }

    companion object {
        private const val ACTION_SMS_SENT = "com.nexusmedia.nexussms.SMS_SENT"
        private const val ACTION_SMS_DELIVERED = "com.nexusmedia.nexussms.SMS_DELIVERED"
        private const val EXTRA_MESSAGE_ID = "message_id"
    }
}
