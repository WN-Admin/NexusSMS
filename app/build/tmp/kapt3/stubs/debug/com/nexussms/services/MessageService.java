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

@dagger.hilt.android.AndroidEntryPoint
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000J\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0010 \n\u0002\b\u0004\b\u0007\u0018\u00002\u00020\u0001:\u0001!B\u0005\u00a2\u0006\u0002\u0010\u0002J\u0012\u0010\u0013\u001a\u00020\u00142\b\u0010\u0015\u001a\u0004\u0018\u00010\u0016H\u0016J\u0016\u0010\u0017\u001a\u00020\u00182\u0006\u0010\u0019\u001a\u00020\u001a2\u0006\u0010\u001b\u001a\u00020\u001aJ&\u0010\u001c\u001a\u00020\u00182\u0006\u0010\u0019\u001a\u00020\u001a2\u0006\u0010\u001b\u001a\u00020\u001a2\u000e\b\u0002\u0010\u001d\u001a\b\u0012\u0004\u0012\u00020\u001a0\u001eJ \u0010\u001f\u001a\u00020\u00182\u0006\u0010\u0019\u001a\u00020\u001a2\u0006\u0010\u001b\u001a\u00020\u001a2\b\b\u0002\u0010 \u001a\u00020\u001aR\u0012\u0010\u0003\u001a\u00060\u0004R\u00020\u0000X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001e\u0010\u0005\u001a\u00020\u00068\u0006@\u0006X\u0087.\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0007\u0010\b\"\u0004\b\t\u0010\nR\u001e\u0010\u000b\u001a\u00020\f8\u0006@\u0006X\u0087.\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\r\u0010\u000e\"\u0004\b\u000f\u0010\u0010R\u000e\u0010\u0011\u001a\u00020\u0012X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\""}, d2 = {"Lcom/nexussms/services/MessageService;", "Landroid/app/Service;", "()V", "binder", "Lcom/nexussms/services/MessageService$MessageServiceBinder;", "encryptionManager", "Lcom/nexussms/security/EncryptionManager;", "getEncryptionManager", "()Lcom/nexussms/security/EncryptionManager;", "setEncryptionManager", "(Lcom/nexussms/security/EncryptionManager;)V", "messageRepository", "Lcom/nexussms/data/repository/MessageRepository;", "getMessageRepository", "()Lcom/nexussms/data/repository/MessageRepository;", "setMessageRepository", "(Lcom/nexussms/data/repository/MessageRepository;)V", "scope", "Lkotlinx/coroutines/CoroutineScope;", "onBind", "Landroid/os/IBinder;", "intent", "Landroid/content/Intent;", "sendEncryptedMessage", "", "phoneNumber", "", "content", "sendRCSMessage", "attachments", "", "sendSMS", "encryptionType", "MessageServiceBinder", "app_debug"})
public final class MessageService extends android.app.Service {
    @javax.inject.Inject
    public com.nexussms.data.repository.MessageRepository messageRepository;
    @javax.inject.Inject
    public com.nexussms.security.EncryptionManager encryptionManager;
    @org.jetbrains.annotations.NotNull
    private final com.nexussms.services.MessageService.MessageServiceBinder binder = null;
    @org.jetbrains.annotations.NotNull
    private final kotlinx.coroutines.CoroutineScope scope = null;
    
    public MessageService() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull
    public final com.nexussms.data.repository.MessageRepository getMessageRepository() {
        return null;
    }
    
    public final void setMessageRepository(@org.jetbrains.annotations.NotNull
    com.nexussms.data.repository.MessageRepository p0) {
    }
    
    @org.jetbrains.annotations.NotNull
    public final com.nexussms.security.EncryptionManager getEncryptionManager() {
        return null;
    }
    
    public final void setEncryptionManager(@org.jetbrains.annotations.NotNull
    com.nexussms.security.EncryptionManager p0) {
    }
    
    @java.lang.Override
    @org.jetbrains.annotations.NotNull
    public android.os.IBinder onBind(@org.jetbrains.annotations.Nullable
    android.content.Intent intent) {
        return null;
    }
    
    public final void sendSMS(@org.jetbrains.annotations.NotNull
    java.lang.String phoneNumber, @org.jetbrains.annotations.NotNull
    java.lang.String content, @org.jetbrains.annotations.NotNull
    java.lang.String encryptionType) {
    }
    
    public final void sendRCSMessage(@org.jetbrains.annotations.NotNull
    java.lang.String phoneNumber, @org.jetbrains.annotations.NotNull
    java.lang.String content, @org.jetbrains.annotations.NotNull
    java.util.List<java.lang.String> attachments) {
    }
    
    public final void sendEncryptedMessage(@org.jetbrains.annotations.NotNull
    java.lang.String phoneNumber, @org.jetbrains.annotations.NotNull
    java.lang.String content) {
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0012\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\b\u0086\u0004\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002J\u0006\u0010\u0003\u001a\u00020\u0004\u00a8\u0006\u0005"}, d2 = {"Lcom/nexussms/services/MessageService$MessageServiceBinder;", "Landroid/os/Binder;", "(Lcom/nexussms/services/MessageService;)V", "getService", "Lcom/nexussms/services/MessageService;", "app_debug"})
    public final class MessageServiceBinder extends android.os.Binder {
        
        public MessageServiceBinder() {
            super();
        }
        
        @org.jetbrains.annotations.NotNull
        public final com.nexussms.services.MessageService getService() {
            return null;
        }
    }
}