package com.nexusmedia.nexussms.ui.theme

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color as AndroidColor
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
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sign

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
    return try { Color(AndroidColor.parseColor(hex)) } catch (_: Exception) { Color(fallback) }
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
        if (primaryHex != null) buildColorSchemeFromSeed(prefs) else null
    } ?: when {
        dynamicColor && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S -> {
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

private fun buildColorSchemeFromSeed(prefs: SharedPreferences): androidx.compose.material3.ColorScheme {
    val isDark = prefs.getBoolean("is_dark_mode", false)
    val primaryHex = prefs.getString("primary_color", null) ?: "#2196F3"
    val seedArgb = try {
        AndroidColor.parseColor(primaryHex)
    } catch (_: Exception) {
        AndroidColor.parseColor("#2196F3")
    }

    val hct = Hct.fromInt(seedArgb)

    val scheme = if (isDark) {
        TonalSchemeGenerator.generateDark(hct)
    } else {
        TonalSchemeGenerator.generateLight(hct)
    }

    val bgOverride = prefs.getString("background_color", null)
    val surfaceOverride = prefs.getString("surface_color", null)

    return androidx.compose.material3.ColorScheme(
        primary = scheme.primary,
        onPrimary = scheme.onPrimary,
        primaryContainer = scheme.primaryContainer,
        onPrimaryContainer = scheme.onPrimaryContainer,
        secondary = scheme.secondary,
        onSecondary = scheme.onSecondary,
        secondaryContainer = scheme.secondaryContainer,
        onSecondaryContainer = scheme.onSecondaryContainer,
        tertiary = scheme.tertiary,
        onTertiary = scheme.onTertiary,
        tertiaryContainer = scheme.tertiaryContainer,
        onTertiaryContainer = scheme.onTertiaryContainer,
        error = scheme.error,
        onError = scheme.onError,
        errorContainer = scheme.errorContainer,
        onErrorContainer = scheme.onErrorContainer,
        background = if (bgOverride != null) parseHex(bgOverride, scheme.background.toArgb().toLong()) else scheme.background,
        onBackground = scheme.onBackground,
        surface = if (surfaceOverride != null) parseHex(surfaceOverride, scheme.surface.toArgb().toLong()) else scheme.surface,
        onSurface = scheme.onSurface,
        surfaceVariant = scheme.surfaceVariant,
        onSurfaceVariant = scheme.onSurfaceVariant,
        outline = scheme.outline,
        outlineVariant = scheme.outlineVariant,
        inverseSurface = scheme.inverseSurface,
        inverseOnSurface = scheme.inverseOnSurface,
        inversePrimary = scheme.inversePrimary,
        surfaceTint = scheme.primary,
        surfaceDim = scheme.surfaceDim,
        surfaceBright = scheme.surfaceBright,
        surfaceContainerLowest = scheme.surfaceContainerLowest,
        surfaceContainerLow = scheme.surfaceContainerLow,
        surfaceContainer = scheme.surfaceContainer,
        surfaceContainerHigh = scheme.surfaceContainerHigh,
        surfaceContainerHighest = scheme.surfaceContainerHighest,
        scrim = scheme.scrim
    )
}

private object TonalSchemeGenerator {
    fun generateLight(seed: Hct): androidx.compose.material3.ColorScheme {
        val palette = TonalPalette.fromHct(seed)
        return androidx.compose.material3.ColorScheme(
            primary = palette.primary(40),
            onPrimary = palette.primary(100),
            primaryContainer = palette.primary(90),
            onPrimaryContainer = palette.primary(10),
            secondary = palette.secondary(40),
            onSecondary = palette.secondary(100),
            secondaryContainer = palette.secondary(90),
            onSecondaryContainer = palette.secondary(10),
            tertiary = palette.tertiary(40),
            onTertiary = palette.tertiary(100),
            tertiaryContainer = palette.tertiary(90),
            onTertiaryContainer = palette.tertiary(10),
            error = Color(0xFFBA1A1A),
            onError = Color(0xFFFFFFFF),
            errorContainer = Color(0xFFFFDAD6),
            onErrorContainer = Color(0xFF410002),
            background = palette.neutral(99),
            onBackground = palette.neutral(10),
            surface = palette.neutral(99),
            onSurface = palette.neutral(10),
            surfaceVariant = palette.neutralVariant(90),
            onSurfaceVariant = palette.neutralVariant(30),
            outline = palette.neutralVariant(50),
            outlineVariant = palette.neutralVariant(80),
            inverseSurface = palette.neutral(20),
            inverseOnSurface = palette.neutral(95),
            inversePrimary = palette.primary(80),
            surfaceTint = palette.primary(40),
            surfaceDim = palette.neutral(87),
            surfaceBright = palette.neutral(98),
            surfaceContainerLowest = palette.neutral(100),
            surfaceContainerLow = palette.neutral(96),
            surfaceContainer = palette.neutral(94),
            surfaceContainerHigh = palette.neutral(92),
            surfaceContainerHighest = palette.neutral(90),
            scrim = Color(0xFF000000)
        )
    }

    fun generateDark(seed: Hct): androidx.compose.material3.ColorScheme {
        val palette = TonalPalette.fromHct(seed)
        return androidx.compose.material3.ColorScheme(
            primary = palette.primary(80),
            onPrimary = palette.primary(20),
            primaryContainer = palette.primary(30),
            onPrimaryContainer = palette.primary(90),
            secondary = palette.secondary(80),
            onSecondary = palette.secondary(20),
            secondaryContainer = palette.secondary(30),
            onSecondaryContainer = palette.secondary(90),
            tertiary = palette.tertiary(80),
            onTertiary = palette.tertiary(20),
            tertiaryContainer = palette.tertiary(30),
            onTertiaryContainer = palette.tertiary(90),
            error = Color(0xFFFFB4AB),
            onError = Color(0xFF690005),
            errorContainer = Color(0xFF93000A),
            onErrorContainer = Color(0xFFFFDAD6),
            background = palette.neutral(6),
            onBackground = palette.neutral(90),
            surface = palette.neutral(6),
            onSurface = palette.neutral(90),
            surfaceVariant = palette.neutralVariant(30),
            onSurfaceVariant = palette.neutralVariant(80),
            outline = palette.neutralVariant(60),
            outlineVariant = palette.neutralVariant(30),
            inverseSurface = palette.neutral(90),
            inverseOnSurface = palette.neutral(20),
            inversePrimary = palette.primary(40),
            surfaceTint = palette.primary(80),
            surfaceDim = palette.neutral(6),
            surfaceBright = palette.neutral(24),
            surfaceContainerLowest = palette.neutral(4),
            surfaceContainerLow = palette.neutral(10),
            surfaceContainer = palette.neutral(12),
            surfaceContainerHigh = palette.neutral(17),
            surfaceContainerHighest = palette.neutral(22),
            scrim = Color(0xFF000000)
        )
    }
}

private class TonalPalette private constructor(
    private val hue: Double,
    private val chroma: Double,
    private val neutralHue: Double,
    private val neutralChroma: Double
) {
    fun primary(tone: Int): Color {
        return Color(Hct.solveToArgb(hue, chroma, tone.toDouble()))
    }

    fun secondary(tone: Int): Color {
        return Color(Hct.solveToArgb(hue, chroma * 0.33, tone.toDouble()))
    }

    fun tertiary(tone: Int): Color {
        return Color(Hct.solveToArgb(hue + 60.0, chroma * 0.66, tone.toDouble()))
    }

    fun neutral(tone: Int): Color {
        return Color(Hct.solveToArgb(neutralHue, neutralChroma * 0.04, tone.toDouble()))
    }

    fun neutralVariant(tone: Int): Color {
        return Color(Hct.solveToArgb(neutralHue, neutralChroma * 0.08, tone.toDouble()))
    }

    companion object {
        fun fromHct(seed: Hct): TonalPalette {
            return TonalPalette(
                hue = seed.hue,
                chroma = seed.chroma,
                neutralHue = seed.hue,
                neutralChroma = max(seed.chroma, 4.0)
            )
        }
    }
}

private class Hct private constructor(
    val hue: Double,
    val chroma: Double,
    val tone: Double
) {
    fun toInt(): Int = solveToArgb(hue, chroma, tone)

    companion object {
        fun fromInt(argb: Int): Hct {
            val r = android.graphics.Color.red(argb) / 255.0
            val g = android.graphics.Color.green(argb) / 255.0
            val b = android.graphics.Color.blue(argb) / 255.0
            val l = 0.2126 * r + 0.7152 * g + 0.0722 * b
            val tone = l * 100.0
            val hsv = FloatArray(3)
            android.graphics.Color.RGBToHSV(
                android.graphics.Color.red(argb),
                android.graphics.Color.green(argb),
                android.graphics.Color.blue(argb),
                hsv
            )
            val hue = hsv[0].toDouble()
            val sat = hsv[1].toDouble()
            val chroma = sat * 100.0 * (1 - abs(tone - 50) / 50.0) * 1.2
            return Hct(hue, min(chroma, 150.0), tone)
        }

        fun solveToArgb(hue: Double, chroma: Double, tone: Double): Int {
            val h = ((hue % 360) + 360) % 360
            val t = tone.coerceIn(0.0, 100.0)
            val c = chroma.coerceAtMost(150.0)

            if (t < 1.0) return android.graphics.Color.rgb(0, 0, 0)
            if (t > 99.0) return android.graphics.Color.rgb(255, 255, 255)

            val l = t / 100.0
            val maxSatChroma = c / 100.0

            val hRad = h * Math.PI / 180.0
            val a = maxSatChroma * min(l, 1.0 - l)
            val f = fun(n: Double): Double {
                val k = (n + h / 30.0) % 12.0
                return l - a * max(-1.0, min(min(k - 3.0, 9.0 - k), 1.0))
            }

            val r = (f(0.0) * 255).roundToInt().coerceIn(0, 255)
            val g = (f(8.0) * 255).roundToInt().coerceIn(0, 255)
            val b = (f(4.0) * 255).roundToInt().coerceIn(0, 255)

            return android.graphics.Color.rgb(r, g, b)
        }
    }
}
