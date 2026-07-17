package com.nexusmedia.nexussms.features.backup

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.google.gson.Gson
import com.nexusmedia.nexussms.data.database.BackupMetadataDao
import com.nexusmedia.nexussms.data.database.MessageDao
import com.nexusmedia.nexussms.data.models.BackupMetadata
import com.nexusmedia.nexussms.data.models.Conversation
import com.nexusmedia.nexussms.data.models.Message
import com.nexusmedia.nexussms.data.models.Shortcut
import com.nexusmedia.nexussms.data.models.Signature
import com.nexusmedia.nexussms.data.models.Theme
import com.nexusmedia.nexussms.data.repository.ConversationRepository
import com.nexusmedia.nexussms.data.repository.ShortcutRepository
import com.nexusmedia.nexussms.data.repository.SignatureRepository
import com.nexusmedia.nexussms.data.repository.ThemeRepository
import com.nexusmedia.nexussms.features.backup.models.BackupData
import com.nexusmedia.nexussms.features.backup.models.ConversationData
import com.nexusmedia.nexussms.features.backup.models.MessageData
import com.nexusmedia.nexussms.features.backup.models.ShortcutData
import com.nexusmedia.nexussms.features.backup.models.SignatureData
import com.nexusmedia.nexussms.features.backup.models.ThemeData
import com.nexusmedia.nexussms.security.EncryptionManager
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoogleDriveBackupService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val backupMetadataDao: BackupMetadataDao,
    private val shortcutRepository: ShortcutRepository,
    private val signatureRepository: SignatureRepository,
    private val themeRepository: ThemeRepository,
    private val conversationRepository: ConversationRepository,
    private val messageDao: MessageDao,
    private val googleDriveClient: GoogleDriveClient,
    private val encryptionManager: EncryptionManager
) {
    companion object {
        private const val BACKUP_WORK_NAME = "nexussms_auto_backup"
        private const val ENCRYPTION_PREFIX = "ENC:"
        private val ALL_DATA_TYPES = listOf("shortcuts", "signatures", "themes", "conversations", "messages")
    }

    private val gson = Gson()

    private val encryptedPrefs by lazy {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        androidx.security.crypto.EncryptedSharedPreferences.create(
            context,
            "gdrive_encrypted_prefs",
            masterKey,
            androidx.security.crypto.EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            androidx.security.crypto.EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    private fun getBackupPassphrase(): String {
        val existing = encryptedPrefs.getString("backup_passphrase", null)
        if (existing != null) return existing
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%"
        val generated = (1..32).map { chars.random() }.joinToString("")
        encryptedPrefs.edit().putString("backup_passphrase", generated).apply()
        return generated
    }

    suspend fun createBackup(
        dataTypes: List<String> = ALL_DATA_TYPES,
        encrypt: Boolean = true,
        isAutomatic: Boolean = false
    ): Result<BackupMetadata> = withContext(Dispatchers.IO) {
        try {
            val backupId = java.util.UUID.randomUUID().toString()
            val metadata = BackupMetadata(
                id = backupId,
                backupType = "GOOGLE_DRIVE",
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
                payload = "PBKDF2:" + encryptionManager.encryptWithPassphrase(payload, getBackupPassphrase())
                finalEncrypted = true
                finalAlgorithm = "AES-256-GCM-PBKDF2"
            } else {
                finalEncrypted = false
                finalAlgorithm = ""
            }

            val fileName = "nexussms_backup_${backupId}_${System.currentTimeMillis()}.json"
            val fileId = googleDriveClient.uploadFile(fileName, payload)

            if (fileId == null) {
                val failed = metadata.copy(
                    status = "FAILED",
                    errorMessage = "Failed to upload to Google Drive"
                )
                backupMetadataDao.updateBackup(failed)
                return@withContext Result.failure(Exception("Failed to upload to Google Drive"))
            }

            val completed = metadata.copy(
                status = "COMPLETED",
                size = payload.length.toLong(),
                googleDriveFileId = fileId,
                encryptedBackup = finalEncrypted,
                encryptionAlgorithm = finalAlgorithm
            )
            backupMetadataDao.updateBackup(completed)
            Timber.d("Backup completed: $backupId, fileId: $fileId")
            Result.success(completed)
        } catch (e: Exception) {
            Timber.e(e, "Backup failed")
            Result.failure(e)
        }
    }

    suspend fun restoreBackup(backupId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val metadata = backupMetadataDao.getBackupById(backupId)
                ?: return@withContext Result.failure(Exception("Backup not found: $backupId"))

            val fileId = metadata.googleDriveFileId
                ?: return@withContext Result.failure(Exception("No Google Drive file ID for backup: $backupId"))

            var payload = googleDriveClient.downloadFile(fileId)
                ?: return@withContext Result.failure(Exception("Failed to download backup file"))

            if (payload.startsWith("PBKDF2:")) {
                val encryptedData = payload.removePrefix("PBKDF2:")
                payload = encryptionManager.decryptWithPassphrase(encryptedData, getBackupPassphrase())
            } else if (payload.startsWith(ENCRYPTION_PREFIX)) {
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

            backupData.conversations.forEach { convData ->
                val existing = conversationRepository.getConversationById(convData.id)
                val conversation = Conversation(
                    id = convData.id,
                    participantPhoneNumbers = convData.participantPhoneNumbers,
                    displayName = convData.displayName,
                    isGroupChat = convData.isGroupChat,
                    groupChatName = convData.groupChatName,
                    lastMessage = convData.lastMessage,
                    lastMessageTime = convData.lastMessageTime,
                    unreadCount = convData.unreadCount,
                    isPinned = convData.isPinned,
                    isMuted = convData.isMuted,
                    isArchived = convData.isArchived,
                    isBlocked = convData.isBlocked,
                    createdAt = convData.createdAt,
                    updatedAt = convData.updatedAt,
                    sourcePlatform = convData.sourcePlatform,
                    encryptionEnabled = convData.encryptionEnabled
                )
                if (existing != null) {
                    conversationRepository.updateConversation(conversation)
                } else {
                    conversationRepository.insertConversation(conversation)
                }
            }

            backupData.messages.forEach { msgData ->
                val message = Message(
                    id = msgData.id,
                    conversationId = msgData.conversationId,
                    senderPhoneNumber = msgData.senderPhoneNumber,
                    recipientPhoneNumber = msgData.recipientPhoneNumber,
                    content = msgData.content,
                    type = msgData.type,
                    timestamp = msgData.timestamp,
                    status = msgData.status,
                    isEncrypted = msgData.isEncrypted,
                    isRead = msgData.isRead,
                    isDeleted = msgData.isDeleted,
                    sourcePlatform = msgData.sourcePlatform
                )
                try {
                    messageDao.insertMessage(message)
                } catch (e: Exception) {
                    Timber.w(e, "Failed to restore message ${msgData.id} (FK may be missing)")
                }
            }

            Timber.d("Backup restored: $backupId")
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Restore failed")
            Result.failure(e)
        }
    }

    suspend fun scheduleAutoBackup(frequency: String) = withContext(Dispatchers.IO) {
        try {
            val repeatInterval: Long
            val timeUnit: TimeUnit

            when (frequency) {
                "HOURLY" -> {
                    repeatInterval = 1
                    timeUnit = TimeUnit.HOURS
                }
                "DAILY" -> {
                    repeatInterval = 1
                    timeUnit = TimeUnit.DAYS
                }
                "WEEKLY" -> {
                    repeatInterval = 7
                    timeUnit = TimeUnit.DAYS
                }
                "MONTHLY" -> {
                    repeatInterval = 30
                    timeUnit = TimeUnit.DAYS
                }
                else -> {
                    repeatInterval = 1
                    timeUnit = TimeUnit.DAYS
                }
            }

            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresBatteryNotLow(true)
                .build()

            val workRequest = PeriodicWorkRequestBuilder<BackupWorker>(repeatInterval, timeUnit)
                .setConstraints(constraints)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 10, TimeUnit.SECONDS)
                .addTag("auto_backup")
                .build()

            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(
                    BACKUP_WORK_NAME,
                    ExistingPeriodicWorkPolicy.UPDATE,
                    workRequest
                )

            val nextBackupTime = System.currentTimeMillis() + timeUnit.toMillis(repeatInterval)
            val latestBackup = backupMetadataDao.getLatestBackup("GOOGLE_DRIVE")
            if (latestBackup != null) {
                backupMetadataDao.updateBackup(
                    latestBackup.copy(
                        nextScheduledBackup = nextBackupTime,
                        backupFrequency = frequency,
                        isAutomatic = true
                    )
                )
            }

            Timber.d("Auto backup scheduled: $frequency")
        } catch (e: Exception) {
            Timber.e(e, "Failed to schedule auto backup")
        }
    }

    suspend fun cancelAutoBackup() = withContext(Dispatchers.IO) {
        try {
            WorkManager.getInstance(context).cancelUniqueWork(BACKUP_WORK_NAME)
            Timber.d("Auto backup cancelled")
        } catch (e: Exception) {
            Timber.e(e, "Failed to cancel auto backup")
        }
    }

    fun getBackupHistory(): Flow<List<BackupMetadata>> = backupMetadataDao.getAllBackups()

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

        val conversations = if (dataTypes.contains("conversations")) {
            conversationRepository.getAllConversationsList().map { c ->
                ConversationData(
                    id = c.id,
                    participantPhoneNumbers = c.participantPhoneNumbers,
                    displayName = c.displayName,
                    isGroupChat = c.isGroupChat,
                    groupChatName = c.groupChatName,
                    lastMessage = c.lastMessage,
                    lastMessageTime = c.lastMessageTime,
                    unreadCount = c.unreadCount,
                    isPinned = c.isPinned,
                    isMuted = c.isMuted,
                    isArchived = c.isArchived,
                    isBlocked = c.isBlocked,
                    createdAt = c.createdAt,
                    updatedAt = c.updatedAt,
                    sourcePlatform = c.sourcePlatform,
                    encryptionEnabled = c.encryptionEnabled
                )
            }
        } else emptyList()

        val messages = if (dataTypes.contains("messages")) {
            val allMessages = mutableListOf<MessageData>()
            val convList = conversationRepository.getAllConversationsList()
            for (c in convList) {
                try {
                    val convMessages = messageDao.getAllMessagesByConversation(c.id).first()
                    allMessages.addAll(convMessages.map { m ->
                        MessageData(
                            id = m.id,
                            conversationId = m.conversationId,
                            senderPhoneNumber = m.senderPhoneNumber,
                            recipientPhoneNumber = m.recipientPhoneNumber,
                            content = m.content,
                            type = m.type,
                            timestamp = m.timestamp,
                            status = m.status,
                            isEncrypted = m.isEncrypted,
                            isRead = m.isRead,
                            isDeleted = m.isDeleted,
                            sourcePlatform = m.sourcePlatform
                        )
                    })
                } catch (e: Exception) {
                    Timber.w(e, "Failed to backup messages for conversation ${c.id}")
                }
            }
            allMessages
        } else emptyList()

        return BackupData(
            timestamp = System.currentTimeMillis(),
            shortcuts = shortcuts,
            signatures = signatures,
            themes = themes,
            conversations = conversations,
            messages = messages
        )
    }
}
