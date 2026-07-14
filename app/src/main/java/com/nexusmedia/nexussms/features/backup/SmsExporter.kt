package com.nexusmedia.nexussms.features.backup

import android.content.Context
import android.net.Uri
import com.google.gson.Gson
import com.nexusmedia.nexussms.data.repository.ConversationRepository
import com.nexusmedia.nexussms.data.repository.MessageRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SmsExporter @Inject constructor(
    @ApplicationContext private val context: Context,
    private val messageRepository: MessageRepository,
    private val conversationRepository: ConversationRepository
) {
    data class ExportResult(val success: Boolean, val messageCount: Int, val error: String? = null)

    suspend fun exportToJson(outputUri: Uri): ExportResult {
        return try {
            val conversations = conversationRepository.getAllConversations().first()
            val exportData = mutableListOf<Map<String, Any>>()

            for (conv in conversations) {
                val messages = messageRepository.getConversationMessages(conv.id).first()
                exportData.add(
                    mapOf(
                        "conversation" to mapOf(
                            "id" to conv.id,
                            "displayName" to conv.displayName,
                            "participantPhoneNumbers" to conv.participantPhoneNumbers,
                            "isGroupChat" to conv.isGroupChat
                        ),
                        "messages" to messages.map { msg ->
                            mapOf(
                                "id" to msg.id,
                                "senderPhoneNumber" to msg.senderPhoneNumber,
                                "content" to msg.content,
                                "timestamp" to msg.timestamp,
                                "type" to msg.type,
                                "status" to msg.status
                            )
                        }
                    )
                )
            }

            val json = Gson().toJson(exportData)
            context.contentResolver.openOutputStream(outputUri)?.use { output ->
                output.write(json.toByteArray())
            }

            ExportResult(true, exportData.sumOf { (it["messages"] as? List<*>)?.size ?: 0 })
        } catch (e: Exception) {
            Timber.e(e, "JSON export failed")
            ExportResult(false, 0, e.message)
        }
    }

    suspend fun exportToCsv(outputUri: Uri): ExportResult {
        return try {
            val conversations = conversationRepository.getAllConversations().first()
            val writer = context.contentResolver.openOutputStream(outputUri)?.bufferedWriter()
                ?: return ExportResult(false, 0, "Cannot open file")

            var count = 0
            writer.use {
                it.write("conversation_name,phone_number,sender,content,timestamp,type,status\n")
                for (conv in conversations) {
                    val messages = messageRepository.getConversationMessages(conv.id).first()
                    for (msg in messages) {
                        val escapedContent = msg.content.replace("\"", "\"\"").replace("\n", " ")
                        val escapedName = conv.displayName.replace("\"", "\"\"")
                        it.write("\"$escapedName\",\"${conv.participantPhoneNumbers}\",\"${msg.senderPhoneNumber}\",\"$escapedContent\",${msg.timestamp},\"${msg.type}\",\"${msg.status}\"\n")
                        count++
                    }
                }
            }
            ExportResult(true, count)
        } catch (e: Exception) {
            Timber.e(e, "CSV export failed")
            ExportResult(false, 0, e.message)
        }
    }
}
