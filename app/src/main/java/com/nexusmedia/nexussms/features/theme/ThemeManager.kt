package com.nexusmedia.nexussms.features.theme

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.nexusmedia.nexussms.data.models.Theme
import com.nexusmedia.nexussms.data.repository.ThemeRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ThemeManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val themeRepository: ThemeRepository
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val prefs = context.getSharedPreferences("theme_prefs", Context.MODE_PRIVATE)

    private val _activeTheme = MutableStateFlow(defaultLightTheme)
    val activeTheme: StateFlow<Theme> = _activeTheme.asStateFlow()

    private val _revision = MutableStateFlow(0)
    val revision: StateFlow<Int> = _revision.asStateFlow()

    init {
        scope.launch {
            initializeDefaultThemes()
            val savedId = prefs.getString("current_theme_id", null)
            val theme = if (!savedId.isNullOrEmpty()) {
                themeRepository.getThemeById(savedId)
            } else null
            val resolved = theme ?: defaultLightTheme
            _activeTheme.value = resolved
            if (prefs.getString("primary_color", null) == null) {
                applyTheme(resolved)
            }
        }
    }

    private suspend fun initializeDefaultThemes() {
        val existing = themeRepository.getBuiltInThemes().first()
        if (existing.isEmpty()) {
            builtInThemes.forEach { themeRepository.insertTheme(it) }
        }
    }

    fun applyTheme(theme: Theme) {
        _activeTheme.value = theme
        _revision.value = _revision.value + 1
        val editor = prefs.edit()
        editor.putString("current_theme_id", theme.id)
        editor.putString("primary_color", theme.primaryColor)
        editor.putString("secondary_color", theme.secondaryColor)
        editor.putString("background_color", theme.backgroundColor)
        editor.putString("surface_color", theme.surfaceColor)
        editor.putString("error_color", theme.errorColor)
        editor.putString("text_color", theme.textColor)
        editor.putString("text_color_secondary", theme.textColorSecondary)
        editor.putString("bubble_color_sent", theme.bubbleColorSent)
        editor.putString("bubble_color_received", theme.bubbleColorReceived)
        editor.putString("bubble_text_color_sent", theme.bubbleTextColorSent)
        editor.putString("bubble_text_color_received", theme.bubbleTextColorReceived)
        editor.putString("bubble_style", theme.bubbleStyle)
        editor.putInt("bubble_corner_radius", theme.bubbleCornerRadius)
        editor.putFloat("bubble_elevation", theme.bubbleElevation)
        editor.putBoolean("is_dark_mode", theme.isDarkMode)
        editor.commit()
    }

    fun setDarkMode(isDark: Boolean) {
        prefs.edit().putBoolean("is_dark_mode", isDark).commit()
    }

    fun isDarkMode(): Boolean = prefs.getBoolean("is_dark_mode", false)

    fun hexToColor(hex: String): Color {
        return try {
            Color(android.graphics.Color.parseColor(hex))
        } catch (_: Exception) {
            Color.Gray
        }
    }

    fun colorToHex(color: Color): String {
        return String.format("#%06X", 0xFFFFFF and color.toArgb())
    }

    companion object {
        val defaultLightTheme = Theme(
            id = "builtin_light",
            name = "Light",
            primaryColor = "#2196F3",
            secondaryColor = "#03DAC6",
            bubbleColorSent = "#2196F3",
            bubbleColorReceived = "#E8E8E8",
            textColor = "#212121",
            backgroundColor = "#FFFFFF",
            surfaceColor = "#FFFFFF",
            textColorSecondary = "#757575",
            bubbleTextColorSent = "#FFFFFF",
            bubbleTextColorReceived = "#000000",
            isDarkMode = false,
            isCustom = false
        )

        val defaultDarkTheme = Theme(
            id = "builtin_dark",
            name = "Dark",
            primaryColor = "#BB86FC",
            secondaryColor = "#03DAC6",
            bubbleColorSent = "#7C4DFF",
            bubbleColorReceived = "#2C2C2C",
            textColor = "#E1E1E1",
            backgroundColor = "#121212",
            surfaceColor = "#1E1E1E",
            textColorSecondary = "#9E9E9E",
            bubbleTextColorSent = "#FFFFFF",
            bubbleTextColorReceived = "#E1E1E1",
            isDarkMode = true,
            isCustom = false
        )

        val builtInThemes = listOf(
            defaultLightTheme,
            defaultDarkTheme,
            Theme(id = "builtin_ocean", name = "Ocean", primaryColor = "#0077B6", secondaryColor = "#00B4D8",
                bubbleColorSent = "#0077B6", bubbleColorReceived = "#CAF0F8",
                textColor = "#023E8A", backgroundColor = "#F0F8FF", surfaceColor = "#FFFFFF",
                textColorSecondary = "#577590", bubbleTextColorSent = "#FFFFFF", bubbleTextColorReceived = "#023E8A",
                isDarkMode = false, isCustom = false),
            Theme(id = "builtin_forest", name = "Forest", primaryColor = "#2D6A4F", secondaryColor = "#52B788",
                bubbleColorSent = "#2D6A4F", bubbleColorReceived = "#D8F3DC",
                textColor = "#1B4332", backgroundColor = "#F0FFF4", surfaceColor = "#FFFFFF",
                textColorSecondary = "#6B9080", bubbleTextColorSent = "#FFFFFF", bubbleTextColorReceived = "#1B4332",
                isDarkMode = false, isCustom = false),
            Theme(id = "builtin_sunset", name = "Sunset", primaryColor = "#E85D04", secondaryColor = "#FAA307",
                bubbleColorSent = "#E85D04", bubbleColorReceived = "#FFE5CC",
                textColor = "#370617", backgroundColor = "#FFF8F0", surfaceColor = "#FFFFFF",
                textColorSecondary = "#9D4EDD", bubbleTextColorSent = "#FFFFFF", bubbleTextColorReceived = "#370617",
                isDarkMode = false, isCustom = false),
            Theme(id = "builtin_purple_night", name = "Purple Night", primaryColor = "#7B2CBF", secondaryColor = "#C77DFF",
                bubbleColorSent = "#7B2CBF", bubbleColorReceived = "#E0AAFF",
                textColor = "#10002B", backgroundColor = "#240046", surfaceColor = "#3C096C",
                textColorSecondary = "#9D4EDD", bubbleTextColorSent = "#FFFFFF", bubbleTextColorReceived = "#10002B",
                isDarkMode = true, isCustom = false),
            Theme(id = "builtin_midnight", name = "Midnight", primaryColor = "#0077B6", secondaryColor = "#00B4D8",
                bubbleColorSent = "#0077B6", bubbleColorReceived = "#1B263B",
                textColor = "#E0E1DD", backgroundColor = "#0B132B", surfaceColor = "#1C2541",
                textColorSecondary = "#778DA9", bubbleTextColorSent = "#FFFFFF", bubbleTextColorReceived = "#E0E1DD",
                isDarkMode = true, isCustom = false),
            Theme(id = "builtin_rose_gold", name = "Rose Gold", primaryColor = "#B76E79", secondaryColor = "#E8A0BF",
                bubbleColorSent = "#B76E79", bubbleColorReceived = "#FFE4EC",
                textColor = "#4A0E2E", backgroundColor = "#FFF5F7", surfaceColor = "#FFFFFF",
                textColorSecondary = "#C97D8E", bubbleTextColorSent = "#FFFFFF", bubbleTextColorReceived = "#4A0E2E",
                isDarkMode = false, isCustom = false),
            Theme(id = "builtin_neon", name = "Neon", primaryColor = "#FF006E", secondaryColor = "#3A86FF",
                bubbleColorSent = "#FF006E", bubbleColorReceived = "#1A1A2E",
                textColor = "#E0E0E0", backgroundColor = "#0F0F1A", surfaceColor = "#1A1A2E",
                textColorSecondary = "#8338EC", bubbleTextColorSent = "#FFFFFF", bubbleTextColorReceived = "#E0E0E0",
                isDarkMode = true, isCustom = false),
            Theme(id = "builtin_minimal", name = "Minimal", primaryColor = "#333333", secondaryColor = "#666666",
                bubbleColorSent = "#333333", bubbleColorReceived = "#F0F0F0",
                textColor = "#333333", backgroundColor = "#FFFFFF", surfaceColor = "#FAFAFA",
                textColorSecondary = "#999999", bubbleTextColorSent = "#FFFFFF", bubbleTextColorReceived = "#333333",
                bubbleCornerRadius = 4, isDarkMode = false, isCustom = false),
            Theme(id = "builtin_mint", name = "Mint", primaryColor = "#00C49A", secondaryColor = "#00E5CC",
                bubbleColorSent = "#00C49A", bubbleColorReceived = "#E0FFF5",
                textColor = "#004D40", backgroundColor = "#F0FFFC", surfaceColor = "#FFFFFF",
                textColorSecondary = "#26A69A", bubbleTextColorSent = "#FFFFFF", bubbleTextColorReceived = "#004D40",
                isDarkMode = false, isCustom = false),
            Theme(id = "builtin_lavender", name = "Lavender", primaryColor = "#7C4DFF", secondaryColor = "#B388FF",
                bubbleColorSent = "#7C4DFF", bubbleColorReceived = "#EDE7F6",
                textColor = "#311B92", backgroundColor = "#F5F0FF", surfaceColor = "#FFFFFF",
                textColorSecondary = "#9575CD", bubbleTextColorSent = "#FFFFFF", bubbleTextColorReceived = "#311B92",
                isDarkMode = false, isCustom = false)
        )
    }
}
