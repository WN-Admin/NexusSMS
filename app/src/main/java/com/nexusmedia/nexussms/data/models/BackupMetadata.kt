package com.nexusmedia.nexussms.data.models

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "backup_metadata",
    indices = [Index("backupType"), Index("timestamp")]
)
data class BackupMetadata(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    
    val backupType: String, // GOOGLE_DRIVE, LOCAL, MANUAL
    val timestamp: Long = System.currentTimeMillis(),
    val size: Long = 0, // bytes
    
    val dataIncluded: String = "", // JSON: ["shortcuts", "signatures", "themes", "settings"]
    val googleDriveFileId: String? = null,
    val googleDriveFolderId: String? = null,
    
    val status: String = "PENDING", // PENDING, IN_PROGRESS, COMPLETED, FAILED
    val errorMessage: String? = null,
    
    val isAutomatic: Boolean = false,
    val nextScheduledBackup: Long? = null,
    val backupFrequency: String = "DAILY", // HOURLY, DAILY, WEEKLY, MONTHLY
    
    val encryptedBackup: Boolean = true,
    val encryptionAlgorithm: String = "AES-256-GCM"
)
