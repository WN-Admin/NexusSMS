package com.nexusmedia.nexussms.data.database

import androidx.room.*
import com.nexusmedia.nexussms.data.models.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: Message): Long
    
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertImportedMessage(message: Message): Long
    
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertImportedMessages(messages: List<Message>)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<Message>)
    
    @Update
    suspend fun updateMessage(message: Message)
    
    @Delete
    suspend fun deleteMessage(message: Message)
    
    @Query("SELECT * FROM messages WHERE id = :messageId")
    suspend fun getMessageById(messageId: String): Message?

    @Query("SELECT * FROM messages WHERE type = :type")
    fun getMessagesByType(type: String): Flow<List<Message>>
    
    @Query("""
        SELECT * FROM messages 
        WHERE conversationId = :conversationId 
        ORDER BY timestamp DESC 
        LIMIT :limit OFFSET :offset
    """)
    fun getMessagesByConversation(
        conversationId: String,
        limit: Int,
        offset: Int
    ): Flow<List<Message>>

    @Query("""
        SELECT * FROM messages 
        WHERE conversationId = :conversationId 
        ORDER BY timestamp DESC
    """)
    fun getAllMessagesByConversation(conversationId: String): Flow<List<Message>>
    
    @Query("""
        SELECT * FROM messages 
        WHERE conversationId = :conversationId 
        AND status = :status
    """)
    fun getMessagesByStatus(
        conversationId: String,
        status: String
    ): Flow<List<Message>>

    @Query("SELECT sourceSmsId FROM messages WHERE conversationId = :conversationId AND sourceSmsId IS NOT NULL")
    suspend fun getImportedSourceSmsIds(conversationId: String): List<Long>

    @Query("DELETE FROM messages WHERE sourceSmsId IN (:smsIds)")
    suspend fun deleteMessagesBySourceSmsIds(smsIds: List<Long>)

    @Query("DELETE FROM messages WHERE id IN (:messageIds)")
    suspend fun deleteMessagesByIds(messageIds: List<String>)

    @Query("SELECT id FROM messages WHERE sourceSmsId IS NULL AND conversationId = :conversationId")
    suspend fun getNonImportedMessageIds(conversationId: String): List<String>
    
    @Query("""
        SELECT * FROM messages 
        WHERE isScheduled = 1 
        AND status = 'PENDING'
        ORDER BY scheduledTime ASC
    """)
    fun getPendingScheduledMessages(): Flow<List<Message>>
    
    @Query("DELETE FROM messages WHERE id = :messageId")
    suspend fun hardDeleteMessage(messageId: String)
}

@Dao
interface ConversationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConversation(conversation: Conversation)
    
    @Update
    suspend fun updateConversation(conversation: Conversation)
    
    @Delete
    suspend fun deleteConversation(conversation: Conversation)
    
    @Query("SELECT * FROM conversations WHERE id = :conversationId")
    suspend fun getConversationById(conversationId: String): Conversation?
    
    @Query("""
        SELECT * FROM conversations 
        WHERE isArchived = 0 
        ORDER BY lastMessageTime DESC
    """)
    fun getAllConversations(): Flow<List<Conversation>>
    
    @Query("""
        SELECT * FROM conversations 
        WHERE isArchived = 1 
        ORDER BY lastMessageTime DESC
    """)
    fun getArchivedConversations(): Flow<List<Conversation>>
    
    @Query("SELECT * FROM conversations WHERE isMuted = 0 AND unreadCount > 0")
    fun getUnreadConversations(): Flow<List<Conversation>>

    @Query("SELECT * FROM conversations WHERE isPinned = 1 ORDER BY lastMessageTime DESC")
    fun getPinnedConversations(): Flow<List<Conversation>>

    @Query("""
        SELECT * FROM conversations
        WHERE isArchived = 0 AND sourcePlatform = :platform
        ORDER BY lastMessageTime DESC
    """)
    fun getConversationsByPlatform(platform: String): Flow<List<Conversation>>

    @Query("SELECT DISTINCT sourcePlatform FROM conversations WHERE isArchived = 0")
    fun getActivePlatforms(): Flow<List<String>>
    
    @Query("""
        SELECT * FROM conversations 
        WHERE participantPhoneNumbers LIKE '%' || :phoneNumber || '%'
    """)
    suspend fun findConversationWithParticipant(phoneNumber: String): Conversation?
}

