package com.nexusmedia.nexussms.features.security

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
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
    @ApplicationContext private val context: Context,
    private val masterKey: MasterKey
) {
    companion object {
        private const val VAULT_PREFS = "vault_prefs"
        private const val PIN_HASH_KEY = "vault_pin_hash"
        private const val PIN_SALT_KEY = "vault_pin_salt"
        private const val PATTERN_HASH_KEY = "vault_pattern_hash"
        private const val DECOY_PIN_HASH_KEY = "decoy_pin_hash"
        private const val DECOY_PIN_SALT_KEY = "decoy_pin_salt"
        private const val HIDDEN_CONVERSATIONS_KEY = "hidden_conversations"
        private const val VAULT_ENABLED_KEY = "vault_enabled"
        private const val LAST_ACCESS_KEY = "last_access"
        private const val ACCESS_COUNT_KEY = "access_count"
        private const val HIDE_FROM_RECENTS_KEY = "hide_from_recents"
        private const val FAKE_VAULT_KEY = "fake_vault_enabled"
        private const val FAILED_ATTEMPTS_KEY = "failed_attempts"
        private const val LOCKOUT_UNTIL_KEY = "lockout_until"

        private const val PBKDF2_ITERATIONS = 100_000
        private const val SALT_LENGTH = 16
        private const val MAX_FAILED_ATTEMPTS = 5
        private const val LOCKOUT_DURATION_MS = 30_000L // 30 seconds
    }

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
        val pinSaltBytes = ByteArray(SALT_LENGTH).also { SecureRandom().nextBytes(it) }
        val pinSalt = java.util.Base64.getEncoder().encodeToString(pinSaltBytes)
        val pinHash = hashPinWithSalt(pin, pinSaltBytes)
        vaultPrefs.edit()
            .putBoolean(VAULT_ENABLED_KEY, true)
            .putString(PIN_HASH_KEY, pinHash)
            .putString(PIN_SALT_KEY, pinSalt)
            .apply()

        if (decoyPin != null) {
            val decoySaltBytes = ByteArray(SALT_LENGTH).also { SecureRandom().nextBytes(it) }
            val decoySalt = java.util.Base64.getEncoder().encodeToString(decoySaltBytes)
            val decoyHash = hashPinWithSalt(decoyPin, decoySaltBytes)
            vaultPrefs.edit()
                .putBoolean(FAKE_VAULT_KEY, true)
                .putString(DECOY_PIN_HASH_KEY, decoyHash)
                .putString(DECOY_PIN_SALT_KEY, decoySalt)
                .apply()
        }

        _vaultState.value = VaultState.UNLOCKED
        return true
    }

    fun unlockVault(pin: String): VaultUnlockResult {
        val now = System.currentTimeMillis()
        val lockoutUntil = vaultPrefs.getLong(LOCKOUT_UNTIL_KEY, 0)
        if (now < lockoutUntil) {
            val remaining = ((lockoutUntil - now) / 1000).toInt()
            return VaultUnlockResult.ERROR("Too many attempts. Try again in ${remaining}s")
        }

        val storedPinHash = vaultPrefs.getString(PIN_HASH_KEY, null)
        val storedSalt = vaultPrefs.getString(PIN_SALT_KEY, null)

        if (storedPinHash == null || storedSalt == null) {
            return VaultUnlockResult.ERROR("Vault not set up")
        }

        val inputHash = hashPinWithSalt(pin, storedSalt)

        if (MessageDigest.isEqual(inputHash.toByteArray(Charsets.UTF_8), storedPinHash.toByteArray(Charsets.UTF_8))) {
            _vaultState.value = VaultState.UNLOCKED
            updateAccessStats()
            vaultPrefs.edit()
                .putInt(FAILED_ATTEMPTS_KEY, 0)
                .apply()
            return VaultUnlockResult.SUCCESS
        }

        val decoyPinHash = vaultPrefs.getString(DECOY_PIN_HASH_KEY, null)
        val decoySalt = vaultPrefs.getString(DECOY_PIN_SALT_KEY, null)
        if (decoyPinHash != null && decoySalt != null) {
            val decoyInputHash = hashPinWithSalt(pin, decoySalt)
            if (MessageDigest.isEqual(decoyInputHash.toByteArray(Charsets.UTF_8), decoyPinHash.toByteArray(Charsets.UTF_8))) {
                _vaultState.value = VaultState.DECOY
                updateAccessStats()
                vaultPrefs.edit()
                    .putInt(FAILED_ATTEMPTS_KEY, 0)
                    .apply()
                return VaultUnlockResult.DECOY
            }
        }

        val failedAttempts = vaultPrefs.getInt(FAILED_ATTEMPTS_KEY, 0) + 1
        vaultPrefs.edit()
            .putInt(FAILED_ATTEMPTS_KEY, failedAttempts)
            .apply()

        if (failedAttempts >= MAX_FAILED_ATTEMPTS) {
            vaultPrefs.edit()
                .putLong(LOCKOUT_UNTIL_KEY, now + LOCKOUT_DURATION_MS)
                .putInt(FAILED_ATTEMPTS_KEY, 0)
                .apply()
            return VaultUnlockResult.ERROR("Too many attempts. Locked for 30 seconds")
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
        val removed = current.find { it.id == hiddenId }
        if (removed != null) {
            current.removeAll { it.id == hiddenId }
            _hiddenConversations.value = current
            saveHiddenConversations(current)
            return removed.originalConversationId
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
        val storedHash = vaultPrefs.getString(PIN_HASH_KEY, null)
        val storedSalt = vaultPrefs.getString(PIN_SALT_KEY, null) ?: return false
        if (storedHash == null) return false

        val oldHash = hashPinWithSalt(oldPin, storedSalt)
        if (!MessageDigest.isEqual(oldHash.toByteArray(Charsets.UTF_8), storedHash.toByteArray(Charsets.UTF_8))) return false

        val newSaltBytes = ByteArray(SALT_LENGTH).also { SecureRandom().nextBytes(it) }
        val newSalt = java.util.Base64.getEncoder().encodeToString(newSaltBytes)
        val newHash = hashPinWithSalt(newPin, newSaltBytes)
        vaultPrefs.edit()
            .putString(PIN_HASH_KEY, newHash)
            .putString(PIN_SALT_KEY, newSalt)
            .apply()

        return true
    }

    fun disableVault(pin: String): Boolean {
        val storedHash = vaultPrefs.getString(PIN_HASH_KEY, null)
        val storedSalt = vaultPrefs.getString(PIN_SALT_KEY, null) ?: return false
        if (storedHash == null) return false

        val pinHash = hashPinWithSalt(pin, storedSalt)
        if (!MessageDigest.isEqual(pinHash.toByteArray(Charsets.UTF_8), storedHash.toByteArray(Charsets.UTF_8))) return false

        vaultPrefs.edit()
            .putBoolean(VAULT_ENABLED_KEY, false)
            .remove(PIN_HASH_KEY)
            .remove(PIN_SALT_KEY)
            .remove(PATTERN_HASH_KEY)
            .remove(DECOY_PIN_HASH_KEY)
            .remove(DECOY_PIN_SALT_KEY)
            .remove(FAKE_VAULT_KEY)
            .remove(FAILED_ATTEMPTS_KEY)
            .remove(LOCKOUT_UNTIL_KEY)
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
            try {
                val type = object : TypeToken<List<HiddenConversation>>() {}.type
                _hiddenConversations.value = gson.fromJson(json, type)
            } catch (e: Exception) {
                _hiddenConversations.value = emptyList()
                vaultPrefs.edit().remove(HIDDEN_CONVERSATIONS_KEY).apply()
            }
        }
    }

    private fun saveHiddenConversations(conversations: List<HiddenConversation>) {
        val json = gson.toJson(conversations)
        vaultPrefs.edit()
            .putString(HIDDEN_CONVERSATIONS_KEY, json)
            .apply()
    }

    private fun hashPinWithSalt(pin: String, salt: ByteArray): String {
        val spec = PBEKeySpec(pin.toCharArray(), salt, PBKDF2_ITERATIONS, 256)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val hash = factory.generateSecret(spec).encoded
        return hash.joinToString("") { "%02x".format(it) }
    }

    private fun hashPinWithSalt(pin: String, saltBase64: String): String {
        val salt = java.util.Base64.getDecoder().decode(saltBase64)
        return hashPinWithSalt(pin, salt)
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
