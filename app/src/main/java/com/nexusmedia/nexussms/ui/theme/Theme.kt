package com.nexusmedia.nexussms.ui.theme

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

@Immutable
data class BubbleTheme(
    val sentColor: Color = Color(0xFF2196F3),
    val receivedColor: Color = Color(0xFFE8E8E8),
    val sentTextColor: Color = Color.White,
    val receivedTextColor: Color = Color.Black,
    val cornerRadius: Int = 16,
    val elevation: Float = 2f
)

val LocalBubbleTheme = compositionLocalOf { BubbleTheme() }

private fun parseHex(hex: String, fallback: Long): Color {
    return try { Color(android.graphics.Color.parseColor(hex)) } catch (_: Exception) { Color(fallback) }
}

private fun contrasting(color: Color): Color {
    val luminance = 0.299 * color.red + 0.587 * color.green + 0.114 * color.blue
    return if (luminance > 0.5) Color.Black else Color.White
}

@Composable
fun NexusSMSTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("theme_prefs", Context.MODE_PRIVATE) }

    var themeRevision by remember { mutableIntStateOf(0) }

    DisposableEffect(Unit) {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, _ -> themeRevision++ }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        onDispose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }

    val colorScheme = remember(themeRevision) {
        val primaryHex = prefs.getString("primary_color", null)
        if (primaryHex != null) buildColorScheme(prefs) else null
    } ?: when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> darkColorScheme(
            primary = Color(0xFFBB86FC), onPrimary = Color(0xFF1F1B3D),
            secondary = Color(0xFF03DAC6), background = Color(0xFF121212),
            surface = Color(0xFF1E1E1E), error = Color(0xFFFF6B6B)
        )
        else -> lightColorScheme(
            primary = Color(0xFF2196F3), onPrimary = Color.White,
            secondary = Color(0xFF03DAC6), background = Color(0xFFFAFAFA),
            surface = Color.White, error = Color(0xFFB00020)
        )
    }

    val bubbleTheme = remember(themeRevision) { buildBubbleTheme(prefs) }

    CompositionLocalProvider(LocalBubbleTheme provides bubbleTheme) {
        MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
    }
}

private fun buildBubbleTheme(prefs: SharedPreferences): BubbleTheme {
    val primary = prefs.getString("primary_color", "#2196F3") ?: "#2196F3"
    return BubbleTheme(
        sentColor = parseHex(prefs.getString("bubble_color_sent", null) ?: primary, 0xFF2196F3),
        receivedColor = parseHex(prefs.getString("bubble_color_received", null) ?: "#E8E8E8", 0xFFE8E8E8),
        sentTextColor = parseHex(prefs.getString("bubble_text_color_sent", null) ?: "#FFFFFF", 0xFFFFFFFF),
        receivedTextColor = parseHex(prefs.getString("bubble_text_color_received", null) ?: "#000000", 0xFF000000),
        cornerRadius = prefs.getInt("bubble_corner_radius", 16),
        elevation = prefs.getFloat("bubble_elevation", 2f)
    )
}

private fun buildColorScheme(prefs: SharedPreferences): androidx.compose.material3.ColorScheme {
    fun p(key: String, fallback: String): Color {
        val hex = prefs.getString(key, null) ?: return Color(android.graphics.Color.parseColor(fallback))
        return try { Color(android.graphics.Color.parseColor(hex)) } catch (_: Exception) { Color(android.graphics.Color.parseColor(fallback)) }
    }
    val isDark = prefs.getBoolean("is_dark_mode", false)
    val primary = p("primary_color", "#2196F3")
    val secondary = p("secondary_color", "#03DAC6")
    val background = p("background_color", if (isDark) "#121212" else "#FFFFFF")
    val surface = p("surface_color", if (isDark) "#1E1E1E" else "#FFFFFF")
    val error = p("error_color", "#B00020")
    val onBg = p("text_color", if (isDark) "#E1E1E1" else "#212121")
    val onSfVar = p("text_color_secondary", "#757575")

    return if (isDark) darkColorScheme(
        primary = primary, onPrimary = contrasting(primary),
        primaryContainer = primary.copy(alpha = 0.3f), onPrimaryContainer = onBg,
        secondary = secondary, onSecondary = contrasting(secondary),
        secondaryContainer = secondary.copy(alpha = 0.3f), onSecondaryContainer = onBg,
        background = background, onBackground = onBg,
        surface = surface, onSurface = onBg, onSurfaceVariant = onSfVar,
        error = error, onError = contrasting(error)
    ) else lightColorScheme(
        primary = primary, onPrimary = contrasting(primary),
        primaryContainer = primary.copy(alpha = 0.12f), onPrimaryContainer = primary,
        secondary = secondary, onSecondary = contrasting(secondary),
        secondaryContainer = secondary.copy(alpha = 0.12f), onSecondaryContainer = secondary,
        background = background, onBackground = onBg,
        surface = surface, onSurface = onBg, onSurfaceVariant = onSfVar,
        error = error, onError = contrasting(error)
    )
}
