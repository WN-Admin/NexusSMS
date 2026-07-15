package com.nexusmedia.nexussms.features.backup.models

data class BackupData(
    val timestamp: Long,
    val version: String = "2.0",
    val shortcuts: List<ShortcutData>,
    val signatures: List<SignatureData>,
    val themes: List<ThemeData>,
    val conversations: List<ConversationData> = emptyList(),
    val messages: List<MessageData> = emptyList()
)

data class ShortcutData(
    val trigger: String,
    val expansion: String,
    val description: String = "",
    val category: String = "General",
    val isActive: Boolean = true,
    val priority: Int = 0
)

data class SignatureData(
    val name: String,
    val content: String,
    val isDefault: Boolean = false,
    val format: String = "TEXT",
    val fontFamily: String? = null,
    val fontSize: Int = 12
)

data class ThemeData(
    val name: String,
    val primaryColor: String,
    val secondaryColor: String,
    val backgroundColor: String,
    val surfaceColor: String,
    val textColor: String,
    val bubbleColorSent: String,
    val bubbleColorReceived: String,
    val isDarkMode: Boolean = false,
    val bubbleStyle: String = "ROUNDED"
)

data class ConversationData(
    val id: String,
    val participantPhoneNumbers: String,
    val displayName: String,
    val isGroupChat: Boolean = false,
    val groupChatName: String? = null,
    val lastMessage: String = "",
    val lastMessageTime: Long = 0,
    val unreadCount: Int = 0,
    val isPinned: Boolean = false,
    val isMuted: Boolean = false,
    val isArchived: Boolean = false,
    val isBlocked: Boolean = false,
    val createdAt: Long = 0,
    val updatedAt: Long = 0,
    val sourcePlatform: String = "SMS",
    val encryptionEnabled: Boolean = false
)

data class MessageData(
    val id: String,
    val conversationId: String,
    val senderPhoneNumber: String,
    val recipientPhoneNumber: String,
    val content: String,
    val type: String,
    val timestamp: Long,
    val status: String,
    val isEncrypted: Boolean = false,
    val isRead: Boolean = false,
    val isDeleted: Boolean = false,
    val sourcePlatform: String = "SMS"
)
