package com.nexusmedia.nexussms.security.e2e

import org.junit.Assert.*
import org.junit.Test

class DoubleRatchetTest {

    private fun genKeyPair(): RatchetKeyPair {
        val (pub, priv) = CryptoPrimitives.generateX25519KeyPair()
        return RatchetKeyPair(pub, priv)
    }

    @Test
    fun testEncryptDecryptRoundTrip() {
        val aliceIdentity = genKeyPair()
        val bobIdentity = genKeyPair()
        val aliceRatchet = genKeyPair()
        val bobRatchet = genKeyPair()

        val sharedSecret = CryptoPrimitives.randomBytes(32)
        val aliceState = DoubleRatchet.initAlice(sharedSecret, aliceIdentity.publicKey, bobIdentity.publicKey, aliceRatchet, bobRatchet.publicKey)
        val bobState = DoubleRatchet.initBob(sharedSecret, aliceIdentity.publicKey, bobIdentity.publicKey, bobRatchet)

        val (message, _) = DoubleRatchet.encrypt(aliceState, "Hello, Bob!".toByteArray())
        val result = DoubleRatchet.decrypt(bobState, message)

        assertNotNull("Decryption should succeed", result)
        assertEquals("Hello, Bob!", String(result!!.first))
    }

    @Test
    fun testBidirectionalExchange() {
        val aliceIdentity = genKeyPair()
        val bobIdentity = genKeyPair()
        val aliceRatchet = genKeyPair()
        val bobRatchet = genKeyPair()

        val sharedSecret = CryptoPrimitives.randomBytes(32)
        var aliceState = DoubleRatchet.initAlice(sharedSecret, aliceIdentity.publicKey, bobIdentity.publicKey, aliceRatchet, bobRatchet.publicKey)
        var bobState = DoubleRatchet.initBob(sharedSecret, aliceIdentity.publicKey, bobIdentity.publicKey, bobRatchet)

        val aliceMessages = listOf("A1", "A2", "A3")
        val bobMessages = listOf("B1", "B2", "B3")

        for (text in aliceMessages) {
            val (msg, newState) = DoubleRatchet.encrypt(aliceState, text.toByteArray())
            val (plaintext, bobNewState) = DoubleRatchet.decrypt(bobState, msg)!!
            assertEquals(text, String(plaintext))
            aliceState = newState
            bobState = bobNewState
        }

        for (text in bobMessages) {
            val (msg, newState) = DoubleRatchet.encrypt(bobState, text.toByteArray())
            val (plaintext, aliceNewState) = DoubleRatchet.decrypt(aliceState, msg)!!
            assertEquals(text, String(plaintext))
            bobState = newState
            aliceState = aliceNewState
        }
    }

    @Test
    fun testOutOfOrderDelivery() {
        val aliceIdentity = genKeyPair()
        val bobIdentity = genKeyPair()
        val aliceRatchet = genKeyPair()
        val bobRatchet = genKeyPair()

        val sharedSecret = CryptoPrimitives.randomBytes(32)
        var aliceState = DoubleRatchet.initAlice(sharedSecret, aliceIdentity.publicKey, bobIdentity.publicKey, aliceRatchet, bobRatchet.publicKey)
        var bobState = DoubleRatchet.initBob(sharedSecret, aliceIdentity.publicKey, bobIdentity.publicKey, bobRatchet)

        val messages = mutableListOf<Pair<ByteArray, RatchetMessage>>()
        for (i in 0..4) {
            val (msg, newState) = DoubleRatchet.encrypt(aliceState, "msg$i".toByteArray())
            messages.add("msg$i".toByteArray() to msg)
            aliceState = newState
        }

        val receivedOrder = listOf(0, 2, 4, 1, 3)
        for (idx in receivedOrder) {
            val (expectedPlaintext, ratchetMsg) = messages[idx]
            val result = DoubleRatchet.decrypt(bobState, ratchetMsg)
            assertNotNull("Failed to decrypt message $idx", result)
            assertEquals(String(expectedPlaintext), String(result!!.first))
            bobState = result.second
        }
    }

