package com.nexusmedia.nexussms.features.security

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.nexusmedia.nexussms.data.database.AppSecuritySettingsDao
import com.nexusmedia.nexussms.data.models.AppSecuritySettings
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BiometricAuthManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val appSecuritySettingsDao: AppSecuritySettingsDao,
    private val appLockManager: AppLockManager
) {
    companion object {
        private const val SESSION_TIMEOUT_MS = 300000L
    }

    fun isBiometricAvailable(): Boolean {
        return try {
            val biometricManager = BiometricManager.from(context)
            biometricManager.canAuthenticate(
                BiometricManager.Authenticators.BIOMETRIC_STRONG
            ) == BiometricManager.BIOMETRIC_SUCCESS
        } catch (e: Exception) {
            Timber.e(e, "Error checking biometric availability")
            false
        }
    }

    fun canAuthenticate(): Int {
        return try {
            val biometricManager = BiometricManager.from(context)
            biometricManager.canAuthenticate(
                BiometricManager.Authenticators.BIOMETRIC_STRONG
            )
        } catch (e: Exception) {
            Timber.e(e, "Error checking biometric capability")
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE
        }
    }

    suspend fun authenticateWithBiometric(
        activity: FragmentActivity,
        title: String,
        subtitle: String = "",
        description: String = "",
        onResult: (Boolean) -> Unit
    ) = withContext(Dispatchers.Main) {
        try {
            val executor = ContextCompat.getMainExecutor(context)

            val callback = object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    Timber.d("Biometric authentication succeeded")
                    onResult(true)
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    Timber.d("Biometric authentication error: $errorCode - $errString")
                    onResult(false)
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Timber.d("Biometric authentication failed")
                    onResult(false)
                }
            }

            val promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle(title)
                .setSubtitle(subtitle)
                .setDescription(description)
                .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
                .setConfirmationRequired(false)
                .build()

            val biometricPrompt = BiometricPrompt(activity, executor, callback)
            biometricPrompt.authenticate(promptInfo)
        } catch (e: Exception) {
            Timber.e(e, "Failed to start biometric authentication")
            withContext(Dispatchers.Main) {
                onResult(false)
            }
        }
    }

    fun showBiometricPrompt(
        activity: FragmentActivity,
        title: String = "Authenticate",
        subtitle: String = "Verify your identity",
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val executor = ContextCompat.getMainExecutor(context)
        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                onSuccess()
            }
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                onError(errString.toString())
            }
            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
            }
        }
        val biometricPrompt = BiometricPrompt(activity, executor, callback)
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
            .setNegativeButtonText("Cancel")
            .build()
        biometricPrompt.authenticate(promptInfo)
    }

    suspend fun setupAppLock(lockType: String, lockValue: String?): Result<Unit> {
        return if (lockValue != null) {
            appLockManager.setLock(lockType, lockValue)
        } else {
            Result.success(Unit)
        }
    }

    suspend fun enableBiometric(
        biometricType: String = "FINGERPRINT",
        requireOnStartup: Boolean = false,
        requireForSensitiveActions: Boolean = false,
        timeout: Long = SESSION_TIMEOUT_MS
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val existing = appSecuritySettingsDao.getSecuritySettingsSync()
            val settings = (existing ?: AppSecuritySettings()).copy(
                biometricEnabled = true,
                biometricType = biometricType,
                requireBiometricOnStartup = requireOnStartup,
                requireBiometricForSensitiveActions = requireForSensitiveActions,
                biometricTimeout = timeout,
                updatedAt = System.currentTimeMillis()
            )
            appSecuritySettingsDao.insertSettings(settings)
            Timber.d("Biometric enabled: $biometricType")
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to enable biometric")
            Result.failure(e)
        }
    }

    suspend fun disableBiometric(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val existing = appSecuritySettingsDao.getSecuritySettingsSync()
            if (existing != null) {
                appSecuritySettingsDao.insertSettings(
                    existing.copy(
                        biometricEnabled = false,
                        updatedAt = System.currentTimeMillis()
                    )
                )
            }
            Timber.d("Biometric disabled")
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to disable biometric")
            Result.failure(e)
        }
    }

    suspend fun isSessionLocked(): Boolean = withContext(Dispatchers.IO) {
        try {
            val settings = appSecuritySettingsDao.getSecuritySettingsSync() ?: return@withContext false
            if (!settings.appLockEnabled && !settings.biometricEnabled) return@withContext false
            if (settings.isSessionLocked) return@withContext true

            val elapsed = System.currentTimeMillis() - settings.lastAuthTime
            val timeout = if (settings.biometricEnabled) settings.biometricTimeout
            else settings.appLockTimeout
            elapsed > timeout
        } catch (e: Exception) {
            Timber.e(e, "Error checking session lock")
            true
        }
    }

    suspend fun updateAuthTime() = withContext(Dispatchers.IO) {
        try {
            appSecuritySettingsDao.updateLastAuthTime(System.currentTimeMillis())
            Timber.d("Authentication time updated")
        } catch (e: Exception) {
            Timber.e(e, "Failed to update auth time")
        }
    }

    suspend fun lockSession() = withContext(Dispatchers.IO) {
        try {
            appSecuritySettingsDao.updateSessionLocked(true)
            Timber.d("Session locked")
        } catch (e: Exception) {
            Timber.e(e, "Failed to lock session")
        }
    }
}
