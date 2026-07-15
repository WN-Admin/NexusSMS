package com.nexusmedia.nexussms.features.security

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

data class VaultConfig(
    val isEnabled: Boolean = false,
    val vaultPin: String? = null,
    val vaultPattern: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val lastAccessed: Long = 0,
    val accessCount: Long = 0,
    val isLocked: Boolean = true,
    val autoLockTimeout: Long = 300000,
    val hideFromRecents: Boolean = true,
    val fakeVaultEnabled: Boolean = false,
    val decoyPin: String? = null
)

data class HiddenConversation(
    val id: String,
    val originalConversationId: String,
    val displayName: String,
    val lastMessage: String,
    val lastMessageTime: Long,
    val createdAt: Long = System.currentTimeMillis(),
    val isPinned: Boolean = false,
    val isMuted: Boolean = false
)

@Singleton
class VaultManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val VAULT_PREFS = "vault_prefs"
        private const val PIN_HASH_KEY = "vault_pin_hash"
        private const val PATTERN_HASH_KEY = "vault_pattern_hash"
        private const val DECOY_PIN_HASH_KEY = "decoy_pin_hash"
        private const val HIDDEN_CONVERSATIONS_KEY = "hidden_conversations"
        private const val VAULT_ENABLED_KEY = "vault_enabled"
        private const val LAST_ACCESS_KEY = "last_access"
        private const val ACCESS_COUNT_KEY = "access_count"
        private const val HIDE_FROM_RECENTS_KEY = "hide_from_recents"
        private const val FAKE_VAULT_KEY = "fake_vault_enabled"
    }

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val vaultPrefs = EncryptedSharedPreferences.create(
        context,
        VAULT_PREFS,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    private val gson = Gson()

    private val _vaultState = MutableStateFlow(VaultState.LOCKED)
    val vaultState: StateFlow<VaultState> = _vaultState.asStateFlow()

    private val _hiddenConversations = MutableStateFlow<List<HiddenConversation>>(emptyList())
    val hiddenConversations: StateFlow<List<HiddenConversation>> = _hiddenConversations.asStateFlow()

    init {
        loadHiddenConversations()
    }

    fun isVaultEnabled(): Boolean {
        return vaultPrefs.getBoolean(VAULT_ENABLED_KEY, false)
    }

    fun hasVaultPin(): Boolean {
        return vaultPrefs.getString(PIN_HASH_KEY, null) != null
    }

    fun setupVault(pin: String, decoyPin: String? = null): Boolean {
        val pinHash = hashPin(pin)
        vaultPrefs.edit()
            .putBoolean(VAULT_ENABLED_KEY, true)
            .putString(PIN_HASH_KEY, pinHash)
            .apply()

        if (decoyPin != null) {
            val decoyHash = hashPin(decoyPin)
            vaultPrefs.edit()
                .putBoolean(FAKE_VAULT_KEY, true)
                .putString(DECOY_PIN_HASH_KEY, decoyHash)
                .apply()
        }

        _vaultState.value = VaultState.UNLOCKED
        return true
    }

    fun unlockVault(pin: String): VaultUnlockResult {
        val storedPinHash = vaultPrefs.getString(PIN_HASH_KEY, null)

        if (storedPinHash == null) {
            return VaultUnlockResult.ERROR("Vault not set up")
        }

        val inputHash = hashPin(pin)

        if (inputHash == storedPinHash) {
            _vaultState.value = VaultState.UNLOCKED
            updateAccessStats()
            return VaultUnlockResult.SUCCESS
        }

        val decoyPinHash = vaultPrefs.getString(DECOY_PIN_HASH_KEY, null)
        if (decoyPinHash != null && inputHash == decoyPinHash) {
            _vaultState.value = VaultState.DECOY
            updateAccessStats()
            return VaultUnlockResult.DECOY
        }

        return VaultUnlockResult.INCORRECT_PIN
    }

    fun lockVault() {
        _vaultState.value = VaultState.LOCKED
        _hiddenConversations.value = emptyList()
    }

    fun isVaultUnlocked(): Boolean {
        return _vaultState.value == VaultState.UNLOCKED
    }

    fun isDecoyVault(): Boolean {
        return _vaultState.value == VaultState.DECOY
    }

    fun hideConversation(conversationId: String, displayName: String, lastMessage: String, lastMessageTime: Long): Boolean {
        if (!isVaultUnlocked()) return false

        val hidden = HiddenConversation(
            id = java.util.UUID.randomUUID().toString(),
            originalConversationId = conversationId,
            displayName = displayName,
            lastMessage = lastMessage,
            lastMessageTime = lastMessageTime
        )

        val current = _hiddenConversations.value.toMutableList()
        current.add(hidden)
        _hiddenConversations.value = current
        saveHiddenConversations(current)

        return true
    }

    fun unhideConversation(hiddenId: String): String? {
        if (!isVaultUnlocked()) return null

        val current = _hiddenConversations.value.toMutableList()
        val removed = current.removeAll { it.id == hiddenId }

        if (removed) {
            _hiddenConversations.value = current
            saveHiddenConversations(current)

            val conversation = current.find { it.id == hiddenId }
            return conversation?.originalConversationId
        }

        return null
    }

    fun getHiddenConversations(): List<HiddenConversation> {
        if (!isVaultUnlocked()) return emptyList()
        return _hiddenConversations.value
    }

    fun isConversationHidden(conversationId: String): Boolean {
        return _hiddenConversations.value.any { it.originalConversationId == conversationId }
    }

    fun updateAutoLockTimeout(timeout: Long) {
        vaultPrefs.edit()
            .putLong("auto_lock_timeout", timeout)
            .apply()
    }

    fun getAutoLockTimeout(): Long {
        return vaultPrefs.getLong("auto_lock_timeout", 300000)
    }

    fun setHideFromRecents(hide: Boolean) {
        vaultPrefs.edit()
            .putBoolean(HIDE_FROM_RECENTS_KEY, hide)
            .apply()
    }

    fun shouldHideFromRecents(): Boolean {
        return vaultPrefs.getBoolean(HIDE_FROM_RECENTS_KEY, true)
    }

    fun changePin(oldPin: String, newPin: String): Boolean {
        val oldHash = hashPin(oldPin)
        val storedHash = vaultPrefs.getString(PIN_HASH_KEY, null)

        if (oldHash != storedHash) return false

        val newHash = hashPin(newPin)
        vaultPrefs.edit()
            .putString(PIN_HASH_KEY, newHash)
            .apply()

        return true
    }

    fun disableVault(pin: String): Boolean {
        val pinHash = hashPin(pin)
        val storedHash = vaultPrefs.getString(PIN_HASH_KEY, null)

        if (pinHash != storedHash) return false

        vaultPrefs.edit()
            .putBoolean(VAULT_ENABLED_KEY, false)
            .remove(PIN_HASH_KEY)
            .remove(PATTERN_HASH_KEY)
            .remove(DECOY_PIN_HASH_KEY)
            .remove(FAKE_VAULT_KEY)
            .apply()

        _vaultState.value = VaultState.DISABLED
        _hiddenConversations.value = emptyList()

        return true
    }

    fun deleteAllHiddenConversations() {
        _hiddenConversations.value = emptyList()
        saveHiddenConversations(emptyList())
    }

    private fun updateAccessStats() {
        val lastAccess = vaultPrefs.getLong(LAST_ACCESS_KEY, 0)
        val accessCount = vaultPrefs.getLong(ACCESS_COUNT_KEY, 0)

        vaultPrefs.edit()
            .putLong(LAST_ACCESS_KEY, System.currentTimeMillis())
            .putLong(ACCESS_COUNT_KEY, accessCount + 1)
            .apply()
    }

    private fun loadHiddenConversations() {
        val json = vaultPrefs.getString(HIDDEN_CONVERSATIONS_KEY, null)
        if (json != null) {
            val type = object : TypeToken<List<HiddenConversation>>() {}.type
            _hiddenConversations.value = gson.fromJson(json, type)
        }
    }

    private fun saveHiddenConversations(conversations: List<HiddenConversation>) {
        val json = gson.toJson(conversations)
        vaultPrefs.edit()
            .putString(HIDDEN_CONVERSATIONS_KEY, json)
            .apply()
    }

    private fun hashPin(pin: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(pin.toByteArray())
        return hash.joinToString("") { "%02x".format(it) }
    }
}

enum class VaultState {
    DISABLED,
    LOCKED,
    UNLOCKED,
    DECOY
}

sealed class VaultUnlockResult {
    object SUCCESS : VaultUnlockResult()
    object DECOY : VaultUnlockResult()
    object INCORRECT_PIN : VaultUnlockResult()
    data class ERROR(val message: String) : VaultUnlockResult()
}