    @Test
    fun testSkippedKeyEviction() {
        val aliceIdentity = genKeyPair()
        val bobIdentity = genKeyPair()
        val aliceRatchet = genKeyPair()
        val bobRatchet = genKeyPair()

        val sharedSecret = CryptoPrimitives.randomBytes(32)
        var aliceState = DoubleRatchet.initAlice(sharedSecret, aliceIdentity.publicKey, bobIdentity.publicKey, aliceRatchet, bobRatchet.publicKey)
        var bobState = DoubleRatchet.initBob(sharedSecret, aliceIdentity.publicKey, bobIdentity.publicKey, bobRatchet)

        val messages = mutableListOf<RatchetMessage>()
        for (i in 0..DoubleRatchet.MAX_SKIPPED_KEYS) {
            val (msg, newState) = DoubleRatchet.encrypt(aliceState, "msg$i".toByteArray())
            messages.add(msg)
            aliceState = newState
        }

        for (i in 0 until messages.size - 1) {
            val result = DoubleRatchet.decrypt(bobState, messages[i])
            assertNotNull("Message $i should decrypt even when skipped", result)
            bobState = result!!.second
        }
    }

    @Test
    fun testMessageEquality() {
        val aliceIdentity = genKeyPair()
        val bobIdentity = genKeyPair()
        val aliceRatchet = genKeyPair()
        val bobRatchet = genKeyPair()

        val sharedSecret = CryptoPrimitives.randomBytes(32)
        val aliceState = DoubleRatchet.initAlice(sharedSecret, aliceIdentity.publicKey, bobIdentity.publicKey, aliceRatchet, bobRatchet.publicKey)

        val (msg1, _) = DoubleRatchet.encrypt(aliceState, "test".toByteArray())
        val (msg2, _) = DoubleRatchet.encrypt(aliceState, "test".toByteArray())

        assertFalse("Ciphertexts should differ due to random nonce", msg1.ciphertext.contentEquals(msg2.ciphertext))

        val bobState = DoubleRatchet.initBob(sharedSecret, aliceIdentity.publicKey, bobIdentity.publicKey, bobRatchet)

        val result1 = DoubleRatchet.decrypt(bobState, msg1)
        val result2 = DoubleRatchet.decrypt(bobState, msg2)

        assertNotNull(result1)
        assertNotNull(result2)
        assertEquals(String(result1!!.first), String(result2!!.first))
    }

    @Test
    fun testWrongKeyFails() {
        val sharedSecret = CryptoPrimitives.randomBytes(32)
        val aliceIdentity = genKeyPair()
        val bobIdentity = genKeyPair()
        val aliceRatchet = genKeyPair()
        val bobRatchet = genKeyPair()
        val wrongRatchet = genKeyPair()

        val aliceState = DoubleRatchet.initAlice(sharedSecret, aliceIdentity.publicKey, bobIdentity.publicKey, aliceRatchet, bobRatchet.publicKey)
        val (msg, _) = DoubleRatchet.encrypt(aliceState, "secret".toByteArray())

        val wrongSharedSecret = CryptoPrimitives.randomBytes(32)
        val wrongState = DoubleRatchet.initBob(wrongSharedSecret, aliceIdentity.publicKey, bobIdentity.publicKey, wrongRatchet)

        val result = DoubleRatchet.decrypt(wrongState, msg)
        assertNull("Decryption with wrong key should fail", result)
    }

    @Test
    fun testX3DHSharedSecret() {
        val aliceIdentity = genKeyPair()
        val bobIdentity = genKeyPair()
        val bobSignedPrekey = genKeyPair()
        val ephemeral = genKeyPair()

        val bundle = PreKeyBundle(
            identityKey = bobIdentity.publicKey,
            signedPrekeyPublic = bobSignedPrekey.publicKey,
            signedPrekeySignature = CryptoPrimitives.randomBytes(64),
            oneTimePrekeyPublic = null
        )

        val aliceResult = X3DH.computeAsAlice(aliceIdentity, bundle, ephemeral)
        val bobResult = X3DH.computeAsBob(
            aliceIdentityKey = aliceIdentity.publicKey,
            ephemeralPublicKey = ephemeral.publicKey,
            bobIdentityKey = bobIdentity,
            bobSignedPrekey = bobSignedPrekey,
            bobOneTimePrekey = null
        )

        assertTrue("Alice and Bob should derive the same shared secret",
            aliceResult.sharedSecret.contentEquals(bobResult.sharedSecret))
    }

