package com.nexusmedia.nexussms.features.matrix

import android.content.Context
import com.nexusmedia.nexussms.data.models.Conversation
import com.nexusmedia.nexussms.data.models.Message
import com.nexusmedia.nexussms.data.repository.ConversationRepository
import com.nexusmedia.nexussms.data.repository.MessageRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

data class MatrixSyncResult(
    val success: Boolean,
    val conversationsImported: Int = 0,
    val messagesImported: Int = 0,
    val error: String? = null
)

@Singleton
class MatrixSyncService @Inject constructor(
    private val matrixClient: MatrixClient,
    private val conversationRepository: ConversationRepository,
    private val messageRepository: MessageRepository,
    private val matrixAuthService: MatrixAuthService,
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val PREFS_NAME = "matrix_sync_state"
        private const val KEY_LAST_SYNC_TOKEN = "last_sync_token"
        private const val MAX_BACKFILL_PAGES = 10
        private const val BACKFILL_PAGE_SIZE = 50
    }

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private var lastSyncToken: String?
        get() = prefs.getString(KEY_LAST_SYNC_TOKEN, null)
        set(value) { prefs.edit().putString(KEY_LAST_SYNC_TOKEN, value).apply() }

    suspend fun initialSync(): MatrixSyncResult {
        val token = matrixAuthService.getAccessToken() ?: return MatrixSyncResult(false, error = "Not logged in")
        return sync(token, since = null, timeout = 10000L)
    }

    suspend fun incrementalSync(): MatrixSyncResult {
        val token = matrixAuthService.getAccessToken() ?: return MatrixSyncResult(false, error = "Not logged in")
        val since = lastSyncToken
        if (since == null) {
            Timber.w("No sync token, performing initial sync")
            return initialSync()
        }
        return sync(token, since = since, timeout = 30000L)
    }

    suspend fun syncForRoom(roomId: String): MatrixSyncResult {
        val token = matrixAuthService.getAccessToken() ?: return MatrixSyncResult(false, error = "Not logged in")
        val since = lastSyncToken
        return sync(token, since = since, timeout = 5000L, filterRoom = roomId)
    }

    private suspend fun sync(
        token: String,
        since: String?,
        timeout: Long,
        filterRoom: String? = null
    ): MatrixSyncResult {
        return try {
            val api = matrixClient.getApi()
            val authHeader = "Bearer $token"
            val response = api.sync(authHeader, since, timeout)

            lastSyncToken = response.nextBatch

            val existingConversations = conversationRepository.getAllConversations().first()
            var convsImported = 0
            var msgsImported = 0

            val joinedRooms = response.rooms?.join ?: emptyMap()

            for ((roomId, roomSync) in joinedRooms) {
                if (filterRoom != null && roomId != filterRoom) continue

                val roomName = extractRoomName(roomSync, roomId)
                val members = extractMembers(roomSync)
                val displayName = if (members.size == 2) {
                    members.firstOrNull { it != matrixAuthService.getUserId() } ?: roomName
                } else {
                    roomName
                }

                val participantJson = if (members.isNotEmpty()) {
                    """${members.map { "\"$it\"" }.joinToString(",")}"""
                } else {
                    """["${matrixAuthService.getUserId() ?: "self"}"]"""
                }

                val existing = existingConversations.find {
                    it.participantPhoneNumbers.contains(roomId) && it.sourcePlatform == "MATRIX"
                }

                val conversation = if (existing != null) {
                    existing.copy(displayName = displayName)
                } else {
                    convsImported++
                    Conversation(
                        participantPhoneNumbers = participantJson,
                        displayName = displayName,
                        sourcePlatform = "MATRIX",
                        sourceAccountId = roomId,
                        isGroupChat = members.size > 2
                    )
                }

                val convId = if (existing != null) {
                    conversationRepository.updateConversation(conversation)
                    existing.id
                } else {
                    conversationRepository.insertConversation(conversation)
                    conversation.id
                }

                val events = roomSync.timeline?.events ?: emptyList()
                val existingMessages = messageRepository.getConversationMessages(convId).first()
                val existingEventIds = existingMessages.mapNotNull { msg ->
                    val metadata = msg.metadata
                    if (metadata.contains("eventId")) {
                        try {
                            val json = org.json.JSONObject(metadata)
                            json.optString("eventId")
                        } catch (_: Exception) { null }
                    } else null
                }.toSet()

                var latestTimestamp = conversation.lastMessageTime
                var lastMessageContent = conversation.lastMessage

                for (event in events) {
                    if (event.eventId in existingEventIds) continue
                    if (event.type != "m.room.message") continue

                    val msgtype = event.content["msgtype"] as? String ?: continue
                    val body = event.content["body"] as? String ?: continue

                    val isFromMe = event.sender == matrixAuthService.getUserId()
                    val timestamp = event.originServerTs

                    val contentType = when (msgtype) {
                        "m.text" -> "TEXT"
                        "m.image" -> "IMAGE"
                        "m.file" -> "FILE"
                        "m.audio" -> "AUDIO"
                        "m.video" -> "VIDEO"
                        "m.notice" -> "TEXT"
                        else -> "TEXT"
                    }

                    val mediaUrl = when (msgtype) {
                        "m.image", "m.file", "m.audio", "m.video" -> event.content["url"] as? String ?: ""
                        else -> ""
                    }

                    val message = Message(
                        conversationId = convId,
                        senderPhoneNumber = if (isFromMe) "self" else event.sender,
                        recipientPhoneNumber = if (isFromMe) event.sender else "self",
                        content = body,
                        type = contentType,
                        timestamp = timestamp,
                        status = "DELIVERED",
                        sourcePlatform = "MATRIX",
                        mediaUrls = mediaUrl,
                        metadata = """{"eventId":"${event.eventId}","roomId":"$roomId","platform":"MATRIX"}"""
                    )

                    messageRepository.insertMessage(message)
                    msgsImported++

                    if (timestamp > latestTimestamp) {
                        latestTimestamp = timestamp
                        lastMessageContent = body
                    }
                }

                val timeline = roomSync.timeline
                if (timeline?.limited == true && timeline.prevBatch != null) {
                    msgsImported += backfillRoom(authHeader, api, roomId, convId, timeline.prevBatch, existingEventIds)
                }

                val unreadCount = roomSync.unreadNotifications?.notificationCount ?: 0
                val updatedConv = conversation.copy(
                    id = convId,
                    lastMessage = lastMessageContent.ifEmpty { conversation.lastMessage },
                    lastMessageTime = latestTimestamp,
                    unreadCount = unreadCount.coerceAtLeast(conversation.unreadCount),
                    updatedAt = System.currentTimeMillis()
                )
                conversationRepository.updateConversation(updatedConv)
            }

            Timber.d("Matrix sync: %d conversations, %d messages imported", convsImported, msgsImported)
            MatrixSyncResult(
                success = true,
                conversationsImported = convsImported,
                messagesImported = msgsImported
            )
        } catch (e: Exception) {
            Timber.e(e, "Matrix sync failed")
            MatrixSyncResult(success = false, error = parseSyncError(e))
        }
    }

    private suspend fun backfillRoom(
        authHeader: String,
        api: MatrixApi,
        roomId: String,
        convId: String,
        startToken: String,
        existingEventIds: Set<String>
    ): Int {
        var prevBatch: String? = startToken
        var pagesFetched = 0
        var imported = 0

        while (prevBatch != null && pagesFetched < MAX_BACKFILL_PAGES) {
            try {
                val page = api.getRoomMessages(authHeader, roomId, from = prevBatch, dir = "b", limit = BACKFILL_PAGE_SIZE)
                val chunk = page.chunk ?: emptyList()

                for (event in chunk) {
                    if (event.eventId in existingEventIds) continue
                    if (event.type != "m.room.message") continue

                    val msgtype = event.content["msgtype"] as? String ?: continue
                    val body = event.content["body"] as? String ?: continue

                    val isFromMe = event.sender == matrixAuthService.getUserId()
                    val timestamp = event.originServerTs

                    val contentType = when (msgtype) {
                        "m.text" -> "TEXT"
                        "m.image" -> "IMAGE"
                        "m.file" -> "FILE"
                        "m.audio" -> "AUDIO"
                        "m.video" -> "VIDEO"
                        "m.notice" -> "TEXT"
                        else -> "TEXT"
                    }

                    val mediaUrl = when (msgtype) {
                        "m.image", "m.file", "m.audio", "m.video" -> event.content["url"] as? String ?: ""
                        else -> ""
                    }

                    val message = Message(
                        conversationId = convId,
                        senderPhoneNumber = if (isFromMe) "self" else event.sender,
                        recipientPhoneNumber = if (isFromMe) event.sender else "self",
                        content = body,
                        type = contentType,
                        timestamp = timestamp,
                        status = "DELIVERED",
                        sourcePlatform = "MATRIX",
                        mediaUrls = mediaUrl,
                        metadata = """{"eventId":"${event.eventId}","roomId":"$roomId","platform":"MATRIX"}"""
                    )

                    messageRepository.insertMessage(message)
                    imported++
                }

                prevBatch = page.end
                pagesFetched++

                if (chunk.isEmpty()) break
            } catch (e: Exception) {
                Timber.w(e, "Failed to backfill room %s page %d", roomId, pagesFetched)
                break
            }
        }

        if (pagesFetched > 0) {
            Timber.d("Backfilled %d messages from %d pages for room %s", imported, pagesFetched, roomId)
        }
        return imported
    }

    private fun extractRoomName(roomSync: MatrixRoomSync, roomId: String): String {
        val stateEvents = roomSync.state?.events ?: emptyList()
        val nameEvent = stateEvents.find { it.type == "m.room.name" }
        val name = nameEvent?.content?.get("name") as? String
        return if (!name.isNullOrBlank()) name else roomId.substringAfter(":").take(20)
    }

    private fun extractMembers(roomSync: MatrixRoomSync): List<String> {
        val stateEvents = roomSync.state?.events ?: emptyList()
        val memberEvents = stateEvents.filter { it.type == "m.room.member" && it.content["membership"] == "join" }
        return memberEvents.mapNotNull { it.stateKey }.filter { it.isNotBlank() }
    }

    private fun parseSyncError(e: Exception): String {
        val msg = e.message ?: return "Sync failed"
        return when {
            msg.contains("M_UNKNOWN_TOKEN") || msg.contains("401") -> "Session expired. Please reconnect"
            msg.contains("M_FORBIDDEN") -> "Access denied"
            msg.contains("Unable to resolve host") -> "Cannot reach homeserver"
            msg.contains("timeout") -> "Connection timed out"
            else -> msg
        }
    }
}
