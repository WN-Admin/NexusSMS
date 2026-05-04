package com.nexussms.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import com.nexussms.data.models.Message
import com.nexussms.data.models.Conversation
import com.nexussms.data.models.Shortcut
import com.nexussms.data.models.ScheduledMessage
import com.nexussms.data.models.UserSignature
import com.nexussms.data.models.Theme
import com.nexussms.data.models.SocialAccount
import com.nexussms.data.models.ContactShortcut
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {
    @Insert
    suspend fun insertMessage(message: Message): Long

    @Update
    suspend fun updateMessage(message: Message)

    @Delete
    suspend fun deleteMessage(message: Message)

    @Query("SELECT * FROM messages WHERE id = :id")
    fun getMessage(id: Long): Flow<Message?>

    @Query("SELECT * FROM messages WHERE conversationId = :conversationId ORDER BY timestamp DESC")
    fun getConversationMessages(conversationId: Long): Flow<List<Message>>

    @Query("SELECT * FROM messages WHERE conversationId = :conversationId AND isRead = 0 ORDER BY timestamp DESC")
    fun getUnreadMessages(conversationId: Long): Flow<List<Message>>

    @Query("SELECT COUNT(*) FROM messages WHERE conversationId = :conversationId AND isRead = 0")
    fun getUnreadCount(conversationId: Long): Flow<Int>

    @Query("DELETE FROM messages WHERE conversationId = :conversationId")
    suspend fun deleteConversationMessages(conversationId: Long)

    @Query("UPDATE messages SET isRead = 1 WHERE conversationId = :conversationId")
    suspend fun markConversationAsRead(conversationId: Long)

    @Query("SELECT * FROM messages WHERE timestamp >= :startTime ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentMessages(startTime: Long, limit: Int = 100): Flow<List<Message>>

    @Query("SELECT * FROM messages WHERE messageType = :type ORDER BY timestamp DESC")
    fun getMessagesByType(type: String): Flow<List<Message>>
}

@Dao
interface ConversationDao {
    @Insert
    suspend fun insertConversation(conversation: Conversation): Long

    @Update
    suspend fun updateConversation(conversation: Conversation)

    @Delete
    suspend fun deleteConversation(conversation: Conversation)

    @Query("SELECT * FROM conversations ORDER BY lastMessageTime DESC")
    fun getAllConversations(): Flow<List<Conversation>>

    @Query("SELECT * FROM conversations WHERE id = :id")
    fun getConversation(id: Long): Flow<Conversation?>

    @Query("SELECT * FROM conversations WHERE participantPhone = :phone LIMIT 1")
    fun getConversationByPhone(phone: String): Flow<Conversation?>

    @Query("SELECT * FROM conversations WHERE isPinned = 1 ORDER BY lastMessageTime DESC")
    fun getPinnedConversations(): Flow<List<Conversation>>

    @Query("UPDATE conversations SET unreadCount = unreadCount + 1 WHERE id = :id")
    suspend fun incrementUnreadCount(id: Long)

    @Query("UPDATE conversations SET unreadCount = 0 WHERE id = :id")
    suspend fun clearUnreadCount(id: Long)

    @Query("DELETE FROM conversations WHERE id = :id")
    suspend fun deleteConversationById(id: Long)
}

@Dao
interface ShortcutDao {
    @Insert
    suspend fun insertShortcut(shortcut: Shortcut): Long

    @Update
    suspend fun updateShortcut(shortcut: Shortcut)

    @Delete
    suspend fun deleteShortcut(shortcut: Shortcut)

    @Query("SELECT * FROM shortcuts ORDER BY usageCount DESC")
    fun getAllShortcuts(): Flow<List<Shortcut>>

    @Query("SELECT * FROM shortcuts WHERE trigger = :trigger LIMIT 1")
    fun getShortcut(trigger: String): Flow<Shortcut?>

    @Query("SELECT * FROM shortcuts WHERE trigger LIKE :pattern")
    fun searchShortcuts(pattern: String): Flow<List<Shortcut>>

    @Query("UPDATE shortcuts SET usageCount = usageCount + 1 WHERE id = :id")
    suspend fun incrementUsageCount(id: Long)
}

@Dao
interface ScheduledMessageDao {
    @Insert
    suspend fun insertScheduledMessage(message: ScheduledMessage): Long

    @Update
    suspend fun updateScheduledMessage(message: ScheduledMessage)

    @Delete
    suspend fun deleteScheduledMessage(message: ScheduledMessage)

    @Query("SELECT * FROM scheduled_messages ORDER BY scheduledTime ASC")
    fun getAllScheduledMessages(): Flow<List<ScheduledMessage>>

    @Query("SELECT * FROM scheduled_messages WHERE status = 'SCHEDULED' ORDER BY scheduledTime ASC")
    fun getPendingScheduledMessages(): Flow<List<ScheduledMessage>>

    @Query("SELECT * FROM scheduled_messages WHERE id = :id")
    fun getScheduledMessage(id: Long): Flow<ScheduledMessage?>

    @Query("DELETE FROM scheduled_messages WHERE id = :id")
    suspend fun deleteScheduledMessageById(id: Long)
}

@Dao
interface SignatureDao {
    @Insert
    suspend fun insertSignature(signature: UserSignature): Long

    @Update
    suspend fun updateSignature(signature: UserSignature)

    @Delete
    suspend fun deleteSignature(signature: UserSignature)

    @Query("SELECT * FROM user_signatures")
    fun getAllSignatures(): Flow<List<UserSignature>>

    @Query("SELECT * FROM user_signatures WHERE isDefault = 1 LIMIT 1")
    fun getDefaultSignature(): Flow<UserSignature?>
}

@Dao
interface ThemeDao {
    @Insert
    suspend fun insertTheme(theme: Theme): Long

    @Update
    suspend fun updateTheme(theme: Theme)

    @Delete
    suspend fun deleteTheme(theme: Theme)

    @Query("SELECT * FROM themes ORDER BY name ASC")
    fun getAllThemes(): Flow<List<Theme>>

    @Query("SELECT * FROM themes WHERE id = :id")
    fun getTheme(id: Long): Flow<Theme?>

    @Query("SELECT * FROM themes WHERE isCustom = 0 ORDER BY name ASC")
    fun getDefaultThemes(): Flow<List<Theme>>

    @Query("SELECT * FROM themes WHERE isCustom = 1 ORDER BY name ASC")
    fun getCustomThemes(): Flow<List<Theme>>
}

@Dao
interface SocialAccountDao {
    @Insert
    suspend fun insertAccount(account: SocialAccount): Long

    @Update
    suspend fun updateAccount(account: SocialAccount)

    @Delete
    suspend fun deleteAccount(account: SocialAccount)

    @Query("SELECT * FROM social_accounts WHERE isActive = 1")
    fun getActiveAccounts(): Flow<List<SocialAccount>>

    @Query("SELECT * FROM social_accounts WHERE platform = :platform")
    fun getAccountsByPlatform(platform: String): Flow<List<SocialAccount>>

    @Query("SELECT * FROM social_accounts")
    fun getAllAccounts(): Flow<List<SocialAccount>>
}

@Dao
interface ContactShortcutDao {
    @Insert
    suspend fun insertContactShortcut(contactShortcut: ContactShortcut): Long

    @Update
    suspend fun updateContactShortcut(contactShortcut: ContactShortcut)

    @Delete
    suspend fun deleteContactShortcut(contactShortcut: ContactShortcut)

    @Query("SELECT * FROM contact_shortcuts WHERE contactPhone = :phone AND isEnabled = 1")
    fun getEnabledShortcutsForContact(phone: String): Flow<List<ContactShortcut>>

    @Query("DELETE FROM contact_shortcuts WHERE contactPhone = :phone")
    suspend fun deleteAllShortcutsForContact(phone: String)
}
