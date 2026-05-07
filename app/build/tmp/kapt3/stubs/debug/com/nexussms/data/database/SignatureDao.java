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

@kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u0000,\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\t\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0002\b\u0002\bg\u0018\u00002\u00020\u0001J\u0016\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u0005H\u00a7@\u00a2\u0006\u0002\u0010\u0006J\u0016\u0010\u0007\u001a\u00020\b2\u0006\u0010\u0004\u001a\u00020\u0005H\u00a7@\u00a2\u0006\u0002\u0010\u0006J\u0016\u0010\t\u001a\u00020\b2\u0006\u0010\u0004\u001a\u00020\u0005H\u00a7@\u00a2\u0006\u0002\u0010\u0006J\u0014\u0010\n\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00050\f0\u000bH\'J\u0010\u0010\r\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u00050\u000bH\'\u00a8\u0006\u000e\u00c0\u0006\u0003"}, d2 = {"Lcom/nexussms/data/database/SignatureDao;", "", "insertSignature", "", "signature", "Lcom/nexussms/data/models/UserSignature;", "(Lcom/nexussms/data/models/UserSignature;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "updateSignature", "", "deleteSignature", "getAllSignatures", "Lkotlinx/coroutines/flow/Flow;", "", "getDefaultSignature", "app_debug"})
@androidx.room.Dao()
public abstract interface SignatureDao {
    
    @androidx.room.Insert()
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object insertSignature(@org.jetbrains.annotations.NotNull()
    com.nexussms.data.models.UserSignature signature, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.Long> $completion);
    
    @androidx.room.Update()
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object updateSignature(@org.jetbrains.annotations.NotNull()
    com.nexussms.data.models.UserSignature signature, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Delete()
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object deleteSignature(@org.jetbrains.annotations.NotNull()
    com.nexussms.data.models.UserSignature signature, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Query(value = "SELECT * FROM user_signatures")
    @org.jetbrains.annotations.NotNull()
    public abstract kotlinx.coroutines.flow.Flow<java.util.List<com.nexussms.data.models.UserSignature>> getAllSignatures();
    
    @androidx.room.Query(value = "SELECT * FROM user_signatures WHERE isDefault = 1 LIMIT 1")
    @org.jetbrains.annotations.NotNull()
    public abstract kotlinx.coroutines.flow.Flow<com.nexussms.data.models.UserSignature> getDefaultSignature();
}