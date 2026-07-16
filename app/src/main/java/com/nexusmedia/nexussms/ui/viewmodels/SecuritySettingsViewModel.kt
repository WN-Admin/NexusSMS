package com.nexusmedia.nexussms.ui.viewmodels

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexusmedia.nexussms.data.database.AppSecuritySettingsDao
import com.nexusmedia.nexussms.data.models.AppSecuritySettings
import com.nexusmedia.nexussms.features.security.AppLockManager
import com.nexusmedia.nexussms.features.security.BiometricAuthManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SecuritySettingsViewModel @Inject constructor(
    private val appSecuritySettingsDao: AppSecuritySettingsDao,
    private val biometricAuthManager: BiometricAuthManager,
    private val appLockManager: AppLockManager
) : ViewModel() {

    private val _settings = MutableStateFlow<AppSecuritySettings?>(null)
    val settings: StateFlow<AppSecuritySettings?> = _settings.asStateFlow()

    init {
        viewModelScope.launch {
            var current = appSecuritySettingsDao.getSecuritySettingsSync()
            if (current == null) {
                current = AppSecuritySettings()
                appSecuritySettingsDao.insertSettings(current)
            }
            _settings.value = current
        }
        appSecuritySettingsDao.getSecuritySettings()
            .onEach { _settings.value = it ?: _settings.value ?: AppSecuritySettings() }
            .launchIn(viewModelScope)
    }

    private fun update(transform: AppSecuritySettings.() -> AppSecuritySettings) {
        val current = _settings.value ?: AppSecuritySettings()
        val updated = current.transform()
        _settings.value = updated
        viewModelScope.launch {
            appSecuritySettingsDao.insertSettings(updated)
        }
    }

    fun toggleAppLock(enabled: Boolean) {
        update { copy(appLockEnabled = enabled) }
    }

    fun setLockType(type: String) {
        update { copy(appLockType = type) }
    }

    fun setupAppLock(pin: String) {
        viewModelScope.launch {
            appLockManager.setLock("PIN", pin)
        }
    }

    fun onBiometricSettingToggle(
        activity: FragmentActivity,
        enabled: Boolean,
        onVerified: () -> Unit
    ) {
        if (enabled) {
            biometricAuthManager.showBiometricPrompt(
                activity = activity,
                title = "Verify Identity",
                subtitle = "Authenticate to enable biometric security",
                onSuccess = { onVerified() },
                onError = { }
            )
        } else {
            onVerified()
        }
    }

    fun toggleBiometricOnStartup(enabled: Boolean) {
        update { copy(requireBiometricOnStartup = enabled, biometricEnabled = enabled) }
    }

    fun toggleBiometricForRead(enabled: Boolean) {
        update { copy(requireBiometricForRead = enabled) }
    }

    fun toggleBiometricForSend(enabled: Boolean) {
        update { copy(requireBiometricForSend = enabled) }
    }

    fun toggleBiometricForDelete(enabled: Boolean) {
        update { copy(requireBiometricForDelete = enabled) }
    }

    fun toggleBiometricForForward(enabled: Boolean) {
        update { copy(requireBiometricForForward = enabled) }
    }

    fun toggleHideMessages(enabled: Boolean) {
        update { copy(hideMessages = enabled) }
    }

    fun toggleHideNotificationContent(enabled: Boolean) {
        update { copy(hideNotificationContent = enabled) }
    }

    fun toggleDisableScreenshots(enabled: Boolean) {
        update { copy(disableScreenshots = enabled) }
    }
}
