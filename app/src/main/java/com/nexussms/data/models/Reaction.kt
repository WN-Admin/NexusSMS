package com.nexussms.data.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "reactions",
    primaryKeys = ["messageId", "emoji", "senderPhoneNumber"],
    foreignKeys = [
        ForeignKey(
            entity = Message::class,
            parentColumns = ["id"],
            childColumns = ["messageId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("messageId"), Index("emoji")]
)
data class Reaction(
    val messageId: String,
    val emoji: String,
    val senderPhoneNumber: String,
    val timestamp: Long = System.currentTimeMillis()
)
