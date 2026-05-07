package com.nexussms.data.models

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "signatures",
    indices = [Index("isDefault", orders = [Index.Order.DESC])]
)
data class Signature(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    
    val name: String,
    val content: String,
    val isDefault: Boolean = false,
    
    val format: String = "TEXT", // TEXT, HTML, RICH_TEXT
    val fontFamily: String? = null,
    val fontSize: Int = 12,
    
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
