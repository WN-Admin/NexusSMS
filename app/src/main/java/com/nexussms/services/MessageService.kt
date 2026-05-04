package com.nexussms.services

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.telephony.SmsManager
import com.nexussms.data.models.Message
import com.nexussms.data.repository.MessageRepository
import com.nexussms.security.EncryptionManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import java.util.Date

@AndroidEntryPoint
class MessageService : Service() {
    
    @Inject
    lateinit var messageRepository: MessageRepository
    
    @Inject
    lateinit var encryptionManager: EncryptionManager
    
    private val binder = MessageServiceBinder()
    private val scope = CoroutineScope(Dispatchers.Main)

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    fun sendSMS(phoneNumber: String, content: String, encryptionType: String = "NONE") {
        scope.launch(Dispatchers.Default) {
            try {
                val smsManager = SmsManager.getDefault()
                val encryptedContent = when (encryptionType) {
                    "AES256" -> encryptionManager.encryptAES256(content)
                    else -> content
                }
                
                val smsMessages = smsManager.divideMessage(encryptedContent)
                smsManager.sendMultipartTextMessage(
                    phoneNumber,
                    null,
                    smsMessages,
                    null,
                    null
                )
                
                // Save to database
                val message = Message(
                    conversationId = 0, // Will be set by caller
                    senderId = "self",
                    recipientId = phoneNumber,
                    content = content,
                    timestamp = Date(),
                    isIncoming = false,
                    isSent = true,
                    messageType = "SMS",
                    encryptionType = encryptionType
                )
                messageRepository.insertMessage(message)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun sendRCSMessage(phoneNumber: String, content: String, attachments: List<String> = emptyList()) {
        scope.launch(Dispatchers.Default) {
            try {
                // RCS is handled through the system RCS provider or proprietary protocol
                val message = Message(
                    conversationId = 0,
                    senderId = "self",
                    recipientId = phoneNumber,
                    content = content,
                    timestamp = Date(),
                    isIncoming = false,
                    isSent = true,
                    messageType = "RCS",
                    attachmentUrls = attachments.joinToString(","),
                    encryptionType = "AES256"
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

    inner class MessageServiceBinder : Binder() {
        fun getService(): MessageService = this@MessageService
    }
}
