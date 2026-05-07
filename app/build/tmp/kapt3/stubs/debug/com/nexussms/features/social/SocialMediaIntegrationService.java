package com.nexussms.features.social;

import com.nexussms.data.models.SocialAccount;
import com.nexussms.data.models.Message;
import com.nexussms.data.repository.SocialAccountRepository;
import com.nexussms.data.repository.MessageRepository;
import javax.inject.Inject;
import javax.inject.Singleton;
import kotlinx.coroutines.flow.Flow;
import java.util.Date;

/**
 * Supports integration with multiple social media platforms:
 * - Facebook Messenger
 * - Discord
 * - Telegram
 * - Viber
 * - Matrix
 */
@javax.inject.Singleton()
@kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u0000P\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\t\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\t\n\u0002\u0010 \n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0002\b\u0007\n\u0002\u0018\u0002\n\u0002\b\t\b\u0007\u0018\u00002\u00020\u0001:\u0001/B\u0019\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\u0004\b\u0006\u0010\u0007JB\u0010\b\u001a\u00020\t2\u0006\u0010\n\u001a\u00020\u000b2\u0006\u0010\f\u001a\u00020\r2\u0006\u0010\u000e\u001a\u00020\r2\u0006\u0010\u000f\u001a\u00020\r2\b\b\u0002\u0010\u0010\u001a\u00020\r2\b\b\u0002\u0010\u0011\u001a\u00020\rH\u0086@\u00a2\u0006\u0002\u0010\u0012J>\u0010\u0013\u001a\u00020\t2\u0006\u0010\n\u001a\u00020\u000b2\u0006\u0010\u0014\u001a\u00020\r2\u0006\u0010\u0015\u001a\u00020\r2\u000e\b\u0002\u0010\u0016\u001a\b\u0012\u0004\u0012\u00020\r0\u00172\u0006\u0010\u0018\u001a\u00020\tH\u0086@\u00a2\u0006\u0002\u0010\u0019J\u001a\u0010\u001a\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u001c0\u00170\u001b2\u0006\u0010\n\u001a\u00020\u000bJ\u0012\u0010\u001d\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u001c0\u00170\u001bJ\u0016\u0010\u001e\u001a\u00020\u001f2\u0006\u0010\f\u001a\u00020\rH\u0086@\u00a2\u0006\u0002\u0010 J\u0016\u0010!\u001a\u00020\u001f2\u0006\u0010\n\u001a\u00020\u000bH\u0086@\u00a2\u0006\u0002\u0010\"J\u001e\u0010#\u001a\u00020\u001f2\u0006\u0010\f\u001a\u00020\r2\u0006\u0010$\u001a\u00020\rH\u0086@\u00a2\u0006\u0002\u0010%J*\u0010&\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\'0\u00170\u001b2\u0006\u0010\n\u001a\u00020\u000b2\u0006\u0010\u0018\u001a\u00020\tH\u0086@\u00a2\u0006\u0002\u0010(J&\u0010)\u001a\u00020\t2\u0006\u0010\u0014\u001a\u00020\r2\u0006\u0010\u0015\u001a\u00020\r2\u0006\u0010\u0018\u001a\u00020\tH\u0086@\u00a2\u0006\u0002\u0010*J&\u0010+\u001a\u00020\t2\u0006\u0010\u0014\u001a\u00020\r2\u0006\u0010\u0015\u001a\u00020\r2\u0006\u0010\u0018\u001a\u00020\tH\u0086@\u00a2\u0006\u0002\u0010*J&\u0010,\u001a\u00020\t2\u0006\u0010\u0014\u001a\u00020\r2\u0006\u0010\u0015\u001a\u00020\r2\u0006\u0010\u0018\u001a\u00020\tH\u0086@\u00a2\u0006\u0002\u0010*J&\u0010-\u001a\u00020\t2\u0006\u0010\u0014\u001a\u00020\r2\u0006\u0010\u0015\u001a\u00020\r2\u0006\u0010\u0018\u001a\u00020\tH\u0086@\u00a2\u0006\u0002\u0010*J&\u0010.\u001a\u00020\t2\u0006\u0010\u0014\u001a\u00020\r2\u0006\u0010\u0015\u001a\u00020\r2\u0006\u0010\u0018\u001a\u00020\tH\u0086@\u00a2\u0006\u0002\u0010*R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u00060"}, d2 = {"Lcom/nexussms/features/social/SocialMediaIntegrationService;", "", "socialAccountRepository", "Lcom/nexussms/data/repository/SocialAccountRepository;", "messageRepository", "Lcom/nexussms/data/repository/MessageRepository;", "<init>", "(Lcom/nexussms/data/repository/SocialAccountRepository;Lcom/nexussms/data/repository/MessageRepository;)V", "connectAccount", "", "platform", "Lcom/nexussms/features/social/SocialMediaIntegrationService$SocialPlatform;", "accountId", "", "username", "accessToken", "refreshToken", "displayName", "(Lcom/nexussms/features/social/SocialMediaIntegrationService$SocialPlatform;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "sendSocialMediaMessage", "recipientId", "content", "attachments", "", "conversationId", "(Lcom/nexussms/features/social/SocialMediaIntegrationService$SocialPlatform;Ljava/lang/String;Ljava/lang/String;Ljava/util/List;JLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getConnectedAccounts", "Lkotlinx/coroutines/flow/Flow;", "Lcom/nexussms/data/models/SocialAccount;", "getAllConnectedAccounts", "disconnectAccount", "", "(Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "syncMessagesFromPlatform", "(Lcom/nexussms/features/social/SocialMediaIntegrationService$SocialPlatform;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "updateAccountToken", "newToken", "(Ljava/lang/String;Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getSocialMediaMessages", "Lcom/nexussms/data/models/Message;", "(Lcom/nexussms/features/social/SocialMediaIntegrationService$SocialPlatform;JLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "facebookMessengerSend", "(Ljava/lang/String;Ljava/lang/String;JLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "discordSend", "telegramSend", "viberSend", "matrixSend", "SocialPlatform", "app_debug"})
public final class SocialMediaIntegrationService {
    @org.jetbrains.annotations.NotNull()
    private final com.nexussms.data.repository.SocialAccountRepository socialAccountRepository = null;
    @org.jetbrains.annotations.NotNull()
    private final com.nexussms.data.repository.MessageRepository messageRepository = null;
    
    @javax.inject.Inject()
    public SocialMediaIntegrationService(@org.jetbrains.annotations.NotNull()
    com.nexussms.data.repository.SocialAccountRepository socialAccountRepository, @org.jetbrains.annotations.NotNull()
    com.nexussms.data.repository.MessageRepository messageRepository) {
        super();
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object connectAccount(@org.jetbrains.annotations.NotNull()
    com.nexussms.features.social.SocialMediaIntegrationService.SocialPlatform platform, @org.jetbrains.annotations.NotNull()
    java.lang.String accountId, @org.jetbrains.annotations.NotNull()
    java.lang.String username, @org.jetbrains.annotations.NotNull()
    java.lang.String accessToken, @org.jetbrains.annotations.NotNull()
    java.lang.String refreshToken, @org.jetbrains.annotations.NotNull()
    java.lang.String displayName, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.Long> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object sendSocialMediaMessage(@org.jetbrains.annotations.NotNull()
    com.nexussms.features.social.SocialMediaIntegrationService.SocialPlatform platform, @org.jetbrains.annotations.NotNull()
    java.lang.String recipientId, @org.jetbrains.annotations.NotNull()
    java.lang.String content, @org.jetbrains.annotations.NotNull()
    java.util.List<java.lang.String> attachments, long conversationId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.Long> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.Flow<java.util.List<com.nexussms.data.models.SocialAccount>> getConnectedAccounts(@org.jetbrains.annotations.NotNull()
    com.nexussms.features.social.SocialMediaIntegrationService.SocialPlatform platform) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.Flow<java.util.List<com.nexussms.data.models.SocialAccount>> getAllConnectedAccounts() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object disconnectAccount(@org.jetbrains.annotations.NotNull()
    java.lang.String accountId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object syncMessagesFromPlatform(@org.jetbrains.annotations.NotNull()
    com.nexussms.features.social.SocialMediaIntegrationService.SocialPlatform platform, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object updateAccountToken(@org.jetbrains.annotations.NotNull()
    java.lang.String accountId, @org.jetbrains.annotations.NotNull()
    java.lang.String newToken, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object getSocialMediaMessages(@org.jetbrains.annotations.NotNull()
    com.nexussms.features.social.SocialMediaIntegrationService.SocialPlatform platform, long conversationId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlinx.coroutines.flow.Flow<? extends java.util.List<com.nexussms.data.models.Message>>> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object facebookMessengerSend(@org.jetbrains.annotations.NotNull()
    java.lang.String recipientId, @org.jetbrains.annotations.NotNull()
    java.lang.String content, long conversationId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.Long> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object discordSend(@org.jetbrains.annotations.NotNull()
    java.lang.String recipientId, @org.jetbrains.annotations.NotNull()
    java.lang.String content, long conversationId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.Long> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object telegramSend(@org.jetbrains.annotations.NotNull()
    java.lang.String recipientId, @org.jetbrains.annotations.NotNull()
    java.lang.String content, long conversationId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.Long> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object viberSend(@org.jetbrains.annotations.NotNull()
    java.lang.String recipientId, @org.jetbrains.annotations.NotNull()
    java.lang.String content, long conversationId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.Long> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object matrixSend(@org.jetbrains.annotations.NotNull()
    java.lang.String recipientId, @org.jetbrains.annotations.NotNull()
    java.lang.String content, long conversationId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.Long> $completion) {
        return null;
    }
    
    @kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0010\u0010\n\u0002\b\b\b\u0086\u0081\u0002\u0018\u00002\b\u0012\u0004\u0012\u00020\u00000\u0001B\t\b\u0002\u00a2\u0006\u0004\b\u0002\u0010\u0003j\u0002\b\u0004j\u0002\b\u0005j\u0002\b\u0006j\u0002\b\u0007j\u0002\b\b\u00a8\u0006\t"}, d2 = {"Lcom/nexussms/features/social/SocialMediaIntegrationService$SocialPlatform;", "", "<init>", "(Ljava/lang/String;I)V", "FACEBOOK_MESSENGER", "DISCORD", "TELEGRAM", "VIBER", "MATRIX", "app_debug"})
    public static enum SocialPlatform {
        /*public static final*/ FACEBOOK_MESSENGER /* = new FACEBOOK_MESSENGER() */,
        /*public static final*/ DISCORD /* = new DISCORD() */,
        /*public static final*/ TELEGRAM /* = new TELEGRAM() */,
        /*public static final*/ VIBER /* = new VIBER() */,
        /*public static final*/ MATRIX /* = new MATRIX() */;
        
        SocialPlatform() {
        }
        
        @org.jetbrains.annotations.NotNull()
        public static kotlin.enums.EnumEntries<com.nexussms.features.social.SocialMediaIntegrationService.SocialPlatform> getEntries() {
            return null;
        }
    }
}