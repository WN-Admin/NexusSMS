package com.nexussms.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.provider.Telephony;
import com.nexussms.data.models.Conversation;
import com.nexussms.data.models.Message;
import com.nexussms.data.repository.ConversationRepository;
import com.nexussms.data.repository.MessageRepository;
import dagger.hilt.android.AndroidEntryPoint;
import kotlinx.coroutines.Dispatchers;
import java.util.Date;
import javax.inject.Inject;

@dagger.hilt.android.AndroidEntryPoint()
@kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u0000.\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\b\u0007\u0018\u00002\u00020\u0001B\u0007\u00a2\u0006\u0004\b\u0002\u0010\u0003J\u001c\u0010\u0010\u001a\u00020\u00112\b\u0010\u0012\u001a\u0004\u0018\u00010\u00132\b\u0010\u0014\u001a\u0004\u0018\u00010\u0015H\u0016R\u001e\u0010\u0004\u001a\u00020\u00058\u0006@\u0006X\u0087.\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0006\u0010\u0007\"\u0004\b\b\u0010\tR\u001e\u0010\n\u001a\u00020\u000b8\u0006@\u0006X\u0087.\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\f\u0010\r\"\u0004\b\u000e\u0010\u000f\u00a8\u0006\u0016"}, d2 = {"Lcom/nexussms/receivers/SmsReceiver;", "Landroid/content/BroadcastReceiver;", "<init>", "()V", "messageRepository", "Lcom/nexussms/data/repository/MessageRepository;", "getMessageRepository", "()Lcom/nexussms/data/repository/MessageRepository;", "setMessageRepository", "(Lcom/nexussms/data/repository/MessageRepository;)V", "conversationRepository", "Lcom/nexussms/data/repository/ConversationRepository;", "getConversationRepository", "()Lcom/nexussms/data/repository/ConversationRepository;", "setConversationRepository", "(Lcom/nexussms/data/repository/ConversationRepository;)V", "onReceive", "", "context", "Landroid/content/Context;", "intent", "Landroid/content/Intent;", "app_debug"})
public final class SmsReceiver extends android.content.BroadcastReceiver {
    @javax.inject.Inject()
    public com.nexussms.data.repository.MessageRepository messageRepository;
    @javax.inject.Inject()
    public com.nexussms.data.repository.ConversationRepository conversationRepository;
    
    public SmsReceiver() {
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
    public final com.nexussms.data.repository.ConversationRepository getConversationRepository() {
        return null;
    }
    
    public final void setConversationRepository(@org.jetbrains.annotations.NotNull()
    com.nexussms.data.repository.ConversationRepository p0) {
    }
    
    @java.lang.Override()
    public void onReceive(@org.jetbrains.annotations.Nullable()
    android.content.Context context, @org.jetbrains.annotations.Nullable()
    android.content.Intent intent) {
    }
}