package com.nexussms.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Embedded
import androidx.room.TypeConverters
import com.nexussms.data.converters.DateConverter
import java.util.Date

@Entity(tableName = "messages")
@TypeConverters(DateConverter::class)
data class Message(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val conversationId: Long,
    val senderId: String,
    val recipientId: String,
    val content: String,
    val timestamp: Date = Date(),
    val isIncoming: Boolean,
    val isSent: Boolean = false,
    val isDelivered: Boolean = false,
    val isRead: Boolean = false,
    val attachmentUrls: String = "", // JSON array
    val messageType: String = "SMS", // SMS, RCS, SOCIAL
    val socialMediaPlatform: String = "", // Facebook, Discord, Telegram, etc
    val encryptionType: String = "NONE", // NONE, AES256, SIGNAL
    val signature: String = "",
    val hasReactions: Boolean = false,
    val reactionData: String = "" // JSON
)

@Entity(tableName = "conversations")
@TypeConverters(DateConverter::class)
data class Conversation(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val participantPhone: String,
    val participantName: String = "",
    val participantAvatar: String = "",
    val lastMessage: String = "",
    val lastMessageTime: Date = Date(),
    val unreadCount: Int = 0,
    val isPinned: Boolean = false,
    val isMuted: Boolean = false,
    val theme: String = "default",
    val messageType: String = "SMS", // SMS, RCS, SOCIAL
    val socialMediaPlatform: String = ""
)

@Entity(tableName = "shortcuts")
data class Shortcut(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val trigger: String, // e.g., "!ato" or "@ato"
    val expansion: String, // e.g., "At The Office"
    val category: String = "",
    val usageCount: Int = 0
)

@Entity(tableName = "scheduled_messages")
@TypeConverters(DateConverter::class)
data class ScheduledMessage(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val conversationId: Long,
    val recipientPhone: String,
    val content: String,
    val scheduledTime: Date,
    val createdTime: Date = Date(),
    val isRCS: Boolean = false,
    val attachmentUrls: String = "", // JSON array
    val status: String = "SCHEDULED" // SCHEDULED, SENT, FAILED, CANCELLED
)

@Entity(tableName = "user_signatures")
data class UserSignature(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val signature: String,
    val isDefault: Boolean = false
)

@Entity(tableName = "themes")
data class Theme(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val primaryColor: String, // Hex color
    val secondaryColor: String,
    val bubbleColorSent: String,
    val bubbleColorReceived: String,
    val textColor: String,
    val backgroundColor: String,
    val isDarkMode: Boolean = false,
    val isCustom: Boolean = false
)

@Entity(tableName = "social_accounts")
data class SocialAccount(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val platform: String, // Facebook, Discord, Telegram, Viber, etc
    val accountId: String,
    val username: String,
    val accessToken: String,
    val refreshToken: String = "",
    val isActive: Boolean = true,
    val displayName: String = ""
)

@Entity(tableName = "contact_shortcuts")
data class ContactShortcut(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val contactPhone: String,
    val shortcutId: Long,
    val isEnabled: Boolean = true
)

data class ConversationWithMessages(
    @Embedded
    val conversation: Conversation,
    val unreadMessages: Int = 0,
    val lastMessagePreview: String = ""
)
