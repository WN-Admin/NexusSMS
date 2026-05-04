package com.nexussms.ui.viewmodels;

import androidx.lifecycle.ViewModel;
import com.nexussms.data.models.Theme;
import com.nexussms.data.models.UserSignature;
import com.nexussms.data.repository.SignatureRepository;
import com.nexussms.data.repository.SocialAccountRepository;
import com.nexussms.data.repository.ThemeRepository;
import com.nexussms.features.theme.ThemeManager;
import dagger.hilt.android.lifecycle.HiltViewModel;
import kotlinx.coroutines.flow.StateFlow;
import javax.inject.Inject;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000b\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\b\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\r\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0005\b\u0007\u0018\u00002\u00020\u0001B\'\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0007\u0012\u0006\u0010\b\u001a\u00020\t\u00a2\u0006\u0002\u0010\nJF\u0010\u001d\u001a\u00020\u001e2\u0006\u0010\u001f\u001a\u00020 2\u0006\u0010!\u001a\u00020 2\u0006\u0010\"\u001a\u00020 2\u0006\u0010#\u001a\u00020 2\u0006\u0010$\u001a\u00020 2\u0006\u0010%\u001a\u00020 2\u0006\u0010&\u001a\u00020 2\u0006\u0010\u0018\u001a\u00020\u000fJ \u0010\'\u001a\u00020\u001e2\u0006\u0010\u001f\u001a\u00020 2\u0006\u0010(\u001a\u00020 2\b\b\u0002\u0010)\u001a\u00020\u000fJ\u000e\u0010*\u001a\u00020\u001e2\u0006\u0010(\u001a\u00020\u0012J\u000e\u0010+\u001a\u00020\u001e2\u0006\u0010,\u001a\u00020\rJ\u0012\u0010-\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020/0\u00110.J\u000e\u00100\u001a\u00020\u001e2\u0006\u0010,\u001a\u00020\rJ\u000e\u00101\u001a\u00020\u001e2\u0006\u00102\u001a\u00020\u000fJ\u000e\u00103\u001a\u00020\u001e2\u0006\u0010(\u001a\u00020\u0012R\u0016\u0010\u000b\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\r0\fX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u000e\u001a\b\u0012\u0004\u0012\u00020\u000f0\fX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001a\u0010\u0010\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00120\u00110\fX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001a\u0010\u0013\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\r0\u00110\fX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0019\u0010\u0014\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\r0\u0015\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0016\u0010\u0017R\u0017\u0010\u0018\u001a\b\u0012\u0004\u0012\u00020\u000f0\u0015\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0018\u0010\u0017R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001d\u0010\u0019\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00120\u00110\u0015\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001a\u0010\u0017R\u000e\u0010\u0006\u001a\u00020\u0007X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\tX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001d\u0010\u001b\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\r0\u00110\u0015\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001c\u0010\u0017\u00a8\u00064"}, d2 = {"Lcom/nexussms/ui/viewmodels/SettingsViewModel;", "Landroidx/lifecycle/ViewModel;", "themeRepository", "Lcom/nexussms/data/repository/ThemeRepository;", "signatureRepository", "Lcom/nexussms/data/repository/SignatureRepository;", "socialAccountRepository", "Lcom/nexussms/data/repository/SocialAccountRepository;", "themeManager", "Lcom/nexussms/features/theme/ThemeManager;", "(Lcom/nexussms/data/repository/ThemeRepository;Lcom/nexussms/data/repository/SignatureRepository;Lcom/nexussms/data/repository/SocialAccountRepository;Lcom/nexussms/features/theme/ThemeManager;)V", "_currentTheme", "Lkotlinx/coroutines/flow/MutableStateFlow;", "Lcom/nexussms/data/models/Theme;", "_isDarkMode", "", "_signatures", "", "Lcom/nexussms/data/models/UserSignature;", "_themes", "currentTheme", "Lkotlinx/coroutines/flow/StateFlow;", "getCurrentTheme", "()Lkotlinx/coroutines/flow/StateFlow;", "isDarkMode", "signatures", "getSignatures", "themes", "getThemes", "createCustomTheme", "", "name", "", "primaryColor", "secondaryColor", "bubbleColorSent", "bubbleColorReceived", "textColor", "backgroundColor", "createSignature", "signature", "isDefault", "deleteSignature", "deleteTheme", "theme", "getConnectedSocialAccounts", "Lkotlinx/coroutines/flow/Flow;", "Lcom/nexussms/data/models/SocialAccount;", "setCurrentTheme", "toggleDarkMode", "isDark", "updateSignature", "app_debug"})
@dagger.hilt.android.lifecycle.HiltViewModel
public final class SettingsViewModel extends androidx.lifecycle.ViewModel {
    @org.jetbrains.annotations.NotNull
    private final com.nexussms.data.repository.ThemeRepository themeRepository = null;
    @org.jetbrains.annotations.NotNull
    private final com.nexussms.data.repository.SignatureRepository signatureRepository = null;
    @org.jetbrains.annotations.NotNull
    private final com.nexussms.data.repository.SocialAccountRepository socialAccountRepository = null;
    @org.jetbrains.annotations.NotNull
    private final com.nexussms.features.theme.ThemeManager themeManager = null;
    @org.jetbrains.annotations.NotNull
    private final kotlinx.coroutines.flow.MutableStateFlow<java.util.List<com.nexussms.data.models.Theme>> _themes = null;
    @org.jetbrains.annotations.NotNull
    private final kotlinx.coroutines.flow.StateFlow<java.util.List<com.nexussms.data.models.Theme>> themes = null;
    @org.jetbrains.annotations.NotNull
    private final kotlinx.coroutines.flow.MutableStateFlow<java.util.List<com.nexussms.data.models.UserSignature>> _signatures = null;
    @org.jetbrains.annotations.NotNull
    private final kotlinx.coroutines.flow.StateFlow<java.util.List<com.nexussms.data.models.UserSignature>> signatures = null;
    @org.jetbrains.annotations.NotNull
    private final kotlinx.coroutines.flow.MutableStateFlow<com.nexussms.data.models.Theme> _currentTheme = null;
    @org.jetbrains.annotations.NotNull
    private final kotlinx.coroutines.flow.StateFlow<com.nexussms.data.models.Theme> currentTheme = null;
    @org.jetbrains.annotations.NotNull
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.Boolean> _isDarkMode = null;
    @org.jetbrains.annotations.NotNull
    private final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> isDarkMode = null;
    
