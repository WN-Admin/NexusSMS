package com.nexusmedia.nexussms.ui.viewmodels

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexusmedia.nexussms.data.database.AppSecuritySettingsDao
import com.nexusmedia.nexussms.features.security.AppLockManager
import com.nexusmedia.nexussms.features.security.BiometricAuthManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppLockViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val appSecuritySettingsDao: AppSecuritySettingsDao,
    private val appLockManager: AppLockManager,
    private val biometricAuthManager: BiometricAuthManager
) : ViewModel() {

    private val _isLocked = MutableStateFlow(true)
    val isLocked: StateFlow<Boolean> = _isLocked.asStateFlow()

    private val _pinInput = MutableStateFlow("")
    val pinInput: StateFlow<String> = _pinInput.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _biometricAvailable = MutableStateFlow(false)
    val biometricAvailable: StateFlow<Boolean> = _biometricAvailable.asStateFlow()

    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()

    private val _lockoutRemaining = MutableStateFlow(0)
    val lockoutRemaining: StateFlow<Int> = _lockoutRemaining.asStateFlow()

    init {
        appSecuritySettingsDao.getSecuritySettings()
            .onEach { sec ->
                _isLocked.value = sec?.isSessionLocked ?: true
                _biometricAvailable.value = checkBiometricAvailability() && (sec?.biometricEnabled == true)
            }
            .launchIn(viewModelScope)
    }

    private fun checkBiometricAvailability(): Boolean {
        return try {
            val biometricManager = BiometricManager.from(context)
            biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK) == BiometricManager.BIOMETRIC_SUCCESS
        } catch (e: Exception) {
            false
        }
    }

    fun onBiometricAuthenticated() {
        _isAuthenticated.value = true
        _isLocked.value = false
        _error.value = null
        viewModelScope.launch {
            appSecuritySettingsDao.updateLastAuthTime(System.currentTimeMillis())
            appSecuritySettingsDao.updateSessionLocked(false)
        }
    }

    fun verifyPin(pin: String) {
        viewModelScope.launch {
            val remaining = appLockManager.getLockoutRemainingSeconds()
            if (remaining > 0) {
                _error.value = "Too many attempts. Try again in ${remaining}s"
                _pinInput.value = ""
                return@launch
            }

            val pinConfigured = appLockManager.isPinConfigured()
            if (!pinConfigured) {
                _error.value = "No PIN configured"
                return@launch
            }

            val success = appLockManager.verifyLock(pin)
            if (success) {
                _isAuthenticated.value = true
                _isLocked.value = false
                _error.value = null
                _pinInput.value = ""
                appSecuritySettingsDao.updateLastAuthTime(System.currentTimeMillis())
                appSecuritySettingsDao.updateSessionLocked(false)
            } else {
                val newRemaining = appLockManager.getLockoutRemainingSeconds()
                _error.value = if (newRemaining > 0) {
                    "Too many attempts. Try again in ${newRemaining}s"
                } else {
                    "Incorrect PIN"
                }
                _pinInput.value = ""
            }
        }
    }

    fun verifyBiometric(activity: FragmentActivity) {
        biometricAuthManager.showBiometricPrompt(
            activity = activity,
            title = "Unlock NexusSMS",
            subtitle = "Authenticate to continue",
            onSuccess = { onBiometricAuthenticated() },
            onError = { _error.value = it }
        )
    }

    fun updatePinInput(value: String) {
        if (value.length <= 12) {
            _pinInput.value = value
            _error.value = null
        }
    }

    fun lock() {
        _isLocked.value = true
        _isAuthenticated.value = false
        _pinInput.value = ""
        _error.value = null
        viewModelScope.launch {
            appSecuritySettingsDao.updateSessionLocked(true)
        }
    }

    fun unlock() {
        _isLocked.value = false
        _isAuthenticated.value = true
        viewModelScope.launch {
            appSecuritySettingsDao.updateSessionLocked(false)
        }
    }
}
