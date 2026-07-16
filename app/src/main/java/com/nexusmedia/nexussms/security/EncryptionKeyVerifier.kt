package com.nexusmedia.nexussms.security

import android.util.Base64
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
    private val keyExchangeManager: KeyExchangeManager
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
        val hasKeys = keyExchangeManager.getReceivedKeys(contactId).isNotEmpty()
        val safetyNumber = safetyNumberManager.getSafetyNumber(contactId)

        return when {
            isVerified -> VerificationStatus.Verified
            hasKeys -> VerificationStatus.Unverified
            safetyNumber != null -> VerificationStatus.PendingVerification
            else -> VerificationStatus.NoKeys
        }
    }
}
