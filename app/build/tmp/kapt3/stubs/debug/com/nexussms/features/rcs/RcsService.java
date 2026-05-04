package com.nexussms.features.rcs;

import android.content.Context;
import com.nexussms.data.models.Message;
import com.nexussms.data.repository.MessageRepository;
import dagger.hilt.android.qualifiers.ApplicationContext;
import javax.inject.Inject;
import javax.inject.Singleton;
import kotlinx.coroutines.flow.Flow;
import java.util.Date;

/**
 * RCS (Rich Communication Services) implementation
 * Provides a proprietary protocol similar to Google Messages RCS
 */
@javax.inject.Singleton
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000L\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\t\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\t\n\u0002\u0010\u000b\n\u0002\b\u0006\b\u0007\u0018\u00002\u00020\u0001:\u0001$B\u0019\b\u0007\u0012\b\b\u0001\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\u0002\u0010\u0006J!\u0010\u0007\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\n2\u0006\u0010\u000b\u001a\u00020\fH\u0086@\u00f8\u0001\u0000\u00a2\u0006\u0002\u0010\rJ\u0019\u0010\u000e\u001a\u00020\u000f2\u0006\u0010\u0010\u001a\u00020\fH\u0086@\u00f8\u0001\u0000\u00a2\u0006\u0002\u0010\u0011J\u001a\u0010\u0012\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00150\u00140\u00132\u0006\u0010\u0016\u001a\u00020\nJ9\u0010\u0017\u001a\u00020\n2\u0006\u0010\u0010\u001a\u00020\f2\u0006\u0010\u0018\u001a\u00020\f2\u000e\b\u0002\u0010\u0019\u001a\b\u0012\u0004\u0012\u00020\f0\u00142\u0006\u0010\u0016\u001a\u00020\nH\u0086@\u00f8\u0001\u0000\u00a2\u0006\u0002\u0010\u001aJ\u0019\u0010\u001b\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\nH\u0086@\u00f8\u0001\u0000\u00a2\u0006\u0002\u0010\u001cJ!\u0010\u001d\u001a\u00020\b2\u0006\u0010\u0010\u001a\u00020\f2\u0006\u0010\u001e\u001a\u00020\u001fH\u0086@\u00f8\u0001\u0000\u00a2\u0006\u0002\u0010 J)\u0010!\u001a\u00020\b2\u0006\u0010\u0010\u001a\u00020\f2\u0006\u0010\"\u001a\u00020\f2\u0006\u0010\u0016\u001a\u00020\nH\u0086@\u00f8\u0001\u0000\u00a2\u0006\u0002\u0010#R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u0082\u0002\u0004\n\u0002\b\u0019\u00a8\u0006%"}, d2 = {"Lcom/nexussms/features/rcs/RcsService;", "", "context", "Landroid/content/Context;", "messageRepository", "Lcom/nexussms/data/repository/MessageRepository;", "(Landroid/content/Context;Lcom/nexussms/data/repository/MessageRepository;)V", "addReaction", "", "messageId", "", "reaction", "", "(JLjava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "checkRcsCapability", "Lcom/nexussms/features/rcs/RcsService$RcsCapability;", "phoneNumber", "(Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getRcsMessages", "Lkotlinx/coroutines/flow/Flow;", "", "Lcom/nexussms/data/models/Message;", "conversationId", "sendRcsMessage", "content", "attachments", "(Ljava/lang/String;Ljava/lang/String;Ljava/util/List;JLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "sendReadReceipt", "(JLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "sendTypingIndicator", "isTyping", "", "(Ljava/lang/String;ZLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "shareSticker", "stickerId", "(Ljava/lang/String;Ljava/lang/String;JLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "RcsCapability", "app_debug"})
public final class RcsService {
    @org.jetbrains.annotations.NotNull
    private final android.content.Context context = null;
    @org.jetbrains.annotations.NotNull
    private final com.nexussms.data.repository.MessageRepository messageRepository = null;
    
    @javax.inject.Inject
    public RcsService(@dagger.hilt.android.qualifiers.ApplicationContext
    @org.jetbrains.annotations.NotNull
    android.content.Context context, @org.jetbrains.annotations.NotNull
    com.nexussms.data.repository.MessageRepository messageRepository) {
        super();
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.Object sendRcsMessage(@org.jetbrains.annotations.NotNull
    java.lang.String phoneNumber, @org.jetbrains.annotations.NotNull
    java.lang.String content, @org.jetbrains.annotations.NotNull
    java.util.List<java.lang.String> attachments, long conversationId, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super java.lang.Long> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.Object sendTypingIndicator(@org.jetbrains.annotations.NotNull
    java.lang.String phoneNumber, boolean isTyping, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.Object sendReadReceipt(long messageId, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.Object addReaction(long messageId, @org.jetbrains.annotations.NotNull
    java.lang.String reaction, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.Object shareSticker(@org.jetbrains.annotations.NotNull
    java.lang.String phoneNumber, @org.jetbrains.annotations.NotNull
    java.lang.String stickerId, long conversationId, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.Object checkRcsCapability(@org.jetbrains.annotations.NotNull
    java.lang.String phoneNumber, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super com.nexussms.features.rcs.RcsService.RcsCapability> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final kotlinx.coroutines.flow.Flow<java.util.List<com.nexussms.data.models.Message>> getRcsMessages(long conversationId) {
        return null;
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000 \n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u001a\n\u0002\u0010\b\n\u0002\b\u0002\b\u0086\b\u0018\u00002\u00020\u0001B=\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0005\u0012\u0006\u0010\u0007\u001a\u00020\u0005\u0012\u0006\u0010\b\u001a\u00020\u0005\u0012\u0006\u0010\t\u001a\u00020\u0005\u0012\u0006\u0010\n\u001a\u00020\u0005\u00a2\u0006\u0002\u0010\u000bJ\t\u0010\u0015\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u0016\u001a\u00020\u0005H\u00c6\u0003J\t\u0010\u0017\u001a\u00020\u0005H\u00c6\u0003J\t\u0010\u0018\u001a\u00020\u0005H\u00c6\u0003J\t\u0010\u0019\u001a\u00020\u0005H\u00c6\u0003J\t\u0010\u001a\u001a\u00020\u0005H\u00c6\u0003J\t\u0010\u001b\u001a\u00020\u0005H\u00c6\u0003JO\u0010\u001c\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00052\b\b\u0002\u0010\u0006\u001a\u00020\u00052\b\b\u0002\u0010\u0007\u001a\u00020\u00052\b\b\u0002\u0010\b\u001a\u00020\u00052\b\b\u0002\u0010\t\u001a\u00020\u00052\b\b\u0002\u0010\n\u001a\u00020\u0005H\u00c6\u0001J\u0013\u0010\u001d\u001a\u00020\u00052\b\u0010\u001e\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010\u001f\u001a\u00020 H\u00d6\u0001J\t\u0010!\u001a\u00020\u0003H\u00d6\u0001R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\f\u0010\rR\u0011\u0010\n\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000e\u0010\u000fR\u0011\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0010\u0010\u000fR\u0011\u0010\b\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0011\u0010\u000fR\u0011\u0010\u0007\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0012\u0010\u000fR\u0011\u0010\t\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0013\u0010\u000fR\u0011\u0010\u0006\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0014\u0010\u000f\u00a8\u0006\""}, d2 = {"Lcom/nexussms/features/rcs/RcsService$RcsCapability;", "", "phoneNumber", "", "supportsRcs", "", "supportsTypingIndicator", "supportsReadReceipt", "supportsReactions", "supportsStickers", "supportsGiphy", "(Ljava/lang/String;ZZZZZZ)V", "getPhoneNumber", "()Ljava/lang/String;", "getSupportsGiphy", "()Z", "getSupportsRcs", "getSupportsReactions", "getSupportsReadReceipt", "getSupportsStickers", "getSupportsTypingIndicator", "component1", "component2", "component3", "component4", "component5", "component6", "component7", "copy", "equals", "other", "hashCode", "", "toString", "app_debug"})
    public static final class RcsCapability {
        @org.jetbrains.annotations.NotNull
        private final java.lang.String phoneNumber = null;
        private final boolean supportsRcs = false;
        private final boolean supportsTypingIndicator = false;
        private final boolean supportsReadReceipt = false;
        private final boolean supportsReactions = false;
        private final boolean supportsStickers = false;
        private final boolean supportsGiphy = false;
        
        public RcsCapability(@org.jetbrains.annotations.NotNull
        java.lang.String phoneNumber, boolean supportsRcs, boolean supportsTypingIndicator, boolean supportsReadReceipt, boolean supportsReactions, boolean supportsStickers, boolean supportsGiphy) {
            super();
        }
        
        @org.jetbrains.annotations.NotNull
        public final java.lang.String getPhoneNumber() {
            return null;
        }
        
        public final boolean getSupportsRcs() {
            return false;
        }
        
        public final boolean getSupportsTypingIndicator() {
            return false;
        }
        
        public final boolean getSupportsReadReceipt() {
            return false;
        }
        
        public final boolean getSupportsReactions() {
            return false;
        }
        
        public final boolean getSupportsStickers() {
            return false;
        }
        
        public final boolean getSupportsGiphy() {
            return false;
        }
        
        @org.jetbrains.annotations.NotNull
        public final java.lang.String component1() {
            return null;
        }
        
        public final boolean component2() {
            return false;
        }
        
        public final boolean component3() {
            return false;
        }
        
        public final boolean component4() {
            return false;
        }
        
        public final boolean component5() {
            return false;
        }
        
        public final boolean component6() {
            return false;
        }
        
        public final boolean component7() {
            return false;
        }
        
        @org.jetbrains.annotations.NotNull
        public final com.nexussms.features.rcs.RcsService.RcsCapability copy(@org.jetbrains.annotations.NotNull
        java.lang.String phoneNumber, boolean supportsRcs, boolean supportsTypingIndicator, boolean supportsReadReceipt, boolean supportsReactions, boolean supportsStickers, boolean supportsGiphy) {
            return null;
        }
        
        @java.lang.Override
        public boolean equals(@org.jetbrains.annotations.Nullable
        java.lang.Object other) {
            return false;
        }
        
        @java.lang.Override
        public int hashCode() {
            return 0;
        }
        
        @java.lang.Override
        @org.jetbrains.annotations.NotNull
        public java.lang.String toString() {
            return null;
        }
    }
}