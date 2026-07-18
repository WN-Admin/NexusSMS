package com.nexusmedia.nexussms.data.repository

import android.content.Context
import android.net.Uri
import android.provider.ContactsContract
import android.provider.Telephony
import com.nexusmedia.nexussms.data.models.ContactAvatar
import com.nexusmedia.nexussms.data.models.Conversation
import com.nexusmedia.nexussms.data.models.Message
import com.nexusmedia.nexussms.utils.Validators
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
    private val conversationRepository: ConversationRepository,
    private val contactAvatarRepository: ContactAvatarRepository
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
            return ImportResult(0, 0, error = "SMS permission denied")
        }

        if (cursor == null) {
            Timber.w("SMS cursor is null - no messages or permission denied")
            return ImportResult(0, 0, error = "Unable to access SMS messages")
        }

        Timber.d("SMS cursor count: ${cursor.count}")

        val conversations = mutableMapOf<String, MutableList<Message>>()

        cursor.use {
            val idIndex = it.getColumnIndex(Telephony.Sms._ID)
            val addressIndex = it.getColumnIndex(Telephony.Sms.ADDRESS)
            val bodyIndex = it.getColumnIndex(Telephony.Sms.BODY)
            val dateIndex = it.getColumnIndex(Telephony.Sms.DATE)
            val typeIndex = it.getColumnIndex(Telephony.Sms.TYPE)

            if (addressIndex < 0 || bodyIndex < 0 || dateIndex < 0 || typeIndex < 0) {
                Timber.e("SMS column index error: address=$addressIndex, body=$bodyIndex, date=$dateIndex, type=$typeIndex")
                return ImportResult(0, 0, error = "SMS database schema error")
            }

            while (it.moveToNext()) {
                val smsId = if (idIndex >= 0) it.getLong(idIndex) else null
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
                    isEncrypted = false,
                    sourceSmsId = smsId
                )

                conversations.getOrPut(Validators.normalizePhone(address)) { mutableListOf() }.add(message)
            }
        }

        Timber.d("Found ${conversations.size} unique conversations with ${conversations.values.sumOf { it.size }} messages")

        val existingConversations = conversationRepository.getAllConversations().first()

        for ((normalizedAddress, messages) in conversations) {
            val address = messages.firstOrNull()?.let { msg ->
                if (msg.senderPhoneNumber == "self") msg.recipientPhoneNumber else msg.senderPhoneNumber
            } ?: normalizedAddress
            val existing = existingConversations.find {
                Validators.normalizePhone(it.participantPhoneNumbers) == normalizedAddress
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
                val result = messageRepository.insertImportedMessage(message.copy(conversationId = conversationId))
                if (result != -1L) imported++
            }
        }

        Timber.d("Import complete: $imported new messages in ${conversations.size} conversations")
        return ImportResult(imported, conversations.size)
    }

    suspend fun resyncFromDevice(): ResyncResult {
        Timber.d("Starting SMS re-sync from device...")

        val cursor = try {
            context.contentResolver.query(
                Telephony.Sms.CONTENT_URI,
                arrayOf(Telephony.Sms._ID, Telephony.Sms.ADDRESS),
                null, null, null
            )
        } catch (e: SecurityException) {
            Timber.e(e, "Permission denied querying SMS for resync")
            return ResyncResult(0, error = "SMS permission denied")
        }

        if (cursor == null) {
            return ResyncResult(0, error = "Unable to access SMS messages")
        }

        val currentSmsIds = mutableSetOf<Long>()
        cursor.use {
            val idIndex = it.getColumnIndex(Telephony.Sms._ID)
            if (idIndex < 0) return ResyncResult(0, error = "SMS schema error")
            while (it.moveToNext()) {
                currentSmsIds.add(it.getLong(idIndex))
            }
        }

        Timber.d("Device has ${currentSmsIds.size} SMS messages")

        var removed = 0
        val existingConversations = conversationRepository.getAllConversations().first()

        for (conversation in existingConversations) {
            val importedIds = messageRepository.getImportedSourceSmsIds(conversation.id)
            if (importedIds.isEmpty()) continue

            val staleIds = importedIds.filter { it !in currentSmsIds }
            if (staleIds.isNotEmpty()) {
                messageRepository.deleteMessagesByIds(staleIds.map { it.toString() })
                removed += staleIds.size
                Timber.d("Removed ${staleIds.size} deleted messages from conversation ${conversation.displayName}")
            }
        }

        Timber.d("Re-sync complete: removed $removed stale messages")
        return ResyncResult(removed)
    }

    private fun lookupContact(phoneNumber: String): Pair<String, String?> {
        val uri = Uri.withAppendedPath(
            ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
            Uri.encode(phoneNumber)
        )
        val cursor = try {
            context.contentResolver.query(
                uri,
                arrayOf(
                    ContactsContract.PhoneLookup.DISPLAY_NAME,
                    ContactsContract.PhoneLookup.PHOTO_THUMBNAIL_URI
                ),
                null, null, null
            )
        } catch (e: SecurityException) {
            Timber.e(e, "Permission denied looking up contact for $phoneNumber")
            null
        }
        cursor?.use {
            if (it.moveToFirst()) {
                val nameIndex = it.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME)
                val photoIndex = it.getColumnIndex(ContactsContract.PhoneLookup.PHOTO_THUMBNAIL_URI)
                val name = if (nameIndex >= 0) it.getString(nameIndex) else null
                val photo = if (photoIndex >= 0) it.getString(photoIndex) else null
                return Pair(name ?: phoneNumber, photo)
            }
        }
        return Pair(phoneNumber, null)
    }

    private fun getContactName(phoneNumber: String): String = lookupContact(phoneNumber).first

    suspend fun importContactAvatars(): Int {
        val conversations = conversationRepository.getAllConversations().first()
        val avatars = mutableListOf<ContactAvatar>()

        for (conversation in conversations) {
            val phone = conversation.participantPhoneNumbers
            val normalized = Validators.normalizePhone(phone)
            val (_, photoUri) = lookupContact(phone)
            if (photoUri != null) {
                avatars.add(
                    ContactAvatar(
                        normalizedPhone = normalized,
                        photoUri = photoUri,
                        displayName = conversation.displayName,
                        updatedAt = System.currentTimeMillis()
                    )
                )
            }
        }

        if (avatars.isNotEmpty()) {
            contactAvatarRepository.upsertAll(avatars)
            Timber.d("Imported ${avatars.size} contact avatars")
        }
        return avatars.size
    }

    data class ImportResult(val messagesImported: Int, val conversationsImported: Int, val error: String? = null)
    data class ResyncResult(val messagesRemoved: Int, val error: String? = null)
}
