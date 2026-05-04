package com.nexussms.data.repository;

import com.nexussms.data.database.ScheduledMessageDao;
import com.nexussms.data.models.ScheduledMessage;
import kotlinx.coroutines.flow.Flow;
import javax.inject.Inject;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00004\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\t\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0002\b\u0005\u0018\u00002\u00020\u0001B\u000f\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u0019\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\bH\u0086@\u00f8\u0001\u0000\u00a2\u0006\u0002\u0010\tJ\u0019\u0010\n\u001a\u00020\u00062\u0006\u0010\u000b\u001a\u00020\fH\u0086@\u00f8\u0001\u0000\u00a2\u0006\u0002\u0010\rJ\u0012\u0010\u000e\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\b0\u00100\u000fJ\u0012\u0010\u0011\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\b0\u00100\u000fJ\u0016\u0010\u0012\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\b0\u000f2\u0006\u0010\u000b\u001a\u00020\fJ\u0019\u0010\u0013\u001a\u00020\f2\u0006\u0010\u0007\u001a\u00020\bH\u0086@\u00f8\u0001\u0000\u00a2\u0006\u0002\u0010\tJ\u0019\u0010\u0014\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\bH\u0086@\u00f8\u0001\u0000\u00a2\u0006\u0002\u0010\tR\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u0082\u0002\u0004\n\u0002\b\u0019\u00a8\u0006\u0015"}, d2 = {"Lcom/nexussms/data/repository/ScheduledMessageRepository;", "", "scheduledMessageDao", "Lcom/nexussms/data/database/ScheduledMessageDao;", "(Lcom/nexussms/data/database/ScheduledMessageDao;)V", "deleteScheduledMessage", "", "message", "Lcom/nexussms/data/models/ScheduledMessage;", "(Lcom/nexussms/data/models/ScheduledMessage;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "deleteScheduledMessageById", "id", "", "(JLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getAllScheduledMessages", "Lkotlinx/coroutines/flow/Flow;", "", "getPendingScheduledMessages", "getScheduledMessage", "insertScheduledMessage", "updateScheduledMessage", "app_debug"})
public final class ScheduledMessageRepository {
    @org.jetbrains.annotations.NotNull
    private final com.nexussms.data.database.ScheduledMessageDao scheduledMessageDao = null;
    
    @javax.inject.Inject
    public ScheduledMessageRepository(@org.jetbrains.annotations.NotNull
    com.nexussms.data.database.ScheduledMessageDao scheduledMessageDao) {
        super();
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.Object insertScheduledMessage(@org.jetbrains.annotations.NotNull
    com.nexussms.data.models.ScheduledMessage message, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super java.lang.Long> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.Object updateScheduledMessage(@org.jetbrains.annotations.NotNull
    com.nexussms.data.models.ScheduledMessage message, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.Object deleteScheduledMessage(@org.jetbrains.annotations.NotNull
    com.nexussms.data.models.ScheduledMessage message, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.Object deleteScheduledMessageById(long id, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final kotlinx.coroutines.flow.Flow<java.util.List<com.nexussms.data.models.ScheduledMessage>> getAllScheduledMessages() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final kotlinx.coroutines.flow.Flow<java.util.List<com.nexussms.data.models.ScheduledMessage>> getPendingScheduledMessages() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final kotlinx.coroutines.flow.Flow<com.nexussms.data.models.ScheduledMessage> getScheduledMessage(long id) {
        return null;
    }
}