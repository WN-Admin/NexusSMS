package com.nexusmedia.nexussms.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexusmedia.nexussms.data.models.UnifiedContact
import com.nexusmedia.nexussms.data.repository.UnifiedContactRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UnifiedContactsListViewModel @Inject constructor(
    private val unifiedContactRepository: UnifiedContactRepository
) : ViewModel() {

    private val _contacts = MutableStateFlow<List<UnifiedContact>>(emptyList())
    val contacts: StateFlow<List<UnifiedContact>> = _contacts.asStateFlow()

    private val _favorites = MutableStateFlow<List<UnifiedContact>>(emptyList())
    val favorites: StateFlow<List<UnifiedContact>> = _favorites.asStateFlow()

    val searchQuery = MutableStateFlow("")
    val showHidden = MutableStateFlow(false)

    init {
        viewModelScope.launch {
            combine(
                searchQuery,
                showHidden
            ) { query, showHidden ->
                Pair(query, showHidden)
            }.collectLatest { (query, showHidden) ->
                val flow = if (query.isNotEmpty()) {
                    unifiedContactRepository.searchContacts(query)
                } else if (showHidden) {
                    unifiedContactRepository.getAllContacts()
                } else {
                    unifiedContactRepository.getVisibleContacts()
                }

                flow.collect { contactList ->
                    _contacts.value = contactList
                }
            }
        }

        viewModelScope.launch {
            unifiedContactRepository.getFavoriteContacts().collect {
                _favorites.value = it
            }
        }
    }

    fun toggleFavorite(contactId: String) {
        viewModelScope.launch {
            unifiedContactRepository.toggleFavorite(contactId)
        }
    }
}
