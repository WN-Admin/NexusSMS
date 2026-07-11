package com.nexusmedia.nexussms.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexusmedia.nexussms.data.models.Theme
import com.nexusmedia.nexussms.data.models.Signature
import com.nexusmedia.nexussms.data.repository.SignatureRepository
import com.nexusmedia.nexussms.data.repository.SocialAccountRepository
import com.nexusmedia.nexussms.data.repository.ThemeRepository
import com.nexusmedia.nexussms.features.theme.ThemeManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val themeRepository: ThemeRepository,
    private val signatureRepository: SignatureRepository,
    private val socialAccountRepository: SocialAccountRepository,
    private val themeManager: ThemeManager
) : ViewModel() {

    private val _themes = MutableStateFlow<List<Theme>>(emptyList())
    val themes: StateFlow<List<Theme>> = _themes.asStateFlow()

    private val _signatures = MutableStateFlow<List<Signature>>(emptyList())
    val signatures: StateFlow<List<Signature>> = _signatures.asStateFlow()

    private val _currentTheme = MutableStateFlow<Theme?>(null)
    val currentTheme: StateFlow<Theme?> = _currentTheme.asStateFlow()

    private val _isDarkMode = MutableStateFlow(false)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    init {
        // One-time seeding of built-in themes if DB is empty.
        viewModelScope.launch {
            themeManager.initializeDefaultThemes()
        }
        // Continuous observers — Room emits on every change automatically.
        themeRepository.getAllThemes()
            .onEach { _themes.value = it }
            .launchIn(viewModelScope)

        signatureRepository.getAllSignatures()
            .onEach { _signatures.value = it }
            .launchIn(viewModelScope)
    }

    fun setCurrentTheme(theme: Theme) {
        _currentTheme.value = theme
        _isDarkMode.value = theme.isDarkMode
    }

    fun createCustomTheme(
        name: String,
        primaryColor: String,
        secondaryColor: String,
        bubbleColorSent: String,
        bubbleColorReceived: String,
        textColor: String,
        backgroundColor: String,
        isDarkMode: Boolean
    ) {
        viewModelScope.launch {
            themeRepository.insertTheme(
                Theme(
                    name = name,
                    primaryColor = primaryColor,
                    secondaryColor = secondaryColor,
                    bubbleColorSent = bubbleColorSent,
                    bubbleColorReceived = bubbleColorReceived,
                    textColor = textColor,
                    backgroundColor = backgroundColor,
                    isDarkMode = isDarkMode,
                    isCustom = true
                )
            )
            // No need to manually reload — Room flow re-emits.
        }
    }

    fun deleteTheme(theme: Theme) {
        viewModelScope.launch {
            themeRepository.deleteTheme(theme)
        }
    }

    fun createSignature(name: String, signature: String, isDefault: Boolean = false) {
        viewModelScope.launch {
            signatureRepository.insertSignature(
                Signature(
                    name = name,
                    content = signature,
                    isDefault = isDefault
                )
            )
        }
    }

    fun updateSignature(signature: Signature) {
        viewModelScope.launch {
            signatureRepository.updateSignature(signature)
        }
    }

    fun deleteSignature(signature: Signature) {
        viewModelScope.launch {
            signatureRepository.deleteSignature(signature)
        }
    }

    fun toggleDarkMode(isDark: Boolean) {
        _isDarkMode.value = isDark
    }

    fun getConnectedSocialAccounts() = socialAccountRepository.getConnectedAccounts()
}
