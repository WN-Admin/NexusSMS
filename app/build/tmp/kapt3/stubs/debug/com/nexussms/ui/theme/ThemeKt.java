package com.nexussms.ui.theme;

import android.os.Build;
import androidx.compose.material3.ColorScheme;
import androidx.compose.runtime.Composable;

@kotlin.Metadata(mv = {2, 2, 0}, k = 2, xi = 48, d1 = {"\u0000*\n\u0000\n\u0002\u0018\u0002\n\u0002\b!\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\u001a/\u0010%\u001a\u00020&2\b\b\u0002\u0010\'\u001a\u00020(2\b\b\u0002\u0010)\u001a\u00020(2\u0011\u0010*\u001a\r\u0012\u0004\u0012\u00020&0+\u00a2\u0006\u0002\b,H\u0007\"\u0010\u0010\u0000\u001a\u00020\u0001X\u0082\u0004\u00a2\u0006\u0004\n\u0002\u0010\u0002\"\u0010\u0010\u0003\u001a\u00020\u0001X\u0082\u0004\u00a2\u0006\u0004\n\u0002\u0010\u0002\"\u0010\u0010\u0004\u001a\u00020\u0001X\u0082\u0004\u00a2\u0006\u0004\n\u0002\u0010\u0002\"\u0010\u0010\u0005\u001a\u00020\u0001X\u0082\u0004\u00a2\u0006\u0004\n\u0002\u0010\u0002\"\u0010\u0010\u0006\u001a\u00020\u0001X\u0082\u0004\u00a2\u0006\u0004\n\u0002\u0010\u0002\"\u0010\u0010\u0007\u001a\u00020\u0001X\u0082\u0004\u00a2\u0006\u0004\n\u0002\u0010\u0002\"\u0010\u0010\b\u001a\u00020\u0001X\u0082\u0004\u00a2\u0006\u0004\n\u0002\u0010\u0002\"\u0010\u0010\t\u001a\u00020\u0001X\u0082\u0004\u00a2\u0006\u0004\n\u0002\u0010\u0002\"\u0010\u0010\n\u001a\u00020\u0001X\u0082\u0004\u00a2\u0006\u0004\n\u0002\u0010\u0002\"\u0010\u0010\u000b\u001a\u00020\u0001X\u0082\u0004\u00a2\u0006\u0004\n\u0002\u0010\u0002\"\u0010\u0010\f\u001a\u00020\u0001X\u0082\u0004\u00a2\u0006\u0004\n\u0002\u0010\u0002\"\u0010\u0010\r\u001a\u00020\u0001X\u0082\u0004\u00a2\u0006\u0004\n\u0002\u0010\u0002\"\u0010\u0010\u000e\u001a\u00020\u0001X\u0082\u0004\u00a2\u0006\u0004\n\u0002\u0010\u0002\"\u0010\u0010\u000f\u001a\u00020\u0001X\u0082\u0004\u00a2\u0006\u0004\n\u0002\u0010\u0002\"\u0010\u0010\u0010\u001a\u00020\u0001X\u0082\u0004\u00a2\u0006\u0004\n\u0002\u0010\u0002\"\u0010\u0010\u0011\u001a\u00020\u0001X\u0082\u0004\u00a2\u0006\u0004\n\u0002\u0010\u0002\"\u0010\u0010\u0012\u001a\u00020\u0001X\u0082\u0004\u00a2\u0006\u0004\n\u0002\u0010\u0002\"\u0010\u0010\u0013\u001a\u00020\u0001X\u0082\u0004\u00a2\u0006\u0004\n\u0002\u0010\u0002\"\u0010\u0010\u0014\u001a\u00020\u0001X\u0082\u0004\u00a2\u0006\u0004\n\u0002\u0010\u0002\"\u0010\u0010\u0015\u001a\u00020\u0001X\u0082\u0004\u00a2\u0006\u0004\n\u0002\u0010\u0002\"\u0010\u0010\u0016\u001a\u00020\u0001X\u0082\u0004\u00a2\u0006\u0004\n\u0002\u0010\u0002\"\u0010\u0010\u0017\u001a\u00020\u0001X\u0082\u0004\u00a2\u0006\u0004\n\u0002\u0010\u0002\"\u0010\u0010\u0018\u001a\u00020\u0001X\u0082\u0004\u00a2\u0006\u0004\n\u0002\u0010\u0002\"\u0010\u0010\u0019\u001a\u00020\u0001X\u0082\u0004\u00a2\u0006\u0004\n\u0002\u0010\u0002\"\u0010\u0010\u001a\u001a\u00020\u0001X\u0082\u0004\u00a2\u0006\u0004\n\u0002\u0010\u0002\"\u0010\u0010\u001b\u001a\u00020\u0001X\u0082\u0004\u00a2\u0006\u0004\n\u0002\u0010\u0002\"\u0010\u0010\u001c\u001a\u00020\u0001X\u0082\u0004\u00a2\u0006\u0004\n\u0002\u0010\u0002\"\u0010\u0010\u001d\u001a\u00020\u0001X\u0082\u0004\u00a2\u0006\u0004\n\u0002\u0010\u0002\"\u0010\u0010\u001e\u001a\u00020\u0001X\u0082\u0004\u00a2\u0006\u0004\n\u0002\u0010\u0002\"\u0010\u0010\u001f\u001a\u00020\u0001X\u0082\u0004\u00a2\u0006\u0004\n\u0002\u0010\u0002\"\u0010\u0010 \u001a\u00020\u0001X\u0082\u0004\u00a2\u0006\u0004\n\u0002\u0010\u0002\"\u0010\u0010!\u001a\u00020\u0001X\u0082\u0004\u00a2\u0006\u0004\n\u0002\u0010\u0002\"\u000e\u0010\"\u001a\u00020#X\u0082\u0004\u00a2\u0006\u0002\n\u0000\"\u000e\u0010$\u001a\u00020#X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006-"}, d2 = {"LightPrimary", "Landroidx/compose/ui/graphics/Color;", "J", "LightOnPrimary", "LightPrimaryContainer", "LightOnPrimaryContainer", "LightSecondary", "LightOnSecondary", "LightSecondaryContainer", "LightOnSecondaryContainer", "LightTertiary", "LightOnTertiary", "LightBackground", "LightOnBackground", "LightSurface", "LightOnSurface", "LightError", "LightOnError", "DarkPrimary", "DarkOnPrimary", "DarkPrimaryContainer", "DarkOnPrimaryContainer", "DarkSecondary", "DarkOnSecondary", "DarkSecondaryContainer", "DarkOnSecondaryContainer", "DarkTertiary", "DarkOnTertiary", "DarkBackground", "DarkOnBackground", "DarkSurface", "DarkOnSurface", "DarkError", "DarkOnError", "LightColorScheme", "Landroidx/compose/material3/ColorScheme;", "DarkColorScheme", "NexusSMSTheme", "", "darkTheme", "", "dynamicColor", "content", "Lkotlin/Function0;", "Landroidx/compose/runtime/Composable;", "app_debug"})
public final class ThemeKt {
    private static final long LightPrimary = 0L;
    private static final long LightOnPrimary = 0L;
    private static final long LightPrimaryContainer = 0L;
    private static final long LightOnPrimaryContainer = 0L;
    private static final long LightSecondary = 0L;
    private static final long LightOnSecondary = 0L;
    private static final long LightSecondaryContainer = 0L;
    private static final long LightOnSecondaryContainer = 0L;
    private static final long LightTertiary = 0L;
    private static final long LightOnTertiary = 0L;
    private static final long LightBackground = 0L;
    private static final long LightOnBackground = 0L;
    private static final long LightSurface = 0L;
    private static final long LightOnSurface = 0L;
    private static final long LightError = 0L;
    private static final long LightOnError = 0L;
    private static final long DarkPrimary = 0L;
    private static final long DarkOnPrimary = 0L;
    private static final long DarkPrimaryContainer = 0L;
    private static final long DarkOnPrimaryContainer = 0L;
    private static final long DarkSecondary = 0L;
    private static final long DarkOnSecondary = 0L;
    private static final long DarkSecondaryContainer = 0L;
    private static final long DarkOnSecondaryContainer = 0L;
    private static final long DarkTertiary = 0L;
    private static final long DarkOnTertiary = 0L;
    private static final long DarkBackground = 0L;
    private static final long DarkOnBackground = 0L;
    private static final long DarkSurface = 0L;
    private static final long DarkOnSurface = 0L;
    private static final long DarkError = 0L;
    private static final long DarkOnError = 0L;
    @org.jetbrains.annotations.NotNull()
    private static final androidx.compose.material3.ColorScheme LightColorScheme = null;
    @org.jetbrains.annotations.NotNull()
    private static final androidx.compose.material3.ColorScheme DarkColorScheme = null;
    
    @androidx.compose.runtime.Composable()
    public static final void NexusSMSTheme(boolean darkTheme, boolean dynamicColor, @org.jetbrains.annotations.NotNull()
    androidx.compose.runtime.internal.ComposableFunction0<kotlin.Unit> content) {
    }
}