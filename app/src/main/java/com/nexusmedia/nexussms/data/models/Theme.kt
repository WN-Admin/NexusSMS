package com.nexusmedia.nexussms.data.models

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "themes",
    indices = [Index("isDefault", orders = [Index.Order.DESC])]
)
data class Theme(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    
    val name: String,
    val isDefault: Boolean = false,
    val isCustom: Boolean = true,
    
    // Color Scheme
    val primaryColor: String, // #121212
    val secondaryColor: String, // #03DAC6
    val backgroundColor: String,
    val surfaceColor: String = backgroundColor,
    val errorColor: String = "#B00020",
    
    val textColor: String,
    val textColorSecondary: String = "#757575",
    
    val bubbleColorSent: String,
    val bubbleColorReceived: String,
    val bubbleTextColorSent: String = "#FFFFFF",
    val bubbleTextColorReceived: String = "#000000",
    
    // Typography
    val fontFamily: String = "Inter",
    val fontRegular: String? = null,
    val fontBold: String? = null,
    
    // Bubble Style
    val bubbleStyle: String = "ROUNDED", // ROUNDED, SQUARE, MODERN, SHARP
    val bubbleCornerRadius: Int = 16,
    val bubbleElevation: Float = 4f,
    
    // Additional
    val wallpaperUrl: String? = null,
    val isDarkMode: Boolean = false,
    
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
