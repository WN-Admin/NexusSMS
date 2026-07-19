package com.nexusmedia.nexussms.security.e2e

import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

data class RatchetKeyPair(
    val publicKey: ByteArray,
    val privateKey: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as RatchetKeyPair
        return publicKey.contentEquals(other.publicKey) && privateKey.contentEquals(other.privateKey)
    }
    override fun hashCode(): Int = publicKey.contentHashCode() * 31 + privateKey.contentHashCode()
}

data class RatchetSessionState(
    val aliceIdentityKey: ByteArray,
    val bobIdentityKey: ByteArray,
    val rootKey: ByteArray,
    val sendingChainKey: ByteArray? = null,
    val receivingChainKey: ByteArray? = null,
    val sendingMessageNumber: Long = 0,
    val receivingMessageNumber: Long = 0,
    val previousSendingChainLength: Long = 0,
    val lastRatchetPublicKey: ByteArray? = null,
    val skippedMessageKeys: Map<String, MutableMap<Long, ByteArray>> = emptyMap(),
    val ourRatchetKeyPair: RatchetKeyPair? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as RatchetSessionState
        return rootKey.contentEquals(other.rootKey)
    }
    override fun hashCode(): Int = rootKey.contentHashCode()
}

data class RatchetMessage(
    val ratchetPublicKey: ByteArray,
    val previousChainLength: Long,
    val messageNumber: Long,
    val ciphertext: ByteArray
) {
    fun toWireFormat(): ByteArray {
        val header = ByteArray(48)
        System.arraycopy(ratchetPublicKey, 0, header, 0, 32)
        java.nio.ByteBuffer.wrap(header, 32, 8).putLong(previousChainLength)
        java.nio.ByteBuffer.wrap(header, 40, 8).putLong(messageNumber)
        return header + ciphertext
    }

    companion object {
        fun fromWireFormat(data: ByteArray): RatchetMessage? {
            if (data.size < 48) return null
            val ratchetPublicKey = data.copyOfRange(0, 32)
            val previousChainLength = java.nio.ByteBuffer.wrap(data, 32, 8).long
            val messageNumber = java.nio.ByteBuffer.wrap(data, 40, 8).long
            val ciphertext = data.copyOfRange(48, data.size)
            return RatchetMessage(ratchetPublicKey, previousChainLength, messageNumber, ciphertext)
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as RatchetMessage
        return ratchetPublicKey.contentEquals(other.ratchetPublicKey) &&
                previousChainLength == other.previousChainLength &&
                messageNumber == other.messageNumber &&
                ciphertext.contentEquals(other.ciphertext)
    }

    override fun hashCode(): Int {
        var result = ratchetPublicKey.contentHashCode()
        result = 31 * result + previousChainLength.hashCode()
        result = 31 * result + messageNumber.hashCode()
        result = 31 * result + ciphertext.contentHashCode()
        return result
    }
}

object DoubleRatchet {
    const val MAX_SKIPPED_KEYS = 1000
    private val CHAIN_KEY_PREFIX = "ChainKey".toByteArray()
    private val MESSAGE_KEY_PREFIX = "MessageKey".toByteArray()
    private val RATCHET_INFO = "NexusRatchet".toByteArray()
    private val CHAIN_INFO = "NexusChainKey".toByteArray()

    fun initAlice(
        sharedSecret: ByteArray,
        aliceIdentityKey: ByteArray,
        bobIdentityKey: ByteArray,
        ratchetKeyPair: RatchetKeyPair,
        bobRatchetPublicKey: ByteArray
    ): RatchetSessionState {
        return RatchetSessionState(
            aliceIdentityKey = aliceIdentityKey,
            bobIdentityKey = bobIdentityKey,
            rootKey = sharedSecret,
            ourRatchetKeyPair = ratchetKeyPair,
            lastRatchetPublicKey = bobRatchetPublicKey
        )
    }

    fun initBob(
        sharedSecret: ByteArray,
        aliceIdentityKey: ByteArray,
        bobIdentityKey: ByteArray,
        ratchetKeyPair: RatchetKeyPair
    ): RatchetSessionState {
        return RatchetSessionState(
            aliceIdentityKey = aliceIdentityKey,
            bobIdentityKey = bobIdentityKey,
            rootKey = sharedSecret,
            ourRatchetKeyPair = ratchetKeyPair
        )
    }

    fun encrypt(state: RatchetSessionState, plaintext: ByteArray): Pair<RatchetMessage, RatchetSessionState> {
        val rkp = state.ourRatchetKeyPair ?: throw IllegalStateException("No ratchet key pair")

        var currentState = state
        var sendingChainKey = state.sendingChainKey

        if (sendingChainKey == null) {
            val theirPub = state.lastRatchetPublicKey
                ?: throw IllegalStateException("No ratchet key from peer yet")
            val dhOutput = CryptoPrimitives.x25519DH(rkp.privateKey, theirPub)
            val (newRoot, newChain) = kdfRootKey(state.rootKey, dhOutput)
            sendingChainKey = newChain
            currentState = currentState.copy(rootKey = newRoot)
        }

        val (messageKey, nextChainKey) = chainKeyRatchet(sendingChainKey)
        val ciphertext = CryptoPrimitives.aesGcmEncrypt(messageKey, plaintext, rkp.publicKey)

        val message = RatchetMessage(
            ratchetPublicKey = rkp.publicKey,
            previousChainLength = currentState.previousSendingChainLength,
            messageNumber = currentState.sendingMessageNumber,
            ciphertext = ciphertext
        )

        val updatedState = currentState.copy(
            sendingChainKey = nextChainKey,
            sendingMessageNumber = currentState.sendingMessageNumber + 1
        )

        return message to updatedState
    }

    fun decrypt(state: RatchetSessionState, message: RatchetMessage): Pair<ByteArray, RatchetSessionState>? {
        val senderPubKeyBytes = message.ratchetPublicKey

        var currentState = state
        var receivingChainKey = state.receivingChainKey

        val isNewRatchetKey = state.lastRatchetPublicKey == null ||
                !senderPubKeyBytes.contentEquals(state.lastRatchetPublicKey)

        if (isNewRatchetKey) {
            val rkp = state.ourRatchetKeyPair
                ?: throw IllegalStateException("No ratchet key pair for DH step")

            val dh1 = CryptoPrimitives.x25519DH(rkp.privateKey, senderPubKeyBytes)
            val (newRoot1, rcvChain) = kdfRootKey(state.rootKey, dh1)
            receivingChainKey = rcvChain

            val (newPubKey, newPrivKey) = CryptoPrimitives.generateX25519KeyPair()
            val dh2 = CryptoPrimitives.x25519DH(newPrivKey, senderPubKeyBytes)
            val (newRoot2, sndChain) = kdfRootKey(newRoot1, dh2)

            currentState = currentState.copy(
                rootKey = newRoot2,
                receivingChainKey = rcvChain,
                sendingChainKey = sndChain,
                sendingMessageNumber = 0,
                previousSendingChainLength = currentState.previousSendingChainLength + currentState.sendingMessageNumber,
                lastRatchetPublicKey = senderPubKeyBytes,
                ourRatchetKeyPair = RatchetKeyPair(newPubKey, newPrivKey)
            )
        }

        if (receivingChainKey == null) return null

        var currentChainKey: ByteArray = receivingChainKey
        val currentReceivingNum = currentState.receivingMessageNumber
        val skippedKeys = currentState.skippedMessageKeys.toMutableMap()
        val senderKey = senderPubKeyBytes.contentHashCode().toString()
        val senderMap = skippedKeys.getOrPut(senderKey) { mutableMapOf() }

        if (message.messageNumber < currentReceivingNum) {
            val storedKey = senderMap.remove(message.messageNumber)
                ?: return null
            val plaintext = CryptoPrimitives.aesGcmDecrypt(storedKey, message.ciphertext, senderPubKeyBytes)
                ?: return null
            val updatedState = currentState.copy(skippedMessageKeys = skippedKeys)
            return plaintext to updatedState
        }

        if (message.messageNumber > currentReceivingNum) {
            val skipCount = (message.messageNumber - currentReceivingNum).toInt()
            val totalSkipped = senderMap.size
            if (totalSkipped + skipCount > MAX_SKIPPED_KEYS) {
                return null
            }
            for (num in currentReceivingNum until message.messageNumber) {
                val (skipMsgKey, nextKey) = chainKeyRatchet(currentChainKey)
                senderMap[num] = skipMsgKey
                currentChainKey = nextKey
            }
        }

        val (messageKey, nextChainKey) = chainKeyRatchet(currentChainKey)
        val plaintext = CryptoPrimitives.aesGcmDecrypt(messageKey, message.ciphertext, senderPubKeyBytes)
            ?: return null

        val updatedState = currentState.copy(
            receivingChainKey = nextChainKey,
            receivingMessageNumber = message.messageNumber + 1,
            skippedMessageKeys = skippedKeys
        )

        return plaintext to updatedState
    }

    internal fun chainKeyRatchet(chainKey: ByteArray): Pair<ByteArray, ByteArray> {
        val messageKey = hmacSha256(chainKey, MESSAGE_KEY_PREFIX)
        val nextChainKey = hmacSha256(chainKey, CHAIN_KEY_PREFIX)
        return messageKey to nextChainKey
    }

    private fun kdfRootKey(rootKey: ByteArray, dhOutput: ByteArray): Pair<ByteArray, ByteArray> {
        val newRootKey = CryptoPrimitives.hkdfSha256(rootKey, dhOutput, RATCHET_INFO)
        val chainKey = CryptoPrimitives.hkdfSha256(newRootKey, dhOutput, CHAIN_INFO)
        return newRootKey to chainKey
    }

    private fun hmacSha256(key: ByteArray, data: ByteArray): ByteArray {
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(key, "HmacSHA256"))
        return mac.doFinal(data)
    }
}
