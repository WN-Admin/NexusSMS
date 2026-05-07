package com.nexussms.features.backup.models

data class BackupData(
    val timestamp: Long,
    val version: String = "1.0",
    val shortcuts: List<ShortcutData>,
    val signatures: List<SignatureData>,
    val themes: List<ThemeData>
)

data class ShortcutData(
    val trigger: String,
    val expansion: String,
    val description: String = "",
    val category: String = "General",
    val isActive: Boolean = true,
    val priority: Int = 0
)

data class SignatureData(
    val name: String,
    val content: String,
    val isDefault: Boolean = false,
    val format: String = "TEXT",
    val fontFamily: String? = null,
    val fontSize: Int = 12
)

data class ThemeData(
    val name: String,
    val primaryColor: String,
    val secondaryColor: String,
    val backgroundColor: String,
    val surfaceColor: String,
    val textColor: String,
    val bubbleColorSent: String,
    val bubbleColorReceived: String,
    val isDarkMode: Boolean = false,
    val bubbleStyle: String = "ROUNDED"
)
