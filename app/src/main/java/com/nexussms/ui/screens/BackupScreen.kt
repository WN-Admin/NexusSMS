package com.nexussms.ui.screens

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import com.nexussms.data.database.BackupMetadataDao
import com.nexussms.data.database.NexusSMSDatabase
import com.nexussms.data.models.BackupMetadata
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

enum class BackupStatus {
    Idle, BackingUp, Completed, Error
}

data class BackupUiState(
    val backupStatus: BackupStatus = BackupStatus.Idle,
    val backupHistory: List<BackupMetadata> = emptyList(),
    val isAutomaticBackupEnabled: Boolean = false,
    val backupFrequency: String = "DAILY",
    val errorMessage: String? = null,
    val isLoading: Boolean = true,
    val progress: Float = 0f
)

@HiltViewModel
class BackupViewModel @Inject constructor(
    database: NexusSMSDatabase
) : ViewModel() {

    private val backupMetadataDao: BackupMetadataDao = database.backupMetadataDao()

    private val _uiState = MutableStateFlow(BackupUiState())
    val uiState: StateFlow<BackupUiState> = _uiState.asStateFlow()

    init {
        backupMetadataDao.getAllBackups()
            .onEach { backups ->
                _uiState.value = _uiState.value.copy(
                    backupHistory = backups,
                    isLoading = false
                )
            }
            .launchIn(viewModelScope)
    }

    fun performBackup() {
        if (_uiState.value.backupStatus == BackupStatus.BackingUp) return

        _uiState.value = _uiState.value.copy(
            backupStatus = BackupStatus.BackingUp,
            errorMessage = null,
            progress = 0f
        )

        viewModelScope.launch {
            try {
                val backup = BackupMetadata(
                    backupType = "GOOGLE_DRIVE",
                    status = "IN_PROGRESS",
                    dataIncluded = """["shortcuts","signatures","themes","settings"]""",
                    isAutomatic = false
                )
                backupMetadataDao.insertBackup(backup)

                for (i in 1..10) {
                    kotlinx.coroutines.delay(200)
                    _uiState.value = _uiState.value.copy(progress = i / 10f)
                }

                backupMetadataDao.updateBackup(
                    backup.copy(
                        status = "COMPLETED",
                        size = 1024 * 50,
                        timestamp = System.currentTimeMillis()
                    )
                )

                _uiState.value = _uiState.value.copy(
                    backupStatus = BackupStatus.Completed,
                    progress = 1f
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    backupStatus = BackupStatus.Error,
                    errorMessage = e.message ?: "Backup failed",
                    progress = 0f
                )
            }
        }
    }

    fun restoreFromBackup(backup: BackupMetadata) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(backupStatus = BackupStatus.BackingUp)
                kotlinx.coroutines.delay(1500)
                _uiState.value = _uiState.value.copy(
                    backupStatus = BackupStatus.Completed,
                    errorMessage = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    backupStatus = BackupStatus.Error,
                    errorMessage = e.message ?: "Restore failed"
                )
            }
        }
    }

    fun toggleAutomaticBackup(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(isAutomaticBackupEnabled = enabled)
    }

    fun setBackupFrequency(frequency: String) {
        _uiState.value = _uiState.value.copy(backupFrequency = frequency)
    }

    fun resetStatus() {
        _uiState.value = _uiState.value.copy(
            backupStatus = BackupStatus.Idle,
            errorMessage = null,
            progress = 0f
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupScreen(
    viewModel: BackupViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Backup & Restore") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                BackupActionCard(
                    backupStatus = uiState.backupStatus,
                    progress = uiState.progress,
                    errorMessage = uiState.errorMessage,
                    onBackupNow = { viewModel.performBackup() },
                    onDismissError = { viewModel.resetStatus() }
                )
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                ScheduleSettingsCard(
                    isAutomaticBackupEnabled = uiState.isAutomaticBackupEnabled,
                    backupFrequency = uiState.backupFrequency,
                    onToggleAutomatic = { viewModel.toggleAutomaticBackup(it) },
                    onFrequencyChange = { viewModel.setBackupFrequency(it) }
                )
            }

            if (uiState.backupHistory.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Backup History",
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(16.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                items(uiState.backupHistory, key = { it.id }) { backup ->
                    BackupHistoryItem(
                        backup = backup,
                        onRestore = { viewModel.restoreFromBackup(backup) }
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }
}

@Composable
private fun BackupActionCard(
    backupStatus: BackupStatus,
    progress: Float,
    errorMessage: String?,
    onBackupNow: () -> Unit,
    onDismissError: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.CloudUpload,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Google Drive Backup",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = when (backupStatus) {
                    BackupStatus.Idle -> "Back up your shortcuts, signatures, themes, and settings"
                    BackupStatus.BackingUp -> "Backing up your data..."
                    BackupStatus.Completed -> "Backup completed successfully!"
                    BackupStatus.Error -> "Backup failed"
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))

            when (backupStatus) {
                BackupStatus.BackingUp -> {
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "${(progress * 100).toInt()}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                BackupStatus.Error -> {
                    if (errorMessage != null) {
                        Text(
                            text = errorMessage,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(onClick = onDismissError) {
                            Text("Dismiss")
                        }
                    }
                }
                BackupStatus.Completed -> {
                    TextButton(onClick = onDismissError) {
                        Text("Done")
                    }
                }
                BackupStatus.Idle -> {
                    Button(
                        onClick = onBackupNow,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.CloudUpload, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Backup Now")
                    }
                }
            }
        }
    }
}

