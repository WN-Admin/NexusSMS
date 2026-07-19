package com.nexusmedia.nexussms.security.e2e

import org.bouncycastle.crypto.generators.X25519KeyPairGenerator
import org.bouncycastle.crypto.params.X25519PrivateKeyParameters
import org.bouncycastle.crypto.params.X25519PublicKeyParameters
import org.bouncycastle.crypto.agreement.X25519Agreement
import org.bouncycastle.crypto.AsymmetricCipherKeyPair
import org.bouncycastle.crypto.params.ECDomainParameters
import org.bouncycastle.jce.interfaces.ECPrivateKey
import org.bouncycastle.jce.spec.ECParameterSpec
import org.bouncycastle.jce.provider.BouncyCastleProvider
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.SecureRandom
import java.security.Security
import java.security.spec.ECGenParameterSpec
import javax.crypto.Cipher
import javax.crypto.Mac
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

object CryptoPrimitives {
    private val secureRandom = SecureRandom()
    private const val GCM_TAG_LENGTH = 128
    private const val GCM_IV_LENGTH = 12

    init {
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(BouncyCastleProvider())
        }
    }

    fun generateX25519KeyPair(): Pair<ByteArray, ByteArray> {
        val generator = X25519KeyPairGenerator()
        generator.init(org.bouncycastle.crypto.KeyGenerationParameters(secureRandom, 256))
        val keyPair = generator.generateKeyPair()
        val priv = (keyPair.private as X25519PrivateKeyParameters).encoded
        val pub = (keyPair.public as X25519PublicKeyParameters).encoded
        return pub to priv
    }

    fun x25519DH(myPrivateBytes: ByteArray, theirPublicBytes: ByteArray): ByteArray {
        val privKey = X25519PrivateKeyParameters(myPrivateBytes, 0)
        val pubKey = X25519PublicKeyParameters(theirPublicBytes, 0)
        val agreement = X25519Agreement()
        agreement.init(privKey)
        val sharedSecret = ByteArray(32)
        agreement.calculateAgreement(pubKey, sharedSecret, 0)
        return sharedSecret
    }

    fun hkdfSha256(salt: ByteArray, ikm: ByteArray, info: ByteArray, outputLength: Int = 32): ByteArray {
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(salt, "HmacSHA256"))
        val prk = mac.doFinal(ikm)

        val result = ByteArray(outputLength)
        mac.init(SecretKeySpec(prk, "HmacSHA256"))
        var previous = ByteArray(0)
        var offset = 0
        var blockIndex = 1
        while (offset < outputLength) {
            val input = previous + info + byteArrayOf(blockIndex.toByte())
            previous = mac.doFinal(input)
            val copyLength = minOf(previous.size, outputLength - offset)
            System.arraycopy(previous, 0, result, offset, copyLength)
            offset += copyLength
            blockIndex++
        }
        return result
    }

    fun aesGcmEncrypt(key: ByteArray, plaintext: ByteArray, associatedData: ByteArray = ByteArray(0)): ByteArray {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val iv = ByteArray(GCM_IV_LENGTH)
        secureRandom.nextBytes(iv)
        val spec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
        cipher.init(Cipher.ENCRYPT_MODE, SecretKeySpec(key, "AES"), spec)
        if (associatedData.isNotEmpty()) {
            cipher.updateAAD(associatedData)
        }
        val encrypted = cipher.doFinal(plaintext)
        return iv + encrypted
    }

    fun aesGcmDecrypt(key: ByteArray, ciphertext: ByteArray, associatedData: ByteArray = ByteArray(0)): ByteArray? {
        if (ciphertext.size < GCM_IV_LENGTH + 16) return null
        return try {
            val iv = ciphertext.copyOfRange(0, GCM_IV_LENGTH)
            val encrypted = ciphertext.copyOfRange(GCM_IV_LENGTH, ciphertext.size)
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            val spec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
            cipher.init(Cipher.DECRYPT_MODE, SecretKeySpec(key, "AES"), spec)
            if (associatedData.isNotEmpty()) {
                cipher.updateAAD(associatedData)
            }
            cipher.doFinal(encrypted)
        } catch (e: Exception) {
            null
        }
    }

    fun randomBytes(length: Int): ByteArray {
        val bytes = ByteArray(length)
        secureRandom.nextBytes(bytes)
        return bytes
    }
}