    @Test
    fun testX3DHWithOneTimePrekey() {
        val aliceIdentity = genKeyPair()
        val bobIdentity = genKeyPair()
        val bobSignedPrekey = genKeyPair()
        val bobOneTimePrekey = genKeyPair()
        val ephemeral = genKeyPair()

        val bundle = PreKeyBundle(
            identityKey = bobIdentity.publicKey,
            signedPrekeyPublic = bobSignedPrekey.publicKey,
            signedPrekeySignature = CryptoPrimitives.randomBytes(64),
            oneTimePrekeyPublic = bobOneTimePrekey.publicKey
        )

        val aliceResult = X3DH.computeAsAlice(aliceIdentity, bundle, ephemeral)
        val bobResult = X3DH.computeAsBob(
            aliceIdentityKey = aliceIdentity.publicKey,
            ephemeralPublicKey = ephemeral.publicKey,
            bobIdentityKey = bobIdentity,
            bobSignedPrekey = bobSignedPrekey,
            bobOneTimePrekey = bobOneTimePrekey
        )

        assertTrue("Alice and Bob should derive the same shared secret with one-time prekey",
            aliceResult.sharedSecret.contentEquals(bobResult.sharedSecret))
    }

    @Test
    fun testRatchetPublicKeyChanges() {
        val aliceIdentity = genKeyPair()
        val bobIdentity = genKeyPair()
        val aliceRatchet = genKeyPair()
        val bobRatchet = genKeyPair()

        val sharedSecret = CryptoPrimitives.randomBytes(32)
        var aliceState = DoubleRatchet.initAlice(sharedSecret, aliceIdentity.publicKey, bobIdentity.publicKey, aliceRatchet, bobRatchet.publicKey)
        var bobState = DoubleRatchet.initBob(sharedSecret, aliceIdentity.publicKey, bobIdentity.publicKey, bobRatchet)

        val (msg1, aliceState2) = DoubleRatchet.encrypt(aliceState, "first".toByteArray())
        val (msg2, aliceState3) = DoubleRatchet.encrypt(aliceState2, "second".toByteArray())

        val (pt1, bobState2) = DoubleRatchet.decrypt(bobState, msg1)!!
        val (pt2, bobState3) = DoubleRatchet.decrypt(bobState2, msg2)!!

        assertEquals("first", String(pt1))
        assertEquals("second", String(pt2))

        val (bobReply, _) = DoubleRatchet.encrypt(bobState3, "reply".toByteArray())

        assertFalse("Bob's ratchet public key should differ from Alice's",
            bobReply.ratchetPublicKey.contentEquals(msg1.ratchetPublicKey))

        val (pt3, _) = DoubleRatchet.decrypt(aliceState3, bobReply)!!
        assertEquals("reply", String(pt3))
    }

    @Test
    fun testWireFormatRoundTrip() {
        val ratchetPubKey = CryptoPrimitives.randomBytes(32)
        val ciphertext = CryptoPrimitives.randomBytes(48)
        val msg = RatchetMessage(ratchetPubKey, 5L, 3L, ciphertext)

        val wire = msg.toWireFormat()
        val decoded = RatchetMessage.fromWireFormat(wire)

        assertNotNull(decoded)
        assertTrue(ratchetPubKey.contentEquals(decoded!!.ratchetPublicKey))
        assertEquals(5L, decoded.previousChainLength)
        assertEquals(3L, decoded.messageNumber)
        assertTrue(ciphertext.contentEquals(decoded.ciphertext))
    }

    @Test
    fun testReplayAttackFails() {
        val aliceIdentity = genKeyPair()
        val bobIdentity = genKeyPair()
        val aliceRatchet = genKeyPair()
        val bobRatchet = genKeyPair()

        val sharedSecret = CryptoPrimitives.randomBytes(32)
        var aliceState = DoubleRatchet.initAlice(sharedSecret, aliceIdentity.publicKey, bobIdentity.publicKey, aliceRatchet, bobRatchet.publicKey)
        var bobState = DoubleRatchet.initBob(sharedSecret, aliceIdentity.publicKey, bobIdentity.publicKey, bobRatchet)

        val (msg1, aliceState2) = DoubleRatchet.encrypt(aliceState, "first".toByteArray())
        val (pt1, bobState2) = DoubleRatchet.decrypt(bobState, msg1)!!
        assertEquals("first", String(pt1))

        val result = DoubleRatchet.decrypt(bobState2, msg1)
        assertNull("Replay of already-consumed message should fail", result)
    }

