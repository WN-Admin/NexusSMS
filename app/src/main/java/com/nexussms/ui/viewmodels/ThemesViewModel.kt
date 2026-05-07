package com.nexussms.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexussms.data.models.Theme
import com.nexussms.data.repository.ThemeRepository
import com.nexussms.features.theme.ThemeManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ThemesViewModel @Inject constructor(
    private val themeRepository: ThemeRepository,
    private val themeManager: ThemeManager
) : ViewModel() {

    private val _builtInThemes = MutableStateFlow<List<Theme>>(emptyList())
    val builtInThemes: StateFlow<List<Theme>> = _builtInThemes.asStateFlow()

    private val _customThemes = MutableStateFlow<List<Theme>>(emptyList())
    val customThemes: StateFlow<List<Theme>> = _customThemes.asStateFlow()

    private val _selectedTheme = MutableStateFlow<Theme?>(null)
    val selectedTheme: StateFlow<Theme?> = _selectedTheme.asStateFlow()

    init {
        viewModelScope.launch {
            themeManager.initializeDefaultThemes()
        }
        themeRepository.getBuiltInThemes()
            .onEach { _builtInThemes.value = it }
            .launchIn(viewModelScope)
        themeRepository.getCustomThemes()
            .onEach { _customThemes.value = it }
            .launchIn(viewModelScope)
    }

    fun applyTheme(theme: Theme) {
        _selectedTheme.value = theme
    }

    fun deleteCustom(theme: Theme) {
        viewModelScope.launch {
            themeRepository.deleteTheme(theme)
        }
    }

    fun createCustom(
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
            themeManager.createCustomTheme(
                name = name,
                primaryColor = primaryColor,
                secondaryColor = secondaryColor,
                bubbleColorSent = bubbleColorSent,
                bubbleColorReceived = bubbleColorReceived,
                textColor = textColor,
                backgroundColor = backgroundColor,
                isDarkMode = isDarkMode
            )
        }
    }
}
