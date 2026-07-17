package com.nexusmedia.nexussms.features.security

import com.nexusmedia.nexussms.data.database.AppSecuritySettingsDao
import com.nexusmedia.nexussms.data.models.AppSecuritySettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionManager @Inject constructor(
    private val appSecuritySettingsDao: AppSecuritySettingsDao
) {
    suspend fun isSessionActive(): Boolean = withContext(Dispatchers.IO) {
        try {
            val settings = appSecuritySettingsDao.getSecuritySettingsSync()
            if (settings == null) return@withContext true

            if (!settings.appLockEnabled && !settings.biometricEnabled) return@withContext true

            if (settings.isSessionLocked) return@withContext false

            val elapsed = System.currentTimeMillis() - settings.lastAuthTime
            val timeout = when {
                settings.biometricEnabled -> settings.biometricTimeout
                settings.appLockEnabled -> settings.appLockTimeout
                else -> Long.MAX_VALUE
            }

            elapsed <= timeout
        } catch (e: Exception) {
            Timber.e(e, "Error checking session activity")
            false
        }
    }

    suspend fun startSession() = withContext(Dispatchers.IO) {
        try {
            appSecuritySettingsDao.updateLastAuthTime(System.currentTimeMillis())
            appSecuritySettingsDao.updateSessionLocked(false)
            Timber.d("Session started")
        } catch (e: Exception) {
            Timber.e(e, "Failed to start session")
        }
    }

    suspend fun endSession() = withContext(Dispatchers.IO) {
        try {
            appSecuritySettingsDao.updateSessionLocked(true)
            Timber.d("Session ended")
        } catch (e: Exception) {
            Timber.e(e, "Failed to end session")
        }
    }

    suspend fun getSessionTimeout(): Long = withContext(Dispatchers.IO) {
        try {
            val settings = appSecuritySettingsDao.getSecuritySettingsSync()
            when {
                settings?.biometricEnabled == true -> settings.biometricTimeout
                settings?.appLockEnabled == true -> settings.appLockTimeout
                else -> Long.MAX_VALUE
            }
        } catch (e: Exception) {
            Timber.e(e, "Error getting session timeout")
            300000L
        }
    }

    fun observeSessionStatus(): Flow<Boolean> {
        return appSecuritySettingsDao.getSecuritySettings().map { settings ->
            if (settings == null) return@map true
            if (!settings.appLockEnabled && !settings.biometricEnabled) return@map true
            if (settings.isSessionLocked) return@map false

            val elapsed = System.currentTimeMillis() - settings.lastAuthTime
            val timeout = when {
                settings.biometricEnabled -> settings.biometricTimeout
                settings.appLockEnabled -> settings.appLockTimeout
                else -> Long.MAX_VALUE
            }
            elapsed <= timeout
        }
    }

    suspend fun requireBiometricForAction(action: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val settings = appSecuritySettingsDao.getSecuritySettingsSync()
                ?: return@withContext false

            if (!settings.biometricEnabled || !settings.requireBiometricForSensitiveActions) {
                return@withContext false
            }

            when (action) {
                "READ" -> settings.requireBiometricForRead
                "SEND" -> settings.requireBiometricForSend
                "DELETE" -> settings.requireBiometricForDelete
                "FORWARD" -> settings.requireBiometricForForward
                else -> false
            }
        } catch (e: Exception) {
            Timber.e(e, "Error checking biometric requirement")
            false
        }
    }
}
