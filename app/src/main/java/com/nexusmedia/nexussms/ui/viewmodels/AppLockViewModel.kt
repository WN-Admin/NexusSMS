package com.nexusmedia.nexussms.ui.viewmodels

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexusmedia.nexussms.data.database.AppSecuritySettingsDao
import com.nexusmedia.nexussms.data.models.AppSecuritySettings
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
    private val appSecuritySettingsDao: AppSecuritySettingsDao
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

    private var settings: AppSecuritySettings? = null

    init {
        appSecuritySettingsDao.getSecuritySettings()
            .onEach { sec ->
                settings = sec
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
        val savedValue = settings?.appLockValue
        if (savedValue == null || pin == savedValue) {
            _isAuthenticated.value = true
            _isLocked.value = false
            _error.value = null
            _pinInput.value = ""
            viewModelScope.launch {
                appSecuritySettingsDao.updateLastAuthTime(System.currentTimeMillis())
                appSecuritySettingsDao.updateSessionLocked(false)
            }
        } else {
            _error.value = "Incorrect PIN"
            _pinInput.value = ""
        }
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
