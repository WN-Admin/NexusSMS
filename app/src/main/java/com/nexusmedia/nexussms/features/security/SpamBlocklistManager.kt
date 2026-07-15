package com.nexusmedia.nexussms.features.security

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.spamDataStore: DataStore<Preferences> by preferencesDataStore(name = "spam_blocklist")

@Singleton
class SpamBlocklistManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val spamDetector: SpamDetector
) {
    companion object {
        private val BLOCKED_NUMBERS = stringPreferencesKey("blocked_numbers")
        private val AUTO_BLOCK_ENABLED = booleanPreferencesKey("auto_block_enabled")
        private val BLOCK_THRESHOLD = floatPreferencesKey("block_threshold")
        private val NOTIFICATION_ENABLED = booleanPreferencesKey("spam_notification_enabled")
    }

    val autoBlockEnabled: Flow<Boolean> = context.spamDataStore.data.map { prefs ->
        prefs[AUTO_BLOCK_ENABLED] ?: true
    }

    val blockThreshold: Flow<Float> = context.spamDataStore.data.map { prefs ->
        prefs[BLOCK_THRESHOLD] ?: 0.7f
    }

    val spamNotificationEnabled: Flow<Boolean> = context.spamDataStore.data.map { prefs ->
        prefs[NOTIFICATION_ENABLED] ?: true
    }

    suspend fun setAutoBlockEnabled(enabled: Boolean) {
        context.spamDataStore.edit { prefs ->
            prefs[AUTO_BLOCK_ENABLED] = enabled
        }
    }

    suspend fun setBlockThreshold(threshold: Float) {
        context.spamDataStore.edit { prefs ->
            prefs[BLOCK_THRESHOLD] = threshold
        }
    }

    suspend fun setSpamNotificationEnabled(enabled: Boolean) {
        context.spamDataStore.edit { prefs ->
            prefs[NOTIFICATION_ENABLED] = enabled
        }
    }

    suspend fun blockNumber(number: String) {
        val blocked = getBlockedNumbers().toMutableSet()
        blocked.add(number)
        saveBlockedNumbers(blocked)
    }

    suspend fun unblockNumber(number: String) {
        val blocked = getBlockedNumbers().toMutableSet()
        blocked.remove(number)
        saveBlockedNumbers(blocked)
    }

    suspend fun isBlocked(number: String): Boolean {
        return getBlockedNumbers().contains(number)
    }

    suspend fun getBlockedNumbers(): Set<String> {
        return try {
            val prefs = context.spamDataStore.data.first()
            val json = prefs[BLOCKED_NUMBERS] ?: return emptySet()
            json.split(",").filter { it.isNotBlank() }.toSet()
        } catch (e: Exception) {
            emptySet()
        }
    }

    suspend fun shouldBlock(message: String, senderNumber: String): Boolean {
        if (isBlocked(senderNumber)) return true

        if (!autoBlockEnabled.first()) return false

        val detection = spamDetector.analyzeMessage(message)
        val threshold = blockThreshold.first()
        return detection.isSpam && detection.confidence >= threshold
    }

    suspend fun handleSpamDetection(message: String, senderNumber: String, conversationId: String?): SpamAction {
        val detection = spamDetector.analyzeMessage(message)

        if (!detection.isSpam) {
            return SpamAction.NONE
        }

        val shouldAutoBlock = shouldBlock(message, senderNumber)

        if (shouldAutoBlock) {
            blockNumber(senderNumber)
            return SpamAction.BLOCKED
        }

        return SpamAction.WARNING
    }

    private suspend fun saveBlockedNumbers(numbers: Set<String>) {
        context.spamDataStore.edit { prefs ->
            prefs[BLOCKED_NUMBERS] = numbers.joinToString(",")
        }
    }
}

enum class SpamAction {
    NONE,
    WARNING,
    BLOCKED
}
