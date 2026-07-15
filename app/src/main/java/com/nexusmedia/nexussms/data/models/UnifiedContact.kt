package com.nexusmedia.nexussms.data.models

import androidx.room.*

@Entity(
    tableName = "unified_contacts",
    indices = [
        Index("displayName"),
        Index("isFavorite")
    ]
)
data class UnifiedContact(
    @PrimaryKey
    val id: String = java.util.UUID.randomUUID().toString(),

    val displayName: String,

    // JSON array of phone numbers: ["+15551234567", "+15559876543"]
    val phoneNumbers: String = "[]",

    // JSON array of platform identities
    val platformIdentities: String = "[]",

    val avatarUri: String? = null,

    val isFavorite: Boolean = false,
    val isHidden: Boolean = false,

    val notes: String? = null,

    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

data class PlatformIdentity(
    val platform: String,
    val id: String,
    val username: String?,
    val displayName: String? = null,
    val avatarUrl: String? = null,
    val isPrimary: Boolean = false
)