    @javax.inject.Inject
    public SettingsViewModel(@org.jetbrains.annotations.NotNull
    com.nexussms.data.repository.ThemeRepository themeRepository, @org.jetbrains.annotations.NotNull
    com.nexussms.data.repository.SignatureRepository signatureRepository, @org.jetbrains.annotations.NotNull
    com.nexussms.data.repository.SocialAccountRepository socialAccountRepository, @org.jetbrains.annotations.NotNull
    com.nexussms.features.theme.ThemeManager themeManager) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull
    public final kotlinx.coroutines.flow.StateFlow<java.util.List<com.nexussms.data.models.Theme>> getThemes() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final kotlinx.coroutines.flow.StateFlow<java.util.List<com.nexussms.data.models.UserSignature>> getSignatures() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final kotlinx.coroutines.flow.StateFlow<com.nexussms.data.models.Theme> getCurrentTheme() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> isDarkMode() {
        return null;
    }
    
    public final void setCurrentTheme(@org.jetbrains.annotations.NotNull
    com.nexussms.data.models.Theme theme) {
    }
    
    public final void createCustomTheme(@org.jetbrains.annotations.NotNull
    java.lang.String name, @org.jetbrains.annotations.NotNull
    java.lang.String primaryColor, @org.jetbrains.annotations.NotNull
    java.lang.String secondaryColor, @org.jetbrains.annotations.NotNull
    java.lang.String bubbleColorSent, @org.jetbrains.annotations.NotNull
    java.lang.String bubbleColorReceived, @org.jetbrains.annotations.NotNull
    java.lang.String textColor, @org.jetbrains.annotations.NotNull
    java.lang.String backgroundColor, boolean isDarkMode) {
    }
    
    public final void deleteTheme(@org.jetbrains.annotations.NotNull
    com.nexussms.data.models.Theme theme) {
    }
    
    public final void createSignature(@org.jetbrains.annotations.NotNull
    java.lang.String name, @org.jetbrains.annotations.NotNull
    java.lang.String signature, boolean isDefault) {
    }
    
    public final void updateSignature(@org.jetbrains.annotations.NotNull
    com.nexussms.data.models.UserSignature signature) {
    }
    
    public final void deleteSignature(@org.jetbrains.annotations.NotNull
    com.nexussms.data.models.UserSignature signature) {
    }
    
    public final void toggleDarkMode(boolean isDark) {
    }
    
    @org.jetbrains.annotations.NotNull
    public final kotlinx.coroutines.flow.Flow<java.util.List<com.nexussms.data.models.SocialAccount>> getConnectedSocialAccounts() {
        return null;
    }
}