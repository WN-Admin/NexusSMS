package com.nexussms.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexussms.data.database.BackupMetadataDao
import com.nexussms.data.models.BackupMetadata
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BackupViewModel @Inject constructor(
    private val backupMetadataDao: BackupMetadataDao
) : ViewModel() {

    private val _backups = MutableStateFlow<List<BackupMetadata>>(emptyList())
    val backups: StateFlow<List<BackupMetadata>> = _backups.asStateFlow()

    private val _isBackingUp = MutableStateFlow(false)
    val isBackingUp: StateFlow<Boolean> = _isBackingUp.asStateFlow()

    private val _lastBackup = MutableStateFlow<BackupMetadata?>(null)
    val lastBackup: StateFlow<BackupMetadata?> = _lastBackup.asStateFlow()

    init {
        backupMetadataDao.getAllBackups()
            .onEach { list ->
                _backups.value = list
                _lastBackup.value = list.firstOrNull()
            }
            .launchIn(viewModelScope)
    }

    fun createManualBackup(dataIncluded: List<String> = listOf("shortcuts", "signatures", "themes", "settings", "messages")) {
        viewModelScope.launch {
            _isBackingUp.value = true
            val backup = BackupMetadata(
                backupType = "MANUAL",
                dataIncluded = dataIncluded.joinToString(",", "[", "]") { "\"$it\"" },
                status = "IN_PROGRESS",
                isAutomatic = false
            )
            backupMetadataDao.insertBackup(backup)
            delay(2000)
            backupMetadataDao.updateBackup(backup.copy(status = "COMPLETED", size = 0L))
            _isBackingUp.value = false
        }
    }

    fun restoreBackup(backup: BackupMetadata) {
        viewModelScope.launch {
            backupMetadataDao.updateBackup(backup.copy(status = "IN_PROGRESS"))
            delay(2000)
            backupMetadataDao.updateBackup(backup.copy(status = "COMPLETED"))
        }
    }

    fun scheduleAutoBackup(frequency: String = "DAILY") {
        viewModelScope.launch {
            val nextBackup = System.currentTimeMillis() + when (frequency) {
                "HOURLY" -> 3_600_000L
                "DAILY" -> 86_400_000L
                "WEEKLY" -> 604_800_000L
                "MONTHLY" -> 2_592_000_000L
                else -> 86_400_000L
            }
            val backup = BackupMetadata(
                backupType = "GOOGLE_DRIVE",
                status = "PENDING",
                isAutomatic = true,
                backupFrequency = frequency,
                nextScheduledBackup = nextBackup
            )
            backupMetadataDao.insertBackup(backup)
        }
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