    @Test
    fun testTamperedCiphertextFails() {
        val aliceIdentity = genKeyPair()
        val bobIdentity = genKeyPair()
        val aliceRatchet = genKeyPair()
        val bobRatchet = genKeyPair()

        val sharedSecret = CryptoPrimitives.randomBytes(32)
        val aliceState = DoubleRatchet.initAlice(sharedSecret, aliceIdentity.publicKey, bobIdentity.publicKey, aliceRatchet, bobRatchet.publicKey)
        val bobState = DoubleRatchet.initBob(sharedSecret, aliceIdentity.publicKey, bobIdentity.publicKey, bobRatchet)

        val (msg, _) = DoubleRatchet.encrypt(aliceState, "secret".toByteArray())
        val tamperedCiphertext = msg.ciphertext.copyOf()
        tamperedCiphertext[0] = (tamperedCiphertext[0].toInt() xor 0xFF).toByte()
        val tamperedMsg = RatchetMessage(msg.ratchetPublicKey, msg.previousChainLength, msg.messageNumber, tamperedCiphertext)

        val result = DoubleRatchet.decrypt(bobState, tamperedMsg)
        assertNull("Decryption of tampered ciphertext should fail", result)
    }

    @Test
    fun testTruncatedMessageFails() {
        val wire = RatchetMessage(CryptoPrimitives.randomBytes(32), 0L, 0L, CryptoPrimitives.randomBytes(16)).toWireFormat()
        val truncated = wire.copyOfRange(0, wire.size / 2)
        assertNull("Truncated wire format should fail to parse", RatchetMessage.fromWireFormat(truncated))

        val tooShort = ByteArray(10)
        assertNull("Very short data should fail to parse", RatchetMessage.fromWireFormat(tooShort))
    }

    @Test
    fun testSessionPersistenceRoundTrip() {
        val aliceIdentity = genKeyPair()
        val bobIdentity = genKeyPair()
        val aliceRatchet = genKeyPair()
        val bobRatchet = genKeyPair()

        val sharedSecret = CryptoPrimitives.randomBytes(32)
        var aliceState = DoubleRatchet.initAlice(sharedSecret, aliceIdentity.publicKey, bobIdentity.publicKey, aliceRatchet, bobRatchet.publicKey)
        var bobState = DoubleRatchet.initBob(sharedSecret, aliceIdentity.publicKey, bobIdentity.publicKey, bobRatchet)

        for (i in 0..2) {
            val (msg, newState) = DoubleRatchet.encrypt(aliceState, "msg$i".toByteArray())
            val (pt, bobNewState) = DoubleRatchet.decrypt(bobState, msg)!!
            assertEquals("msg$i", String(pt))
            aliceState = newState
            bobState = bobNewState
        }

        val restoredAlice = RatchetSessionState(
            aliceIdentityKey = aliceState.aliceIdentityKey,
            bobIdentityKey = aliceState.bobIdentityKey,
            rootKey = aliceState.rootKey.copyOf(),
            sendingChainKey = aliceState.sendingChainKey?.copyOf(),
            receivingChainKey = aliceState.receivingChainKey?.copyOf(),
            sendingMessageNumber = aliceState.sendingMessageNumber,
            receivingMessageNumber = aliceState.receivingMessageNumber,
            previousSendingChainLength = aliceState.previousSendingChainLength,
            lastRatchetPublicKey = aliceState.lastRatchetPublicKey?.copyOf(),
            ourRatchetKeyPair = aliceState.ourRatchetKeyPair?.let {
                RatchetKeyPair(it.publicKey.copyOf(), it.privateKey.copyOf())
            }
        )

        val restoredBob = RatchetSessionState(
            aliceIdentityKey = bobState.aliceIdentityKey,
            bobIdentityKey = bobState.bobIdentityKey,
            rootKey = bobState.rootKey.copyOf(),
            sendingChainKey = bobState.sendingChainKey?.copyOf(),
            receivingChainKey = bobState.receivingChainKey?.copyOf(),
            sendingMessageNumber = bobState.sendingMessageNumber,
            receivingMessageNumber = bobState.receivingMessageNumber,
            previousSendingChainLength = bobState.previousSendingChainLength,
            lastRatchetPublicKey = bobState.lastRatchetPublicKey?.copyOf(),
            ourRatchetKeyPair = bobState.ourRatchetKeyPair?.let {
                RatchetKeyPair(it.publicKey.copyOf(), it.privateKey.copyOf())
            }
        )

        val (msg3, aliceState3) = DoubleRatchet.encrypt(aliceState, "msg3".toByteArray())
        val (msg3Restored, _) = DoubleRatchet.encrypt(restoredAlice, "msg3restored".toByteArray())
        assertTrue("Restored state should produce same ratchet key",
            msg3.ratchetPublicKey.contentEquals(msg3Restored.ratchetPublicKey))

        val (pt3, _) = DoubleRatchet.decrypt(restoredBob, msg3)!!
        assertEquals("msg3", String(pt3))
    }

