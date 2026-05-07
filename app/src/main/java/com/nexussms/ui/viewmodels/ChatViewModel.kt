package com.nexussms.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexussms.data.models.Conversation
import com.nexussms.data.models.Message
import com.nexussms.data.repository.ConversationRepository
import com.nexussms.data.repository.MessageRepository
import com.nexussms.features.rcs.RcsService
import com.nexussms.features.shortcodes.ShortcodeExpansionService
import com.nexussms.security.EncryptionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val messageRepository: MessageRepository,
    private val conversationRepository: ConversationRepository,
    private val shortcodeExpansionService: ShortcodeExpansionService,
    private val rcsService: RcsService,
    private val encryptionManager: EncryptionManager
) : ViewModel() {

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    private val _currentConversation = MutableStateFlow<Conversation?>(null)
    val currentConversation: StateFlow<Conversation?> = _currentConversation.asStateFlow()

    private val _messageText = MutableStateFlow("")
    val messageText: StateFlow<String> = _messageText.asStateFlow()

    private val _isSending = MutableStateFlow(false)
    val isSending: StateFlow<Boolean> = _isSending.asStateFlow()

    private val _selectedMessageType = MutableStateFlow("SMS")
    val selectedMessageType: StateFlow<String> = _selectedMessageType.asStateFlow()

    private var conversationJob: Job? = null
    private var messagesJob: Job? = null

    fun loadConversation(conversationId: String) {
        // Cancel previous observers, then start fresh ones in parallel.
        conversationJob?.cancel()
        messagesJob?.cancel()

        conversationJob = conversationRepository.getAllConversations()
            .onEach { list -> 
                _currentConversation.value = list.find { it.id == conversationId }
            }
            .launchIn(viewModelScope)

        messagesJob = messageRepository.getConversationMessages(conversationId)
            .onEach { msgs -> _messages.value = msgs.reversed() }
            .launchIn(viewModelScope)
    }

    fun updateMessageText(text: String) {
        _messageText.value = text
    }

    fun sendMessage(conversationId: String, recipientPhone: String) {
        viewModelScope.launch {
            _isSending.value = true
            try {
                var messageContent = _messageText.value
                messageContent = shortcodeExpansionService.expandMessage(messageContent)
                messageContent = encryptionManager.generateMessageSignature(messageContent)

                when (_selectedMessageType.value) {
                    "SMS" -> {
                        val message = Message(
                            conversationId = conversationId,
                            senderPhoneNumber = "self",
                            recipientPhoneNumber = recipientPhone,
                            content = messageContent,
                            timestamp = System.currentTimeMillis(),
                            type = "TEXT",
                            status = "SENT",
                            isEncrypted = true,
                            encryptionAlgorithm = "AES256"
                        )
                        messageRepository.insertMessage(message)
                    }
                    "RCS" -> {
                        rcsService.sendRcsMessage(
                            recipientPhone,
                            messageContent,
                            emptyList(),
                            conversationId
                        )
                    }
                }

                _messageText.value = ""
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isSending.value = false
            }
        }
    }

    fun deleteMessage(message: Message) {
        viewModelScope.launch {
            messageRepository.deleteMessage(message)
        }
    }

    fun markAsRead(conversationId: String) {
        viewModelScope.launch {
            conversationRepository.markConversationAsRead(conversationId)
        }
    }

    fun setMessageType(type: String) {
        _selectedMessageType.value = type
    }

    fun addReaction(messageId: String, reaction: String) {
        viewModelScope.launch {
            val message = messageRepository.getMessageById(messageId)
            if (message != null) {
                val updatedMessage = message.copy(
                    reactions = reaction
                )
                messageRepository.updateMessage(updatedMessage)
            }
        }
    }
}
