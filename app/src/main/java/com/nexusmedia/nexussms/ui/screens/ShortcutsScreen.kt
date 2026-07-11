package com.nexusmedia.nexussms.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.SortByAlpha
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexusmedia.nexussms.data.models.Shortcut
import com.nexusmedia.nexussms.data.repository.ShortcutRepository
import com.nexusmedia.nexussms.utils.Validators
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

sealed class ShortcutDialogState {
    object Hidden : ShortcutDialogState()
    data class AddEdit(val shortcut: Shortcut? = null) : ShortcutDialogState()
    data class Delete(val shortcut: Shortcut) : ShortcutDialogState()
}

@HiltViewModel
class ShortcutsViewModel @Inject constructor(
    private val shortcutRepository: ShortcutRepository
) : ViewModel() {

    private val _shortcuts = MutableStateFlow<List<Shortcut>>(emptyList())
    val shortcuts: StateFlow<List<Shortcut>> = _shortcuts.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _sortByUsage = MutableStateFlow(false)
    val sortByUsage: StateFlow<Boolean> = _sortByUsage.asStateFlow()

    private val _dialogState = MutableStateFlow<ShortcutDialogState>(ShortcutDialogState.Hidden)
    val dialogState: StateFlow<ShortcutDialogState> = _dialogState.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    val filteredShortcuts: StateFlow<List<Shortcut>> = combine(
        _shortcuts, _searchQuery, _sortByUsage
    ) { shortcuts, query, sortByUsage ->
        val filtered = if (query.isBlank()) shortcuts
        else shortcuts.filter {
            it.trigger.contains(query, ignoreCase = true) ||
                    it.expansion.contains(query, ignoreCase = true) ||
                    it.description.contains(query, ignoreCase = true)
        }
        if (sortByUsage) filtered.sortedByDescending { it.usageCount } else filtered
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        shortcutRepository.getGlobalShortcuts()
            .onEach {
                _shortcuts.value = it
                _isLoading.value = false
            }
            .launchIn(viewModelScope)
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleSortByUsage() {
        _sortByUsage.value = !_sortByUsage.value
    }

    fun showAddDialog() {
        _dialogState.value = ShortcutDialogState.AddEdit(null)
    }

    fun showEditDialog(shortcut: Shortcut) {
        _dialogState.value = ShortcutDialogState.AddEdit(shortcut)
    }

    fun showDeleteDialog(shortcut: Shortcut) {
        _dialogState.value = ShortcutDialogState.Delete(shortcut)
    }

    fun hideDialog() {
        _dialogState.value = ShortcutDialogState.Hidden
    }

    fun saveShortcut(
        existingId: String?,
        trigger: String,
        expansion: String,
        description: String,
        category: String,
        isActive: Boolean
    ) {
        viewModelScope.launch {
            if (existingId != null) {
                val existing = _shortcuts.value.find { it.id == existingId } ?: return@launch
                shortcutRepository.updateShortcut(
                    existing.copy(
                        trigger = trigger,
                        expansion = expansion,
                        description = description,
                        category = category,
                        isActive = isActive,
                        updatedAt = System.currentTimeMillis()
                    )
                )
            } else {
                shortcutRepository.insertShortcut(
                    Shortcut(
                        trigger = trigger,
                        expansion = expansion,
                        description = description,
                        category = category,
                        isActive = isActive
                    )
                )
            }
            hideDialog()
        }
    }

    fun deleteShortcut(shortcut: Shortcut) {
        viewModelScope.launch {
            shortcutRepository.deleteShortcut(shortcut)
            hideDialog()
        }
    }

    fun toggleActive(shortcut: Shortcut) {
        viewModelScope.launch {
            shortcutRepository.updateShortcut(
                shortcut.copy(isActive = !shortcut.isActive, updatedAt = System.currentTimeMillis())
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShortcutsScreen(
    viewModel: ShortcutsViewModel = hiltViewModel()
) {
    val filteredShortcuts by viewModel.filteredShortcuts.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val sortByUsage by viewModel.sortByUsage.collectAsState()
    val dialogState by viewModel.dialogState.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Quick Shortcuts") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.showAddDialog() }) {
                Icon(Icons.Default.Add, contentDescription = "Add Shortcut")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.updateSearchQuery(it) },
                    placeholder = { Text("Search shortcuts...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { viewModel.toggleSortByUsage() }) {
                    Icon(
                        Icons.Default.SortByAlpha,
                        contentDescription = if (sortByUsage) "Sort alphabetically" else "Sort by usage",
                        tint = if (sortByUsage) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (isLoading) {
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "Loading shortcuts...",
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.weight(1f))
            } else if (filteredShortcuts.isEmpty()) {
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = if (searchQuery.isNotBlank()) "No shortcuts match your search" else "No shortcuts yet",
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.weight(1f))
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredShortcuts, key = { it.id }) { shortcut ->
                        ShortcutItem(
                            shortcut = shortcut,
                            onEdit = { viewModel.showEditDialog(shortcut) },
                            onDelete = { viewModel.showDeleteDialog(shortcut) },
                            onToggleActive = { viewModel.toggleActive(shortcut) }
                        )
                    }
                }
            }
        }
    }

    when (val state = dialogState) {
        is ShortcutDialogState.AddEdit -> ShortcutEditDialog(
            shortcut = state.shortcut,
            onSave = { id, trigger, expansion, description, category, isActive ->
                viewModel.saveShortcut(id, trigger, expansion, description, category, isActive)
            },
            onDismiss = { viewModel.hideDialog() }
        )
        is ShortcutDialogState.Delete -> AlertDialog(
            onDismissRequest = { viewModel.hideDialog() },
            title = { Text("Delete Shortcut") },
            text = { Text("Are you sure you want to delete \"${state.shortcut.trigger}\"?") },
            confirmButton = {
                TextButton(onClick = { viewModel.deleteShortcut(state.shortcut) }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.hideDialog() }) {
                    Text("Cancel")
                }
            }
        )
        ShortcutDialogState.Hidden -> { /* no dialog */ }
    }
}

