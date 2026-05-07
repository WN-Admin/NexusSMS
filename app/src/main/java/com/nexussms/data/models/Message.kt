package com.nexussms.data.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "messages",
    foreignKeys = [
        ForeignKey(
            entity = Conversation::class,
            parentColumns = ["id"],
            childColumns = ["conversationId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("conversationId"),
        Index("timestamp"),
        Index("status")
    ]
)
data class Message(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    
    val conversationId: String,
    val senderPhoneNumber: String,
    val recipientPhoneNumber: String,
    val content: String,
    val type: String, // TEXT, IMAGE, VIDEO, AUDIO, LOCATION, FILE
    
    @ColumnInfo(name = "timestamp")
    val timestamp: Long = System.currentTimeMillis(),
    
    val status: String, // SENDING, SENT, DELIVERED, READ, FAILED
    val isEncrypted: Boolean = false,
    val encryptionAlgorithm: String? = null, // AES-256-GCM
    
    @ColumnInfo(name = "metadata")
    val metadata: String = "", // JSON: {mediaUrl, mimeType, size, duration}
    
    val reactions: String = "", // JSON: {emoji: [phoneNumbers]}
    val isRead: Boolean = false,
    val readAt: Long? = null,
    
    @ColumnInfo(name = "mediaUrls")
    val mediaUrls: String = "", // JSON: ["url1", "url2"]
    
    val location: String? = null, // JSON: {latitude, longitude, address}
    val isScheduled: Boolean = false,
    val scheduledTime: Long? = null,
    
    val signatureId: String? = null,
    val shortcutId: String? = null,
    
    val isDeleted: Boolean = false,
    val deletedAt: Long? = null,
    
    @ColumnInfo(name = "rcsMessageId")
    val rcsMessageId: String? = null,
    
    @ColumnInfo(name = "deliveryReport")
    val deliveryReport: String = "" // JSON tracking
)
