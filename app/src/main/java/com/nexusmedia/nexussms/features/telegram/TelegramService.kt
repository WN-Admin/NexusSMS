package com.nexusmedia.nexussms.features.telegram

import android.content.Context
import com.nexusmedia.nexussms.data.models.Conversation
import com.nexusmedia.nexussms.data.models.Message
import com.nexusmedia.nexussms.data.models.SocialAccount
import com.nexusmedia.nexussms.data.repository.ConversationRepository
import com.nexusmedia.nexussms.data.repository.MessageRepository
import com.nexusmedia.nexussms.data.repository.SocialAccountRepository
import com.nexusmedia.nexussms.security.EncryptionManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

data class TelegramAuthResult(
    val success: Boolean,
    val botUsername: String? = null,
    val botId: Long? = null,
    val error: String? = null
)

data class TelegramSyncResult(
    val success: Boolean,
    val conversationsImported: Int = 0,
    val messagesImported: Int = 0,
    val error: String? = null
)

@Singleton
class TelegramService @Inject constructor(
    private val conversationRepository: ConversationRepository,
    private val messageRepository: MessageRepository,
    private val socialAccountRepository: SocialAccountRepository,
    private val encryptionManager: EncryptionManager,
    @ApplicationContext private val context: Context
) {

    companion object {
        private const val PREFS_NAME = "telegram_sync_state"
        private const val KEY_LAST_UPDATE_ID = "last_update_id"
    }

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private var currentApi: TelegramBotApi? = null
    private var lastUpdateId: Long?
        get() = prefs.getLong(KEY_LAST_UPDATE_ID, 0L).let { if (it == 0L) null else it }
        set(value) { prefs.edit().putLong(KEY_LAST_UPDATE_ID, value ?: 0L).apply() }

    private fun getApi(token: String): TelegramBotApi {
        val baseUrl = "https://api.telegram.org/bot$token/"
        val client = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .addInterceptor(
                HttpLoggingInterceptor { msg -> Timber.d("TelegramAPI: %s", msg) }
                    .apply { level = HttpLoggingInterceptor.Level.BODY }
            )
            .build()

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(TelegramBotApi::class.java)
    }

    suspend fun verifyToken(botToken: String): TelegramAuthResult {
        return try {
            val api = getApi(botToken)
            val response = api.getMe()
            if (response.ok && response.result != null) {
                val bot = response.result
                Timber.d("Telegram bot verified: @%s (id=%d)", bot.username, bot.id)
                TelegramAuthResult(
                    success = true,
                    botUsername = bot.username,
                    botId = bot.id
                )
            } else {
                TelegramAuthResult(success = false, error = "Invalid bot token")
            }
        } catch (e: Exception) {
            Timber.e(e, "Telegram token verification failed")
            TelegramAuthResult(success = false, error = e.message ?: "Verification failed")
        }
    }

    suspend fun connectBot(botToken: String): TelegramAuthResult {
        val result = verifyToken(botToken)
        if (result.success) {
            val existing = socialAccountRepository.getAccountByPlatform("TELEGRAM")
            val account = SocialAccount(
                id = existing?.id ?: java.util.UUID.randomUUID().toString(),
                platform = "TELEGRAM",
                userId = result.botId.toString(),
                username = result.botUsername ?: "",
                displayName = "@${result.botUsername}",
                accessToken = encryptionManager.encryptToken(botToken),
                isConnected = true,
                settings = """{"botId":${result.botId}}""",
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
        val account = socialAccountRepository.getAccountByPlatform("TELEGRAM")
        if (account != null) {
            socialAccountRepository.updateAccount(
                account.copy(
                    isConnected = false,
                    accessToken = "",
                    updatedAt = System.currentTimeMillis()
                )
            )
        }
        lastUpdateId = null
        currentApi = null
    }

    suspend fun sync(): TelegramSyncResult {
        val account = socialAccountRepository.getAccountByPlatform("TELEGRAM")
        if (account == null || !account.isConnected) {
            return TelegramSyncResult(false, error = "Telegram bot not connected")
        }
        val token = encryptionManager.decryptToken(account.accessToken)
        if (token.isBlank()) {
            return TelegramSyncResult(false, error = "No bot token")
        }

        return try {
            val api = getApi(token)
            currentApi = api

            val response = api.getUpdates(
                offset = lastUpdateId,
                limit = 100,
                timeout = 5
            )

            if (!response.ok) {
                return TelegramSyncResult(false, error = "Failed to get updates")
            }

            val updates = response.result ?: emptyList()
            var convsImported = 0
            var msgsImported = 0

            val existingConversations = conversationRepository.getAllConversations().first()

            for (update in updates) {
                val msg = update.message ?: update.channelPost
                if (msg == null) {
                    lastUpdateId = update.updateId + 1
                    continue
                }
                lastUpdateId = update.updateId + 1

                val text = msg.text ?: msg.caption
                val hasMedia = msg.photo != null || msg.document != null || msg.voice != null
                if (text.isNullOrBlank() && !hasMedia) continue

                val chatId = msg.chat.id
                val chatName = msg.chat.displayName
                val isGroup = msg.chat.type == "group" || msg.chat.type == "supergroup" || msg.chat.type == "channel"

                val existing = existingConversations.find {
                    it.participantPhoneNumbers.contains("tg_$chatId") && it.sourcePlatform == "TELEGRAM"
                }

                val conversation = if (existing != null) {
                    existing.copy(displayName = chatName)
                } else {
                    convsImported++
                    Conversation(
                        participantPhoneNumbers = """["tg_$chatId"]""",
                        displayName = chatName,
                        sourcePlatform = "TELEGRAM",
                        sourceAccountId = chatId.toString(),
                        isGroupChat = isGroup,
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

                val isFromMe = msg.from?.isBot == true && msg.from.id.toString() == account.userId

                val contentText = text ?: when {
                    msg.photo != null -> "[Photo]"
                    msg.document != null -> "[Document: ${msg.document.fileName ?: "file"}]"
                    msg.voice != null -> "[Voice message]"
                    else -> ""
                }

                val message = Message(
                    conversationId = convId,
                    senderPhoneNumber = if (isFromMe) "self" else "tg_${msg.from?.id ?: "unknown"}",
                    recipientPhoneNumber = "tg_$chatId",
                    content = contentText,
                    type = when {
                        msg.photo != null -> "IMAGE"
                        msg.document != null -> "FILE"
                        msg.voice != null -> "AUDIO"
                        else -> "TEXT"
                    },
                    timestamp = msg.date * 1000L,
                    status = "DELIVERED",
                    sourcePlatform = "TELEGRAM",
                    metadata = """{"updateId":${update.updateId},"chatId":$chatId,"messageId":${msg.messageId},"platform":"TELEGRAM"}"""
                )

                messageRepository.insertMessage(message)
                msgsImported++

                conversationRepository.updateConversation(
                    (if (existing != null) conversation.copy(id = convId) else conversation.copy(id = convId)).copy(
                        lastMessage = contentText.ifEmpty { "[media]" },
                        lastMessageTime = msg.date * 1000L,
                        updatedAt = System.currentTimeMillis()
                    )
                )
            }

            if (updates.isNotEmpty()) {
                Timber.d("Telegram sync: %d conversations, %d messages imported", convsImported, msgsImported)
            }

            TelegramSyncResult(
                success = true,
                conversationsImported = convsImported,
                messagesImported = msgsImported
            )
        } catch (e: Exception) {
            Timber.e(e, "Telegram sync failed")
            TelegramSyncResult(success = false, error = e.message ?: "Sync failed")
        }
    }

    suspend fun sendMessage(chatId: Long, text: String): Boolean {
        val account = socialAccountRepository.getAccountByPlatform("TELEGRAM")
        if (account == null || !account.isConnected) return false

        return try {
            val api = getApi(encryptionManager.decryptToken(account.accessToken))
            val response = api.sendMessage(TelegramSendMessageRequest(chatId = chatId, text = text))
            response.ok
        } catch (e: Exception) {
            Timber.e(e, "Telegram send failed")
            false
        }
    }

    fun isLoggedIn(): Boolean {
        return currentApi != null
    }
}
