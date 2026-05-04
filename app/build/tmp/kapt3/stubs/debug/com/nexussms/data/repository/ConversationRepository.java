package com.nexussms.data.repository;

import com.nexussms.data.database.ConversationDao;
import com.nexussms.data.models.Conversation;
import kotlinx.coroutines.flow.Flow;
import javax.inject.Inject;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000<\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\t\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0002\b\u0005\u0018\u00002\u00020\u0001B\u000f\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u0019\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\bH\u0086@\u00f8\u0001\u0000\u00a2\u0006\u0002\u0010\tJ\u0019\u0010\n\u001a\u00020\u00062\u0006\u0010\u000b\u001a\u00020\fH\u0086@\u00f8\u0001\u0000\u00a2\u0006\u0002\u0010\rJ\u0019\u0010\u000e\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\bH\u0086@\u00f8\u0001\u0000\u00a2\u0006\u0002\u0010\tJ\u0012\u0010\u000f\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\f0\u00110\u0010J\u0016\u0010\u0012\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\f0\u00102\u0006\u0010\u0007\u001a\u00020\bJ\u0016\u0010\u0013\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\f0\u00102\u0006\u0010\u0014\u001a\u00020\u0015J\u0012\u0010\u0016\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\f0\u00110\u0010J\u0019\u0010\u0017\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\bH\u0086@\u00f8\u0001\u0000\u00a2\u0006\u0002\u0010\tJ\u0019\u0010\u0018\u001a\u00020\b2\u0006\u0010\u000b\u001a\u00020\fH\u0086@\u00f8\u0001\u0000\u00a2\u0006\u0002\u0010\rJ\u0019\u0010\u0019\u001a\u00020\u00062\u0006\u0010\u000b\u001a\u00020\fH\u0086@\u00f8\u0001\u0000\u00a2\u0006\u0002\u0010\rR\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u0082\u0002\u0004\n\u0002\b\u0019\u00a8\u0006\u001a"}, d2 = {"Lcom/nexussms/data/repository/ConversationRepository;", "", "conversationDao", "Lcom/nexussms/data/database/ConversationDao;", "(Lcom/nexussms/data/database/ConversationDao;)V", "clearUnreadCount", "", "id", "", "(JLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "deleteConversation", "conversation", "Lcom/nexussms/data/models/Conversation;", "(Lcom/nexussms/data/models/Conversation;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "deleteConversationById", "getAllConversations", "Lkotlinx/coroutines/flow/Flow;", "", "getConversation", "getConversationByPhone", "phone", "", "getPinnedConversations", "incrementUnreadCount", "insertConversation", "updateConversation", "app_debug"})
public final class ConversationRepository {
    @org.jetbrains.annotations.NotNull
    private final com.nexussms.data.database.ConversationDao conversationDao = null;
    
    @javax.inject.Inject
    public ConversationRepository(@org.jetbrains.annotations.NotNull
    com.nexussms.data.database.ConversationDao conversationDao) {
        super();
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.Object insertConversation(@org.jetbrains.annotations.NotNull
    com.nexussms.data.models.Conversation conversation, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super java.lang.Long> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.Object updateConversation(@org.jetbrains.annotations.NotNull
    com.nexussms.data.models.Conversation conversation, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.Object deleteConversation(@org.jetbrains.annotations.NotNull
    com.nexussms.data.models.Conversation conversation, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.Object deleteConversationById(long id, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final kotlinx.coroutines.flow.Flow<java.util.List<com.nexussms.data.models.Conversation>> getAllConversations() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final kotlinx.coroutines.flow.Flow<com.nexussms.data.models.Conversation> getConversation(long id) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final kotlinx.coroutines.flow.Flow<com.nexussms.data.models.Conversation> getConversationByPhone(@org.jetbrains.annotations.NotNull
    java.lang.String phone) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final kotlinx.coroutines.flow.Flow<java.util.List<com.nexussms.data.models.Conversation>> getPinnedConversations() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.Object incrementUnreadCount(long id, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.Object clearUnreadCount(long id, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
}