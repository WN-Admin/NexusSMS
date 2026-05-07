package com.nexussms.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_security_settings")
data class AppSecuritySettings(
    @PrimaryKey
    val id: String = "default",
    
    // Biometric Settings
    val biometricEnabled: Boolean = false,
    val biometricType: String = "FINGERPRINT", // FINGERPRINT, FACE, IRIS, MULTIPLE
    val requireBiometricOnStartup: Boolean = false,
    val requireBiometricForSensitiveActions: Boolean = false,
    val biometricTimeout: Long = 300000, // 5 minutes in ms
    
    // App Lock
    val appLockEnabled: Boolean = false,
    val appLockType: String = "PIN", // PIN, PATTERN, PASSWORD, BIOMETRIC
    val appLockValue: String? = null, // Encrypted PIN/pattern hash
    val appLockTimeout: Long = 300000, // 5 minutes
    
    // Sensitive Actions requiring biometric
    val requireBiometricForRead: Boolean = false,
    val requireBiometricForSend: Boolean = false,
    val requireBiometricForDelete: Boolean = false,
    val requireBiometricForForward: Boolean = false,
    
    // Session
    val lastAuthTime: Long = 0,
    val isSessionLocked: Boolean = false,
    
    // Settings
    val hideMessages: Boolean = false, // Hide content in lock screen
    val hideNotificationContent: Boolean = false,
    val disableScreenshots: Boolean = false,
    
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