@Dao
interface ShortcutDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShortcut(shortcut: Shortcut)
    
    @Update
    suspend fun updateShortcut(shortcut: Shortcut)
    
    @Delete
    suspend fun deleteShortcut(shortcut: Shortcut)
    
    @Query("SELECT * FROM shortcuts WHERE id = :shortcutId")
    suspend fun getShortcutById(shortcutId: String): Shortcut?
    
    @Query("SELECT * FROM shortcuts WHERE trigger = :trigger LIMIT 1")
    suspend fun getShortcutByTrigger(trigger: String): Shortcut?
    
    @Query("""
        SELECT * FROM shortcuts 
        WHERE (contactPhoneNumber IS NULL OR contactPhoneNumber = :contactPhoneNumber)
        AND isActive = 1
        ORDER BY priority DESC, usageCount DESC
    """)
    fun getShortcutsByContact(contactPhoneNumber: String): Flow<List<Shortcut>>
    
    @Query("SELECT * FROM shortcuts WHERE contactPhoneNumber IS NULL ORDER BY usageCount DESC")
    fun getGlobalShortcuts(): Flow<List<Shortcut>>
}

@Dao
interface SignatureDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSignature(signature: Signature)
    
    @Update
    suspend fun updateSignature(signature: Signature)
    
    @Delete
    suspend fun deleteSignature(signature: Signature)
    
    @Query("SELECT * FROM signatures WHERE id = :signatureId")
    suspend fun getSignatureById(signatureId: String): Signature?
    
    @Query("SELECT * FROM signatures WHERE isDefault = 1 LIMIT 1")
    suspend fun getDefaultSignature(): Signature?
    
    @Query("SELECT * FROM signatures ORDER BY isDefault DESC, name ASC")
    fun getAllSignatures(): Flow<List<Signature>>

    @Query("UPDATE signatures SET isDefault = 0")
    suspend fun clearDefaultSignature()

    @Query("UPDATE signatures SET isDefault = 1 WHERE id = :signatureId")
    suspend fun markAsDefault(signatureId: String)
}

@Dao
interface ThemeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTheme(theme: Theme)
    
    @Update
    suspend fun updateTheme(theme: Theme)
    
    @Delete
    suspend fun deleteTheme(theme: Theme)
    
    @Query("SELECT * FROM themes WHERE id = :themeId")
    suspend fun getThemeById(themeId: String): Theme?

    @Query("SELECT * FROM themes WHERE id = :themeId")
    fun getThemeFlow(themeId: String): Flow<Theme?>
    
    @Query("SELECT * FROM themes WHERE isDefault = 1 LIMIT 1")
    suspend fun getDefaultTheme(): Theme?
    
    @Query("SELECT * FROM themes ORDER BY name ASC")
    fun getAllThemes(): Flow<List<Theme>>

    @Query("""
        SELECT * FROM themes 
        WHERE isCustom = 0 
        ORDER BY name ASC
    """)
    fun getBuiltInThemes(): Flow<List<Theme>>
    
    @Query("SELECT * FROM themes WHERE isCustom = 1 ORDER BY createdAt DESC")
    fun getCustomThemes(): Flow<List<Theme>>
}

@Dao
interface ScheduledMessageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScheduledMessage(message: ScheduledMessage)
    
    @Update
    suspend fun updateScheduledMessage(message: ScheduledMessage)
    
    @Delete
    suspend fun deleteScheduledMessage(message: ScheduledMessage)
    
    @Query("""
        SELECT * FROM scheduled_messages 
        WHERE status = 'PENDING' 
        AND scheduledTime <= :currentTime
        ORDER BY scheduledTime ASC
    """)
    suspend fun getDueMessages(currentTime: Long): List<ScheduledMessage>
    
    @Query("""
        SELECT * FROM scheduled_messages 
        WHERE conversationId = :conversationId 
        AND status != 'SENT'
        ORDER BY scheduledTime DESC
    """)
    fun getScheduledMessages(conversationId: String): Flow<List<ScheduledMessage>>

    @Query("SELECT * FROM scheduled_messages WHERE status = :status")
    fun getScheduledMessagesByStatus(status: String): Flow<List<ScheduledMessage>>

    @Query("SELECT * FROM scheduled_messages WHERE id = :scheduledMessageId")
    suspend fun getScheduledMessageById(scheduledMessageId: String): ScheduledMessage?

    @Query("UPDATE scheduled_messages SET status = :status, failureReason = :reason WHERE id = :scheduledMessageId")
    suspend fun updateStatus(
        scheduledMessageId: String,
        status: String,
        reason: String? = null
    )
}

