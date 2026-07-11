package com.nexusmedia.nexussms.features.security

import com.nexusmedia.nexussms.data.database.AppSecuritySettingsDao
import com.nexusmedia.nexussms.data.models.AppSecuritySettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.security.MessageDigest
import java.util.Base64
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppLockManager @Inject constructor(
    private val appSecuritySettingsDao: AppSecuritySettingsDao
) {
    companion object {
        private const val TAG = "AppLockManager"
    }

    suspend fun isAppLockEnabled(): Boolean = withContext(Dispatchers.IO) {
        try {
            val settings = appSecuritySettingsDao.getSecuritySettingsSync()
            settings?.appLockEnabled ?: false
        } catch (e: Exception) {
            Timber.e(TAG, "Error checking app lock status: ${e.message}")
            false
        }
    }

    suspend fun getLockType(): String = withContext(Dispatchers.IO) {
        try {
            val settings = appSecuritySettingsDao.getSecuritySettingsSync()
            settings?.appLockType ?: "PIN"
        } catch (e: Exception) {
            Timber.e(TAG, "Error getting lock type: ${e.message}")
            "PIN"
        }
    }

    suspend fun verifyLock(input: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val settings = appSecuritySettingsDao.getSecuritySettingsSync()
                ?: return@withContext false

            val storedValue = settings.appLockValue ?: return@withContext false
            val hashedInput = hashInput(input)
            hashedInput == storedValue
        } catch (e: Exception) {
            Timber.e(TAG, "Error verifying lock: ${e.message}")
            false
        }
    }

    suspend fun setLock(type: String, value: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val existing = appSecuritySettingsDao.getSecuritySettingsSync()
            val hashedValue = hashInput(value)
            val settings = (existing ?: AppSecuritySettings()).copy(
                appLockEnabled = true,
                appLockType = type,
                appLockValue = hashedValue,
                updatedAt = System.currentTimeMillis()
            )
            appSecuritySettingsDao.insertSettings(settings)
            Timber.d("Lock set: $type")
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(TAG, "Failed to set lock: ${e.message}")
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
            Timber.d("Lock disabled")
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(TAG, "Failed to disable lock: ${e.message}")
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
            Timber.e(TAG, "Failed to update settings: ${e.message}")
        }
    }

    suspend fun getSecuritySettings(): AppSecuritySettings? = withContext(Dispatchers.IO) {
        try {
            appSecuritySettingsDao.getSecuritySettingsSync()
        } catch (e: Exception) {
            Timber.e(TAG, "Error getting security settings: ${e.message}")
            null
        }
    }

    private fun hashInput(input: String): String {
        return try {
            val digest = MessageDigest.getInstance("SHA-256")
            val hash = digest.digest(input.toByteArray(Charsets.UTF_8))
            Base64.getEncoder().encodeToString(hash)
        } catch (e: Exception) {
            Timber.e(TAG, "Failed to hash input: ${e.message}")
            input
        }
    }
}
