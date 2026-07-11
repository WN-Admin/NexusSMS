package com.nexusmedia.nexussms.features.theme

import android.content.Context
import com.nexusmedia.nexussms.data.models.Theme
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ThemePreference @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs = context.getSharedPreferences("theme_prefs", Context.MODE_PRIVATE)
    private val _currentThemeId = MutableStateFlow(prefs.getString("current_theme_id", "") ?: "")
    val currentThemeId: StateFlow<String> = _currentThemeId.asStateFlow()

    fun setTheme(theme: Theme) {
        prefs.edit().apply {
            putString("current_theme_id", theme.id)
            putString("primary_color", theme.primaryColor)
            putString("secondary_color", theme.secondaryColor)
            putString("background_color", theme.backgroundColor)
            putString("surface_color", theme.surfaceColor)
            putString("error_color", theme.errorColor)
            putString("text_color", theme.textColor)
            putString("text_color_secondary", theme.textColorSecondary)
            putBoolean("is_dark_mode", theme.isDarkMode)
            apply()
        }
        _currentThemeId.value = theme.id
    }
}
