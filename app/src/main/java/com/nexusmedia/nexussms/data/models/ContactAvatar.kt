package com.nexusmedia.nexussms.data.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "contact_avatars")
data class ContactAvatar(
    @PrimaryKey
    @ColumnInfo(name = "normalized_phone")
    val normalizedPhone: String,

    @ColumnInfo(name = "photo_uri")
    val photoUri: String? = null,

    @ColumnInfo(name = "display_name")
    val displayName: String? = null,

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)
