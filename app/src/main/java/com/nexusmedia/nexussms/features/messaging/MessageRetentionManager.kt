package com.nexusmedia.nexussms.features.messaging

import android.content.Context
import com.nexusmedia.nexussms.data.repository.ConversationRepository
import com.nexusmedia.nexussms.data.repository.MessageRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MessageRetentionManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val messageRepository: MessageRepository,
    private val conversationRepository: ConversationRepository
) {
    private val prefs = context.getSharedPreferences("retention_prefs", Context.MODE_PRIVATE)

    var retentionDays: Int
        get() = prefs.getInt("retention_days", 0)
        set(value) = prefs.edit().putInt("retention_days", value.coerceAtLeast(0)).apply()

    var retentionEnabled: Boolean
        get() = prefs.getBoolean("retention_enabled", false)
        set(value) = prefs.edit().putBoolean("retention_enabled", value).apply()

    suspend fun enforceRetention() {
        if (!retentionEnabled || retentionDays <= 0) return
        val cutoff = System.currentTimeMillis() - (retentionDays * 24L * 60 * 60 * 1000)
        try {
            val conversations = conversationRepository.getAllConversations().first()
            var deletedCount = 0
            for (conversation in conversations) {
                val messages = messageRepository.getConversationMessages(conversation.id).first()
                val messagesToDelete = messages.filter { msg ->
                    msg.timestamp < cutoff && !msg.isLocked
                }
                if (messagesToDelete.isNotEmpty()) {
                    messageRepository.deleteMessagesByIds(messagesToDelete.map { msg -> msg.id })
                    deletedCount += messagesToDelete.size
                }
            }
            Timber.d("Retention enforced: deleted $deletedCount messages older than $retentionDays days")
        } catch (e: Exception) {
            Timber.e(e, "Error enforcing message retention")
        }
    }
}
