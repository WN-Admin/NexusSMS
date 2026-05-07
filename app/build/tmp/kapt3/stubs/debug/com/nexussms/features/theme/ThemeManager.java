package com.nexussms.features.theme;

import com.nexussms.data.models.Theme;
import com.nexussms.data.repository.ThemeRepository;
import kotlinx.coroutines.flow.Flow;
import javax.inject.Inject;
import javax.inject.Singleton;

@javax.inject.Singleton()
@kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u0000J\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010\t\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0007\n\u0002\u0010\u000b\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0002\b\t\b\u0007\u0018\u0000 +2\u00020\u0001:\u0001+B\u0011\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0004\b\u0004\u0010\u0005J\u000e\u0010\u0006\u001a\u00020\u0007H\u0086@\u00a2\u0006\u0002\u0010\bJ\u0012\u0010\t\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\f0\u000b0\nJ\u0012\u0010\r\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\f0\u000b0\nJ\u0012\u0010\u000e\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\f0\u000b0\nJ\u0016\u0010\u000f\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\f0\n2\u0006\u0010\u0010\u001a\u00020\u0011JN\u0010\u0012\u001a\u00020\u00112\u0006\u0010\u0013\u001a\u00020\u00142\u0006\u0010\u0015\u001a\u00020\u00142\u0006\u0010\u0016\u001a\u00020\u00142\u0006\u0010\u0017\u001a\u00020\u00142\u0006\u0010\u0018\u001a\u00020\u00142\u0006\u0010\u0019\u001a\u00020\u00142\u0006\u0010\u001a\u001a\u00020\u00142\u0006\u0010\u001b\u001a\u00020\u001cH\u0086@\u00a2\u0006\u0002\u0010\u001dJ\u0016\u0010\u001e\u001a\u00020\u00072\u0006\u0010\u001f\u001a\u00020\fH\u0086@\u00a2\u0006\u0002\u0010 J\u0016\u0010!\u001a\u00020\u00072\u0006\u0010\u001f\u001a\u00020\fH\u0086@\u00a2\u0006\u0002\u0010 J\u0015\u0010\"\u001a\u00020#2\u0006\u0010$\u001a\u00020\u0014\u00a2\u0006\u0004\b%\u0010&J\u0015\u0010\'\u001a\u00020\u00142\u0006\u0010(\u001a\u00020#\u00a2\u0006\u0004\b)\u0010*R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006,"}, d2 = {"Lcom/nexussms/features/theme/ThemeManager;", "", "themeRepository", "Lcom/nexussms/data/repository/ThemeRepository;", "<init>", "(Lcom/nexussms/data/repository/ThemeRepository;)V", "initializeDefaultThemes", "", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getAllThemes", "Lkotlinx/coroutines/flow/Flow;", "", "Lcom/nexussms/data/models/Theme;", "getDefaultThemes", "getCustomThemes", "getTheme", "id", "", "createCustomTheme", "name", "", "primaryColor", "secondaryColor", "bubbleColorSent", "bubbleColorReceived", "textColor", "backgroundColor", "isDarkMode", "", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;ZLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "updateTheme", "theme", "(Lcom/nexussms/data/models/Theme;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "deleteTheme", "hexToColor", "Landroidx/compose/ui/graphics/Color;", "hex", "hexToColor-vNxB06k", "(Ljava/lang/String;)J", "colorToHex", "color", "colorToHex-8_81llA", "(J)Ljava/lang/String;", "Companion", "app_debug"})
public final class ThemeManager {
    @org.jetbrains.annotations.NotNull()
    private final com.nexussms.data.repository.ThemeRepository themeRepository = null;
    @org.jetbrains.annotations.NotNull()
    private static final com.nexussms.data.models.Theme defaultLightTheme = null;
    @org.jetbrains.annotations.NotNull()
    private static final com.nexussms.data.models.Theme defaultDarkTheme = null;
    @org.jetbrains.annotations.NotNull()
    private static final java.util.List<com.nexussms.data.models.Theme> builtInThemes = null;
    @org.jetbrains.annotations.NotNull()
    public static final com.nexussms.features.theme.ThemeManager.Companion Companion = null;
    
    @javax.inject.Inject()
    public ThemeManager(@org.jetbrains.annotations.NotNull()
    com.nexussms.data.repository.ThemeRepository themeRepository) {
        super();
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object initializeDefaultThemes(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.Flow<java.util.List<com.nexussms.data.models.Theme>> getAllThemes() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.Flow<java.util.List<com.nexussms.data.models.Theme>> getDefaultThemes() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.Flow<java.util.List<com.nexussms.data.models.Theme>> getCustomThemes() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.Flow<com.nexussms.data.models.Theme> getTheme(long id) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object createCustomTheme(@org.jetbrains.annotations.NotNull()
    java.lang.String name, @org.jetbrains.annotations.NotNull()
    java.lang.String primaryColor, @org.jetbrains.annotations.NotNull()
    java.lang.String secondaryColor, @org.jetbrains.annotations.NotNull()
    java.lang.String bubbleColorSent, @org.jetbrains.annotations.NotNull()
    java.lang.String bubbleColorReceived, @org.jetbrains.annotations.NotNull()
    java.lang.String textColor, @org.jetbrains.annotations.NotNull()
    java.lang.String backgroundColor, boolean isDarkMode, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.Long> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object updateTheme(@org.jetbrains.annotations.NotNull()
    com.nexussms.data.models.Theme theme, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object deleteTheme(@org.jetbrains.annotations.NotNull()
    com.nexussms.data.models.Theme theme, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u0000\u001c\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010 \n\u0002\b\u0003\b\u0086\u0003\u0018\u00002\u00020\u0001B\t\b\u0002\u00a2\u0006\u0004\b\u0002\u0010\u0003R\u0011\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0006\u0010\u0007R\u0011\u0010\b\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\t\u0010\u0007R\u0017\u0010\n\u001a\b\u0012\u0004\u0012\u00020\u00050\u000b\u00a2\u0006\b\n\u0000\u001a\u0004\b\f\u0010\r\u00a8\u0006\u000e"}, d2 = {"Lcom/nexussms/features/theme/ThemeManager$Companion;", "", "<init>", "()V", "defaultLightTheme", "Lcom/nexussms/data/models/Theme;", "getDefaultLightTheme", "()Lcom/nexussms/data/models/Theme;", "defaultDarkTheme", "getDefaultDarkTheme", "builtInThemes", "", "getBuiltInThemes", "()Ljava/util/List;", "app_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.nexussms.data.models.Theme getDefaultLightTheme() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.nexussms.data.models.Theme getDefaultDarkTheme() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.util.List<com.nexussms.data.models.Theme> getBuiltInThemes() {
            return null;
        }
    }
}