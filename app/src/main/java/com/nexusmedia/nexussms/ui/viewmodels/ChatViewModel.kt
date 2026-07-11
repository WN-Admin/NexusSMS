package com.nexusmedia.nexussms.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexusmedia.nexussms.data.models.Conversation
import com.nexusmedia.nexussms.data.models.Message
import com.nexusmedia.nexussms.data.repository.ConversationRepository
import com.nexusmedia.nexussms.data.repository.MessageRepository
import com.nexusmedia.nexussms.data.repository.ScheduledMessageRepository
import com.nexusmedia.nexussms.features.rcs.RcsService
import com.nexusmedia.nexussms.features.shortcodes.ShortcodeExpansionService
import com.nexusmedia.nexussms.security.EncryptionManager
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
    private val scheduledMessageRepository: ScheduledMessageRepository,
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

    private val _shortcutSuggestions = MutableStateFlow<List<ShortcodeExpansionService.ShortcutPreview>>(emptyList())
    val shortcutSuggestions: StateFlow<List<ShortcodeExpansionService.ShortcutPreview>> = _shortcutSuggestions.asStateFlow()

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
        checkForShortcutTriggers(text)
    }

    fun sendMessage(conversationId: String, recipientPhone: String) {
        viewModelScope.launch {
            _isSending.value = true
            try {
                var messageContent = _messageText.value
                messageContent = shortcodeExpansionService.expandMessage(messageContent)
                messageContent = encryptionManager.generateMessageSignature(messageContent)

                val attachments = _pendingAttachments.value
                val mediaUrlsStr = attachments.joinToString(",")

                when (_selectedMessageType.value) {
                    "SMS" -> {
                        val message = Message(
                            conversationId = conversationId,
                            senderPhoneNumber = "self",
                            recipientPhoneNumber = recipientPhone,
                            content = messageContent,
                            timestamp = System.currentTimeMillis(),
                            type = if (attachments.isNotEmpty()) "MMS" else "TEXT",
                            status = "SENT",
                            isEncrypted = true,
                            encryptionAlgorithm = "AES256",
                            mediaUrls = mediaUrlsStr
                        )
                        messageRepository.insertMessage(message)
                    }
                    "RCS" -> {
                        rcsService.sendRcsMessage(
                            recipientPhone,
                            messageContent,
                            attachments,
                            conversationId
                        )
                    }
                }

                _messageText.value = ""
                _pendingAttachments.value = emptyList()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isSending.value = false
            }
        }
    }

    fun sendLocation(conversationId: String, locationUrl: String) {
        viewModelScope.launch {
            _messageText.value = locationUrl
            val recipient = _currentConversation.value?.participantPhoneNumbers ?: ""
            sendMessage(conversationId, recipient)
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

    private val _pendingAttachments = MutableStateFlow<List<String>>(emptyList())
    val pendingAttachments: StateFlow<List<String>> = _pendingAttachments.asStateFlow()

    fun attachFile(uri: String) {
        viewModelScope.launch {
            _pendingAttachments.value = _pendingAttachments.value + uri
        }
    }

    fun attachImage(uri: String) {
        viewModelScope.launch {
            _pendingAttachments.value = _pendingAttachments.value + uri
        }
    }

    fun removeAttachment(uri: String) {
        _pendingAttachments.value = _pendingAttachments.value - uri
    }

    fun sendMessageWithSticker(conversationId: String, recipientPhone: String, stickerId: String) {
        viewModelScope.launch {
            _isSending.value = true
            try {
                rcsService.shareSticker(recipientPhone, stickerId, conversationId)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isSending.value = false
            }
        }
    }

    fun scheduleMessage(conversationId: String, recipientPhone: String, scheduleAt: Long) {
        viewModelScope.launch {
            val content = _messageText.value
            if (content.isBlank()) return@launch
            val scheduledMessage = com.nexusmedia.nexussms.data.models.ScheduledMessage(
                conversationId = conversationId,
                recipientPhoneNumber = recipientPhone,
                content = content,
                scheduledTime = scheduleAt,
                status = "PENDING"
            )
            scheduledMessageRepository.insertScheduledMessage(scheduledMessage)
            _messageText.value = ""
            _shortcutSuggestions.value = emptyList()
        }
    }

    private fun checkForShortcutTriggers(text: String) {
        viewModelScope.launch {
            val words = text.split(" ")
            val lastWord = words.lastOrNull() ?: ""
            if (lastWord.startsWith("!") || lastWord.startsWith("@")) {
                _shortcutSuggestions.value = shortcodeExpansionService.previewExpansions(lastWord)
            } else {
                _shortcutSuggestions.value = emptyList()
            }
        }
    }

    fun applyShortcutSuggestion(trigger: String, expansion: String) {
        val text = _messageText.value
        val words = text.split(" ")
        val lastWord = words.lastOrNull() ?: ""
        if (lastWord == trigger) {
            val prefix = text.substringBeforeLast(lastWord)
            _messageText.value = prefix + expansion
        }
        _shortcutSuggestions.value = emptyList()
    }

    companion object {
        private val SHORTCODE_PATTERN = listOf("!", "@")
    }
}