@Composable
private fun ShortcutItem(
    shortcut: Shortcut,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggleActive: () -> Unit
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onEdit() }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = shortcut.trigger,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "→ ${shortcut.expansion}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (shortcut.description.isNotBlank()) {
                    Text(
                        text = shortcut.description,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = shortcut.category,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Used ${shortcut.usageCount} times",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Switch(
                    checked = shortcut.isActive,
                    onCheckedChange = { onToggleActive() }
                )
                Spacer(modifier = Modifier.width(4.dp))
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit")
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
        HorizontalDivider()
    }
}

@Composable
private fun ShortcutEditDialog(
    shortcut: Shortcut?,
    onSave: (existingId: String?, trigger: String, expansion: String, description: String, category: String, isActive: Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    var trigger by remember(shortcut) { mutableStateOf(shortcut?.trigger ?: "") }
    var expansion by remember(shortcut) { mutableStateOf(shortcut?.expansion ?: "") }
    var description by remember(shortcut) { mutableStateOf(shortcut?.description ?: "") }
    var category by remember(shortcut) { mutableStateOf(shortcut?.category ?: "General") }
    var isActive by remember(shortcut) { mutableStateOf(shortcut?.isActive ?: true) }
    var triggerError by remember { mutableStateOf(false) }
    var expansionError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (shortcut != null) "Edit Shortcut" else "Add Shortcut") },
        text = {
            Column {
                OutlinedTextField(
                    value = trigger,
                    onValueChange = {
                        trigger = it
                        triggerError = false
                    },
                    label = { Text("Trigger") },
                    placeholder = { Text("!hello") },
                    isError = triggerError,
                    supportingText = if (triggerError) {
                        { Text("Must start with ! or @ and be at least 2 characters") }
                    } else null,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = expansion,
                    onValueChange = {
                        expansion = it
                        expansionError = false
                    },
                    label = { Text("Expansion") },
                    placeholder = { Text("Hello, how are you?") },
                    isError = expansionError,
                    supportingText = if (expansionError) {
                        { Text("Expansion cannot be empty") }
                    } else null,
                    singleLine = false,
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Category") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Active", style = MaterialTheme.typography.bodyMedium)
                    Switch(checked = isActive, onCheckedChange = { isActive = it })
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    triggerError = !Validators.isValidShortcutTrigger(trigger)
                    expansionError = !Validators.isNonBlank(expansion)
                    if (!triggerError && !expansionError) {
                        onSave(shortcut?.id, trigger.trim(), expansion.trim(), description.trim(), category.trim(), isActive)
                    }
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
