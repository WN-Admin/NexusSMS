package com.nexusmedia.nexussms.features.theme

import androidx.compose.ui.graphics.Color
import com.nexusmedia.nexussms.data.models.Theme
import com.nexusmedia.nexussms.data.repository.ThemeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ThemeManager @Inject constructor(
    private val themeRepository: ThemeRepository
) {

    companion object {
        val defaultLightTheme = Theme(
            name = "Light",
            primaryColor = "#2196F3",
            secondaryColor = "#03DAC6",
            bubbleColorSent = "#2196F3",
            bubbleColorReceived = "#E8E8E8",
            textColor = "#000000",
            backgroundColor = "#FFFFFF",
            isDarkMode = false,
            isCustom = false
        )

        val defaultDarkTheme = Theme(
            name = "Dark",
            primaryColor = "#BB86FC",
            secondaryColor = "#03DAC6",
            bubbleColorSent = "#BB86FC",
            bubbleColorReceived = "#3F3F3F",
            textColor = "#FFFFFF",
            backgroundColor = "#121212",
            isDarkMode = true,
            isCustom = false
        )

        val builtInThemes = listOf(
            defaultLightTheme,
            defaultDarkTheme,
            Theme(name = "Ocean", primaryColor = "#006994", secondaryColor = "#0097A7",
                bubbleColorSent = "#006994", bubbleColorReceived = "#B3E5FC",
                textColor = "#FFFFFF", backgroundColor = "#E0F2F1",
                isDarkMode = false, isCustom = false),
            Theme(name = "Forest", primaryColor = "#1B5E20", secondaryColor = "#558B2F",
                bubbleColorSent = "#558B2F", bubbleColorReceived = "#C8E6C9",
                textColor = "#1B5E20", backgroundColor = "#F1F8E9",
                isDarkMode = false, isCustom = false),
            Theme(name = "Sunset", primaryColor = "#E65100", secondaryColor = "#FF6F00",
                bubbleColorSent = "#FF6F00", bubbleColorReceived = "#FFE0B2",
                textColor = "#FFFFFF", backgroundColor = "#FFF3E0",
                isDarkMode = false, isCustom = false),
            Theme(name = "Purple Night", primaryColor = "#4A148C", secondaryColor = "#7B1FA2",
                bubbleColorSent = "#7B1FA2", bubbleColorReceived = "#E1BEE7",
                textColor = "#FFFFFF", backgroundColor = "#F3E5F5",
                isDarkMode = true, isCustom = false),
            Theme(name = "Midnight", primaryColor = "#0D47A1", secondaryColor = "#1565C0",
                bubbleColorSent = "#1565C0", bubbleColorReceived = "#BBDEFB",
                textColor = "#FFFFFF", backgroundColor = "#0D47A1",
                isDarkMode = true, isCustom = false),
            Theme(name = "Rose Gold", primaryColor = "#C2185B", secondaryColor = "#E91E63",
                bubbleColorSent = "#E91E63", bubbleColorReceived = "#F8BBD0",
                textColor = "#FFFFFF", backgroundColor = "#FCE4EC",
                isDarkMode = false, isCustom = false)
        )
    }

    suspend fun initializeDefaultThemes() {
        // Materialize the current theme list (one-shot) before deciding to seed.
        val existing = themeRepository.getBuiltInThemes().first()
        if (existing.isEmpty()) {
            builtInThemes.forEach { themeRepository.insertTheme(it) }
        }
    }

    fun getAllThemes(): Flow<List<Theme>> = themeRepository.getAllThemes()
    fun getDefaultThemes(): Flow<List<Theme>> = themeRepository.getBuiltInThemes()
    fun getCustomThemes(): Flow<List<Theme>> = themeRepository.getCustomThemes()
    fun getTheme(id: String): Flow<Theme?> = themeRepository.getThemeFlow(id)

    suspend fun createCustomTheme(
        name: String,
        primaryColor: String,
        secondaryColor: String,
        bubbleColorSent: String,
        bubbleColorReceived: String,
        textColor: String,
        backgroundColor: String,
        isDarkMode: Boolean
    ): String {
        val theme = Theme(
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
        themeRepository.insertTheme(theme)
        return theme.id
    }

    suspend fun updateTheme(theme: Theme) = themeRepository.updateTheme(theme)
    suspend fun deleteTheme(theme: Theme) = themeRepository.deleteTheme(theme)

    fun hexToColor(hex: String): Color = Color(android.graphics.Color.parseColor(hex))

    fun colorToHex(color: Color): String {
        val argb = color.value.toLong()
        return String.format("#%08X", argb).substring(0, 7)
    }
}
