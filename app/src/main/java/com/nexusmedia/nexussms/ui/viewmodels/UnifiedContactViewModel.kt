package com.nexusmedia.nexussms.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexusmedia.nexussms.data.models.UnifiedContact
import com.nexusmedia.nexussms.data.models.PlatformIdentity
import com.nexusmedia.nexussms.data.repository.UnifiedContactRepository
import com.nexusmedia.nexussms.data.repository.ConversationRepository
import com.nexusmedia.nexussms.data.models.Conversation
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UnifiedContactViewModel @Inject constructor(
    private val unifiedContactRepository: UnifiedContactRepository,
    private val conversationRepository: ConversationRepository
) : ViewModel() {

    private val _contact = MutableStateFlow<UnifiedContact?>(null)
    val contact: StateFlow<UnifiedContact?> = _contact.asStateFlow()

    private val _linkedConversations = MutableStateFlow<List<Conversation>>(emptyList())
    val linkedConversations: StateFlow<List<Conversation>> = _linkedConversations.asStateFlow()

    private val _allContacts = MutableStateFlow<List<UnifiedContact>>(emptyList())
    val allContacts: StateFlow<List<UnifiedContact>> = _allContacts.asStateFlow()

    val isEditing = MutableStateFlow(false)
    val editName = MutableStateFlow("")
    val showMergeDialog = MutableStateFlow(false)

    private val gson = Gson()

    fun loadContact(contactId: String) {
        viewModelScope.launch {
            _contact.value = unifiedContactRepository.getContactById(contactId)

            val c = _contact.value ?: return@launch
            val phones: List<String> = gson.fromJson(
                c.phoneNumbers,
                object : TypeToken<List<String>>() {}.type
            )
            val identities: List<PlatformIdentity> = gson.fromJson(
                c.platformIdentities,
                object : TypeToken<List<PlatformIdentity>>() {}.type
            )

            val allConversations = conversationRepository.getAllConversationsList()
            val linked = allConversations.filter { conv ->
                val participantPhones: List<String> = gson.fromJson(
                    conv.participantPhoneNumbers,
                    object : TypeToken<List<String>>() {}.type
                )
                phones.any { phone -> participantPhones.contains(phone) } ||
                        identities.any { id -> participantPhones.contains(id.id) }
            }

            _linkedConversations.value = linked.sortedByDescending { it.lastMessageTime }
        }

        viewModelScope.launch {
            unifiedContactRepository.getAllContacts().collect {
                _allContacts.value = it
            }
        }
    }

    fun startEdit() {
        isEditing.value = true
        editName.value = _contact.value?.displayName ?: ""
    }

    fun cancelEdit() {
        isEditing.value = false
        editName.value = ""
    }

    fun saveEdits() {
        val current = _contact.value ?: return
        viewModelScope.launch {
            unifiedContactRepository.updateContact(current.copy(displayName = editName.value))
            isEditing.value = false
            _contact.value = unifiedContactRepository.getContactById(current.id)
        }
    }

    fun updateNotes(notes: String) {
        val current = _contact.value ?: return
        viewModelScope.launch {
            unifiedContactRepository.updateContact(current.copy(notes = notes))
            _contact.value = unifiedContactRepository.getContactById(current.id)
        }
    }

    fun toggleFavorite() {
        val current = _contact.value ?: return
        viewModelScope.launch {
            unifiedContactRepository.toggleFavorite(current.id)
            _contact.value = unifiedContactRepository.getContactById(current.id)
        }
    }

    fun setHidden(hidden: Boolean) {
        val current = _contact.value ?: return
        viewModelScope.launch {
            unifiedContactRepository.setHidden(current.id, hidden)
            _contact.value = unifiedContactRepository.getContactById(current.id)
        }
    }

    fun deleteContact() {
        val current = _contact.value ?: return
        viewModelScope.launch {
            unifiedContactRepository.deleteContact(current)
        }
    }

    fun mergeWith(otherId: String) {
        val current = _contact.value ?: return
        viewModelScope.launch {
            unifiedContactRepository.mergeContacts(current.id, otherId)
            showMergeDialog.value = false
            _contact.value = unifiedContactRepository.getContactById(current.id)
        }
    }
}
