package com.nexusmedia.nexussms.data.repository

import android.content.Context
import android.net.Uri
import android.provider.ContactsContract
import android.provider.Telephony
import com.nexusmedia.nexussms.data.models.Conversation
import com.nexusmedia.nexussms.data.models.Message
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import timber.log.Timber
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SmsImporter @Inject constructor(
    @ApplicationContext private val context: Context,
    private val messageRepository: MessageRepository,
    private val conversationRepository: ConversationRepository
) {
    suspend fun importAllSms(): ImportResult {
        var imported = 0
        Timber.d("Starting SMS import...")

        val cursor = try {
            context.contentResolver.query(
                Telephony.Sms.CONTENT_URI,
                null, null, null,
                "${Telephony.Sms.DATE} ASC"
            )
        } catch (e: SecurityException) {
            Timber.e(e, "Permission denied querying SMS")
            return ImportResult(0, 0)
        }

        if (cursor == null) {
            Timber.w("SMS cursor is null - no messages or permission denied")
            return ImportResult(0, 0)
        }

        Timber.d("SMS cursor count: ${cursor.count}")

        val conversations = mutableMapOf<String, MutableList<Message>>()

        cursor.use {
            val addressIndex = it.getColumnIndex(Telephony.Sms.ADDRESS)
            val bodyIndex = it.getColumnIndex(Telephony.Sms.BODY)
            val dateIndex = it.getColumnIndex(Telephony.Sms.DATE)
            val typeIndex = it.getColumnIndex(Telephony.Sms.TYPE)

            if (addressIndex < 0 || bodyIndex < 0 || dateIndex < 0 || typeIndex < 0) {
                Timber.e("SMS column index error: address=$addressIndex, body=$bodyIndex, date=$dateIndex, type=$typeIndex")
                return ImportResult(0, 0)
            }

            while (it.moveToNext()) {
                val address = it.getString(addressIndex) ?: "unknown"
                val body = it.getString(bodyIndex) ?: continue
                val date = it.getLong(dateIndex)
                val type = it.getInt(typeIndex)

                val isSelf = type == Telephony.Sms.MESSAGE_TYPE_SENT

                val message = Message(
                    id = UUID.randomUUID().toString(),
                    conversationId = address,
                    senderPhoneNumber = if (isSelf) "self" else address,
                    recipientPhoneNumber = if (isSelf) address else "self",
                    content = body,
                    timestamp = date,
                    type = "TEXT",
                    status = "SENT",
                    isEncrypted = false
                )

                conversations.getOrPut(normalizePhone(address)) { mutableListOf() }.add(message)
            }
        }

        Timber.d("Found ${conversations.size} unique conversations with ${conversations.values.sumOf { it.size }} messages")

        val existingConversations = conversationRepository.getAllConversations().first()

        for ((normalizedAddress, messages) in conversations) {
            val address = messages.firstOrNull()?.let { msg ->
                if (msg.senderPhoneNumber == "self") msg.recipientPhoneNumber else msg.senderPhoneNumber
            } ?: normalizedAddress
            val existing = existingConversations.find {
                normalizePhone(it.participantPhoneNumbers) == normalizedAddress
            }

            val conversationId = if (existing != null) {
                existing.id
            } else {
                val contactName = getContactName(address)
                val newConv = Conversation(
                    id = UUID.randomUUID().toString(),
                    participantPhoneNumbers = address,
                    displayName = contactName,
                    lastMessage = messages.lastOrNull()?.content ?: "",
                    lastMessageTime = messages.lastOrNull()?.timestamp ?: 0L,
                    unreadCount = 0
                )
                conversationRepository.insertConversation(newConv)
                newConv.id
            }

            for (message in messages) {
                messageRepository.insertMessage(message.copy(conversationId = conversationId))
                imported++
            }
        }

        Timber.d("Import complete: $imported messages in ${conversations.size} conversations")
        return ImportResult(imported, conversations.size)
    }

    private fun normalizePhone(phone: String): String {
        return phone.replace(Regex("[^+\\d]"), "")
    }

    private fun getContactName(phoneNumber: String): String {
        val uri = Uri.withAppendedPath(
            ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
            Uri.encode(phoneNumber)
        )
        val cursor = try {
            context.contentResolver.query(
                uri,
                arrayOf(ContactsContract.PhoneLookup.DISPLAY_NAME),
                null, null, null
            )
        } catch (e: SecurityException) {
            Timber.e(e, "Permission denied looking up contact for $phoneNumber")
            null
        }
        cursor?.use {
            if (it.moveToFirst()) {
                val nameIndex = it.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME)
                if (nameIndex >= 0) {
                    return it.getString(nameIndex) ?: phoneNumber
                }
            }
        }
        return phoneNumber
    }

    data class ImportResult(val messagesImported: Int, val conversationsImported: Int)
}
