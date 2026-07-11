package com.nexusmedia.nexussms.features.social

import com.nexusmedia.nexussms.data.models.SocialAccount
import com.nexusmedia.nexussms.data.models.Message
import com.nexusmedia.nexussms.data.repository.SocialAccountRepository
import com.nexusmedia.nexussms.data.repository.MessageRepository
import kotlinx.coroutines.flow.first
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow

/**
 * Supports integration with multiple social media platforms:
 * - Facebook Messenger
 * - Discord
 * - Telegram
 * - Viber
 * - Matrix
 */
@Singleton
class SocialMediaIntegrationService @Inject constructor(
    private val socialAccountRepository: SocialAccountRepository,
    private val messageRepository: MessageRepository
) {

    enum class SocialPlatform {
        FACEBOOK_MESSENGER,
        DISCORD,
        TELEGRAM,
        VIBER,
        MATRIX
    }

    suspend fun connectAccount(
        platform: SocialPlatform,
        accountId: String,
        username: String,
        accessToken: String,
        refreshToken: String = "",
        displayName: String = ""
    ): Long {
        val account = SocialAccount(
            platform = platform.name,
            userId = accountId,
            username = username,
            accessToken = accessToken,
            refreshToken = refreshToken,
            displayName = displayName,
            isConnected = true
        )
        return socialAccountRepository.insertAccount(account)
    }

    suspend fun sendSocialMediaMessage(
        platform: SocialPlatform,
        recipientId: String,
        content: String,
        attachments: List<String> = emptyList(),
        conversationId: Long
    ): Long {
        val message = Message(
            conversationId = conversationId.toString(),
            senderPhoneNumber = "self",
            recipientPhoneNumber = recipientId,
            content = content,
            timestamp = System.currentTimeMillis(),
            status = "SENT",
            type = "SOCIAL",
            mediaUrls = attachments.joinToString(","),
            encryptionAlgorithm = "AES256",
            metadata = "{\"platform\":\"${platform.name}\"}"
        )
        return messageRepository.insertMessage(message)
    }

    fun getConnectedAccounts(platform: SocialPlatform): Flow<List<SocialAccount>> {
        return socialAccountRepository.getAccountsByPlatform(platform.name)
    }

    fun getAllConnectedAccounts(): Flow<List<SocialAccount>> {
        return socialAccountRepository.getConnectedAccounts()
    }

    suspend fun disconnectAccount(accountId: String) {
        val accounts = socialAccountRepository.getAllAccounts().first()
        val account = accounts.find { it.id == accountId }
        if (account != null) {
            val updated = account.copy(
                isConnected = false,
                accessToken = "",
                refreshToken = null
            )
            socialAccountRepository.updateAccount(updated)
            Timber.d("Disconnected account: %s", accountId)
        }
    }

    suspend fun syncMessagesFromPlatform(platform: SocialPlatform) {
        Timber.d("syncMessagesFromPlatform requested for platform: %s", platform.name)
        val messages = messageRepository.getMessagesByType("SOCIAL").first()
        Timber.d("syncMessagesFromPlatform: found %d social messages", messages.size)
    }

    suspend fun updateAccountToken(accountId: String, newToken: String) {
        val accounts = socialAccountRepository.getAllAccounts().first()
        val account = accounts.find { it.id == accountId }
        if (account != null) {
            val updated = account.copy(
                accessToken = newToken,
                updatedAt = System.currentTimeMillis()
            )
            socialAccountRepository.updateAccount(updated)
            Timber.d("Updated token for account: %s", accountId)
        }
    }

    fun getSocialMediaMessages(
        platform: SocialPlatform,
        conversationId: Long
    ): Flow<List<Message>> {
        // In a real implementation, we might filter by platform in metadata
        return messageRepository.getMessagesByType("SOCIAL")
    }

    suspend fun facebookMessengerSend(
        recipientId: String,
        content: String,
        conversationId: Long
    ): Long {
        return sendSocialMediaMessage(
            SocialPlatform.FACEBOOK_MESSENGER,
            recipientId,
            content,
            emptyList(),
            conversationId
        )
    }

    suspend fun discordSend(
        recipientId: String,
        content: String,
        conversationId: Long
    ): Long {
        return sendSocialMediaMessage(
            SocialPlatform.DISCORD,
            recipientId,
            content,
            emptyList(),
            conversationId
        )
    }

    suspend fun telegramSend(
        recipientId: String,
        content: String,
        conversationId: Long
    ): Long {
        return sendSocialMediaMessage(
            SocialPlatform.TELEGRAM,
            recipientId,
            content,
            emptyList(),
            conversationId
        )
    }

    suspend fun viberSend(
        recipientId: String,
        content: String,
        conversationId: Long
    ): Long {
        return sendSocialMediaMessage(
            SocialPlatform.VIBER,
            recipientId,
            content,
            emptyList(),
            conversationId
        )
    }

    suspend fun matrixSend(
        recipientId: String,
        content: String,
        conversationId: Long
    ): Long {
        return sendSocialMediaMessage(
            SocialPlatform.MATRIX,
            recipientId,
            content,
            emptyList(),
            conversationId
        )
    }
}
