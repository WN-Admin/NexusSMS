package com.nexusmedia.nexussms.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexusmedia.nexussms.data.models.Conversation
import com.nexusmedia.nexussms.data.models.Message
import com.nexusmedia.nexussms.data.repository.ConversationRepository
import com.nexusmedia.nexussms.data.repository.MessageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SearchResult(
    val conversations: List<Conversation> = emptyList(),
    val messages: List<Message> = emptyList()
)

@OptIn(FlowPreview::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val conversationRepository: ConversationRepository,
    private val messageRepository: MessageRepository
) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _results = MutableStateFlow(SearchResult())
    val results: StateFlow<SearchResult> = _results.asStateFlow()

    init {
        viewModelScope.launch {
            _query
                .debounce(250)
                .distinctUntilChanged()
                .flatMapLatest { q ->
                    if (q.length < 2) {
                        flowOf(SearchResult())
                    } else {
                        kotlinx.coroutines.flow.combine(
                            conversationRepository.searchConversations(q),
                            messageRepository.searchMessages(q)
                        ) { convos, messages ->
                            SearchResult(convos, messages)
                        }
                    }
                }
                .collect { _results.value = it }
        }
    }

    fun setQuery(value: String) {
        _query.value = value
    }
}
