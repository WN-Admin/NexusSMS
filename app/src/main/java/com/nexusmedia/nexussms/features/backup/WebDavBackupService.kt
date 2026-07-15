package com.nexusmedia.nexussms.features.backup

import com.google.gson.Gson
import com.nexusmedia.nexussms.data.database.BackupMetadataDao
import com.nexusmedia.nexussms.data.models.BackupMetadata
import com.nexusmedia.nexussms.data.models.Shortcut
import com.nexusmedia.nexussms.data.models.Signature
import com.nexusmedia.nexussms.data.models.Theme
import com.nexusmedia.nexussms.data.repository.ShortcutRepository
import com.nexusmedia.nexussms.data.repository.SignatureRepository
import com.nexusmedia.nexussms.data.repository.ThemeRepository
import com.nexusmedia.nexussms.features.backup.models.BackupData
import com.nexusmedia.nexussms.features.backup.models.ShortcutData
import com.nexusmedia.nexussms.features.backup.models.SignatureData
import com.nexusmedia.nexussms.features.backup.models.ThemeData
import com.nexusmedia.nexussms.security.EncryptionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WebDavBackupService @Inject constructor(
    private val webDavClient: WebDavClient,
    private val encryptionManager: EncryptionManager,
    private val shortcutRepository: ShortcutRepository,
    private val signatureRepository: SignatureRepository,
    private val themeRepository: ThemeRepository,
    private val backupMetadataDao: BackupMetadataDao,
    private val gson: Gson
) {
    companion object {
        private const val TAG = "WebDavBackupService"
        private const val BACKUP_FOLDER = "NexusSMS_Backups"
        private const val BACKUP_FILE_PREFIX = "nexussms_backup_"
        private const val ENCRYPTION_PREFIX = "ENC:"
        private val ALL_DATA_TYPES = listOf("shortcuts", "signatures", "themes")
    }

    suspend fun createBackup(
        dataTypes: List<String> = ALL_DATA_TYPES,
        encrypt: Boolean = true,
        isAutomatic: Boolean = false
    ): Result<BackupMetadata> = withContext(Dispatchers.IO) {
        try {
            if (!webDavClient.isAuthenticated()) {
                return@withContext Result.failure(Exception("WebDAV not authenticated"))
            }

            val backupId = java.util.UUID.randomUUID().toString()
            val metadata = BackupMetadata(
                id = backupId,
                backupType = "WEBDAV",
                dataIncluded = gson.toJson(dataTypes),
                status = "IN_PROGRESS",
                isAutomatic = isAutomatic
            )
            backupMetadataDao.insertBackup(metadata)

            val backupData = buildBackupData(dataTypes)
            var payload = gson.toJson(backupData)

            val finalEncrypted: Boolean
            val finalAlgorithm: String

            if (encrypt) {
                payload = ENCRYPTION_PREFIX + encryptionManager.encryptAES256(payload)
                finalEncrypted = true
                finalAlgorithm = "AES-256-CBC"
            } else {
                finalEncrypted = false
                finalAlgorithm = ""
            }

            webDavClient.createDirectory(BACKUP_FOLDER)

            val fileName = "${BACKUP_FILE_PREFIX}${backupId}_${System.currentTimeMillis()}.json"
            val filePath = "$BACKUP_FOLDER/$fileName"
            val uploadedPath = webDavClient.uploadFile(filePath, payload)

            if (uploadedPath == null) {
                val failed = metadata.copy(
                    status = "FAILED",
                    errorMessage = "Failed to upload to WebDAV server"
                )
                backupMetadataDao.updateBackup(failed)
                return@withContext Result.failure(Exception("Failed to upload to WebDAV server"))
            }

            val completed = metadata.copy(
                status = "COMPLETED",
                size = payload.length.toLong(),
                encryptedBackup = finalEncrypted,
                encryptionAlgorithm = finalAlgorithm
            )
            backupMetadataDao.updateBackup(completed)
            Timber.d("WebDAV backup completed: $backupId")
            Result.success(completed)
        } catch (e: Exception) {
            Timber.e(TAG, "WebDAV backup failed: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun restoreBackup(backupId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            if (!webDavClient.isAuthenticated()) {
                return@withContext Result.failure(Exception("WebDAV not authenticated"))
            }

            val metadata = backupMetadataDao.getBackupById(backupId)
                ?: return@withContext Result.failure(Exception("Backup not found: $backupId"))

            val files = webDavClient.listFiles(BACKUP_FOLDER)
            val backupFile = files.find { it.name.contains(backupId) }
                ?: return@withContext Result.failure(Exception("Backup file not found on WebDAV server"))

            var payload = webDavClient.downloadFile(backupFile.path)
                ?: return@withContext Result.failure(Exception("Failed to download backup file"))

            if (payload.startsWith(ENCRYPTION_PREFIX)) {
                val encryptedData = payload.removePrefix(ENCRYPTION_PREFIX)
                payload = encryptionManager.decryptAES256(encryptedData)
            }

            val backupData: BackupData = gson.fromJson(payload, BackupData::class.java)

            backupData.shortcuts.forEach { shortcutData ->
                val existing = shortcutRepository.getShortcutByTrigger(shortcutData.trigger)
                val shortcut = Shortcut(
                    id = existing?.id ?: java.util.UUID.randomUUID().toString(),
                    trigger = shortcutData.trigger,
                    expansion = shortcutData.expansion,
                    description = shortcutData.description,
                    category = shortcutData.category,
                    isActive = shortcutData.isActive,
                    priority = shortcutData.priority
                )
                if (existing != null) {
                    shortcutRepository.updateShortcut(shortcut)
                } else {
                    shortcutRepository.insertShortcut(shortcut)
                }
            }

            backupData.signatures.forEach { signatureData ->
                val signature = Signature(
                    name = signatureData.name,
                    content = signatureData.content,
                    isDefault = signatureData.isDefault,
                    format = signatureData.format,
                    fontFamily = signatureData.fontFamily,
                    fontSize = signatureData.fontSize
                )
                signatureRepository.insertSignature(signature)
            }

            backupData.themes.forEach { themeData ->
                val theme = Theme(
                    name = themeData.name,
                    isDefault = false,
                    isCustom = true,
                    primaryColor = themeData.primaryColor,
                    secondaryColor = themeData.secondaryColor,
                    backgroundColor = themeData.backgroundColor,
                    surfaceColor = themeData.surfaceColor,
                    textColor = themeData.textColor,
                    bubbleColorSent = themeData.bubbleColorSent,
                    bubbleColorReceived = themeData.bubbleColorReceived,
                    isDarkMode = themeData.isDarkMode,
                    bubbleStyle = themeData.bubbleStyle
                )
                themeRepository.insertTheme(theme)
            }

            Timber.d("WebDAV backup restored: $backupId")
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(TAG, "WebDAV restore failed: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun listBackups(): List<BackupMetadata> = withContext(Dispatchers.IO) {
        try {
            if (!webDavClient.isAuthenticated()) return@withContext emptyList()

            val files = webDavClient.listFiles(BACKUP_FOLDER)
            files.filter { it.name.startsWith(BACKUP_FILE_PREFIX) && it.name.endsWith(".json") }
                .map { file ->
                    val parts = file.name.removePrefix(BACKUP_FILE_PREFIX).removeSuffix(".json").split("_")
                    val backupId = parts.firstOrNull() ?: file.name
                    val timestamp = parts.getOrNull(1)?.toLongOrNull() ?: file.lastModified

                    BackupMetadata(
                        id = backupId,
                        backupType = "WEBDAV",
                        timestamp = timestamp,
                        size = file.size,
                        status = "COMPLETED",
                        encryptedBackup = true
                    )
                }
                .sortedByDescending { it.timestamp }
        } catch (e: Exception) {
            Timber.e(TAG, "Failed to list WebDAV backups: ${e.message}")
            emptyList()
        }
    }

    suspend fun deleteBackup(backupId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            if (!webDavClient.isAuthenticated()) return@withContext false

            val files = webDavClient.listFiles(BACKUP_FOLDER)
            val backupFile = files.find { it.name.contains(backupId) } ?: return@withContext false

            webDavClient.deleteFile(backupFile.path)
        } catch (e: Exception) {
            Timber.e(TAG, "Failed to delete WebDAV backup: ${e.message}")
            false
        }
    }

    suspend fun testConnection(url: String, username: String, password: String): Boolean {
        return webDavClient.authenticate(url, username, password)
    }

    private suspend fun buildBackupData(dataTypes: List<String>): BackupData {
        val shortcuts = if (dataTypes.contains("shortcuts")) {
            shortcutRepository.getGlobalShortcuts().first().map { s ->
                ShortcutData(
                    trigger = s.trigger,
                    expansion = s.expansion,
                    description = s.description,
                    category = s.category,
                    isActive = s.isActive,
                    priority = s.priority
                )
            }
        } else emptyList()

        val signatures = if (dataTypes.contains("signatures")) {
            signatureRepository.getAllSignatures().first().map { s ->
                SignatureData(
                    name = s.name,
                    content = s.content,
                    isDefault = s.isDefault,
                    format = s.format,
                    fontFamily = s.fontFamily,
                    fontSize = s.fontSize
                )
            }
        } else emptyList()

        val themes = if (dataTypes.contains("themes")) {
            themeRepository.getAllThemes().first().map { t ->
                ThemeData(
                    name = t.name,
                    primaryColor = t.primaryColor,
                    secondaryColor = t.secondaryColor,
                    backgroundColor = t.backgroundColor,
                    surfaceColor = t.surfaceColor,
                    textColor = t.textColor,
                    bubbleColorSent = t.bubbleColorSent,
                    bubbleColorReceived = t.bubbleColorReceived,
                    isDarkMode = t.isDarkMode,
                    bubbleStyle = t.bubbleStyle
                )
            }
        } else emptyList()

        return BackupData(
            timestamp = System.currentTimeMillis(),
            shortcuts = shortcuts,
            signatures = signatures,
            themes = themes
        )
    }
}
