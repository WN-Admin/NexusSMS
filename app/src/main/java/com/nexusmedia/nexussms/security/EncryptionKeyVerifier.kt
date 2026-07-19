package com.nexusmedia.nexussms.security

import android.util.Base64
import com.nexusmedia.nexussms.security.e2e.E2EKeyManager
import com.nexusmedia.nexussms.security.e2e.E2ESessionManager
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

enum class VerificationResult {
    Verified,
    Unverified,
    NewKey,
    KeyChanged
}

enum class VerificationStatus {
    Verified,
    Unverified,
    PendingVerification,
    NoKeys
}

@Singleton
class EncryptionKeyVerifier @Inject constructor(
    private val safetyNumberManager: SafetyNumberManager,
    private val keyExchangeManager: KeyExchangeManager,
    private val e2eKeyManager: E2EKeyManager,
    private val e2eSessionManager: E2ESessionManager
) {
    fun verifyContact(contactId: String, theirPublicKeyBytes: ByteArray): VerificationResult {
        val existingKeys = keyExchangeManager.getReceivedKeys(contactId)

        if (existingKeys.isEmpty()) {
            return VerificationResult.NewKey
        }

        val theirPublicKeyBase64 = Base64.encodeToString(theirPublicKeyBytes, Base64.NO_WRAP)
        val matchingKey = existingKeys.find {
            it.publicKey == theirPublicKeyBase64
        }

        return if (matchingKey != null) {
            if (safetyNumberManager.isVerified(contactId)) {
                VerificationResult.Verified
            } else {
                VerificationResult.Unverified
            }
        } else {
            VerificationResult.KeyChanged
        }
    }

    fun getKeyFingerprint(publicKey: ByteArray): String {
        return safetyNumberManager.generateSafetyNumber(
            contactId = "",
            myPublicKey = publicKey,
            theirPublicKey = publicKey
        ).myFingerprint
    }

    fun getVerificationStatus(contactId: String): VerificationStatus {
        val isVerified = safetyNumberManager.isVerified(contactId)
        val hasLegacyKeys = keyExchangeManager.getReceivedKeys(contactId).isNotEmpty()
        val hasE2eSession = run {
            val identity = e2eKeyManager.getIdentityOrNull()
            identity != null
        }
        val safetyNumber = safetyNumberManager.getSafetyNumber(contactId)

        return when {
            isVerified -> VerificationStatus.Verified
            hasLegacyKeys || hasE2eSession -> VerificationStatus.Unverified
            safetyNumber != null -> VerificationStatus.PendingVerification
            else -> VerificationStatus.NoKeys
        }
    }

    suspend fun getMyE2EIdentityPublicKey(): ByteArray? {
        return e2eKeyManager.getIdentityOrNull()?.identityKeyPair?.publicKey
    }

    fun hasE2ESessionForContact(contactId: String): Boolean {
        return run {
            val identity = e2eKeyManager.getIdentityOrNull() ?: return@run false
            identity.registeredWithServer
        }
    }
}
