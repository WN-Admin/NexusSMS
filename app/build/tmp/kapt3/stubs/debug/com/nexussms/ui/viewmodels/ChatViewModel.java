package com.nexussms.ui.viewmodels;

import androidx.lifecycle.ViewModel;
import com.nexussms.data.models.Conversation;
import com.nexussms.data.models.Message;
import com.nexussms.data.repository.ConversationRepository;
import com.nexussms.data.repository.MessageRepository;
import com.nexussms.features.rcs.RcsService;
import com.nexussms.features.shortcodes.ShortcodeExpansionService;
import com.nexussms.security.EncryptionManager;
import dagger.hilt.android.lifecycle.HiltViewModel;
import kotlinx.coroutines.flow.StateFlow;
import java.util.Date;
import javax.inject.Inject;

@kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u0000n\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0010\u000b\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\t\n\u0002\b\r\b\u0007\u0018\u00002\u00020\u0001B1\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0007\u0012\u0006\u0010\b\u001a\u00020\t\u0012\u0006\u0010\n\u001a\u00020\u000b\u00a2\u0006\u0004\b\f\u0010\rJ\u000e\u0010\'\u001a\u00020(2\u0006\u0010)\u001a\u00020*J\u000e\u0010+\u001a\u00020(2\u0006\u0010,\u001a\u00020\u001bJ\u0016\u0010-\u001a\u00020(2\u0006\u0010)\u001a\u00020*2\u0006\u0010.\u001a\u00020\u001bJ\u000e\u0010/\u001a\u00020(2\u0006\u00100\u001a\u00020\u0011J\u000e\u00101\u001a\u00020(2\u0006\u0010)\u001a\u00020*J\u000e\u00102\u001a\u00020(2\u0006\u00103\u001a\u00020\u001bJ\u0016\u00104\u001a\u00020(2\u0006\u00105\u001a\u00020*2\u0006\u00106\u001a\u00020\u001bR\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0007X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\tX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\n\u001a\u00020\u000bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001a\u0010\u000e\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00110\u00100\u000fX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001d\u0010\u0012\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00110\u00100\u0013\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0014\u0010\u0015R\u0016\u0010\u0016\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u00170\u000fX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0019\u0010\u0018\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u00170\u0013\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0019\u0010\u0015R\u0014\u0010\u001a\u001a\b\u0012\u0004\u0012\u00020\u001b0\u000fX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010\u001c\u001a\b\u0012\u0004\u0012\u00020\u001b0\u0013\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001d\u0010\u0015R\u0014\u0010\u001e\u001a\b\u0012\u0004\u0012\u00020\u001f0\u000fX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010 \u001a\b\u0012\u0004\u0012\u00020\u001f0\u0013\u00a2\u0006\b\n\u0000\u001a\u0004\b \u0010\u0015R\u0014\u0010!\u001a\b\u0012\u0004\u0012\u00020\u001b0\u000fX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010\"\u001a\b\u0012\u0004\u0012\u00020\u001b0\u0013\u00a2\u0006\b\n\u0000\u001a\u0004\b#\u0010\u0015R\u0010\u0010$\u001a\u0004\u0018\u00010%X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010&\u001a\u0004\u0018\u00010%X\u0082\u000e\u00a2\u0006\u0002\n\u0000\u00a8\u00067"}, d2 = {"Lcom/nexussms/ui/viewmodels/ChatViewModel;", "Landroidx/lifecycle/ViewModel;", "messageRepository", "Lcom/nexussms/data/repository/MessageRepository;", "conversationRepository", "Lcom/nexussms/data/repository/ConversationRepository;", "shortcodeExpansionService", "Lcom/nexussms/features/shortcodes/ShortcodeExpansionService;", "rcsService", "Lcom/nexussms/features/rcs/RcsService;", "encryptionManager", "Lcom/nexussms/security/EncryptionManager;", "<init>", "(Lcom/nexussms/data/repository/MessageRepository;Lcom/nexussms/data/repository/ConversationRepository;Lcom/nexussms/features/shortcodes/ShortcodeExpansionService;Lcom/nexussms/features/rcs/RcsService;Lcom/nexussms/security/EncryptionManager;)V", "_messages", "Lkotlinx/coroutines/flow/MutableStateFlow;", "", "Lcom/nexussms/data/models/Message;", "messages", "Lkotlinx/coroutines/flow/StateFlow;", "getMessages", "()Lkotlinx/coroutines/flow/StateFlow;", "_currentConversation", "Lcom/nexussms/data/models/Conversation;", "currentConversation", "getCurrentConversation", "_messageText", "", "messageText", "getMessageText", "_isSending", "", "isSending", "_selectedMessageType", "selectedMessageType", "getSelectedMessageType", "conversationJob", "Lkotlinx/coroutines/Job;", "messagesJob", "loadConversation", "", "conversationId", "", "updateMessageText", "text", "sendMessage", "recipientPhone", "deleteMessage", "message", "markAsRead", "setMessageType", "type", "addReaction", "messageId", "reaction", "app_debug"})
@dagger.hilt.android.lifecycle.HiltViewModel()
public final class ChatViewModel extends androidx.lifecycle.ViewModel {
    @org.jetbrains.annotations.NotNull()
    private final com.nexussms.data.repository.MessageRepository messageRepository = null;
    @org.jetbrains.annotations.NotNull()
    private final com.nexussms.data.repository.ConversationRepository conversationRepository = null;
    @org.jetbrains.annotations.NotNull()
    private final com.nexussms.features.shortcodes.ShortcodeExpansionService shortcodeExpansionService = null;
    @org.jetbrains.annotations.NotNull()
    private final com.nexussms.features.rcs.RcsService rcsService = null;
    @org.jetbrains.annotations.NotNull()
    private final com.nexussms.security.EncryptionManager encryptionManager = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.util.List<com.nexussms.data.models.Message>> _messages = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.util.List<com.nexussms.data.models.Message>> messages = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<com.nexussms.data.models.Conversation> _currentConversation = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<com.nexussms.data.models.Conversation> currentConversation = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.String> _messageText = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.String> messageText = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.Boolean> _isSending = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> isSending = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.String> _selectedMessageType = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.String> selectedMessageType = null;
    @org.jetbrains.annotations.Nullable()
    private kotlinx.coroutines.Job conversationJob;
    @org.jetbrains.annotations.Nullable()
    private kotlinx.coroutines.Job messagesJob;
    
