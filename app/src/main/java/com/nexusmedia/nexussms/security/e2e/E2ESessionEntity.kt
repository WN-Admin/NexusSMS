package com.nexusmedia.nexussms.security.e2e

import android.util.Base64
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

@Entity(
    tableName = "e2e_sessions",
    indices = [Index("contactId", unique = true)]
)
data class E2ESessionEntity(
    @PrimaryKey val contactId: String,
    val rootKey: String,
    @ColumnInfo(defaultValue = "") val sendingChainKey: String,
    @ColumnInfo(defaultValue = "") val receivingChainKey: String,
    @ColumnInfo(defaultValue = "0") val sendingMessageNumber: Long = 0,
    @ColumnInfo(defaultValue = "0") val receivingMessageNumber: Long = 0,
    @ColumnInfo(defaultValue = "0") val previousSendingChainLength: Long = 0,
    @ColumnInfo(defaultValue = "") val lastRatchetPublicKey: String,
    val ourRatchetKeyPairPublic: String,
    val ourRatchetKeyPairPrivate: String,
    @ColumnInfo(defaultValue = "{}") val skippedMessageKeys: String = "{}",
    @ColumnInfo(defaultValue = "") val aliceIdentityKey: String = "",
    @ColumnInfo(defaultValue = "") val bobIdentityKey: String = "",
    val createdAt: Long,
    val updatedAt: Long
) {
    companion object {
        private val gson = Gson()
        private const val B64 = Base64.NO_WRAP

        fun encode(bytes: ByteArray?): String = if (bytes == null) "" else Base64.encodeToString(bytes, B64)
        fun decode(s: String): ByteArray? = if (s.isEmpty()) null else Base64.decode(s, B64)

        fun fromState(contactId: String, state: RatchetSessionState): E2ESessionEntity {
            val skipped = mutableMapOf<String, MutableMap<Long, String>>()
            state.skippedMessageKeys.forEach { (senderKey, msgMap) ->
                skipped[senderKey] = mutableMapOf<Long, String>().also { m ->
                    msgMap.forEach { (num, key) -> m[num] = encode(key) }
                }
            }
            return E2ESessionEntity(
                contactId = contactId,
                rootKey = encode(state.rootKey),
                sendingChainKey = encode(state.sendingChainKey),
                receivingChainKey = encode(state.receivingChainKey),
                sendingMessageNumber = state.sendingMessageNumber,
                receivingMessageNumber = state.receivingMessageNumber,
                previousSendingChainLength = state.previousSendingChainLength,
                lastRatchetPublicKey = encode(state.lastRatchetPublicKey),
                ourRatchetKeyPairPublic = encode(state.ourRatchetKeyPair?.publicKey),
                ourRatchetKeyPairPrivate = encode(state.ourRatchetKeyPair?.privateKey),
                skippedMessageKeys = gson.toJson(skipped),
                aliceIdentityKey = encode(state.aliceIdentityKey),
                bobIdentityKey = encode(state.bobIdentityKey),
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
        }

        fun toState(entity: E2ESessionEntity): RatchetSessionState {
            val skippedType = object : TypeToken<Map<String, Map<Long, String>>>() {}.type
            val rawSkipped: Map<String, Map<Long, String>> = try {
                gson.fromJson(entity.skippedMessageKeys, skippedType) ?: emptyMap()
            } catch (_: Exception) { emptyMap() }

            val skippedKeys = mutableMapOf<String, MutableMap<Long, ByteArray>>()
            rawSkipped.forEach { (senderKey, msgMap) ->
                skippedKeys[senderKey] = mutableMapOf<Long, ByteArray>().also { m ->
                    msgMap.forEach { (num, b64) -> m[num] = decode(b64) ?: ByteArray(0) }
                }
            }

            return RatchetSessionState(
                aliceIdentityKey = decode(entity.aliceIdentityKey) ?: ByteArray(0),
                bobIdentityKey = decode(entity.bobIdentityKey) ?: ByteArray(0),
                rootKey = decode(entity.rootKey) ?: ByteArray(0),
                sendingChainKey = decode(entity.sendingChainKey),
                receivingChainKey = decode(entity.receivingChainKey),
                sendingMessageNumber = entity.sendingMessageNumber,
                receivingMessageNumber = entity.receivingMessageNumber,
                previousSendingChainLength = entity.previousSendingChainLength,
                lastRatchetPublicKey = decode(entity.lastRatchetPublicKey),
                skippedMessageKeys = skippedKeys,
                ourRatchetKeyPair = if (entity.ourRatchetKeyPairPublic.isNotEmpty())
                    RatchetKeyPair(decode(entity.ourRatchetKeyPairPublic)!!, decode(entity.ourRatchetKeyPairPrivate)!!)
                else null
            )
        }
    }
}
