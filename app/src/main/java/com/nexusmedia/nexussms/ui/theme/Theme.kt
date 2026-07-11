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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

// Light colors
private val LightPrimary = Color(0xFF2196F3)
private val LightOnPrimary = Color(0xFFFFFFFF)
private val LightPrimaryContainer = Color(0xFFBBDEFB)
private val LightOnPrimaryContainer = Color(0xFF0D47A1)
private val LightSecondary = Color(0xFF03DAC6)
private val LightOnSecondary = Color(0xFFFFFFFF)
private val LightSecondaryContainer = Color(0xFFA7FFEB)
private val LightOnSecondaryContainer = Color(0xFF00695C)
private val LightTertiary = Color(0xFFFF9800)
private val LightOnTertiary = Color(0xFFFFFFFF)
private val LightBackground = Color(0xFFFAFAFA)
private val LightOnBackground = Color(0xFF212121)
private val LightSurface = Color(0xFFFFFFFF)
private val LightOnSurface = Color(0xFF212121)
private val LightError = Color(0xFFB00020)
private val LightOnError = Color(0xFFFFFFFF)

// Dark colors
private val DarkPrimary = Color(0xFFBB86FC)
private val DarkOnPrimary = Color(0xFF1F1B3D)
private val DarkPrimaryContainer = Color(0xFF4A148C)
private val DarkOnPrimaryContainer = Color(0xFFEADDFF)
private val DarkSecondary = Color(0xFF03DAC6)
private val DarkOnSecondary = Color(0xFF003730)
private val DarkSecondaryContainer = Color(0xFF005047)
private val DarkOnSecondaryContainer = Color(0xFFA7FFEB)
private val DarkTertiary = Color(0xFFFFB74D)
private val DarkOnTertiary = Color(0xFF3F2305)
private val DarkBackground = Color(0xFF121212)
private val DarkOnBackground = Color(0xFFE1E1E1)
private val DarkSurface = Color(0xFF1E1E1E)
private val DarkOnSurface = Color(0xFFE1E1E1)
private val DarkError = Color(0xFFFF6B6B)
private val DarkOnError = Color(0xFF370B1E)

private val LightColorScheme = lightColorScheme(
    primary = LightPrimary,
    onPrimary = LightOnPrimary,
    primaryContainer = LightPrimaryContainer,
    onPrimaryContainer = LightOnPrimaryContainer,
    secondary = LightSecondary,
    onSecondary = LightOnSecondary,
    secondaryContainer = LightSecondaryContainer,
    onSecondaryContainer = LightOnSecondaryContainer,
    tertiary = LightTertiary,
    onTertiary = LightOnTertiary,
    background = LightBackground,
    onBackground = LightOnBackground,
    surface = LightSurface,
    onSurface = LightOnSurface,
    error = LightError,
    onError = LightOnError
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = DarkOnPrimary,
    primaryContainer = DarkPrimaryContainer,
    onPrimaryContainer = DarkOnPrimaryContainer,
    secondary = DarkSecondary,
    onSecondary = DarkOnSecondary,
    secondaryContainer = DarkSecondaryContainer,
    onSecondaryContainer = DarkOnSecondaryContainer,
    tertiary = DarkTertiary,
    onTertiary = DarkOnTertiary,
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    error = DarkError,
    onError = DarkOnError
)

@Composable
fun NexusSMSTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("theme_prefs", Context.MODE_PRIVATE) }

    var themeRevision by remember { mutableStateOf(0) }

    DisposableEffect(Unit) {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, _ ->
            themeRevision++
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        onDispose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }

    val colorScheme = remember(themeRevision) {
        val primaryHex = prefs.getString("primary_color", null)
        if (primaryHex != null) {
            buildThemeColorScheme(prefs)
        } else {
            null
        }
    } ?: when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

private fun buildThemeColorScheme(prefs: SharedPreferences): androidx.compose.material3.ColorScheme {
    fun parseHex(key: String, fallback: String): Color {
        val hex = prefs.getString(key, null)
            ?: return Color(android.graphics.Color.parseColor(fallback))
        return try {
            Color(android.graphics.Color.parseColor(hex))
        } catch (e: Exception) {
            Color(android.graphics.Color.parseColor(fallback))
        }
    }

    val isDark = prefs.getBoolean("is_dark_mode", false)
    val primary = parseHex("primary_color", "#2196F3")
    val secondary = parseHex("secondary_color", "#03DAC6")
    val background = parseHex("background_color", if (isDark) "#121212" else "#FFFFFF")
    val surface = parseHex("surface_color", if (isDark) "#1E1E1E" else "#FFFFFF")
    val error = parseHex("error_color", "#B00020")
    val onBackground = parseHex("text_color", if (isDark) "#E1E1E1" else "#212121")
    val onSurfaceVariant = parseHex("text_color_secondary", "#757575")

    fun contrasting(color: Color): Color {
        val luminance = 0.299 * color.red + 0.587 * color.green + 0.114 * color.blue
        return if (luminance > 0.5) Color.Black else Color.White
    }

    return if (isDark) {
        darkColorScheme(
            primary = primary,
            onPrimary = contrasting(primary),
            primaryContainer = primary.copy(alpha = 0.3f),
            onPrimaryContainer = onBackground,
            secondary = secondary,
            onSecondary = contrasting(secondary),
            secondaryContainer = secondary.copy(alpha = 0.3f),
            onSecondaryContainer = onBackground,
            tertiary = secondary.copy(alpha = 0.7f),
            onTertiary = contrasting(secondary),
            background = background,
            onBackground = onBackground,
            surface = surface,
            onSurface = onBackground,
            onSurfaceVariant = onSurfaceVariant,
            error = error,
            onError = contrasting(error)
        )
    } else {
        lightColorScheme(
            primary = primary,
            onPrimary = contrasting(primary),
            primaryContainer = primary.copy(alpha = 0.12f),
            onPrimaryContainer = primary,
            secondary = secondary,
            onSecondary = contrasting(secondary),
            secondaryContainer = secondary.copy(alpha = 0.12f),
            onSecondaryContainer = secondary,
            tertiary = secondary.copy(alpha = 0.8f),
            onTertiary = contrasting(secondary),
            background = background,
            onBackground = onBackground,
            surface = surface,
            onSurface = onBackground,
            onSurfaceVariant = onSurfaceVariant,
            error = error,
            onError = contrasting(error)
        )
    }
}
