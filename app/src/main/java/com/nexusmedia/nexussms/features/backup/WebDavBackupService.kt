package com.nexusmedia.nexussms.features.backup

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.google.gson.Gson
import com.nexusmedia.nexussms.data.database.BackupMetadataDao
import com.nexusmedia.nexussms.data.database.MessageDao
import com.nexusmedia.nexussms.data.models.BackupMetadata
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
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WebDavBackupService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val webDavClient: WebDavClient,
    private val encryptionManager: EncryptionManager,
    private val shortcutRepository: ShortcutRepository,
    private val signatureRepository: SignatureRepository,
    private val themeRepository: ThemeRepository,
    private val conversationRepository: ConversationRepository,
    private val messageDao: MessageDao,
    private val backupMetadataDao: BackupMetadataDao,
    private val gson: Gson
) {
    companion object {
        private const val BACKUP_FOLDER = "NexusSMS_Backups"
        private const val BACKUP_FILE_PREFIX = "nexussms_backup_"
        private const val ENCRYPTION_PREFIX = "ENC:"
        private val ALL_DATA_TYPES = listOf("shortcuts", "signatures", "themes", "conversations", "messages")

        private const val CREDENTIALS_PREFS = "webdav_credentials"
        private const val KEY_SERVER_URL = "server_url"
        private const val KEY_USERNAME = "username"
        private const val KEY_PASSWORD = "password"
    }

    private val credentialsPrefs: SharedPreferences by lazy {
        context.getSharedPreferences(CREDENTIALS_PREFS, Context.MODE_PRIVATE)
    }

    private val encryptedPrefs: SharedPreferences by lazy {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            context,
            "webdav_encrypted_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    private fun getBackupPassphrase(): String? =
        encryptedPrefs.getString("backup_passphrase", null)

    fun setBackupPassphrase(passphrase: String) {
        encryptedPrefs.edit().putString("backup_passphrase", passphrase).apply()
    }

    fun hasBackupPassphrase(): Boolean =
        encryptedPrefs.getString("backup_passphrase", null).isNullOrBlank().not()

    fun persistCredentials(url: String, username: String, password: String) {
        credentialsPrefs.edit()
            .putString(KEY_SERVER_URL, url)
            .putString(KEY_USERNAME, username)
            .apply()
        encryptedPrefs.edit()
            .putString(KEY_PASSWORD, password)
            .apply()
    }

    fun hasStoredCredentials(): Boolean {
        return credentialsPrefs.getString(KEY_SERVER_URL, null) != null
    }

    suspend fun authenticateFromStoredCredentials(): Boolean {
        val url = credentialsPrefs.getString(KEY_SERVER_URL, null) ?: return false
        val username = credentialsPrefs.getString(KEY_USERNAME, null) ?: return false
        val password = encryptedPrefs.getString(KEY_PASSWORD, null) ?: return false
        return webDavClient.authenticate(url, username, password)
    }

    fun clearStoredCredentials() {
        credentialsPrefs.edit().clear().apply()
        encryptedPrefs.edit().remove(KEY_PASSWORD).apply()
    }

    suspend fun createBackup(
        dataTypes: List<String> = ALL_DATA_TYPES,
        encrypt: Boolean = true,
        isAutomatic: Boolean = false
    ): Result<BackupMetadata> = withContext(Dispatchers.IO) {
        try {
            if (!webDavClient.isAuthenticated()) {
                if (hasStoredCredentials()) {
                    val reconnected = authenticateFromStoredCredentials()
                    if (!reconnected) {
                        return@withContext Result.failure(Exception("WebDAV re-authentication failed"))
                    }
                } else {
                    return@withContext Result.failure(Exception("WebDAV not authenticated"))
                }
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
                val passphrase = getBackupPassphrase()
                if (passphrase.isNullOrBlank()) {
                    return@withContext Result.failure(Exception("Backup passphrase not set. Please set an encryption passphrase in backup settings."))
                }
                payload = "PBKDF2:" + encryptionManager.encryptWithPassphrase(payload, passphrase)
                finalEncrypted = true
                finalAlgorithm = "AES-256-GCM-PBKDF2"
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
            Timber.e(e, "WebDAV backup failed")
            Result.failure(e)
        }
    }

    suspend fun restoreBackup(backupId: String, passphrase: String? = null): Result<Unit> = withContext(Dispatchers.IO) {
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

            if (payload.startsWith("PBKDF2:")) {
                val encryptedData = payload.removePrefix("PBKDF2:")
                val decryptPassphrase = passphrase ?: getBackupPassphrase()
                    ?: return@withContext Result.failure(Exception("Backup is encrypted. Please provide the encryption passphrase."))
                payload = encryptionManager.decryptWithPassphrase(encryptedData, decryptPassphrase)
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
                val conversation = com.nexusmedia.nexussms.data.models.Conversation(
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
                val message = com.nexusmedia.nexussms.data.models.Message(
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

            Timber.d("WebDAV backup restored: $backupId")
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "WebDAV restore failed")
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
            Timber.e(e, "Failed to list WebDAV backups")
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
            Timber.e(e, "Failed to delete WebDAV backup")
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
