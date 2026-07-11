package com.nexusmedia.nexussms.data.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "scheduled_messages",
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
        Index("scheduledTime"),
        Index("status")
    ]
)
data class ScheduledMessage(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    
    val conversationId: String,
    val recipientPhoneNumber: String,
    val content: String,
    
    val scheduledTime: Long,
    val createdAt: Long = System.currentTimeMillis(),
    
    val status: String = "PENDING", // PENDING, SENT, FAILED, CANCELLED
    val failureReason: String? = null,
    val sentAt: Long? = null,
    
    val repeatType: String = "NONE", // ONCE, DAILY, WEEKLY, MONTHLY
    val repeatUntil: Long? = null,
    val repeatDays: String = "", // JSON: [1,3,5] for Mon, Wed, Fri
    
    val mediaUrls: String = "", // JSON: ["url1", "url2"]
    val signatureId: String? = null,
    val notificationEnabled: Boolean = true
)
