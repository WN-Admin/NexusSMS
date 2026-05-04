package com.nexussms.data.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import androidx.room.Upsert;
import com.nexussms.data.models.Message;
import com.nexussms.data.models.Conversation;
import com.nexussms.data.models.Shortcut;
import com.nexussms.data.models.ScheduledMessage;
import com.nexussms.data.models.UserSignature;
import com.nexussms.data.models.Theme;
import com.nexussms.data.models.SocialAccount;
import com.nexussms.data.models.ContactShortcut;
import kotlinx.coroutines.flow.Flow;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00004\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0010\t\n\u0002\b\u0006\bg\u0018\u00002\u00020\u0001J\u0019\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u0005H\u00a7@\u00f8\u0001\u0000\u00a2\u0006\u0002\u0010\u0006J\u0014\u0010\u0007\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00050\t0\bH\'J\u0018\u0010\n\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u00050\b2\u0006\u0010\u000b\u001a\u00020\fH\'J\u0019\u0010\r\u001a\u00020\u00032\u0006\u0010\u000e\u001a\u00020\u000fH\u00a7@\u00f8\u0001\u0000\u00a2\u0006\u0002\u0010\u0010J\u0019\u0010\u0011\u001a\u00020\u000f2\u0006\u0010\u0004\u001a\u00020\u0005H\u00a7@\u00f8\u0001\u0000\u00a2\u0006\u0002\u0010\u0006J\u001c\u0010\u0012\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00050\t0\b2\u0006\u0010\u0013\u001a\u00020\fH\'J\u0019\u0010\u0014\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u0005H\u00a7@\u00f8\u0001\u0000\u00a2\u0006\u0002\u0010\u0006\u0082\u0002\u0004\n\u0002\b\u0019\u00a8\u0006\u0015"}, d2 = {"Lcom/nexussms/data/database/ShortcutDao;", "", "deleteShortcut", "", "shortcut", "Lcom/nexussms/data/models/Shortcut;", "(Lcom/nexussms/data/models/Shortcut;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getAllShortcuts", "Lkotlinx/coroutines/flow/Flow;", "", "getShortcut", "trigger", "", "incrementUsageCount", "id", "", "(JLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "insertShortcut", "searchShortcuts", "pattern", "updateShortcut", "app_debug"})
@androidx.room.Dao
public abstract interface ShortcutDao {
    
    @androidx.room.Insert
    @org.jetbrains.annotations.Nullable
    public abstract java.lang.Object insertShortcut(@org.jetbrains.annotations.NotNull
    com.nexussms.data.models.Shortcut shortcut, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super java.lang.Long> $completion);
    
    @androidx.room.Update
    @org.jetbrains.annotations.Nullable
    public abstract java.lang.Object updateShortcut(@org.jetbrains.annotations.NotNull
    com.nexussms.data.models.Shortcut shortcut, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Delete
    @org.jetbrains.annotations.Nullable
    public abstract java.lang.Object deleteShortcut(@org.jetbrains.annotations.NotNull
    com.nexussms.data.models.Shortcut shortcut, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Query(value = "SELECT * FROM shortcuts ORDER BY usageCount DESC")
    @org.jetbrains.annotations.NotNull
    public abstract kotlinx.coroutines.flow.Flow<java.util.List<com.nexussms.data.models.Shortcut>> getAllShortcuts();
    
    @androidx.room.Query(value = "SELECT * FROM shortcuts WHERE trigger = :trigger LIMIT 1")
    @org.jetbrains.annotations.NotNull
    public abstract kotlinx.coroutines.flow.Flow<com.nexussms.data.models.Shortcut> getShortcut(@org.jetbrains.annotations.NotNull
    java.lang.String trigger);
    
    @androidx.room.Query(value = "SELECT * FROM shortcuts WHERE trigger LIKE :pattern")
    @org.jetbrains.annotations.NotNull
    public abstract kotlinx.coroutines.flow.Flow<java.util.List<com.nexussms.data.models.Shortcut>> searchShortcuts(@org.jetbrains.annotations.NotNull
    java.lang.String pattern);
    
    @androidx.room.Query(value = "UPDATE shortcuts SET usageCount = usageCount + 1 WHERE id = :id")
    @org.jetbrains.annotations.Nullable
    public abstract java.lang.Object incrementUsageCount(long id, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
}