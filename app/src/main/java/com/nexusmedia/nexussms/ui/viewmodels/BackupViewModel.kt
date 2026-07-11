package com.nexusmedia.nexussms.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexusmedia.nexussms.data.database.NexusSMSDatabase
import com.nexusmedia.nexussms.data.models.BackupMetadata
import com.nexusmedia.nexussms.features.backup.GoogleDriveBackupService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BackupViewModel @Inject constructor(
    private val backupMetadataDao: com.nexusmedia.nexussms.data.database.BackupMetadataDao,
    private val googleDriveBackupService: GoogleDriveBackupService
) : ViewModel() {

    private val _backups = MutableStateFlow<List<BackupMetadata>>(emptyList())
    val backups: StateFlow<List<BackupMetadata>> = _backups.asStateFlow()

    private val _isBackingUp = MutableStateFlow(false)
    val isBackingUp: StateFlow<Boolean> = _isBackingUp.asStateFlow()

    private val _lastBackup = MutableStateFlow<BackupMetadata?>(null)
    val lastBackup: StateFlow<BackupMetadata?> = _lastBackup.asStateFlow()

    private val _backupError = MutableStateFlow<String?>(null)
    val backupError: StateFlow<String?> = _backupError.asStateFlow()

    init {
        backupMetadataDao.getAllBackups()
            .onEach { list ->
                _backups.value = list
                _lastBackup.value = list.firstOrNull()
            }
            .launchIn(viewModelScope)
    }

    fun createManualBackup(dataIncluded: List<String> = listOf("shortcuts", "signatures", "themes")) {
        viewModelScope.launch {
            _isBackingUp.value = true
            _backupError.value = null
            try {
                val result = googleDriveBackupService.createBackup(
                    dataTypes = dataIncluded,
                    encrypt = true,
                    isAutomatic = false
                )
                result.onFailure { e ->
                    _backupError.value = e.message ?: "Backup failed"
                }
            } catch (e: Exception) {
                _backupError.value = e.message ?: "Backup failed"
            } finally {
                _isBackingUp.value = false
            }
        }
    }

    fun restoreBackup(backup: BackupMetadata) {
        viewModelScope.launch {
            _isBackingUp.value = true
            _backupError.value = null
            try {
                val result = googleDriveBackupService.restoreBackup(backup.id)
                result.onFailure { e ->
                    _backupError.value = e.message ?: "Restore failed"
                }
            } catch (e: Exception) {
                _backupError.value = e.message ?: "Restore failed"
            } finally {
                _isBackingUp.value = false
            }
        }
    }

    fun scheduleAutoBackup(frequency: String = "DAILY") {
        viewModelScope.launch {
            googleDriveBackupService.scheduleAutoBackup(frequency)
        }
    }

    fun cancelAutoBackup() {
        viewModelScope.launch {
            googleDriveBackupService.cancelAutoBackup()
        }
    }

    fun clearError() {
        _backupError.value = null
    }

    fun getHistory() {
        backupMetadataDao.getAllBackups()
            .onEach { list ->
                _backups.value = list
                _lastBackup.value = list.firstOrNull()
            }
            .launchIn(viewModelScope)
    }
}
