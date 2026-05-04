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

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00002\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0000\n\u0002\u0010\t\n\u0002\b\u0002\bg\u0018\u00002\u00020\u0001J\u0019\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u0005H\u00a7@\u00f8\u0001\u0000\u00a2\u0006\u0002\u0010\u0006J\u0019\u0010\u0007\u001a\u00020\u00032\u0006\u0010\b\u001a\u00020\tH\u00a7@\u00f8\u0001\u0000\u00a2\u0006\u0002\u0010\nJ\u001c\u0010\u000b\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\t0\r0\f2\u0006\u0010\u0004\u001a\u00020\u0005H\'J\u0019\u0010\u000e\u001a\u00020\u000f2\u0006\u0010\b\u001a\u00020\tH\u00a7@\u00f8\u0001\u0000\u00a2\u0006\u0002\u0010\nJ\u0019\u0010\u0010\u001a\u00020\u00032\u0006\u0010\b\u001a\u00020\tH\u00a7@\u00f8\u0001\u0000\u00a2\u0006\u0002\u0010\n\u0082\u0002\u0004\n\u0002\b\u0019\u00a8\u0006\u0011"}, d2 = {"Lcom/nexussms/data/database/ContactShortcutDao;", "", "deleteAllShortcutsForContact", "", "phone", "", "(Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "deleteContactShortcut", "contactShortcut", "Lcom/nexussms/data/models/ContactShortcut;", "(Lcom/nexussms/data/models/ContactShortcut;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getEnabledShortcutsForContact", "Lkotlinx/coroutines/flow/Flow;", "", "insertContactShortcut", "", "updateContactShortcut", "app_debug"})
@androidx.room.Dao
public abstract interface ContactShortcutDao {
    
    @androidx.room.Insert
    @org.jetbrains.annotations.Nullable
    public abstract java.lang.Object insertContactShortcut(@org.jetbrains.annotations.NotNull
    com.nexussms.data.models.ContactShortcut contactShortcut, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super java.lang.Long> $completion);
    
    @androidx.room.Update
    @org.jetbrains.annotations.Nullable
    public abstract java.lang.Object updateContactShortcut(@org.jetbrains.annotations.NotNull
    com.nexussms.data.models.ContactShortcut contactShortcut, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Delete
    @org.jetbrains.annotations.Nullable
    public abstract java.lang.Object deleteContactShortcut(@org.jetbrains.annotations.NotNull
    com.nexussms.data.models.ContactShortcut contactShortcut, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Query(value = "SELECT * FROM contact_shortcuts WHERE contactPhone = :phone AND isEnabled = 1")
    @org.jetbrains.annotations.NotNull
    public abstract kotlinx.coroutines.flow.Flow<java.util.List<com.nexussms.data.models.ContactShortcut>> getEnabledShortcutsForContact(@org.jetbrains.annotations.NotNull
    java.lang.String phone);
    
    @androidx.room.Query(value = "DELETE FROM contact_shortcuts WHERE contactPhone = :phone")
    @org.jetbrains.annotations.Nullable
    public abstract java.lang.Object deleteAllShortcutsForContact(@org.jetbrains.annotations.NotNull
    java.lang.String phone, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
}