@Dao
interface SocialAccountDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccount(account: SocialAccount): Long
    
    @Update
    suspend fun updateAccount(account: SocialAccount)
    
    @Delete
    suspend fun deleteAccount(account: SocialAccount)
    
    @Query("SELECT * FROM social_accounts WHERE platform = :platform")
    suspend fun getAccountByPlatform(platform: String): SocialAccount?

    @Query("SELECT * FROM social_accounts WHERE platform = :platform")
    fun getAccountsByPlatform(platform: String): Flow<List<SocialAccount>>
    
    @Query("SELECT * FROM social_accounts WHERE isConnected = 1")
    fun getConnectedAccounts(): Flow<List<SocialAccount>>
    
    @Query("SELECT * FROM social_accounts ORDER BY updatedAt DESC")
    fun getAllAccounts(): Flow<List<SocialAccount>>
}

@Dao
interface ReactionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReaction(reaction: Reaction)
    
    @Delete
    suspend fun deleteReaction(reaction: Reaction)
    
    @Query("SELECT * FROM reactions WHERE messageId = :messageId")
    fun getReactionsByMessage(messageId: String): Flow<List<Reaction>>
    
    @Query("""
        SELECT emoji, COUNT(*) as count 
        FROM reactions 
        WHERE messageId = :messageId 
        GROUP BY emoji
    """)
    fun getReactionSummary(messageId: String): Flow<List<ReactionCount>>
}

data class ReactionCount(
    val emoji: String,
    val count: Int
)

@Dao
interface BackupMetadataDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBackup(backup: BackupMetadata)
    
    @Update
    suspend fun updateBackup(backup: BackupMetadata)
    
    @Query("SELECT * FROM backup_metadata WHERE id = :id")
    suspend fun getBackupById(id: String): BackupMetadata?
    
    @Query("""
        SELECT * FROM backup_metadata 
        WHERE backupType = :backupType
        ORDER BY timestamp DESC
        LIMIT 1
    """)
    suspend fun getLatestBackup(backupType: String): BackupMetadata?
    
    @Query("""
        SELECT * FROM backup_metadata 
        ORDER BY timestamp DESC
    """)
    fun getAllBackups(): Flow<List<BackupMetadata>>
    
    @Query("""
        SELECT * FROM backup_metadata 
        WHERE status = 'PENDING'
        ORDER BY timestamp ASC
    """)
    suspend fun getPendingBackups(): List<BackupMetadata>
    
    @Query("DELETE FROM backup_metadata WHERE timestamp < :cutoffTime")
    suspend fun deleteOldBackups(cutoffTime: Long)
}

@Dao
interface ContactAvatarDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(avatar: ContactAvatar)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(avatars: List<ContactAvatar>)

    @Query("SELECT * FROM contact_avatars WHERE normalized_phone = :normalizedPhone")
    suspend fun getByPhone(normalizedPhone: String): ContactAvatar?

    @Query("SELECT * FROM contact_avatars ORDER BY display_name ASC")
    fun getAll(): Flow<List<ContactAvatar>>

    @Query("DELETE FROM contact_avatars WHERE normalized_phone = :normalizedPhone")
    suspend fun deleteByPhone(normalizedPhone: String)
}

@Dao
interface AppSecuritySettingsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSettings(settings: AppSecuritySettings)
    
    @Update
    suspend fun updateSettings(settings: AppSecuritySettings)
    
    @Query("SELECT * FROM app_security_settings WHERE id = 'default'")
    fun getSecuritySettings(): Flow<AppSecuritySettings?>
    
    @Query("SELECT * FROM app_security_settings WHERE id = 'default'")
    suspend fun getSecuritySettingsSync(): AppSecuritySettings?
    
    @Query("UPDATE app_security_settings SET lastAuthTime = :time WHERE id = 'default'")
    suspend fun updateLastAuthTime(time: Long)
    
    @Query("UPDATE app_security_settings SET isSessionLocked = :locked WHERE id = 'default'")
    suspend fun updateSessionLocked(locked: Boolean)
}
