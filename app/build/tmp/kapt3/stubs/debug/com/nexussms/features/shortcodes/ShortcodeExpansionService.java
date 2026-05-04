package com.nexussms.features.shortcodes;

import com.nexussms.data.repository.ShortcutRepository;
import javax.inject.Inject;
import javax.inject.Singleton;

@javax.inject.Singleton
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000>\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\b\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\b\n\u0002\b\u0002\b\u0007\u0018\u00002\u00020\u0001B\u000f\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J+\u0010\u0007\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\n2\u0006\u0010\u000b\u001a\u00020\n2\b\b\u0002\u0010\f\u001a\u00020\nH\u0086@\u00f8\u0001\u0000\u00a2\u0006\u0002\u0010\rJ\u0019\u0010\u000e\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\nH\u0086@\u00f8\u0001\u0000\u00a2\u0006\u0002\u0010\u000fJ\u0019\u0010\u0010\u001a\u00020\n2\u0006\u0010\u0011\u001a\u00020\nH\u0086@\u00f8\u0001\u0000\u00a2\u0006\u0002\u0010\u000fJ\u001d\u0010\u0012\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00150\u00140\u0013H\u0086@\u00f8\u0001\u0000\u00a2\u0006\u0002\u0010\u0016J!\u0010\u0017\u001a\b\u0012\u0004\u0012\u00020\u00150\u00142\b\b\u0002\u0010\u0018\u001a\u00020\u0019H\u0086@\u00f8\u0001\u0000\u00a2\u0006\u0002\u0010\u001aR\u000e\u0010\u0005\u001a\u00020\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u0082\u0002\u0004\n\u0002\b\u0019\u00a8\u0006\u001b"}, d2 = {"Lcom/nexussms/features/shortcodes/ShortcodeExpansionService;", "", "shortcutRepository", "Lcom/nexussms/data/repository/ShortcutRepository;", "(Lcom/nexussms/data/repository/ShortcutRepository;)V", "shortcodePattern", "Lkotlin/text/Regex;", "createShortcut", "", "trigger", "", "expansion", "category", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "deleteShortcut", "(Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "expandMessage", "message", "getAllShortcuts", "Lkotlinx/coroutines/flow/Flow;", "", "Lcom/nexussms/data/models/Shortcut;", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getFrequentlyUsedShortcuts", "limit", "", "(ILkotlin/coroutines/Continuation;)Ljava/lang/Object;", "app_debug"})
public final class ShortcodeExpansionService {
    @org.jetbrains.annotations.NotNull
    private final com.nexussms.data.repository.ShortcutRepository shortcutRepository = null;
    @org.jetbrains.annotations.NotNull
    private final kotlin.text.Regex shortcodePattern = null;
    
    @javax.inject.Inject
    public ShortcodeExpansionService(@org.jetbrains.annotations.NotNull
    com.nexussms.data.repository.ShortcutRepository shortcutRepository) {
        super();
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.Object expandMessage(@org.jetbrains.annotations.NotNull
    java.lang.String message, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super java.lang.String> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.Object createShortcut(@org.jetbrains.annotations.NotNull
    java.lang.String trigger, @org.jetbrains.annotations.NotNull
    java.lang.String expansion, @org.jetbrains.annotations.NotNull
    java.lang.String category, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.Object deleteShortcut(@org.jetbrains.annotations.NotNull
    java.lang.String trigger, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.Object getAllShortcuts(@org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super kotlinx.coroutines.flow.Flow<? extends java.util.List<com.nexussms.data.models.Shortcut>>> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.Object getFrequentlyUsedShortcuts(int limit, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super java.util.List<com.nexussms.data.models.Shortcut>> $completion) {
        return null;
    }
}