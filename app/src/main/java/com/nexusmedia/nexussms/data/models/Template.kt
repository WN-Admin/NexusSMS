package com.nexusmedia.nexussms.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "templates")
data class Template(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val content: String,
    val category: String = "General",
    val usageCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
