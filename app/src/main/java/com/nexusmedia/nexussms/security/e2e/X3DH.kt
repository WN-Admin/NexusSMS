package com.nexusmedia.nexussms.security.e2e

data class X3DHResult(
    val sharedSecret: ByteArray,
    val aliceIdentityKey: ByteArray,
    val bobIdentityKey: ByteArray,
    val bobSignedPrekey: ByteArray,
    val ephemeralPublicKey: ByteArray? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as X3DHResult
        return sharedSecret.contentEquals(other.sharedSecret)
    }
    override fun hashCode(): Int = sharedSecret.contentHashCode()
}

data class PreKeyBundle(
    val identityKey: ByteArray,
    val signedPrekeyPublic: ByteArray,
    val signedPrekeySignature: ByteArray,
    val oneTimePrekeyPublic: ByteArray? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as PreKeyBundle
        return identityKey.contentEquals(other.identityKey) &&
                signedPrekeyPublic.contentEquals(other.signedPrekeyPublic)
    }
    override fun hashCode(): Int {
        var result = identityKey.contentHashCode()
        result = 31 * result + signedPrekeyPublic.contentHashCode()
        return result
    }
}

object X3DH {
    private val ZERO_SALT = ByteArray(32)
    private val X3DH_INFO = "NexusSMSX3DH".toByteArray()

    fun computeAsAlice(
        aliceIdentityKey: RatchetKeyPair,
        bundle: PreKeyBundle,
        ephemeralKeyPair: RatchetKeyPair
    ): X3DHResult {
        val dh1 = CryptoPrimitives.x25519DH(aliceIdentityKey.privateKey, bundle.signedPrekeyPublic)
        val dh2 = CryptoPrimitives.x25519DH(ephemeralKeyPair.privateKey, bundle.signedPrekeyPublic)
        val dh3 = CryptoPrimitives.x25519DH(ephemeralKeyPair.privateKey, bundle.identityKey)

        val ikm = if (bundle.oneTimePrekeyPublic != null) {
            val dh4 = CryptoPrimitives.x25519DH(ephemeralKeyPair.privateKey, bundle.oneTimePrekeyPublic)
            dh1 + dh2 + dh3 + dh4
        } else {
            dh1 + dh2 + dh3
        }

        val sharedSecret = CryptoPrimitives.hkdfSha256(ZERO_SALT, ikm, X3DH_INFO)

        return X3DHResult(
            sharedSecret = sharedSecret,
            aliceIdentityKey = aliceIdentityKey.publicKey,
            bobIdentityKey = bundle.identityKey,
            bobSignedPrekey = bundle.signedPrekeyPublic,
            ephemeralPublicKey = ephemeralKeyPair.publicKey
        )
    }

    fun computeAsBob(
        aliceIdentityKey: ByteArray,
        ephemeralPublicKey: ByteArray,
        bobIdentityKey: RatchetKeyPair,
        bobSignedPrekey: RatchetKeyPair,
        bobOneTimePrekey: RatchetKeyPair?
    ): X3DHResult {
        val dh1 = CryptoPrimitives.x25519DH(bobSignedPrekey.privateKey, aliceIdentityKey)
        val dh2 = CryptoPrimitives.x25519DH(bobSignedPrekey.privateKey, ephemeralPublicKey)
        val dh3 = CryptoPrimitives.x25519DH(bobIdentityKey.privateKey, ephemeralPublicKey)

        val ikm = if (bobOneTimePrekey != null) {
            val dh4 = CryptoPrimitives.x25519DH(bobOneTimePrekey.privateKey, ephemeralPublicKey)
            dh1 + dh2 + dh3 + dh4
        } else {
            dh1 + dh2 + dh3
        }

        val sharedSecret = CryptoPrimitives.hkdfSha256(ZERO_SALT, ikm, X3DH_INFO)

        return X3DHResult(
            sharedSecret = sharedSecret,
            aliceIdentityKey = aliceIdentityKey,
            bobIdentityKey = bobIdentityKey.publicKey,
            bobSignedPrekey = bobSignedPrekey.publicKey,
            ephemeralPublicKey = ephemeralPublicKey
        )
    }
}
