package com.nexusmedia.nexussms.security.e2e

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.nexusmedia.nexussms.security.EncryptionManager
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import java.security.SecureRandom
import javax.inject.Inject
import javax.inject.Singleton

data class LocalIdentity(
    val identityKeyPair: RatchetKeyPair,
    val signedPrekey: RatchetKeyPair,
    val signedPrekeyId: Int,
    val oneTimePrekeys: List<RatchetKeyPair>,
    val oneTimePrekeyStartId: Int,
    val deviceId: String,
    val registeredWithServer: Boolean = false,
    val serverApiKey: String? = null,
    val serverUrl: String? = null
)

@Singleton
class E2EKeyManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val encryptionManager: EncryptionManager
) {
    private val gson = Gson()
    private val secureRandom = SecureRandom()
    private val prefs: SharedPreferences by lazy { createEncryptedPrefs() }

    private fun createEncryptedPrefs(): SharedPreferences {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        return EncryptedSharedPreferences.create(
            context,
            "e2e_key_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    private fun generateRandomHex(byteCount: Int = 16): String {
        val bytes = ByteArray(byteCount)
        secureRandom.nextBytes(bytes)
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun getOrCreateIdentity(): LocalIdentity {
        val existing = getIdentityOrNull()
        if (existing != null) {
            return existing
        }

        Timber.d("Generating new E2E identity")

        val (identityPub, identityPriv) = CryptoPrimitives.generateX25519KeyPair()
        val identity = RatchetKeyPair(identityPub, identityPriv)

        val (signedPub, signedPriv) = CryptoPrimitives.generateX25519KeyPair()
        val signedPrekey = RatchetKeyPair(signedPub, signedPriv)

        val oneTimePrekeys = (0 until DEFAULT_OTK_COUNT).map {
            val (pub, priv) = CryptoPrimitives.generateX25519KeyPair()
            RatchetKeyPair(pub, priv)
        }

        val localIdentity = LocalIdentity(
            identityKeyPair = identity,
            signedPrekey = signedPrekey,
            signedPrekeyId = 1,
            oneTimePrekeys = oneTimePrekeys,
            oneTimePrekeyStartId = 1,
            deviceId = generateRandomHex()
        )

        persistIdentity(localIdentity)
        Timber.d("E2E identity created with device ID: ${localIdentity.deviceId}, ${oneTimePrekeys.size} OTKs")
        return localIdentity
    }

    fun getIdentityOrNull(): LocalIdentity? {
        val json = prefs.getString(KEY_IDENTITY, null) ?: return null
        return try {
            val type = object : TypeToken<LocalIdentity>() {}.type
            gson.fromJson(json, type)
        } catch (e: Exception) {
            Timber.e(e, "Failed to deserialize E2E identity")
            null
        }
    }

    fun generateNewSignedPrekey(): RatchetKeyPair {
        val identity = getIdentityOrNull()
            ?: throw IllegalStateException("No identity — call getOrCreateIdentity() first")

        val (pub, priv) = CryptoPrimitives.generateX25519KeyPair()
        val newSignedPrekey = RatchetKeyPair(pub, priv)
        val newId = identity.signedPrekeyId + 1

        val updated = identity.copy(
            signedPrekey = newSignedPrekey,
            signedPrekeyId = newId
        )
        persistIdentity(updated)

        Timber.d("New signed prekey generated (ID: $newId)")
        return newSignedPrekey
    }

    fun generateOneTimePrekeys(count: Int): List<RatchetKeyPair> {
        val identity = getIdentityOrNull()
            ?: throw IllegalStateException("No identity — call getOrCreateIdentity() first")

        val newKeys = (0 until count).map {
            val (pub, priv) = CryptoPrimitives.generateX25519KeyPair()
            RatchetKeyPair(pub, priv)
        }

        val updated = identity.copy(
            oneTimePrekeys = identity.oneTimePrekeys + newKeys
        )
        persistIdentity(updated)

        Timber.d("Generated $count new one-time prekeys (pool size: ${updated.oneTimePrekeys.size})")
        return newKeys
    }

    fun consumeOneTimePrekey(): RatchetKeyPair? {
        val identity = getIdentityOrNull() ?: return null
        if (identity.oneTimePrekeys.isEmpty()) return null

        val consumed = identity.oneTimePrekeys.first()
        val remaining = identity.oneTimePrekeys.drop(1)

        val updated = identity.copy(oneTimePrekeys = remaining)
        persistIdentity(updated)

        Timber.d("Consumed one-time prekey (pool size: ${remaining.size})")
        return consumed
    }

    fun getSignedPrekeyBundle(): PreKeyBundle {
        val identity = getIdentityOrNull()
            ?: throw IllegalStateException("No identity — call getOrCreateIdentity() first")

        val firstOtk = identity.oneTimePrekeys.firstOrNull()

        return PreKeyBundle(
            identityKey = identity.identityKeyPair.publicKey,
            signedPrekeyPublic = identity.signedPrekey.publicKey,
            signedPrekeySignature = CryptoPrimitives.randomBytes(64),
            oneTimePrekeyPublic = firstOtk?.publicKey
        )
    }

    fun markRegistered(serverApiKey: String, serverUrl: String) {
        val identity = getIdentityOrNull()
            ?: throw IllegalStateException("No identity — call getOrCreateIdentity() first")

        val updated = identity.copy(
            registeredWithServer = true,
            serverApiKey = serverApiKey,
            serverUrl = serverUrl
        )
        persistIdentity(updated)

        Timber.d("Identity marked as registered with server: $serverUrl")
    }

    fun updateServerUrl(url: String) {
        val identity = getIdentityOrNull()
            ?: throw IllegalStateException("No identity — call getOrCreateIdentity() first")

        val updated = identity.copy(serverUrl = url)
        persistIdentity(updated)

        Timber.d("Server URL updated: $url")
    }

    private fun persistIdentity(identity: LocalIdentity) {
        try {
            val json = gson.toJson(identity)
            prefs.edit().putString(KEY_IDENTITY, json).apply()
        } catch (e: Exception) {
            Timber.e(e, "Failed to persist E2E identity")
        }
    }

    companion object {
        private const val KEY_IDENTITY = "local_identity"
        private const val DEFAULT_OTK_COUNT = 10
    }
}
