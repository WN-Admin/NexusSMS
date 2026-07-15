package com.nexusmedia.nexussms.security

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.security.MessageDigest
import java.security.SecureRandom
import javax.inject.Inject
import javax.inject.Singleton

data class SafetyNumber(
    val contactId: String,
    val myFingerprint: String,
    val theirFingerprint: String,
    val safetyNumber: String,
    val qrCodeData: String,
    val createdAt: Long = System.currentTimeMillis(),
    val isVerified: Boolean = false,
    val verifiedAt: Long? = null,
    val verificationMethod: VerificationMethod? = null
)

enum class VerificationMethod {
    QR_SCAN,
    SAFETY_NUMBER_COMPARE,
    KEY_FINGERPRINT
}

data class KeyFingerprint(
    val deviceId: String,
    val publicKey: ByteArray,
    val algorithm: String = "AES-256-GCM",
    val createdAt: Long = System.currentTimeMillis()
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as KeyFingerprint
        return deviceId == other.deviceId && publicKey.contentEquals(other.publicKey)
    }

    override fun hashCode(): Int {
        var result = deviceId.hashCode()
        result = 31 * result + publicKey.contentHashCode()
        return result
    }
}

@Singleton
class SafetyNumberManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val encryptionManager: EncryptionManager
) {
    companion object {
        private const val SAFETY_NUMBER_LENGTH = 30
        private const val FINGERPRINT_ALGORITHM = "SHA-256"
        private const val QR_PREFIX = "nexussms://verify/"
    }

    private val verifiedKeys = mutableMapOf<String, SafetyNumber>()

    fun generateSafetyNumber(contactId: String, myPublicKey: ByteArray, theirPublicKey: ByteArray): SafetyNumber {
        val myFingerprint = generateFingerprint(myPublicKey)
        val theirFingerprint = generateFingerprint(theirPublicKey)
        val combinedFingerprints = myFingerprint + theirFingerprint
        val safetyNumber = generateSafetyNumberFromFingerprints(combinedFingerprints)
        val qrData = generateQrData(contactId, myFingerprint, theirFingerprint)
        return SafetyNumber(
            contactId = contactId,
            myFingerprint = myFingerprint,
            theirFingerprint = theirFingerprint,
            safetyNumber = safetyNumber,
            qrCodeData = qrData,
            createdAt = System.currentTimeMillis()
        )
    }

    fun verifySafetyNumber(contactId: String, scannedSafetyNumber: String): Boolean {
        val stored = verifiedKeys[contactId] ?: return false
        return stored.safetyNumber == scannedSafetyNumber
    }

    fun verifyQrCode(contactId: String, qrData: String): Boolean {
        val stored = verifiedKeys[contactId] ?: return false
        if (!qrData.startsWith(QR_PREFIX)) return false
        val data = qrData.removePrefix(QR_PREFIX)
        val parts = data.split("/")
        if (parts.size != 3) return false
        val scannedContactId = parts[0]
        val scannedMyFingerprint = parts[1]
        val scannedTheirFingerprint = parts[2]
        if (scannedContactId != contactId) return false
        return scannedMyFingerprint == stored.theirFingerprint &&
               scannedTheirFingerprint == stored.myFingerprint
    }

    fun verifyKeyFingerprint(contactId: String, theirDeviceId: String, theirPublicKey: ByteArray): Boolean {
        val stored = verifiedKeys[contactId] ?: return false
        val fingerprint = generateFingerprint(theirPublicKey)
        return fingerprint == stored.theirFingerprint
    }

    fun markAsVerified(contactId: String, method: VerificationMethod) {
        val current = verifiedKeys[contactId] ?: return
        verifiedKeys[contactId] = current.copy(
            isVerified = true,
            verifiedAt = System.currentTimeMillis(),
            verificationMethod = method
        )
    }

    fun isVerified(contactId: String): Boolean {
        return verifiedKeys[contactId]?.isVerified ?: false
    }

    fun getSafetyNumber(contactId: String): SafetyNumber? {
        return verifiedKeys[contactId]
    }

    fun storeSafetyNumber(safetyNumber: SafetyNumber) {
        verifiedKeys[safetyNumber.contactId] = safetyNumber
    }

    fun clearVerification(contactId: String) {
        verifiedKeys.remove(contactId)
    }

    private fun generateFingerprint(publicKey: ByteArray): String {
        val digest = MessageDigest.getInstance(FINGERPRINT_ALGORITHM)
        val hash = digest.digest(publicKey)
        return hash.joinToString("") { "%02x".format(it) }
    }

    private fun generateSafetyNumberFromFingerprints(fingerprints: String): String {
        val digest = MessageDigest.getInstance(FINGERPRINT_ALGORITHM)
        val hash = digest.digest(fingerprints.toByteArray())
        val numberBuilder = StringBuilder()
        for (i in 0 until 12) {
            val byte = hash[i].toInt() and 0xFF
            numberBuilder.append(String.format("%03d", byte))
        }
        return numberBuilder.toString().take(SAFETY_NUMBER_LENGTH)
    }

    private fun generateQrData(contactId: String, myFingerprint: String, theirFingerprint: String): String {
        return "$QR_PREFIX$contactId/$myFingerprint/$theirFingerprint"
    }

    fun generateRandomKey(): ByteArray {
        val key = ByteArray(32)
        SecureRandom().nextBytes(key)
        return key
    }
}
