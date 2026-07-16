package com.nexusmedia.nexussms.features.discord

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

data class DiscordAuthResult(
    val success: Boolean,
    val username: String? = null,
    val userId: String? = null,
    val error: String? = null
)

data class DiscordSyncResult(
    val success: Boolean,
    val conversationsImported: Int = 0,
    val messagesImported: Int = 0,
    val error: String? = null
)

@Singleton
class DiscordService @Inject constructor(
    private val conversationRepository: ConversationRepository,
    private val messageRepository: MessageRepository,
    private val socialAccountRepository: SocialAccountRepository,
    private val encryptionManager: EncryptionManager
) {

    private fun getApi(): DiscordApi {
        val client = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(
                HttpLoggingInterceptor { msg -> Timber.d("DiscordAPI: %s", msg) }
                    .apply { level = HttpLoggingInterceptor.Level.BODY }
            )
            .build()

        return Retrofit.Builder()
            .baseUrl("https://discord.com/api/v10/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(DiscordApi::class.java)
    }

    private fun authHeader(token: String) = "Bot $token"

    suspend fun verifyToken(botToken: String): DiscordAuthResult {
        return try {
            val api = getApi()
            val user = api.getMe(authHeader(botToken))
            Timber.d("Discord bot verified: %s (id=%s)", user.username, user.id)
            DiscordAuthResult(
                success = true,
                username = user.username,
                userId = user.id
            )
        } catch (e: Exception) {
            Timber.e(e, "Discord token verification failed")
            DiscordAuthResult(success = false, error = e.message ?: "Verification failed")
        }
    }

    suspend fun connectBot(botToken: String): DiscordAuthResult {
        val result = verifyToken(botToken)
        if (result.success) {
            val existing = socialAccountRepository.getAccountByPlatform("DISCORD")
            val account = SocialAccount(
                id = existing?.id ?: java.util.UUID.randomUUID().toString(),
                platform = "DISCORD",
                userId = result.userId ?: "",
                username = result.username ?: "",
                displayName = result.username ?: "",
                accessToken = encryptionManager.encryptToken(botToken),
                isConnected = true,
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
        socialAccountRepository.getAccountByPlatform("DISCORD")?.let { account ->
            socialAccountRepository.updateAccount(
                account.copy(
                    isConnected = false,
                    accessToken = "",
                    updatedAt = System.currentTimeMillis()
                )
            )
        }
    }

    suspend fun sync(): DiscordSyncResult {
        val account = socialAccountRepository.getAccountByPlatform("DISCORD")
        if (account == null || !account.isConnected) {
            return DiscordSyncResult(false, error = "Discord bot not connected")
        }
        val token = encryptionManager.decryptToken(account.accessToken)
        if (token.isBlank()) {
            return DiscordSyncResult(false, error = "No bot token")
        }

        return try {
            val api = getApi()
            val header = authHeader(token)

            val guilds = api.getGuilds(header)
            val existingConversations = conversationRepository.getAllConversations().first()
            var convsImported = 0
            var msgsImported = 0

            for (guild in guilds) {
                val channels = try {
                    api.getChannels(header, guild.id).filter { it.isTextChannel }
                } catch (e: Exception) {
                    Timber.w(e, "Failed to fetch channels for guild %s", guild.name)
                    continue
                }

                for (channel in channels) {
                    val channelName = "#${channel.name ?: channel.id}"
                    val displayName = if (guild.name.isNotBlank()) {
                        "${guild.name} / $channelName"
                    } else {
                        channelName
                    }

                    val existing = existingConversations.find {
                        it.participantPhoneNumbers.contains("dc_${channel.id}") && it.sourcePlatform == "DISCORD"
                    }

                    val conversation = if (existing != null) {
                        existing.copy(displayName = displayName)
                    } else {
                        convsImported++
                        Conversation(
                            participantPhoneNumbers = """["dc_${channel.id}"]""",
                            displayName = displayName,
                            sourcePlatform = "DISCORD",
                            sourceAccountId = channel.id,
                            isGroupChat = true,
                            avatarUrl = null
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
                        api.getMessages(header, channel.id, limit = 50)
                    } catch (e: Exception) {
                        Timber.w(e, "Failed to fetch messages for channel %s", channelName)
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

                    for (discordMsg in messages.reversed()) {
                        if (discordMsg.id in existingEventIds) continue
                        if (discordMsg.content.isBlank() && discordMsg.attachments.isNullOrEmpty()) continue

                        val isFromMe = discordMsg.author.bot == true && discordMsg.author.id == account.userId
                        val timestampMs = parseDiscordTimestamp(discordMsg.timestamp)

                        val content = discordMsg.content.ifBlank {
                            discordMsg.attachments?.firstOrNull()?.filename ?: "[attachment]"
                        }

                        val mediaUrls = discordMsg.attachments?.joinToString(",") { it.url } ?: ""

                        val message = Message(
                            conversationId = convId,
                            senderPhoneNumber = if (isFromMe) "self" else "dc_${discordMsg.author.id}",
                            recipientPhoneNumber = "dc_${channel.id}",
                            content = content,
                            type = if (discordMsg.attachments?.isNotEmpty() == true) "FILE" else "TEXT",
                            timestamp = timestampMs,
                            status = "DELIVERED",
                            sourcePlatform = "DISCORD",
                            mediaUrls = mediaUrls,
                            metadata = """{"messageId":"${discordMsg.id}","channelId":"${channel.id}","platform":"DISCORD"}"""
                        )

                        messageRepository.insertMessage(message)
                        msgsImported++

                        if (timestampMs > latestTimestamp) {
                            latestTimestamp = timestampMs
                            lastMessageContent = content
                        }
                    }

                    if (messages.isNotEmpty()) {
                        conversationRepository.updateConversation(
                            conversation.copy(
                                id = convId,
                                lastMessage = lastMessageContent.ifEmpty { conversation.lastMessage },
                                lastMessageTime = latestTimestamp,
                                updatedAt = System.currentTimeMillis()
                            )
                        )
                    }
                }
            }

            Timber.d("Discord sync: %d conversations, %d messages imported", convsImported, msgsImported)
            DiscordSyncResult(
                success = true,
                conversationsImported = convsImported,
                messagesImported = msgsImported
            )
        } catch (e: Exception) {
            Timber.e(e, "Discord sync failed")
            DiscordSyncResult(success = false, error = e.message ?: "Sync failed")
        }
    }

    suspend fun sendMessage(channelId: String, content: String): Boolean {
        val account = socialAccountRepository.getAccountByPlatform("DISCORD")
        if (account == null || !account.isConnected) return false

        return try {
            val api = getApi()
            api.sendMessage(authHeader(encryptionManager.decryptToken(account.accessToken)), channelId, DiscordSendMessageRequest(content = content))
            true
        } catch (e: Exception) {
            Timber.e(e, "Discord send failed")
            false
        }
    }

    private fun parseDiscordTimestamp(timestamp: String): Long {
        return try {
            java.time.Instant.parse(timestamp).toEpochMilli()
        } catch (_: Exception) {
            System.currentTimeMillis()
        }
    }
}
