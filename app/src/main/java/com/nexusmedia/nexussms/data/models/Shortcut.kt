package com.nexusmedia.nexussms.data.models

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "shortcuts",
    indices = [
        Index("trigger", unique = true),
        Index("createdAt")
    ]
)
data class Shortcut(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    
    val trigger: String, // e.g., "!ato", "@brb"
    val expansion: String,
    val description: String = "",
    val category: String = "General",
    
    val isActive: Boolean = true,
    val usageCount: Int = 0,
    val lastUsed: Long? = null,
    
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    
    // Per-contact overrides
    val contactPhoneNumber: String? = null, // null = global shortcut
    
    val priority: Int = 0 // Higher priority = checked first
)
