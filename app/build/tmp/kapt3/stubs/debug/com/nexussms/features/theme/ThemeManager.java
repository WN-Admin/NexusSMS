package com.nexussms.features.theme;

import com.nexussms.data.models.Theme;
import com.nexussms.data.repository.ThemeRepository;
import kotlinx.coroutines.flow.Flow;
import javax.inject.Inject;
import javax.inject.Singleton;

@javax.inject.Singleton
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000J\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\t\n\u0002\b\b\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0002\b\r\b\u0007\u0018\u0000 *2\u00020\u0001:\u0001*B\u000f\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u001b\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\b\u00f8\u0001\u0000\u00f8\u0001\u0001\u00a2\u0006\u0004\b\t\u0010\nJQ\u0010\u000b\u001a\u00020\f2\u0006\u0010\r\u001a\u00020\u00062\u0006\u0010\u000e\u001a\u00020\u00062\u0006\u0010\u000f\u001a\u00020\u00062\u0006\u0010\u0010\u001a\u00020\u00062\u0006\u0010\u0011\u001a\u00020\u00062\u0006\u0010\u0012\u001a\u00020\u00062\u0006\u0010\u0013\u001a\u00020\u00062\u0006\u0010\u0014\u001a\u00020\u0015H\u0086@\u00f8\u0001\u0001\u00a2\u0006\u0002\u0010\u0016J\u0019\u0010\u0017\u001a\u00020\u00182\u0006\u0010\u0019\u001a\u00020\u001aH\u0086@\u00f8\u0001\u0001\u00a2\u0006\u0002\u0010\u001bJ\u0012\u0010\u001c\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u001a0\u001e0\u001dJ\u0012\u0010\u001f\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u001a0\u001e0\u001dJ\u0012\u0010 \u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u001a0\u001e0\u001dJ\u0016\u0010!\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u001a0\u001d2\u0006\u0010\"\u001a\u00020\fJ\u001e\u0010#\u001a\u00020\b2\u0006\u0010$\u001a\u00020\u0006\u00f8\u0001\u0002\u00f8\u0001\u0000\u00f8\u0001\u0001\u00a2\u0006\u0004\b%\u0010&J\u0011\u0010\'\u001a\u00020\u0018H\u0086@\u00f8\u0001\u0001\u00a2\u0006\u0002\u0010(J\u0019\u0010)\u001a\u00020\u00182\u0006\u0010\u0019\u001a\u00020\u001aH\u0086@\u00f8\u0001\u0001\u00a2\u0006\u0002\u0010\u001bR\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u0082\u0002\u000f\n\u0005\b\u00a1\u001e0\u0001\n\u0002\b\u0019\n\u0002\b!\u00a8\u0006+"}, d2 = {"Lcom/nexussms/features/theme/ThemeManager;", "", "themeRepository", "Lcom/nexussms/data/repository/ThemeRepository;", "(Lcom/nexussms/data/repository/ThemeRepository;)V", "colorToHex", "", "color", "Landroidx/compose/ui/graphics/Color;", "colorToHex-8_81llA", "(J)Ljava/lang/String;", "createCustomTheme", "", "name", "primaryColor", "secondaryColor", "bubbleColorSent", "bubbleColorReceived", "textColor", "backgroundColor", "isDarkMode", "", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;ZLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "deleteTheme", "", "theme", "Lcom/nexussms/data/models/Theme;", "(Lcom/nexussms/data/models/Theme;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getAllThemes", "Lkotlinx/coroutines/flow/Flow;", "", "getCustomThemes", "getDefaultThemes", "getTheme", "id", "hexToColor", "hex", "hexToColor-vNxB06k", "(Ljava/lang/String;)J", "initializeDefaultThemes", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "updateTheme", "Companion", "app_debug"})
public final class ThemeManager {
    @org.jetbrains.annotations.NotNull
    private final com.nexussms.data.repository.ThemeRepository themeRepository = null;
    @org.jetbrains.annotations.NotNull
    private static final com.nexussms.data.models.Theme defaultLightTheme = null;
    @org.jetbrains.annotations.NotNull
    private static final com.nexussms.data.models.Theme defaultDarkTheme = null;
    @org.jetbrains.annotations.NotNull
    private static final java.util.List<com.nexussms.data.models.Theme> builtInThemes = null;
    @org.jetbrains.annotations.NotNull
    public static final com.nexussms.features.theme.ThemeManager.Companion Companion = null;
    
    @javax.inject.Inject
    public ThemeManager(@org.jetbrains.annotations.NotNull
    com.nexussms.data.repository.ThemeRepository themeRepository) {
        super();
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.Object initializeDefaultThemes(@org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final kotlinx.coroutines.flow.Flow<java.util.List<com.nexussms.data.models.Theme>> getAllThemes() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final kotlinx.coroutines.flow.Flow<java.util.List<com.nexussms.data.models.Theme>> getDefaultThemes() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final kotlinx.coroutines.flow.Flow<java.util.List<com.nexussms.data.models.Theme>> getCustomThemes() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final kotlinx.coroutines.flow.Flow<com.nexussms.data.models.Theme> getTheme(long id) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.Object createCustomTheme(@org.jetbrains.annotations.NotNull
    java.lang.String name, @org.jetbrains.annotations.NotNull
    java.lang.String primaryColor, @org.jetbrains.annotations.NotNull
    java.lang.String secondaryColor, @org.jetbrains.annotations.NotNull
    java.lang.String bubbleColorSent, @org.jetbrains.annotations.NotNull
    java.lang.String bubbleColorReceived, @org.jetbrains.annotations.NotNull
    java.lang.String textColor, @org.jetbrains.annotations.NotNull
    java.lang.String backgroundColor, boolean isDarkMode, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super java.lang.Long> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.Object updateTheme(@org.jetbrains.annotations.NotNull
    com.nexussms.data.models.Theme theme, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.Object deleteTheme(@org.jetbrains.annotations.NotNull
    com.nexussms.data.models.Theme theme, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0018\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\b\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u0017\u0010\u0003\u001a\b\u0012\u0004\u0012\u00020\u00050\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0006\u0010\u0007R\u0011\u0010\b\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\t\u0010\nR\u0011\u0010\u000b\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\f\u0010\n\u00a8\u0006\r"}, d2 = {"Lcom/nexussms/features/theme/ThemeManager$Companion;", "", "()V", "builtInThemes", "", "Lcom/nexussms/data/models/Theme;", "getBuiltInThemes", "()Ljava/util/List;", "defaultDarkTheme", "getDefaultDarkTheme", "()Lcom/nexussms/data/models/Theme;", "defaultLightTheme", "getDefaultLightTheme", "app_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
        
        @org.jetbrains.annotations.NotNull
        public final com.nexussms.data.models.Theme getDefaultLightTheme() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull
        public final com.nexussms.data.models.Theme getDefaultDarkTheme() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull
        public final java.util.List<com.nexussms.data.models.Theme> getBuiltInThemes() {
            return null;
        }
    }
}