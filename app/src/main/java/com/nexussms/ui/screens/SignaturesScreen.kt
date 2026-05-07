package com.nexussms.ui.screens

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexussms.data.models.Signature
import com.nexussms.data.repository.SignatureRepository
import com.nexussms.utils.Validators
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class SignatureDialogState {
    object Hidden : SignatureDialogState()
    data class AddEdit(val signature: Signature? = null) : SignatureDialogState()
    data class Delete(val signature: Signature) : SignatureDialogState()
}

@HiltViewModel
class SignaturesViewModel @Inject constructor(
    private val signatureRepository: SignatureRepository
) : ViewModel() {

    private val _signatures = MutableStateFlow<List<Signature>>(emptyList())
    val signatures: StateFlow<List<Signature>> = _signatures.asStateFlow()

    private val _dialogState = MutableStateFlow<SignatureDialogState>(SignatureDialogState.Hidden)
    val dialogState: StateFlow<SignatureDialogState> = _dialogState.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        signatureRepository.getAllSignatures()
            .onEach {
                _signatures.value = it
                _isLoading.value = false
            }
            .launchIn(viewModelScope)
    }

    fun showAddDialog() {
        _dialogState.value = SignatureDialogState.AddEdit(null)
    }

    fun showEditDialog(signature: Signature) {
        _dialogState.value = SignatureDialogState.AddEdit(signature)
    }

    fun showDeleteDialog(signature: Signature) {
        _dialogState.value = SignatureDialogState.Delete(signature)
    }

    fun hideDialog() {
        _dialogState.value = SignatureDialogState.Hidden
    }

    fun saveSignature(
        existingId: String?,
        name: String,
        content: String,
        isDefault: Boolean
    ) {
        viewModelScope.launch {
            if (existingId != null) {
                val existing = _signatures.value.find { it.id == existingId } ?: return@launch
                signatureRepository.updateSignature(
                    existing.copy(
                        name = name,
                        content = content,
                        isDefault = isDefault,
                        updatedAt = System.currentTimeMillis()
                    )
                )
                if (isDefault) {
                    signatureRepository.setDefaultSignature(existingId)
                }
            } else {
                val newId = Signature(
                    name = name,
                    content = content,
                    isDefault = isDefault
                ).id
                signatureRepository.insertSignature(
                    Signature(
                        name = name,
                        content = content,
                        isDefault = isDefault
                    )
                )
                if (isDefault) {
                    signatureRepository.setDefaultSignature(newId)
                }
            }
            hideDialog()
        }
    }

    fun deleteSignature(signature: Signature) {
        viewModelScope.launch {
            signatureRepository.deleteSignature(signature)
            hideDialog()
        }
    }

    fun setDefaultSignature(signature: Signature) {
        viewModelScope.launch {
            signatureRepository.setDefaultSignature(signature.id)
            signatureRepository.updateSignature(
                signature.copy(isDefault = !signature.isDefault)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignaturesScreen(
    viewModel: SignaturesViewModel = hiltViewModel()
) {
    val signatures by viewModel.signatures.collectAsState()
    val dialogState by viewModel.dialogState.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Signatures") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.showAddDialog() }) {
                Icon(Icons.Default.Add, contentDescription = "Add Signature")
            }
        }
    ) { paddingValues ->
        if (isLoading) {
            Spacer(modifier = Modifier.padding(paddingValues).height(16.dp))
            Text(
                text = "Loading signatures...",
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else if (signatures.isEmpty()) {
            Text(
                text = "No signatures yet",
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                items(signatures, key = { it.id }) { signature ->
                    SignatureItem(
                        signature = signature,
                        onEdit = { viewModel.showEditDialog(signature) },
                        onDelete = { viewModel.showDeleteDialog(signature) },
                        onSetDefault = { viewModel.setDefaultSignature(signature) }
                    )
                }
            }
        }
    }

    when (val state = dialogState) {
        is SignatureDialogState.AddEdit -> SignatureEditDialog(
            signature = state.signature,
            onSave = { id, name, content, isDefault ->
                viewModel.saveSignature(id, name, content, isDefault)
            },
            onDismiss = { viewModel.hideDialog() }
        )
        is SignatureDialogState.Delete -> AlertDialog(
            onDismissRequest = { viewModel.hideDialog() },
            title = { Text("Delete Signature") },
            text = { Text("Are you sure you want to delete \"${state.signature.name}\"?") },
            confirmButton = {
                TextButton(onClick = { viewModel.deleteSignature(state.signature) }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.hideDialog() }) {
                    Text("Cancel")
                }
            }
        )
        SignatureDialogState.Hidden -> { /* no dialog */ }
    }
}

@Composable
private fun SignatureItem(
    signature: Signature,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onSetDefault: () -> Unit
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onEdit() }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onSetDefault) {
                Icon(
                    imageVector = if (signature.isDefault) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                    contentDescription = if (signature.isDefault) "Default signature" else "Set as default",
                    tint = if (signature.isDefault) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = signature.name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    if (signature.isDefault) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {},
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = Color.White
                            ),
                            modifier = Modifier.height(24.dp)
                        ) {
                            Text(
                                text = "Default",
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
                Text(
                    text = signature.content,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
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
        HorizontalDivider()
    }
}

@Composable
private fun SignatureEditDialog(
    signature: Signature?,
    onSave: (existingId: String?, name: String, content: String, isDefault: Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember(signature) { mutableStateOf(signature?.name ?: "") }
    var content by remember(signature) { mutableStateOf(signature?.content ?: "") }
    var isDefault by remember(signature) { mutableStateOf(signature?.isDefault ?: false) }
    var nameError by remember { mutableStateOf(false) }
    var contentError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (signature != null) "Edit Signature" else "Add Signature") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        nameError = false
                    },
                    label = { Text("Name") },
                    placeholder = { Text("Work Signature") },
                    isError = nameError,
                    supportingText = if (nameError) {
                        { Text("Name cannot be empty") }
                    } else null,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = content,
                    onValueChange = {
                        content = it
                        contentError = false
                    },
                    label = { Text("Content") },
                    placeholder = { Text("Sent from my phone") },
                    isError = contentError,
                    supportingText = if (contentError) {
                        { Text("Content cannot be empty") }
                    } else null,
                    singleLine = false,
                    maxLines = 5,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Set as default", style = MaterialTheme.typography.bodyMedium)
                    Switch(checked = isDefault, onCheckedChange = { isDefault = it })
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    nameError = !Validators.isNonBlank(name)
                    contentError = !Validators.isNonBlank(content)
                    if (!nameError && !contentError) {
                        onSave(signature?.id, name.trim(), content.trim(), isDefault)
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
