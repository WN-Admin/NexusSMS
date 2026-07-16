package com.nexusmedia.nexussms.security

import android.content.Context
import android.util.Base64
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import java.security.KeyFactory
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.interfaces.ECPublicKey
import java.security.spec.ECGenParameterSpec
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import javax.crypto.KeyAgreement
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject
import javax.inject.Singleton

data class KeyBundle(
    val deviceId: String,
    val publicKey: String,  // Base64-encoded X.509 DER
    val privateKey: String, // Base64-encoded PKCS8 DER
    val createdAt: Long = System.currentTimeMillis(),
    val keyVersion: Int = 1
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as KeyBundle
        return deviceId == other.deviceId && publicKey == other.publicKey
    }

    override fun hashCode(): Int {
        var result = deviceId.hashCode()
        result = 31 * result + publicKey.hashCode()
        return result
    }
}

data class KeyExchangeMessage(
    val type: String = "KEY_EXCHANGE",
    val deviceId: String,
    val publicKey: String,  // Base64-encoded X.509 DER
    val keyVersion: Int,
    val timestamp: Long = System.currentTimeMillis()
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as KeyExchangeMessage
        return deviceId == other.deviceId && publicKey == other.publicKey
    }

    override fun hashCode(): Int {
        var result = deviceId.hashCode()
        result = 31 * result + publicKey.hashCode()
        return result
    }
}

@Singleton
class KeyExchangeManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val safetyNumberManager: SafetyNumberManager
) {
    companion object {
        private const val TAG = "KeyExchangeManager"
        private const val CURVE = "secp256r1" // NIST P-256
    }

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
        val kpg = KeyPairGenerator.getInstance("EC")
        kpg.initialize(ECGenParameterSpec(CURVE))
        val keyPair = kpg.generateKeyPair()

        val publicKeyBase64 = Base64.encodeToString(
            keyPair.public.encoded, Base64.NO_WRAP
        )
        val privateKeyBase64 = Base64.encodeToString(
            keyPair.private.encoded, Base64.NO_WRAP
        )

        val bundle = KeyBundle(
            deviceId = deviceId,
            publicKey = publicKeyBase64,
            privateKey = privateKeyBase64
        )
        saveMyKeyBundle(bundle)
        Timber.d("Generated new EC keypair for device %s", deviceId)
        return bundle
    }

    fun processKeyExchange(contactId: String, message: KeyExchangeMessage): KeyBundle? {
        val existingKeys = getReceivedKeys(contactId).toMutableList()
        if (existingKeys.any { it.deviceId == message.deviceId && it.keyVersion == message.keyVersion }) {
            return null
        }
        existingKeys.add(KeyBundle(
            deviceId = message.deviceId,
            publicKey = message.publicKey,
            privateKey = "",
            createdAt = message.timestamp,
            keyVersion = message.keyVersion
        ))
        saveReceivedKeys(contactId, existingKeys)

        val myKeyBundle = getMyKeyBundle()
        if (myKeyBundle != null) {
            val myPublicKey = parsePublicKey(myKeyBundle.publicKey)
            val theirPublicKey = parsePublicKey(message.publicKey)
            if (myPublicKey != null && theirPublicKey != null) {
                val safetyNumber = safetyNumberManager.generateSafetyNumber(
                    contactId = contactId,
                    myPublicKey = myPublicKey.encoded,
                    theirPublicKey = theirPublicKey.encoded
                )
                safetyNumberManager.storeSafetyNumber(safetyNumber)
            }
        }
        return existingKeys.last()
    }

    fun deriveSharedSecret(contactId: String): SecretKey? {
        val myBundle = getMyKeyBundle() ?: return null
        val theirKey = getLatestKey(contactId) ?: return null

        val myPrivateKey = parsePrivateKey(myBundle.privateKey) ?: return null
        val theirPublicKey = parsePublicKey(theirKey.publicKey) ?: return null

        return deriveECDHSecret(myPrivateKey, theirPublicKey)
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

    private fun parsePublicKey(base64: String): java.security.PublicKey? {
        return try {
            val bytes = Base64.decode(base64, Base64.NO_WRAP)
            val spec = X509EncodedKeySpec(bytes)
            KeyFactory.getInstance("EC").generatePublic(spec)
        } catch (e: Exception) {
            Timber.e(e, "Failed to parse public key")
            null
        }
    }

    private fun parsePrivateKey(base64: String): java.security.PrivateKey? {
        return try {
            val bytes = Base64.decode(base64, Base64.NO_WRAP)
            val spec = PKCS8EncodedKeySpec(bytes)
            KeyFactory.getInstance("EC").generatePrivate(spec)
        } catch (e: Exception) {
            Timber.e(e, "Failed to parse private key")
            null
        }
    }

    private fun deriveECDHSecret(
        myPrivateKey: java.security.PrivateKey,
        theirPublicKey: java.security.PublicKey
    ): SecretKey {
        val keyAgreement = KeyAgreement.getInstance("ECDH")
        keyAgreement.init(myPrivateKey)
        keyAgreement.doPhase(theirPublicKey, true)
        val sharedSecret = keyAgreement.generateSecret()

        val digest = java.security.MessageDigest.getInstance("SHA-256")
        val derivedKeyBytes = digest.digest(sharedSecret)
        return SecretKeySpec(derivedKeyBytes, "AES")
    }
}
