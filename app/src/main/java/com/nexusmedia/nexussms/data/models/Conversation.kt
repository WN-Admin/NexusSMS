package com.nexusmedia.nexussms.data.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "conversations",
    indices = [Index("lastMessageTime", orders = [Index.Order.DESC])]
)
data class Conversation(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    
    val participantPhoneNumbers: String, // JSON: ["number1", "number2"]
    val displayName: String,
    val avatarUrl: String? = null,
    val isGroupChat: Boolean = false,
    val groupChatName: String? = null,
    
    @ColumnInfo(name = "lastMessage")
    val lastMessage: String = "",
    
    @ColumnInfo(name = "lastMessageTime")
    val lastMessageTime: Long = System.currentTimeMillis(),
    
    val unreadCount: Int = 0,
    val isPinned: Boolean = false,
    val isMuted: Boolean = false,
    val muteUntil: Long? = null,
    
    val themeId: String? = null,
    val backgroundColor: String? = null,
    val wallpaperUrl: String? = null,
    
    val isArchived: Boolean = false,
    val isBlocked: Boolean = false,
    
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    
    val defaultSignatureId: String? = null,
    val autoReplyEnabled: Boolean = false,
    val autoReplyMessage: String? = null,
    
    val encryptionEnabled: Boolean = false,
    val encryptionType: String? = null, // E2EE, TRANSPORT, NONE

    val sourcePlatform: String = "SMS", // SMS, RCS, TELEGRAM, DISCORD, WHATSAPP, SIGNAL, SLACK, etc.
    val sourceAccountId: String? = null  // FK to social_accounts.id for platform conversations
)
