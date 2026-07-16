package com.nexusmedia.nexussms.features.messenger

import com.nexusmedia.nexussms.data.models.Conversation
import com.nexusmedia.nexussms.data.models.Message
import com.nexusmedia.nexussms.data.models.SocialAccount
import com.nexusmedia.nexussms.data.repository.ConversationRepository
import com.nexusmedia.nexussms.data.repository.MessageRepository
import com.nexusmedia.nexussms.data.repository.SocialAccountRepository
import com.nexusmedia.nexussms.security.EncryptionManager
import kotlinx.coroutines.flow.first
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

data class MessengerAuthResult(
    val success: Boolean,
    val pageName: String? = null,
    val pageId: String? = null,
    val error: String? = null
)

data class MessengerSyncResult(
    val success: Boolean,
    val conversationsImported: Int = 0,
    val messagesImported: Int = 0,
    val error: String? = null
)

@Singleton
class MessengerService @Inject constructor(
    private val conversationRepository: ConversationRepository,
    private val messageRepository: MessageRepository,
    private val socialAccountRepository: SocialAccountRepository,
    private val encryptionManager: EncryptionManager
) {

    private fun getApi(): FacebookApi {
        val client = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(
                HttpLoggingInterceptor { msg -> Timber.d("MessengerAPI: %s", msg) }
                    .apply { level = HttpLoggingInterceptor.Level.BODY }
            )
            .build()

        return Retrofit.Builder()
            .baseUrl("https://graph.facebook.com/v18.0/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(FacebookApi::class.java)
    }

    suspend fun verifyToken(pageToken: String): MessengerAuthResult {
        return try {
            val api = getApi()
            val me = api.getMe(pageToken)
            Timber.d("Facebook page verified: %s (id=%s)", me.name, me.id)
            MessengerAuthResult(
                success = true,
                pageName = me.name,
                pageId = me.id
            )
        } catch (e: Exception) {
            Timber.e(e, "Facebook token verification failed")
            MessengerAuthResult(success = false, error = parseFacebookError(e))
        }
    }

    suspend fun connectPage(pageToken: String): MessengerAuthResult {
        val result = verifyToken(pageToken)
        if (result.success) {
            val existing = socialAccountRepository.getAccountByPlatform("FACEBOOK_MESSENGER")
            val account = SocialAccount(
                id = existing?.id ?: java.util.UUID.randomUUID().toString(),
                platform = "FACEBOOK_MESSENGER",
                userId = result.pageId ?: "",
                username = result.pageName?.lowercase()?.replace(" ", "") ?: "",
                displayName = result.pageName ?: "Facebook Messenger",
                accessToken = encryptionManager.encryptToken(pageToken),
                isConnected = true,
                settings = """{"pageId":"${result.pageId}","pageName":"${result.pageName}"}""",
                updatedAt = System.currentTimeMillis(),
                createdAt = existing?.createdAt ?: System.currentTimeMillis()
            )
            if (existing != null) {
                socialAccountRepository.updateAccount(account)
            } else {
                socialAccountRepository.insertAccount(account)
            }
        }
        return result
    }

    suspend fun disconnect() {
        socialAccountRepository.getAccountByPlatform("FACEBOOK_MESSENGER")?.let { account ->
            socialAccountRepository.updateAccount(
                account.copy(
                    isConnected = false,
                    accessToken = "",
                    updatedAt = System.currentTimeMillis()
                )
            )
        }
    }

    suspend fun sync(): MessengerSyncResult {
        val account = socialAccountRepository.getAccountByPlatform("FACEBOOK_MESSENGER")
        if (account == null || !account.isConnected) {
            return MessengerSyncResult(false, error = "Facebook Messenger not connected")
        }
        val token = encryptionManager.decryptToken(account.accessToken)
        if (token.isBlank()) {
            return MessengerSyncResult(false, error = "No page token")
        }

        return try {
            val api = getApi()
            val existingConversations = conversationRepository.getAllConversations().first()
            var convsImported = 0
            var msgsImported = 0

            var cursor: String? = null
            var pagesProcessed = 0
            val maxPages = 3

            do {
                val convResponse = api.getConversations(token, limit = 25, after = cursor)
                val conversations = convResponse.data ?: break

                for (fbConv in conversations) {
                    val participants = fbConv.participants?.data ?: emptyList()
                    val otherParticipant = participants.firstOrNull {
                        it.id != account.userId && it.id != account.username
                    }
                    val displayName = otherParticipant?.name
                        ?: fbConv.snippet?.take(30)
                        ?: "Messenger Chat ${fbConv.id.takeLast(6)}"

                    val existing = existingConversations.find {
                        it.participantPhoneNumbers.contains("fb_${fbConv.id}") && it.sourcePlatform == "FACEBOOK_MESSENGER"
                    }

                    val isGroup = (participants.size > 2)

                    val conversation = if (existing != null) {
                        existing.copy(displayName = displayName)
                    } else {
                        convsImported++
                        Conversation(
                            participantPhoneNumbers = """["fb_${fbConv.id}"]""",
                            displayName = displayName,
                            sourcePlatform = "FACEBOOK_MESSENGER",
                            sourceAccountId = fbConv.id,
                            isGroupChat = isGroup
                        )
                    }

                    val convId = if (existing != null) {
                        conversationRepository.updateConversation(conversation)
                        existing.id
                    } else {
                        conversationRepository.insertConversation(conversation)
                        conversation.id
                    }

                    val messages = try {
                        api.getMessages(fbConv.id, token, limit = 50)
                    } catch (e: Exception) {
                        Timber.w(e, "Failed to fetch messages for conversation %s", fbConv.id)
                        continue
                    }

                    val existingMessages = messageRepository.getConversationMessages(convId).first()
                    val existingEventIds = existingMessages.mapNotNull { msg ->
                        try {
                            val json = org.json.JSONObject(msg.metadata)
                            json.optString("messageId")
                        } catch (_: Exception) { null }
                    }.toSet()

                    var latestTimestamp = conversation.lastMessageTime
                    var lastMessageContent = conversation.lastMessage

                    for (fbMsg in messages.data?.reversed() ?: emptyList()) {
                        if (fbMsg.id in existingEventIds) continue
                        if (fbMsg.message.isNullOrBlank()) continue

                        val isFromMe = fbMsg.from?.id == account.userId
                        val timestampMs = parseFacebookTimestamp(fbMsg.createdTime)

                        val mediaUrls = fbMsg.attachments?.data?.mapNotNull { it.payload?.url }?.joinToString(",") ?: ""

                        val message = Message(
                            conversationId = convId,
                            senderPhoneNumber = if (isFromMe) "self" else "fb_${fbMsg.from?.id ?: "unknown"}",
                            recipientPhoneNumber = "fb_${fbConv.id}",
                            content = fbMsg.message ?: "",
                            type = if (mediaUrls.isNotBlank()) "FILE" else "TEXT",
                            timestamp = timestampMs,
                            status = "DELIVERED",
                            sourcePlatform = "FACEBOOK_MESSENGER",
                            mediaUrls = mediaUrls,
                            metadata = """{"messageId":"${fbMsg.id}","conversationId":"${fbConv.id}","platform":"FACEBOOK_MESSENGER"}"""
                        )

                        messageRepository.insertMessage(message)
                        msgsImported++

                        if (timestampMs > latestTimestamp) {
                            latestTimestamp = timestampMs
                            lastMessageContent = fbMsg.message ?: ""
                        }
                    }

                    conversationRepository.updateConversation(
                        conversation.copy(
                            id = convId,
                            lastMessage = lastMessageContent.ifEmpty { conversation.lastMessage },
                            lastMessageTime = latestTimestamp,
                            unreadCount = fbConv.unreadCount ?: conversation.unreadCount,
                            updatedAt = System.currentTimeMillis()
                        )
                    )
                }

                cursor = convResponse.paging?.cursors?.after
                pagesProcessed++
            } while (cursor != null && pagesProcessed < maxPages)

            Timber.d("Messenger sync: %d conversations, %d messages imported", convsImported, msgsImported)
            MessengerSyncResult(
                success = true,
                conversationsImported = convsImported,
                messagesImported = msgsImported
            )
        } catch (e: Exception) {
            Timber.e(e, "Messenger sync failed")
            MessengerSyncResult(success = false, error = e.message ?: "Sync failed")
        }
    }

    suspend fun sendMessage(recipientId: String, text: String): Boolean {
        val account = socialAccountRepository.getAccountByPlatform("FACEBOOK_MESSENGER")
        if (account == null || !account.isConnected) return false

        return try {
            val api = getApi()
            val request = FacebookSendRequest(
                recipient = FacebookRecipient(id = recipientId),
                message = FacebookMessageBody(text = text)
            )
            val response = api.sendMessage(encryptionManager.decryptToken(account.accessToken), request)
            response.messageId != null
        } catch (e: Exception) {
            Timber.e(e, "Messenger send failed")
            false
        }
    }

    private fun parseFacebookTimestamp(timestamp: String?): Long {
        if (timestamp == null) return System.currentTimeMillis()
        return try {
            java.time.Instant.parse(timestamp).toEpochMilli()
        } catch (_: Exception) {
            try {
                val sdf = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", java.util.Locale.US)
                sdf.parse(timestamp)?.time ?: System.currentTimeMillis()
            } catch (_: Exception) {
                System.currentTimeMillis()
            }
        }
    }

    private fun parseFacebookError(e: Exception): String {
        val msg = e.message ?: return "Verification failed"
        return when {
            msg.contains("OAuthException") || msg.contains("invalid_token") -> "Invalid page access token"
            msg.contains("Error validating access token") -> "Access token expired. Generate a new one"
            msg.contains("Application does not have permission") -> "App needs Messenger Platform permission"
            msg.contains("Unable to resolve host") -> "No network connection"
            else -> msg
        }
    }
}
