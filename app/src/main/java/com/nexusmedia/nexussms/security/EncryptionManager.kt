package com.nexusmedia.nexussms.security

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import java.security.KeyStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EncryptionManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val encryptedSharedPreferences = EncryptedSharedPreferences.create(
        context,
        "secret_shared_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun encryptAES256(plaintext: String): String {
        val secretKey = getOrCreateAESKey()
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        val iv = cipher.iv
        val encryptedBytes = cipher.doFinal(plaintext.toByteArray(Charsets.UTF_8))
        val combined = iv + encryptedBytes
        return Base64.encodeToString(combined, Base64.DEFAULT)
    }

    fun decryptAES256(encryptedText: String): String {
        val secretKey = getOrCreateAESKey()
        val combined = Base64.decode(encryptedText, Base64.DEFAULT)
        val iv = combined.sliceArray(0 until 12)
        val encryptedBytes = combined.sliceArray(12 until combined.size)

        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val spec = GCMParameterSpec(128, iv)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)
        return String(cipher.doFinal(encryptedBytes), Charsets.UTF_8)
    }

    fun storeSecureData(key: String, value: String) {
        encryptedSharedPreferences.edit().putString(key, value).apply()
    }

    fun retrieveSecureData(key: String): String? {
        return encryptedSharedPreferences.getString(key, null)
    }

    fun deleteSecureData(key: String) {
        encryptedSharedPreferences.edit().remove(key).apply()
    }

    private fun getOrCreateAESKey(): SecretKey {
        val keyStore = KeyStore.getInstance("AndroidKeyStore")
        keyStore.load(null)

        return if (keyStore.containsAlias("nexussms_aes_key")) {
            keyStore.getKey("nexussms_aes_key", null) as SecretKey
        } else {
            val keyGenerator = KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES,
                "AndroidKeyStore"
            )
            val keySpec = KeyGenParameterSpec.Builder(
                "nexussms_aes_key",
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setRandomizedEncryptionRequired(true)
                .build()

            keyGenerator.init(keySpec)
            keyGenerator.generateKey()
        }
    }

    fun setSignature(signature: String) {
        storeSecureData("user_signature", signature)
    }

    fun getSignature(): String {
        return retrieveSecureData("user_signature") ?: ""
    }

    fun generateMessageSignature(message: String): String {
        val signature = getSignature()
        return if (signature.isNotEmpty()) {
            "$message\n\n$signature"
        } else {
            message
        }
    }

    fun encryptWithKey(plaintext: String, key: SecretKey): String {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key)
        val iv = cipher.iv
        val encryptedBytes = cipher.doFinal(plaintext.toByteArray(Charsets.UTF_8))
        val combined = iv + encryptedBytes
        return Base64.encodeToString(combined, Base64.DEFAULT)
    }

    fun decryptWithKey(encryptedText: String, key: SecretKey): String {
        val combined = Base64.decode(encryptedText, Base64.DEFAULT)
        val iv = combined.sliceArray(0 until 12)
        val encryptedBytes = combined.sliceArray(12 until combined.size)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val spec = GCMParameterSpec(128, iv)
        cipher.init(Cipher.DECRYPT_MODE, key, spec)
        return String(cipher.doFinal(encryptedBytes), Charsets.UTF_8)
    }

    fun shouldEncryptForContact(phoneNumber: String): Boolean {
        val encryptedContacts = retrieveSecureData("encrypted_contacts")
        return encryptedContacts?.contains(phoneNumber) == true
    }

    fun setEncryptForContact(phoneNumber: String, encrypt: Boolean) {
        val current = retrieveSecureData("encrypted_contacts") ?: ""
        val contacts = if (current.isBlank()) emptySet() else current.split(",").toSet()
        val updated = if (encrypt) contacts + phoneNumber else contacts - phoneNumber
        storeSecureData("encrypted_contacts", updated.joinToString(","))
    }

    fun isEncryptedMessage(message: String): Boolean {
        return message.startsWith("ENC:")
    }
}