    @Test
    fun testReinstallGeneratesNewKeys() {
        val identity1 = CryptoPrimitives.generateX25519KeyPair()
        val identity2 = CryptoPrimitives.generateX25519KeyPair()

        assertFalse("Two identity generations should produce different keys",
            identity1.first.contentEquals(identity2.first))
        assertFalse("Private keys should differ",
            identity1.second.contentEquals(identity2.second))
    }

    @Test
    fun testX3DHWrongBobKeyFails() {
        val aliceIdentity = genKeyPair()
        val bobIdentity = genKeyPair()
        val wrongBobIdentity = genKeyPair()
        val bobSignedPrekey = genKeyPair()
        val ephemeral = genKeyPair()

        val bundle = PreKeyBundle(
            identityKey = bobIdentity.publicKey,
            signedPrekeyPublic = bobSignedPrekey.publicKey,
            signedPrekeySignature = CryptoPrimitives.randomBytes(64),
            oneTimePrekeyPublic = null
        )

        val aliceResult = X3DH.computeAsAlice(aliceIdentity, bundle, ephemeral)
        val bobResult = X3DH.computeAsBob(
            aliceIdentityKey = aliceIdentity.publicKey,
            ephemeralPublicKey = ephemeral.publicKey,
            bobIdentityKey = wrongBobIdentity,
            bobSignedPrekey = bobSignedPrekey,
            bobOneTimePrekey = null
        )

        assertFalse("X3DH with wrong Bob identity key should produce different shared secret",
            aliceResult.sharedSecret.contentEquals(bobResult.sharedSecret))
    }

    @Test
    fun testX3DHWrongOTKFails() {
        val aliceIdentity = genKeyPair()
        val bobIdentity = genKeyPair()
        val bobSignedPrekey = genKeyPair()
        val bobOneTimePrekey = genKeyPair()
        val wrongOTK = genKeyPair()
        val ephemeral = genKeyPair()

        val bundle = PreKeyBundle(
            identityKey = bobIdentity.publicKey,
            signedPrekeyPublic = bobSignedPrekey.publicKey,
            signedPrekeySignature = CryptoPrimitives.randomBytes(64),
            oneTimePrekeyPublic = bobOneTimePrekey.publicKey
        )

        val aliceResult = X3DH.computeAsAlice(aliceIdentity, bundle, ephemeral)
        val bobResult = X3DH.computeAsBob(
            aliceIdentityKey = aliceIdentity.publicKey,
            ephemeralPublicKey = ephemeral.publicKey,
            bobIdentityKey = bobIdentity,
            bobSignedPrekey = bobSignedPrekey,
            bobOneTimePrekey = wrongOTK
        )

        assertFalse("X3DH with wrong OTK should produce different shared secret",
            aliceResult.sharedSecret.contentEquals(bobResult.sharedSecret))
    }

