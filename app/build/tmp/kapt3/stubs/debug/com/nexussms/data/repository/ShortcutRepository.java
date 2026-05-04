package com.nexussms.data.repository;

import com.nexussms.data.database.ShortcutDao;
import com.nexussms.data.models.Shortcut;
import kotlinx.coroutines.flow.Flow;
import javax.inject.Inject;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000<\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0010\t\n\u0002\b\u0006\u0018\u00002\u00020\u0001B\u000f\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u0019\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\bH\u0086@\u00f8\u0001\u0000\u00a2\u0006\u0002\u0010\tJ\u0012\u0010\n\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\b0\f0\u000bJ\u0016\u0010\r\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\b0\u000b2\u0006\u0010\u000e\u001a\u00020\u000fJ\u0019\u0010\u0010\u001a\u00020\u00062\u0006\u0010\u0011\u001a\u00020\u0012H\u0086@\u00f8\u0001\u0000\u00a2\u0006\u0002\u0010\u0013J\u0019\u0010\u0014\u001a\u00020\u00122\u0006\u0010\u0007\u001a\u00020\bH\u0086@\u00f8\u0001\u0000\u00a2\u0006\u0002\u0010\tJ\u001a\u0010\u0015\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\b0\f0\u000b2\u0006\u0010\u0016\u001a\u00020\u000fJ\u0019\u0010\u0017\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\bH\u0086@\u00f8\u0001\u0000\u00a2\u0006\u0002\u0010\tR\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u0082\u0002\u0004\n\u0002\b\u0019\u00a8\u0006\u0018"}, d2 = {"Lcom/nexussms/data/repository/ShortcutRepository;", "", "shortcutDao", "Lcom/nexussms/data/database/ShortcutDao;", "(Lcom/nexussms/data/database/ShortcutDao;)V", "deleteShortcut", "", "shortcut", "Lcom/nexussms/data/models/Shortcut;", "(Lcom/nexussms/data/models/Shortcut;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getAllShortcuts", "Lkotlinx/coroutines/flow/Flow;", "", "getShortcut", "trigger", "", "incrementUsageCount", "id", "", "(JLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "insertShortcut", "searchShortcuts", "pattern", "updateShortcut", "app_debug"})
public final class ShortcutRepository {
    @org.jetbrains.annotations.NotNull
    private final com.nexussms.data.database.ShortcutDao shortcutDao = null;
    
    @javax.inject.Inject
    public ShortcutRepository(@org.jetbrains.annotations.NotNull
    com.nexussms.data.database.ShortcutDao shortcutDao) {
        super();
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.Object insertShortcut(@org.jetbrains.annotations.NotNull
    com.nexussms.data.models.Shortcut shortcut, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super java.lang.Long> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.Object updateShortcut(@org.jetbrains.annotations.NotNull
    com.nexussms.data.models.Shortcut shortcut, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.Object deleteShortcut(@org.jetbrains.annotations.NotNull
    com.nexussms.data.models.Shortcut shortcut, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final kotlinx.coroutines.flow.Flow<java.util.List<com.nexussms.data.models.Shortcut>> getAllShortcuts() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final kotlinx.coroutines.flow.Flow<com.nexussms.data.models.Shortcut> getShortcut(@org.jetbrains.annotations.NotNull
    java.lang.String trigger) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final kotlinx.coroutines.flow.Flow<java.util.List<com.nexussms.data.models.Shortcut>> searchShortcuts(@org.jetbrains.annotations.NotNull
    java.lang.String pattern) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.Object incrementUsageCount(long id, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
}