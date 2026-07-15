package com.nexusmedia.nexussms.security

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

data class KeyBundle(
    val deviceId: String,
    val publicKey: ByteArray,
    val privateKey: ByteArray,
    val createdAt: Long = System.currentTimeMillis(),
    val keyVersion: Int = 1
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as KeyBundle
        return deviceId == other.deviceId && publicKey.contentEquals(other.publicKey)
    }

    override fun hashCode(): Int {
        var result = deviceId.hashCode()
        result = 31 * result + publicKey.contentHashCode()
        return result
    }
}

data class KeyExchangeMessage(
    val type: String = "KEY_EXCHANGE",
    val deviceId: String,
    val publicKey: ByteArray,
    val keyVersion: Int,
    val timestamp: Long = System.currentTimeMillis()
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as KeyExchangeMessage
        return deviceId == other.deviceId && publicKey.contentEquals(other.publicKey)
    }

    override fun hashCode(): Int {
        var result = deviceId.hashCode()
        result = 31 * result + publicKey.contentHashCode()
        return result
    }
}

@Singleton
class KeyExchangeManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val safetyNumberManager: SafetyNumberManager
) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val encryptedPrefs = EncryptedSharedPreferences.create(
        context,
        "key_exchange_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    private val gson = Gson()

    fun generateKeyBundle(): KeyBundle {
        val deviceId = getOrCreateDeviceId()
        val keyPair = safetyNumberManager.generateRandomKey()
        val privateKey = safetyNumberManager.generateRandomKey()
        return KeyBundle(
            deviceId = deviceId,
            publicKey = keyPair,
            privateKey = privateKey
        )
    }

    fun processKeyExchange(contactId: String, message: KeyExchangeMessage): KeyBundle? {
        val existingKeys = getReceivedKeys(contactId).toMutableList()
        if (existingKeys.any { it.deviceId == message.deviceId && it.keyVersion == message.keyVersion }) {
            return null
        }
        existingKeys.add(KeyBundle(
            deviceId = message.deviceId,
            publicKey = message.publicKey,
            privateKey = ByteArray(0),
            createdAt = message.timestamp,
            keyVersion = message.keyVersion
        ))
        saveReceivedKeys(contactId, existingKeys)
        val myKeyBundle = getMyKeyBundle()
        if (myKeyBundle != null) {
            val safetyNumber = safetyNumberManager.generateSafetyNumber(
                contactId = contactId,
                myPublicKey = myKeyBundle.publicKey,
                theirPublicKey = message.publicKey
            )
            safetyNumberManager.storeSafetyNumber(safetyNumber)
        }
        return existingKeys.last()
    }

    fun getMyKeyBundle(): KeyBundle? {
        val json = encryptedPrefs.getString("my_key_bundle", null) ?: return null
        return gson.fromJson(json, KeyBundle::class.java)
    }

    fun saveMyKeyBundle(keyBundle: KeyBundle) {
        val json = gson.toJson(keyBundle)
        encryptedPrefs.edit().putString("my_key_bundle", json).apply()
    }

    fun getReceivedKeys(contactId: String): List<KeyBundle> {
        val json = encryptedPrefs.getString("received_keys_$contactId", null) ?: return emptyList()
        val type = object : TypeToken<List<KeyBundle>>() {}.type
        return gson.fromJson(json, type)
    }

    private fun saveReceivedKeys(contactId: String, keys: List<KeyBundle>) {
        val json = gson.toJson(keys)
        encryptedPrefs.edit().putString("received_keys_$contactId", json).apply()
    }

    private fun getOrCreateDeviceId(): String {
        var deviceId = encryptedPrefs.getString("device_id", null)
        if (deviceId == null) {
            deviceId = "device_${System.currentTimeMillis()}_${(Math.random() * 10000).toInt()}"
            encryptedPrefs.edit().putString("device_id", deviceId).apply()
        }
        return deviceId
    }

    fun getKeyVersion(contactId: String): Int {
        val keys = getReceivedKeys(contactId)
        return keys.maxOfOrNull { it.keyVersion } ?: 0
    }

    fun getLatestKey(contactId: String): KeyBundle? {
        return getReceivedKeys(contactId).maxByOrNull { it.keyVersion }
    }

    fun removeKey(contactId: String, deviceId: String) {
        val keys = getReceivedKeys(contactId).toMutableList()
        keys.removeAll { it.deviceId == deviceId }
        saveReceivedKeys(contactId, keys)
    }

    fun clearAllKeys(contactId: String) {
        saveReceivedKeys(contactId, emptyList())
        safetyNumberManager.clearVerification(contactId)
    }
}
