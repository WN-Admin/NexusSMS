package com.nexusmedia.nexussms.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
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
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
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
