package com.nexusmedia.nexussms.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexusmedia.nexussms.data.models.ScheduledMessage
import com.nexusmedia.nexussms.data.repository.ScheduledMessageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

sealed class ScheduledDialogState {
    object Hidden : ScheduledDialogState()
    data class Reschedule(val message: ScheduledMessage) : ScheduledDialogState()
    data class Cancel(val message: ScheduledMessage) : ScheduledDialogState()
    data class Delete(val message: ScheduledMessage) : ScheduledDialogState()
}

@HiltViewModel
class ScheduledMessagesViewModel @Inject constructor(
    private val scheduledMessageRepository: ScheduledMessageRepository
) : ViewModel() {

    private val _pendingMessages = MutableStateFlow<List<ScheduledMessage>>(emptyList())
    val pendingMessages: StateFlow<List<ScheduledMessage>> = _pendingMessages.asStateFlow()

    private val _cancelledMessages = MutableStateFlow<List<ScheduledMessage>>(emptyList())
    val cancelledMessages: StateFlow<List<ScheduledMessage>> = _cancelledMessages.asStateFlow()

    private val _failedMessages = MutableStateFlow<List<ScheduledMessage>>(emptyList())
    val failedMessages: StateFlow<List<ScheduledMessage>> = _failedMessages.asStateFlow()

    private val _dialogState = MutableStateFlow<ScheduledDialogState>(ScheduledDialogState.Hidden)
    val dialogState: StateFlow<ScheduledDialogState> = _dialogState.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        scheduledMessageRepository.getScheduledMessagesByStatus("PENDING")
            .onEach { messages ->
                _pendingMessages.value = messages
                _isLoading.value = false
            }
            .launchIn(viewModelScope)

        scheduledMessageRepository.getScheduledMessagesByStatus("CANCELLED")
            .onEach { _cancelledMessages.value = it }
            .launchIn(viewModelScope)

        scheduledMessageRepository.getScheduledMessagesByStatus("FAILED")
            .onEach { _failedMessages.value = it }
            .launchIn(viewModelScope)
    }

    fun showRescheduleDialog(message: ScheduledMessage) {
        _dialogState.value = ScheduledDialogState.Reschedule(message)
    }

    fun showCancelDialog(message: ScheduledMessage) {
        _dialogState.value = ScheduledDialogState.Cancel(message)
    }

    fun showDeleteDialog(message: ScheduledMessage) {
        _dialogState.value = ScheduledDialogState.Delete(message)
    }

    fun hideDialog() {
        _dialogState.value = ScheduledDialogState.Hidden
    }

    fun cancelMessage(message: ScheduledMessage) {
        viewModelScope.launch {
            scheduledMessageRepository.cancelScheduledMessage(message.id)
            hideDialog()
        }
    }

    fun rescheduleMessage(message: ScheduledMessage, newTimeMillis: Long) {
        viewModelScope.launch {
            scheduledMessageRepository.rescheduleMessage(message.id, newTimeMillis)
            hideDialog()
        }
    }

    fun deleteMessage(message: ScheduledMessage) {
        viewModelScope.launch {
            scheduledMessageRepository.deleteScheduledMessage(message)
            hideDialog()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduledMessagesScreen(
    viewModel: ScheduledMessagesViewModel = hiltViewModel()
) {
    val pendingMessages by viewModel.pendingMessages.collectAsState()
    val cancelledMessages by viewModel.cancelledMessages.collectAsState()
    val failedMessages by viewModel.failedMessages.collectAsState()
    val dialogState by viewModel.dialogState.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Scheduled Messages") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("Loading scheduled messages...", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                if (pendingMessages.isEmpty() && cancelledMessages.isEmpty() && failedMessages.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "No scheduled messages",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                if (pendingMessages.isNotEmpty()) {
                    item {
                        SectionHeader(title = "Pending (${pendingMessages.size})")
                    }
                    items(pendingMessages, key = { it.id }) { message ->
                        ScheduledMessageItem(
                            message = message,
                            statusColor = MaterialTheme.colorScheme.primary,
                            onCancel = { viewModel.showCancelDialog(message) },
                            onReschedule = { viewModel.showRescheduleDialog(message) },
                            onDelete = { viewModel.showDeleteDialog(message) }
                        )
                    }
                }

                if (failedMessages.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        SectionHeader(title = "Failed (${failedMessages.size})")
                    }
                    items(failedMessages, key = { it.id }) { message ->
                        ScheduledMessageItem(
                            message = message,
                            statusColor = MaterialTheme.colorScheme.error,
                            onCancel = { viewModel.showCancelDialog(message) },
                            onReschedule = { viewModel.showRescheduleDialog(message) },
                            onDelete = { viewModel.showDeleteDialog(message) }
                        )
                    }
                }

                if (cancelledMessages.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        SectionHeader(title = "Cancelled (${cancelledMessages.size})")
                    }
                    items(cancelledMessages, key = { it.id }) { message ->
                        ScheduledMessageItem(
                            message = message,
                            statusColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            onCancel = null,
                            onReschedule = { viewModel.showRescheduleDialog(message) },
                            onDelete = { viewModel.showDeleteDialog(message) }
                        )
                    }
                }

                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }
    }

    when (val state = dialogState) {
        is ScheduledDialogState.Reschedule -> RescheduleDialog(
            message = state.message,
            onReschedule = { newTime -> viewModel.rescheduleMessage(state.message, newTime) },
            onDismiss = { viewModel.hideDialog() }
        )
        is ScheduledDialogState.Cancel -> AlertDialog(
            onDismissRequest = { viewModel.hideDialog() },
            title = { Text("Cancel Message") },
            text = { Text("Are you sure you want to cancel this scheduled message?") },
            confirmButton = {
                TextButton(onClick = { viewModel.cancelMessage(state.message) }) {
                    Text("Cancel Message", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.hideDialog() }) {
                    Text("Keep")
                }
            }
        )
        is ScheduledDialogState.Delete -> AlertDialog(
            onDismissRequest = { viewModel.hideDialog() },
            title = { Text("Delete Message") },
            text = { Text("Are you sure you want to permanently delete this scheduled message?") },
            confirmButton = {
                TextButton(onClick = { viewModel.deleteMessage(state.message) }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.hideDialog() }) {
                    Text("Cancel")
                }
            }
        )
        ScheduledDialogState.Hidden -> { /* no dialog */ }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(16.dp),
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun ScheduledMessageItem(
    message: ScheduledMessage,
    statusColor: Color,
    onCancel: (() -> Unit)?,
    onReschedule: () -> Unit,
    onDelete: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(statusColor, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = message.status,
                        style = MaterialTheme.typography.labelSmall,
                        color = statusColor,
                        fontWeight = FontWeight.Bold
                    )
                }
                Row {
                    if (onCancel != null) {
                        IconButton(onClick = onCancel) {
                            Icon(
                                Icons.Default.Cancel,
                                contentDescription = "Cancel",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                    IconButton(onClick = onReschedule) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "Reschedule",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = onDelete) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message.content,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Schedule,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = dateFormat.format(Date(message.scheduledTime)),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "To: ${message.recipientPhoneNumber}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (message.repeatType != "NONE") {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Repeats: ${message.repeatType}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (message.failureReason != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Error: ${message.failureReason}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun RescheduleDialog(
    message: ScheduledMessage,
    onReschedule: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    var hoursFromNow by remember { mutableStateOf("1") }
    var error by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Reschedule Message") },
        text = {
            Column {
                Text(
                    text = "Current: ${
                        SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
                            .format(Date(message.scheduledTime))
                    }",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = hoursFromNow,
                    onValueChange = {
                        hoursFromNow = it
                        error = false
                    },
                    label = { Text("Hours from now") },
                    isError = error,
                    supportingText = if (error) {
                        { Text("Enter a valid number of hours") }
                    } else null,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val hours = hoursFromNow.toIntOrNull()
                    if (hours != null && hours > 0) {
                        val newTime = System.currentTimeMillis() + (hours * 60 * 60 * 1000L)
                        onReschedule(newTime)
                    } else {
                        error = true
                    }
                }
            ) {
                Text("Reschedule")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
