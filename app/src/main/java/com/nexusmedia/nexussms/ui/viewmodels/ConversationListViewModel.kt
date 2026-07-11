package com.nexusmedia.nexussms.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexusmedia.nexussms.data.models.Conversation
import com.nexusmedia.nexussms.data.repository.ConversationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ConversationListViewModel @Inject constructor(
    private val conversationRepository: ConversationRepository
) : ViewModel() {

    private val _conversationList = MutableStateFlow<List<Conversation>>(emptyList())
    val conversationList: StateFlow<List<Conversation>> = _conversationList.asStateFlow()

    private val _pinnedConversations = MutableStateFlow<List<Conversation>>(emptyList())
    val pinnedConversations: StateFlow<List<Conversation>> = _pinnedConversations.asStateFlow()

    private val _selectedConversation = MutableStateFlow<Conversation?>(null)
    val selectedConversation: StateFlow<Conversation?> = _selectedConversation.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        // Continuously observe conversation lists; toggle isLoading off on the first emission.
        conversationRepository.getAllConversations()
            .onEach {
                _conversationList.value = it
                _isLoading.value = false
            }
            .launchIn(viewModelScope)

        conversationRepository.getPinnedConversations()
            .onEach { _pinnedConversations.value = it }
            .launchIn(viewModelScope)
    }

    fun selectConversation(conversation: Conversation) {
        _selectedConversation.value = conversation
    }

    fun deleteConversation(conversationId: String) {
        viewModelScope.launch {
            conversationRepository.deleteConversationById(conversationId)
        }
    }

    fun pinConversation(conversationId: String) {
        viewModelScope.launch {
            val conversation = conversationRepository.getConversationById(conversationId)
            if (conversation != null) {
                conversationRepository.updateConversation(conversation.copy(isPinned = true))
            }
        }
    }

    fun unpinConversation(conversationId: String) {
        viewModelScope.launch {
            val conversation = conversationRepository.getConversationById(conversationId)
            if (conversation != null) {
                conversationRepository.updateConversation(conversation.copy(isPinned = false))
            }
        }
    }

    fun muteConversation(conversationId: String) {
        viewModelScope.launch {
            val conversation = conversationRepository.getConversationById(conversationId)
            if (conversation != null) {
                conversationRepository.updateConversation(conversation.copy(isMuted = true))
            }
        }
    }

    fun unmuteConversation(conversationId: String) {
        viewModelScope.launch {
            val conversation = conversationRepository.getConversationById(conversationId)
            if (conversation != null) {
                conversationRepository.updateConversation(conversation.copy(isMuted = false))
            }
        }
    }

    fun markAsRead(conversationId: String) {
        viewModelScope.launch {
            conversationRepository.clearUnreadCount(conversationId)
        }
    }
}