    @javax.inject.Inject()
    public ChatViewModel(@org.jetbrains.annotations.NotNull()
    com.nexussms.data.repository.MessageRepository messageRepository, @org.jetbrains.annotations.NotNull()
    com.nexussms.data.repository.ConversationRepository conversationRepository, @org.jetbrains.annotations.NotNull()
    com.nexussms.features.shortcodes.ShortcodeExpansionService shortcodeExpansionService, @org.jetbrains.annotations.NotNull()
    com.nexussms.features.rcs.RcsService rcsService, @org.jetbrains.annotations.NotNull()
    com.nexussms.security.EncryptionManager encryptionManager) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.util.List<com.nexussms.data.models.Message>> getMessages() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<com.nexussms.data.models.Conversation> getCurrentConversation() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.String> getMessageText() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> isSending() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.String> getSelectedMessageType() {
        return null;
    }
    
    public final void loadConversation(long conversationId) {
    }
    
    public final void updateMessageText(@org.jetbrains.annotations.NotNull()
    java.lang.String text) {
    }
    
    public final void sendMessage(long conversationId, @org.jetbrains.annotations.NotNull()
    java.lang.String recipientPhone) {
    }
    
    public final void deleteMessage(@org.jetbrains.annotations.NotNull()
    com.nexussms.data.models.Message message) {
    }
    
    public final void markAsRead(long conversationId) {
    }
    
    public final void setMessageType(@org.jetbrains.annotations.NotNull()
    java.lang.String type) {
    }
    
    public final void addReaction(long messageId, @org.jetbrains.annotations.NotNull()
    java.lang.String reaction) {
    }
}