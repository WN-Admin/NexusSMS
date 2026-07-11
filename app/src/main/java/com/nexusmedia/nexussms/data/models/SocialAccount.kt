package com.nexusmedia.nexussms.data.models

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "social_accounts",
    indices = [
        Index("userId", unique = true),
        Index("platform")
    ]
)
data class SocialAccount(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    
    val platform: String, // DISCORD, TELEGRAM, FACEBOOK_MESSENGER, MATRIX, VIBER
    val userId: String,
    val username: String,
    val displayName: String,
    val avatarUrl: String? = null,
    
    val accessToken: String, // Encrypted
    val refreshToken: String? = null, // Encrypted
    val tokenExpiresAt: Long? = null,
    
    val isConnected: Boolean = true,
    val lastSyncTime: Long? = null,
    
    val settings: String = "", // JSON: platform-specific settings
    
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