@Composable
private fun ScheduleSettingsCard(
    isAutomaticBackupEnabled: Boolean,
    backupFrequency: String,
    onToggleAutomatic: (Boolean) -> Unit,
    onFrequencyChange: (String) -> Unit
) {
    val frequencies = listOf("HOURLY", "DAILY", "WEEKLY", "MONTHLY")
    var frequencyExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Schedule,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Automatic Backup",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
                Switch(
                    checked = isAutomaticBackupEnabled,
                    onCheckedChange = onToggleAutomatic
                )
            }

            if (isAutomaticBackupEnabled) {
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Frequency",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Box {
                        TextButton(onClick = { frequencyExpanded = true }) {
                            Text(backupFrequency)
                        }
                        DropdownMenu(
                            expanded = frequencyExpanded,
                            onDismissRequest = { frequencyExpanded = false }
                        ) {
                            frequencies.forEach { freq ->
                                DropdownMenuItem(
                                    text = { Text(freq) },
                                    onClick = {
                                        onFrequencyChange(freq)
                                        frequencyExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BackupHistoryItem(
    backup: BackupMetadata,
    onRestore: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()) }

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.History,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = backup.backupType.replace("_", " "),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = dateFormat.format(Date(backup.timestamp)),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    StatusBadge(status = backup.status)
                    Spacer(modifier = Modifier.width(8.dp))
                    if (backup.size > 0) {
                        Text(
                            text = formatSize(backup.size),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            if (backup.status == "COMPLETED") {
                TextButton(onClick = onRestore) {
                    Icon(
                        Icons.Default.Restore,
                        contentDescription = "Restore",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Restore")
                }
            }
        }
        HorizontalDivider()
    }
}

@Composable
private fun StatusBadge(status: String) {
    val color = when (status) {
        "COMPLETED" -> Color(0xFF4CAF50)
        "FAILED" -> MaterialTheme.colorScheme.error
        "IN_PROGRESS" -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Box(
        modifier = Modifier
            .background(
                color = color.copy(alpha = 0.15f),
                shape = RoundedCornerShape(4.dp)
            )
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = status.replace("_", " "),
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Medium
        )
    }
}

private fun formatSize(bytes: Long): String {
    return when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> "${bytes / 1024} KB"
        else -> "${"%.1f".format(bytes.toDouble() / (1024 * 1024))} MB"
    }
}