    @Test
    fun testConcurrentEncryptionStateConsistency() {
        val aliceIdentity = genKeyPair()
        val bobIdentity = genKeyPair()
        val aliceRatchet = genKeyPair()
        val bobRatchet = genKeyPair()

        val sharedSecret = CryptoPrimitives.randomBytes(32)
        var aliceState = DoubleRatchet.initAlice(sharedSecret, aliceIdentity.publicKey, bobIdentity.publicKey, aliceRatchet, bobRatchet.publicKey)
        var bobState = DoubleRatchet.initBob(sharedSecret, aliceIdentity.publicKey, bobIdentity.publicKey, bobRatchet)

        val messages = (0 until 50).map { i ->
            val (msg, newState) = DoubleRatchet.encrypt(aliceState, "msg$i".toByteArray())
            aliceState = newState
            i to msg
        }

        for ((i, msg) in messages) {
            val (pt, newState) = DoubleRatchet.decrypt(bobState, msg)!!
            assertEquals("msg$i", String(pt))
            bobState = newState
        }
    }

    @Test
    fun testEmptyMessageEncryptDecrypt() {
        val aliceIdentity = genKeyPair()
        val bobIdentity = genKeyPair()
        val aliceRatchet = genKeyPair()
        val bobRatchet = genKeyPair()

        val sharedSecret = CryptoPrimitives.randomBytes(32)
        val aliceState = DoubleRatchet.initAlice(sharedSecret, aliceIdentity.publicKey, bobIdentity.publicKey, aliceRatchet, bobRatchet.publicKey)
        val bobState = DoubleRatchet.initBob(sharedSecret, aliceIdentity.publicKey, bobIdentity.publicKey, bobRatchet)

        val (msg, _) = DoubleRatchet.encrypt(aliceState, ByteArray(0))
        val (pt, _) = DoubleRatchet.decrypt(bobState, msg)!!
        assertEquals(0, pt.size)
    }

    @Test
    fun testLargeMessageEncryptDecrypt() {
        val aliceIdentity = genKeyPair()
        val bobIdentity = genKeyPair()
        val aliceRatchet = genKeyPair()
        val bobRatchet = genKeyPair()

        val sharedSecret = CryptoPrimitives.randomBytes(32)
        val aliceState = DoubleRatchet.initAlice(sharedSecret, aliceIdentity.publicKey, bobIdentity.publicKey, aliceRatchet, bobRatchet.publicKey)
        val bobState = DoubleRatchet.initBob(sharedSecret, aliceIdentity.publicKey, bobIdentity.publicKey, bobRatchet)

        val largePayload = ByteArray(64 * 1024) { (it % 256).toByte() }
        val (msg, _) = DoubleRatchet.encrypt(aliceState, largePayload)
        val (pt, _) = DoubleRatchet.decrypt(bobState, msg)!!
        assertTrue("Large message should survive round-trip", largePayload.contentEquals(pt))
    }

    @Test
    fun testMultiRatchetStepDropSegments() {
        val aliceIdentity = genKeyPair()
        val bobIdentity = genKeyPair()
        val aliceRatchet = genKeyPair()
        val bobRatchet = genKeyPair()

        val sharedSecret = CryptoPrimitives.randomBytes(32)
        var aliceState = DoubleRatchet.initAlice(sharedSecret, aliceIdentity.publicKey, bobIdentity.publicKey, aliceRatchet, bobRatchet.publicKey)
        var bobState = DoubleRatchet.initBob(sharedSecret, aliceIdentity.publicKey, bobIdentity.publicKey, bobRatchet)

        val (msg0, s0) = DoubleRatchet.encrypt(aliceState, "keep0".toByteArray())
        aliceState = s0
        val (msg1, s1) = DoubleRatchet.encrypt(aliceState, "drop1".toByteArray())
        aliceState = s1
        val (msg2, s2) = DoubleRatchet.encrypt(aliceState, "keep2".toByteArray())
        aliceState = s2
        val (msg3, s3) = DoubleRatchet.encrypt(aliceState, "drop3".toByteArray())
        aliceState = s3
        val (msg4, _) = DoubleRatchet.encrypt(aliceState, "keep4".toByteArray())

        val (pt0, bs0) = DoubleRatchet.decrypt(bobState, msg0)!!
        assertEquals("keep0", String(pt0))
        val (pt2, bs2) = DoubleRatchet.decrypt(bs0, msg2)!!
        assertEquals("keep2", String(pt2))
        val (pt4, _) = DoubleRatchet.decrypt(bs2, msg4)!!
        assertEquals("keep4", String(pt4))
    }
}
