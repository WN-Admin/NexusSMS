package com.nexusmedia.nexussms.security.e2e

import android.content.Context
import android.util.Base64
import com.nexusmedia.nexussms.data.database.E2ESessionDao
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class E2ESessionManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val e2eKeyManager: E2EKeyManager,
    private val preKeyServerClient: PreKeyServerClient,
    private val e2eSessionDao: E2ESessionDao
) {
    private val sessionCache = mutableMapOf<String, RatchetSessionState>()

    suspend fun encryptMessage(contactId: String, plaintext: String): String? = withContext(Dispatchers.IO) {
        try {
            var state = sessionCache[contactId] ?: loadAndCacheSession(contactId)
            if (state == null) {
                Timber.w("No E2E session for %s, cannot encrypt", contactId)
                return@withContext null
            }
            val (message, newState) = DoubleRatchet.encrypt(state, plaintext.toByteArray(Charsets.UTF_8))
            persistState(contactId, newState)
            val wireBase64 = Base64.encodeToString(message.toWireFormat(), Base64.NO_WRAP)
            "E2E:$wireBase64"
        } catch (e: Exception) {
            Timber.e(e, "E2E encrypt failed for %s", contactId)
            null
        }
    }

    suspend fun prepareFirstMessage(contactId: String, plaintext: String, serverUrl: String): String? = withContext(Dispatchers.IO) {
        try {
            val identity = e2eKeyManager.getOrCreateIdentity()

            val bundleResult = preKeyServerClient.fetchBundle(serverUrl, identity.identityKeyPair.publicKey.let {
                Base64.encodeToString(it, Base64.NO_WRAP)
            })
            if (bundleResult.isFailure) {
                Timber.w(bundleResult.exceptionOrNull(), "Failed to fetch bundle for %s", contactId)
                return@withContext null
            }
            val bundle = bundleResult.getOrThrow()

            val prekeyBundle = PreKeyBundle(
                identityKey = Base64.decode(bundle.identityKey, Base64.NO_WRAP),
                signedPrekeyPublic = Base64.decode(bundle.signedPrekey.publicKey, Base64.NO_WRAP),
                signedPrekeySignature = CryptoPrimitives.randomBytes(64),
                oneTimePrekeyPublic = bundle.oneTimePrekey?.publicKey?.let { Base64.decode(it, Base64.NO_WRAP) }
            )

            val aliceIdentity = RatchetKeyPair(identity.identityKeyPair.publicKey, identity.identityKeyPair.privateKey)
            val (ephPub, ephPriv) = CryptoPrimitives.generateX25519KeyPair()
            val ephemeral = RatchetKeyPair(ephPub, ephPriv)

            val x3dhResult = X3DH.computeAsAlice(aliceIdentity, prekeyBundle, ephemeral)

            val (rPub, rPriv) = CryptoPrimitives.generateX25519KeyPair()
            val ratchetKp = RatchetKeyPair(rPub, rPriv)

            val aliceState = DoubleRatchet.initAlice(
                sharedSecret = x3dhResult.sharedSecret,
                aliceIdentityKey = aliceIdentity.publicKey,
                bobIdentityKey = prekeyBundle.identityKey,
                ratchetKeyPair = ratchetKp,
                bobRatchetPublicKey = prekeyBundle.signedPrekeyPublic
            )

            val (message, newState) = DoubleRatchet.encrypt(aliceState, plaintext.toByteArray(Charsets.UTF_8))
            persistState(contactId, newState)

            val initJson = """{"ik":"${Base64.encodeToString(aliceIdentity.publicKey, Base64.NO_WRAP)}","ek":"${Base64.encodeToString(ephemeral.publicKey, Base64.NO_WRAP)}","spkId":${bundle.signedPrekey.id}}"""
            val ratchetWire = Base64.encodeToString(message.toWireFormat(), Base64.NO_WRAP)
            "E2E:INIT:$initJson:$ratchetWire"
        } catch (e: Exception) {
            Timber.e(e, "E2E prepareFirstMessage failed for %s", contactId)
            null
        }
    }

    suspend fun decryptMessage(contactId: String, senderPhone: String, rawBody: String): String? = withContext(Dispatchers.IO) {
        try {
            when {
                rawBody.startsWith("E2E:INIT:") -> handleInitMessage(contactId, senderPhone, rawBody)
                rawBody.startsWith("E2E:") -> handleRatchetMessage(contactId, rawBody)
                else -> null
            }
        } catch (e: Exception) {
            Timber.e(e, "E2E decrypt failed for %s", senderPhone)
            null
        }
    }

    private suspend fun handleInitMessage(contactId: String, senderPhone: String, rawBody: String): String? {
        val afterInit = rawBody.removePrefix("E2E:INIT:")
        val colonIdx = afterInit.indexOf(':')
        if (colonIdx < 0) return null

        val initJson = afterInit.substring(0, colonIdx)
        val wireBase64 = afterInit.substring(colonIdx + 1)

        val gson = com.google.gson.Gson()
        val initData = gson.fromJson(initJson, Map::class.java)
        val aliceIdentityKey = Base64.decode(initData["ik"] as String, Base64.NO_WRAP)
        val aliceEphemeralKey = Base64.decode(initData["ek"] as String, Base64.NO_WRAP)

        val ratchetMsg = RatchetMessage.fromWireFormat(Base64.decode(wireBase64, Base64.NO_WRAP)) ?: return null

        val identity = e2eKeyManager.getOrCreateIdentity()
        val bobIdentity = RatchetKeyPair(identity.identityKeyPair.publicKey, identity.identityKeyPair.privateKey)
        val bobSignedPrekey = RatchetKeyPair(identity.signedPrekey.publicKey, identity.signedPrekey.privateKey)

        val x3dhResult = X3DH.computeAsBob(
            aliceIdentityKey = aliceIdentityKey,
            ephemeralPublicKey = aliceEphemeralKey,
            bobIdentityKey = bobIdentity,
            bobSignedPrekey = bobSignedPrekey,
            bobOneTimePrekey = null
        )

        val (bobRPub, bobRPriv) = CryptoPrimitives.generateX25519KeyPair()
        val bobRatchetKp = RatchetKeyPair(bobRPub, bobRPriv)
        val bobState = DoubleRatchet.initBob(
            sharedSecret = x3dhResult.sharedSecret,
            aliceIdentityKey = aliceIdentityKey,
            bobIdentityKey = bobIdentity.publicKey,
            ratchetKeyPair = bobRatchetKp
        )

        val decryptResult = DoubleRatchet.decrypt(bobState, ratchetMsg) ?: return null
        val (plaintext, newState) = decryptResult
        persistState(contactId, newState)

        return String(plaintext, Charsets.UTF_8)
    }

    private suspend fun handleRatchetMessage(contactId: String, rawBody: String): String? {
        val wireBase64 = rawBody.removePrefix("E2E:")
        val wireBytes = Base64.decode(wireBase64, Base64.NO_WRAP)
        val ratchetMsg = RatchetMessage.fromWireFormat(wireBytes) ?: return null

        val state = sessionCache[contactId] ?: loadAndCacheSession(contactId) ?: return null
        val (plaintext, newState) = DoubleRatchet.decrypt(state, ratchetMsg) ?: return null
        persistState(contactId, newState)

        return String(plaintext, Charsets.UTF_8)
    }

    suspend fun hasActiveSession(contactId: String): Boolean {
        return sessionCache.containsKey(contactId) || e2eSessionDao.getSession(contactId) != null
    }

    suspend fun getPeerIdentityKey(contactId: String): ByteArray? {
        val state = sessionCache[contactId] ?: loadAndCacheSession(contactId) ?: return null
        return state.aliceIdentityKey.takeIf { it.isNotEmpty() }
            ?: state.bobIdentityKey.takeIf { it.isNotEmpty() }
    }

    fun getMyIdentityKey(): ByteArray? {
        return e2eKeyManager.getIdentityOrNull()?.identityKeyPair?.publicKey
    }

    suspend fun deleteSession(contactId: String) {
        sessionCache.remove(contactId)
        e2eSessionDao.deleteSession(contactId)
    }

    private suspend fun persistState(contactId: String, state: RatchetSessionState) {
        sessionCache[contactId] = state
        e2eSessionDao.upsert(E2ESessionEntity.fromState(contactId, state))
    }

    private suspend fun loadAndCacheSession(contactId: String): RatchetSessionState? {
        val entity = e2eSessionDao.getSession(contactId) ?: return null
        val state = E2ESessionEntity.toState(entity)
        sessionCache[contactId] = state
        return state
    }
}
