package com.nexussms.data.repository;

import com.nexussms.data.database.MessageDao;
import com.nexussms.data.models.Message;
import kotlinx.coroutines.flow.Flow;
import javax.inject.Inject;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000D\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\t\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0002\b\u0004\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0010\b\n\u0002\b\u0006\u0018\u00002\u00020\u0001B\u000f\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u0019\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\bH\u0086@\u00f8\u0001\u0000\u00a2\u0006\u0002\u0010\tJ\u0019\u0010\n\u001a\u00020\u00062\u0006\u0010\u000b\u001a\u00020\fH\u0086@\u00f8\u0001\u0000\u00a2\u0006\u0002\u0010\rJ\u001a\u0010\u000e\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\f0\u00100\u000f2\u0006\u0010\u0007\u001a\u00020\bJ\u0016\u0010\u0011\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\f0\u000f2\u0006\u0010\u0012\u001a\u00020\bJ\u001a\u0010\u0013\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\f0\u00100\u000f2\u0006\u0010\u0014\u001a\u00020\u0015J$\u0010\u0016\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\f0\u00100\u000f2\u0006\u0010\u0017\u001a\u00020\b2\b\b\u0002\u0010\u0018\u001a\u00020\u0019J\u0014\u0010\u001a\u001a\b\u0012\u0004\u0012\u00020\u00190\u000f2\u0006\u0010\u0007\u001a\u00020\bJ\u001a\u0010\u001b\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\f0\u00100\u000f2\u0006\u0010\u0007\u001a\u00020\bJ\u0019\u0010\u001c\u001a\u00020\b2\u0006\u0010\u000b\u001a\u00020\fH\u0086@\u00f8\u0001\u0000\u00a2\u0006\u0002\u0010\rJ\u0019\u0010\u001d\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\bH\u0086@\u00f8\u0001\u0000\u00a2\u0006\u0002\u0010\tJ\u0019\u0010\u001e\u001a\u00020\u00062\u0006\u0010\u000b\u001a\u00020\fH\u0086@\u00f8\u0001\u0000\u00a2\u0006\u0002\u0010\rR\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u0082\u0002\u0004\n\u0002\b\u0019\u00a8\u0006\u001f"}, d2 = {"Lcom/nexussms/data/repository/MessageRepository;", "", "messageDao", "Lcom/nexussms/data/database/MessageDao;", "(Lcom/nexussms/data/database/MessageDao;)V", "deleteConversationMessages", "", "conversationId", "", "(JLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "deleteMessage", "message", "Lcom/nexussms/data/models/Message;", "(Lcom/nexussms/data/models/Message;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getConversationMessages", "Lkotlinx/coroutines/flow/Flow;", "", "getMessage", "id", "getMessagesByType", "type", "", "getRecentMessages", "startTime", "limit", "", "getUnreadCount", "getUnreadMessages", "insertMessage", "markConversationAsRead", "updateMessage", "app_debug"})
public final class MessageRepository {
    @org.jetbrains.annotations.NotNull
    private final com.nexussms.data.database.MessageDao messageDao = null;
    
    @javax.inject.Inject
    public MessageRepository(@org.jetbrains.annotations.NotNull
    com.nexussms.data.database.MessageDao messageDao) {
        super();
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.Object insertMessage(@org.jetbrains.annotations.NotNull
    com.nexussms.data.models.Message message, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super java.lang.Long> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.Object updateMessage(@org.jetbrains.annotations.NotNull
    com.nexussms.data.models.Message message, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.Object deleteMessage(@org.jetbrains.annotations.NotNull
    com.nexussms.data.models.Message message, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final kotlinx.coroutines.flow.Flow<com.nexussms.data.models.Message> getMessage(long id) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final kotlinx.coroutines.flow.Flow<java.util.List<com.nexussms.data.models.Message>> getConversationMessages(long conversationId) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final kotlinx.coroutines.flow.Flow<java.util.List<com.nexussms.data.models.Message>> getUnreadMessages(long conversationId) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final kotlinx.coroutines.flow.Flow<java.lang.Integer> getUnreadCount(long conversationId) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.Object deleteConversationMessages(long conversationId, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.Object markConversationAsRead(long conversationId, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final kotlinx.coroutines.flow.Flow<java.util.List<com.nexussms.data.models.Message>> getRecentMessages(long startTime, int limit) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final kotlinx.coroutines.flow.Flow<java.util.List<com.nexussms.data.models.Message>> getMessagesByType(@org.jetbrains.annotations.NotNull
    java.lang.String type) {
        return null;
    }
}