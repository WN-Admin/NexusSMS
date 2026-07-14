package com.nexusmedia.nexussms.features.backup

import android.content.Context
import android.net.Uri
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.nexusmedia.nexussms.data.models.Conversation
import com.nexusmedia.nexussms.data.models.Message
import com.nexusmedia.nexussms.data.repository.ConversationRepository
import com.nexusmedia.nexussms.data.repository.MessageRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SmsRestoreService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val messageRepository: MessageRepository,
    private val conversationRepository: ConversationRepository
) {
    data class RestoreResult(val success: Boolean, val messagesRestored: Int, val error: String? = null)

    suspend fun restoreFromJson(inputUri: Uri): RestoreResult {
        return try {
            val json = context.contentResolver.openInputStream(inputUri)?.bufferedReader()?.readText()
                ?: return RestoreResult(false, 0, "Cannot read file")
            val type = object : TypeToken<List<Map<String, Any>>>() {}.type
            val data: List<Map<String, Any>> = Gson().fromJson(json, type)

            var count = 0
            for (convData in data) {
                @Suppress("UNCHECKED_CAST")
                val convInfo = convData["conversation"] as? Map<String, Any> ?: continue
                val phoneNumbers = convInfo["participantPhoneNumbers"] as? String ?: continue
                val displayName = convInfo["displayName"] as? String ?: phoneNumbers

                var conv = conversationRepository.findConversationWithParticipant(phoneNumbers)
                if (conv == null) {
                    conv = Conversation(
                        participantPhoneNumbers = phoneNumbers,
                        displayName = displayName,
                        isGroupChat = convInfo["isGroupChat"] as? Boolean ?: false,
                        sourcePlatform = "SMS"
                    )
                    conversationRepository.insertConversation(conv)
                    conv = conversationRepository.findConversationWithParticipant(phoneNumbers) ?: continue
                }

                @Suppress("UNCHECKED_CAST")
                val messagesData = convData["messages"] as? List<Map<String, Any>> ?: continue
                for (msgData in messagesData) {
                    val message = Message(
                        conversationId = conv.id,
                        senderPhoneNumber = msgData["senderPhoneNumber"] as? String ?: "",
                        recipientPhoneNumber = phoneNumbers,
                        content = msgData["content"] as? String ?: "",
                        timestamp = (msgData["timestamp"] as? Double)?.toLong() ?: System.currentTimeMillis(),
                        type = msgData["type"] as? String ?: "TEXT",
                        status = msgData["status"] as? String ?: "DELIVERED",
                        isEncrypted = false
                    )
                    messageRepository.insertMessage(message)
                    count++
                }
            }
            RestoreResult(true, count)
        } catch (e: Exception) {
            Timber.e(e, "JSON restore failed")
            RestoreResult(false, 0, e.message)
        }
    }
}
