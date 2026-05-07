package com.nexussms.services;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.telephony.SmsManager;
import com.nexussms.data.models.Message;
import com.nexussms.data.repository.MessageRepository;
import com.nexussms.security.EncryptionManager;
import dagger.hilt.android.AndroidEntryPoint;
import kotlinx.coroutines.Dispatchers;
import javax.inject.Inject;
import java.util.Date;

@dagger.hilt.android.AndroidEntryPoint()
@kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u0000J\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0004\n\u0002\u0010 \n\u0002\b\u0003\b\u0007\u0018\u00002\u00020\u0001:\u0001\"B\u0007\u00a2\u0006\u0004\b\u0002\u0010\u0003J\u0012\u0010\u0014\u001a\u00020\u00152\b\u0010\u0016\u001a\u0004\u0018\u00010\u0017H\u0016J \u0010\u0018\u001a\u00020\u00192\u0006\u0010\u001a\u001a\u00020\u001b2\u0006\u0010\u001c\u001a\u00020\u001b2\b\b\u0002\u0010\u001d\u001a\u00020\u001bJ&\u0010\u001e\u001a\u00020\u00192\u0006\u0010\u001a\u001a\u00020\u001b2\u0006\u0010\u001c\u001a\u00020\u001b2\u000e\b\u0002\u0010\u001f\u001a\b\u0012\u0004\u0012\u00020\u001b0 J\u0016\u0010!\u001a\u00020\u00192\u0006\u0010\u001a\u001a\u00020\u001b2\u0006\u0010\u001c\u001a\u00020\u001bR\u001e\u0010\u0004\u001a\u00020\u00058\u0006@\u0006X\u0087.\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0006\u0010\u0007\"\u0004\b\b\u0010\tR\u001e\u0010\n\u001a\u00020\u000b8\u0006@\u0006X\u0087.\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\f\u0010\r\"\u0004\b\u000e\u0010\u000fR\u0012\u0010\u0010\u001a\u00060\u0011R\u00020\u0000X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0012\u001a\u00020\u0013X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006#"}, d2 = {"Lcom/nexussms/services/MessageService;", "Landroid/app/Service;", "<init>", "()V", "messageRepository", "Lcom/nexussms/data/repository/MessageRepository;", "getMessageRepository", "()Lcom/nexussms/data/repository/MessageRepository;", "setMessageRepository", "(Lcom/nexussms/data/repository/MessageRepository;)V", "encryptionManager", "Lcom/nexussms/security/EncryptionManager;", "getEncryptionManager", "()Lcom/nexussms/security/EncryptionManager;", "setEncryptionManager", "(Lcom/nexussms/security/EncryptionManager;)V", "binder", "Lcom/nexussms/services/MessageService$MessageServiceBinder;", "scope", "Lkotlinx/coroutines/CoroutineScope;", "onBind", "Landroid/os/IBinder;", "intent", "Landroid/content/Intent;", "sendSMS", "", "phoneNumber", "", "content", "encryptionType", "sendRCSMessage", "attachments", "", "sendEncryptedMessage", "MessageServiceBinder", "app_debug"})
public final class MessageService extends android.app.Service {
    @javax.inject.Inject()
    public com.nexussms.data.repository.MessageRepository messageRepository;
    @javax.inject.Inject()
    public com.nexussms.security.EncryptionManager encryptionManager;
    @org.jetbrains.annotations.NotNull()
    private final com.nexussms.services.MessageService.MessageServiceBinder binder = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.CoroutineScope scope = null;
    
    public MessageService() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.nexussms.data.repository.MessageRepository getMessageRepository() {
        return null;
    }
    
    public final void setMessageRepository(@org.jetbrains.annotations.NotNull()
    com.nexussms.data.repository.MessageRepository p0) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.nexussms.security.EncryptionManager getEncryptionManager() {
        return null;
    }
    
    public final void setEncryptionManager(@org.jetbrains.annotations.NotNull()
    com.nexussms.security.EncryptionManager p0) {
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public android.os.IBinder onBind(@org.jetbrains.annotations.Nullable()
    android.content.Intent intent) {
        return null;
    }
    
    public final void sendSMS(@org.jetbrains.annotations.NotNull()
    java.lang.String phoneNumber, @org.jetbrains.annotations.NotNull()
    java.lang.String content, @org.jetbrains.annotations.NotNull()
    java.lang.String encryptionType) {
    }
    
    public final void sendRCSMessage(@org.jetbrains.annotations.NotNull()
    java.lang.String phoneNumber, @org.jetbrains.annotations.NotNull()
    java.lang.String content, @org.jetbrains.annotations.NotNull()
    java.util.List<java.lang.String> attachments) {
    }
    
    public final void sendEncryptedMessage(@org.jetbrains.annotations.NotNull()
    java.lang.String phoneNumber, @org.jetbrains.annotations.NotNull()
    java.lang.String content) {
    }
    
    @kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u0000\u0012\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\b\u0086\u0004\u0018\u00002\u00020\u0001B\u0007\u00a2\u0006\u0004\b\u0002\u0010\u0003J\u0006\u0010\u0004\u001a\u00020\u0005\u00a8\u0006\u0006"}, d2 = {"Lcom/nexussms/services/MessageService$MessageServiceBinder;", "Landroid/os/Binder;", "<init>", "(Lcom/nexussms/services/MessageService;)V", "getService", "Lcom/nexussms/services/MessageService;", "app_debug"})
    public final class MessageServiceBinder extends android.os.Binder {
        
        public MessageServiceBinder() {
            super();
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.nexussms.services.MessageService getService() {
            return null;
        }
    }
}