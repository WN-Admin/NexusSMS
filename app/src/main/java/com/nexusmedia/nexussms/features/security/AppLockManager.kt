package com.nexusmedia.nexussms.features.security

import android.content.Context
import android.content.SharedPreferences
import com.nexusmedia.nexussms.data.database.AppSecuritySettingsDao
import com.nexusmedia.nexussms.data.models.AppSecuritySettings
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppLockManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val appSecuritySettingsDao: AppSecuritySettingsDao
) {
    companion object {
        private const val TAG = "AppLockManager"
        private const val PBKDF2_ITERATIONS = 100_000
        private const val KEY_LENGTH = 256
        private const val SALT_LENGTH = 16
        private const val MAX_FAILED_ATTEMPTS = 5
        private const val LOCKOUT_DURATION_MS = 30_000L
        private const val PREFS_NAME = "app_lock_prefs"
        private const val FAILED_ATTEMPTS_KEY = "failed_attempts"
        private const val LOCKOUT_UNTIL_KEY = "lockout_until"
    }

    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    suspend fun isAppLockEnabled(): Boolean = withContext(Dispatchers.IO) {
        try {
            val settings = appSecuritySettingsDao.getSecuritySettingsSync()
            settings?.appLockEnabled ?: false
        } catch (e: Exception) {
            Timber.e(e, "Error checking app lock status")
            false
        }
    }

    suspend fun getLockType(): String = withContext(Dispatchers.IO) {
        try {
            val settings = appSecuritySettingsDao.getSecuritySettingsSync()
            settings?.appLockType ?: "PIN"
        } catch (e: Exception) {
            Timber.e(e, "Error getting lock type")
            "PIN"
        }
    }

    suspend fun isPinConfigured(): Boolean = withContext(Dispatchers.IO) {
        try {
            val settings = appSecuritySettingsDao.getSecuritySettingsSync()
            settings?.appLockValue != null
        } catch (e: Exception) {
            false
        }
    }

    suspend fun verifyLock(input: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val settings = appSecuritySettingsDao.getSecuritySettingsSync()
                ?: return@withContext false

            val storedValue = settings.appLockValue ?: return@withContext false

            val now = System.currentTimeMillis()
            val lockoutUntil = prefs.getLong(LOCKOUT_UNTIL_KEY, 0)
            if (now < lockoutUntil) {
                val remaining = ((lockoutUntil - now) / 1000).toInt()
                Timber.w("App lock: locked out, %ds remaining", remaining)
                return@withContext false
            }

            val parts = storedValue.split(":", limit = 2)
            if (parts.size != 2) {
                Timber.e("App lock: corrupt stored value format")
                return@withContext false
            }
            val saltBase64 = parts[0]
            val expectedHash = parts[1]

            val inputHash = hashPinWithSalt(input, saltBase64)
            if (MessageDigest.isEqual(inputHash.toByteArray(Charsets.UTF_8), expectedHash.toByteArray(Charsets.UTF_8))) {
                prefs.edit()
                    .putInt(FAILED_ATTEMPTS_KEY, 0)
                    .putLong(LOCKOUT_UNTIL_KEY, 0L)
                    .apply()
                Timber.d("App lock: PIN verified")
                true
            } else {
                val attempts = prefs.getInt(FAILED_ATTEMPTS_KEY, 0) + 1
                prefs.edit().putInt(FAILED_ATTEMPTS_KEY, attempts).apply()

                if (attempts >= MAX_FAILED_ATTEMPTS) {
                    val lockoutEnd = now + LOCKOUT_DURATION_MS
                    prefs.edit().putLong(LOCKOUT_UNTIL_KEY, lockoutEnd).apply()
                    Timber.w("App lock: %d failed attempts, locked out for %ds", attempts, LOCKOUT_DURATION_MS / 1000)
                } else {
                    Timber.w("App lock: incorrect PIN (%d/%d)", attempts, MAX_FAILED_ATTEMPTS)
                }
                false
            }
        } catch (e: Exception) {
            Timber.e(e, "Error verifying lock")
            false
        }
    }

    suspend fun getLockoutRemainingSeconds(): Int = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val lockoutUntil = prefs.getLong(LOCKOUT_UNTIL_KEY, 0)
        if (now < lockoutUntil) ((lockoutUntil - now) / 1000).toInt() else 0
    }

    suspend fun setLock(type: String, value: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val existing = appSecuritySettingsDao.getSecuritySettingsSync()
            val saltBytes = ByteArray(SALT_LENGTH).also { SecureRandom().nextBytes(it) }
            val saltBase64 = android.util.Base64.encodeToString(saltBytes, android.util.Base64.NO_WRAP)
            val hashedValue = hashPinWithSalt(value, saltBase64)
            val storedValue = "$saltBase64:$hashedValue"

            val settings = (existing ?: AppSecuritySettings()).copy(
                appLockEnabled = true,
                appLockType = type,
                appLockValue = storedValue,
                appLockTimeout = 300000L,
                updatedAt = System.currentTimeMillis()
            )
            appSecuritySettingsDao.insertSettings(settings)
            prefs.edit().putInt(FAILED_ATTEMPTS_KEY, 0).putLong(LOCKOUT_UNTIL_KEY, 0L).apply()
            Timber.d("App lock set: $type")
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to set lock")
            Result.failure(e)
        }
    }

    suspend fun disableLock(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val existing = appSecuritySettingsDao.getSecuritySettingsSync()
            if (existing != null) {
                appSecuritySettingsDao.insertSettings(
                    existing.copy(
                        appLockEnabled = false,
                        appLockValue = null,
                        updatedAt = System.currentTimeMillis()
                    )
                )
            }
            prefs.edit().clear().apply()
            Timber.d("App lock disabled")
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to disable lock")
            Result.failure(e)
        }
    }

    suspend fun updateSettings(settings: AppSecuritySettings) = withContext(Dispatchers.IO) {
        try {
            appSecuritySettingsDao.insertSettings(
                settings.copy(updatedAt = System.currentTimeMillis())
            )
            Timber.d("Security settings updated")
        } catch (e: Exception) {
            Timber.e(e, "Failed to update settings")
        }
    }

    suspend fun getSecuritySettings(): AppSecuritySettings? = withContext(Dispatchers.IO) {
        try {
            appSecuritySettingsDao.getSecuritySettingsSync()
        } catch (e: Exception) {
            Timber.e(e, "Error getting security settings")
            null
        }
    }

    private fun hashPinWithSalt(pin: String, saltBase64: String): String {
        val saltBytes = android.util.Base64.decode(saltBase64, android.util.Base64.NO_WRAP)
        return hashPinWithSalt(pin, saltBytes)
    }

    private fun hashPinWithSalt(pin: String, salt: ByteArray): String {
        val spec = PBEKeySpec(pin.toCharArray(), salt, PBKDF2_ITERATIONS, KEY_LENGTH)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val hashBytes = factory.generateSecret(spec).encoded
        return android.util.Base64.encodeToString(hashBytes, android.util.Base64.NO_WRAP)
    }
}
