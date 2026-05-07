package com.nexussms.data.repository;

import com.nexussms.data.database.ScheduledMessageDao;
import com.nexussms.data.models.ScheduledMessage;
import kotlinx.coroutines.flow.Flow;
import javax.inject.Inject;

@kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u00004\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\t\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0002\b\u0003\u0018\u00002\u00020\u0001B\u0011\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0004\b\u0004\u0010\u0005J\u0016\u0010\u0006\u001a\u00020\u00072\u0006\u0010\b\u001a\u00020\tH\u0086@\u00a2\u0006\u0002\u0010\nJ\u0016\u0010\u000b\u001a\u00020\f2\u0006\u0010\b\u001a\u00020\tH\u0086@\u00a2\u0006\u0002\u0010\nJ\u0016\u0010\r\u001a\u00020\f2\u0006\u0010\b\u001a\u00020\tH\u0086@\u00a2\u0006\u0002\u0010\nJ\u0016\u0010\u000e\u001a\u00020\f2\u0006\u0010\u000f\u001a\u00020\u0007H\u0086@\u00a2\u0006\u0002\u0010\u0010J\u0012\u0010\u0011\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\t0\u00130\u0012J\u0012\u0010\u0014\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\t0\u00130\u0012J\u0016\u0010\u0015\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\t0\u00122\u0006\u0010\u000f\u001a\u00020\u0007R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0016"}, d2 = {"Lcom/nexussms/data/repository/ScheduledMessageRepository;", "", "scheduledMessageDao", "Lcom/nexussms/data/database/ScheduledMessageDao;", "<init>", "(Lcom/nexussms/data/database/ScheduledMessageDao;)V", "insertScheduledMessage", "", "message", "Lcom/nexussms/data/models/ScheduledMessage;", "(Lcom/nexussms/data/models/ScheduledMessage;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "updateScheduledMessage", "", "deleteScheduledMessage", "deleteScheduledMessageById", "id", "(JLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getAllScheduledMessages", "Lkotlinx/coroutines/flow/Flow;", "", "getPendingScheduledMessages", "getScheduledMessage", "app_debug"})
public final class ScheduledMessageRepository {
    @org.jetbrains.annotations.NotNull()
    private final com.nexussms.data.database.ScheduledMessageDao scheduledMessageDao = null;
    
    @javax.inject.Inject()
    public ScheduledMessageRepository(@org.jetbrains.annotations.NotNull()
    com.nexussms.data.database.ScheduledMessageDao scheduledMessageDao) {
        super();
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object insertScheduledMessage(@org.jetbrains.annotations.NotNull()
    com.nexussms.data.models.ScheduledMessage message, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.Long> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object updateScheduledMessage(@org.jetbrains.annotations.NotNull()
    com.nexussms.data.models.ScheduledMessage message, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object deleteScheduledMessage(@org.jetbrains.annotations.NotNull()
    com.nexussms.data.models.ScheduledMessage message, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object deleteScheduledMessageById(long id, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.Flow<java.util.List<com.nexussms.data.models.ScheduledMessage>> getAllScheduledMessages() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.Flow<java.util.List<com.nexussms.data.models.ScheduledMessage>> getPendingScheduledMessages() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.Flow<com.nexussms.data.models.ScheduledMessage> getScheduledMessage(long id) {
        return null;
    }
}