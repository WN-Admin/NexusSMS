package com.nexusmedia.nexussms.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexusmedia.nexussms.data.models.Shortcut
import com.nexusmedia.nexussms.data.repository.ShortcutRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ShortcutsViewModel @Inject constructor(
    private val shortcutRepository: ShortcutRepository
) : ViewModel() {

    private val _shortcuts = MutableStateFlow<List<Shortcut>>(emptyList())
    val shortcuts: StateFlow<List<Shortcut>> = _shortcuts.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _showAddDialog = MutableStateFlow(false)
    val showAddDialog: StateFlow<Boolean> = _showAddDialog.asStateFlow()

    private val _editingShortcut = MutableStateFlow<Shortcut?>(null)
    val editingShortcut: StateFlow<Shortcut?> = _editingShortcut.asStateFlow()

    val filteredShortcuts: StateFlow<List<Shortcut>> = combine(
        _shortcuts, _searchQuery
    ) { shortcuts, query ->
        if (query.isBlank()) shortcuts
        else shortcuts.filter {
            it.trigger.contains(query, ignoreCase = true) ||
                it.expansion.contains(query, ignoreCase = true) ||
                it.description.contains(query, ignoreCase = true)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        shortcutRepository.getGlobalShortcuts()
            .onEach { _shortcuts.value = it }
            .launchIn(viewModelScope)
    }

    fun addShortcut(
        trigger: String,
        expansion: String,
        description: String,
        category: String
    ) {
        viewModelScope.launch {
            shortcutRepository.insertShortcut(
                Shortcut(
                    trigger = trigger,
                    expansion = expansion,
                    description = description,
                    category = category
                )
            )
            _showAddDialog.value = false
        }
    }

    fun updateShortcut(shortcut: Shortcut) {
        viewModelScope.launch {
            shortcutRepository.updateShortcut(shortcut)
            _showAddDialog.value = false
            _editingShortcut.value = null
        }
    }

    fun deleteShortcut(shortcut: Shortcut) {
        viewModelScope.launch {
            shortcutRepository.deleteShortcut(shortcut)
        }
    }

    fun toggleActive(shortcut: Shortcut) {
        viewModelScope.launch {
            shortcutRepository.updateShortcut(shortcut.copy(isActive = !shortcut.isActive))
        }
    }

    fun search(query: String) {
        _searchQuery.value = query
    }

    fun showAddDialog() {
        _showAddDialog.value = true
        _editingShortcut.value = null
    }

    fun showEditDialog(shortcut: Shortcut) {
        _editingShortcut.value = shortcut
        _showAddDialog.value = true
    }

    fun dismissDialog() {
        _showAddDialog.value = false
        _editingShortcut.value = null
    }
}
