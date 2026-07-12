package com.nexusmedia.nexussms.features.matrix

import com.nexusmedia.nexussms.data.models.Message
import com.nexusmedia.nexussms.data.repository.MessageRepository
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

data class MatrixSendResult(
    val success: Boolean,
    val eventId: String? = null,
    val error: String? = null
)

@Singleton
class MatrixMessageService @Inject constructor(
    private val matrixClient: MatrixClient,
    private val messageRepository: MessageRepository,
    private val matrixAuthService: MatrixAuthService
) {

    suspend fun sendTextMessage(
        roomId: String,
        content: String,
        conversationId: String,
        recipientId: String = ""
    ): MatrixSendResult {
        val token = matrixAuthService.getAccessToken()
            ?: return MatrixSendResult(false, error = "Not logged in")

        return try {
            val api = matrixClient.getApi()
            val txnId = "nexussms_${System.currentTimeMillis()}_${(Math.random() * 10000).toLong()}"
            val body = MatrixSendMessageBody(body = content)

            val response = api.sendMessage("Bearer $token", roomId, txnId, body)

            val message = Message(
                conversationId = conversationId,
                senderPhoneNumber = "self",
                recipientPhoneNumber = recipientId.ifEmpty { roomId },
                content = content,
                type = "TEXT",
                timestamp = System.currentTimeMillis(),
                status = "SENT",
                sourcePlatform = "MATRIX",
                metadata = """{"eventId":"${response.eventId}","roomId":"$roomId","platform":"MATRIX"}"""
            )
            messageRepository.insertMessage(message)

            Timber.d("Matrix message sent: %s -> %s", response.eventId, roomId)
            MatrixSendResult(success = true, eventId = response.eventId)
        } catch (e: Exception) {
            Timber.e(e, "Matrix send failed")
            MatrixSendResult(success = false, error = parseSendError(e))
        }
    }

    suspend fun sendImageMessage(
        roomId: String,
        mxcUrl: String,
        body: String,
        fileName: String,
        mimeType: String,
        width: Int? = null,
        height: Int? = null,
        size: Long? = null,
        conversationId: String,
        recipientId: String = ""
    ): MatrixSendResult {
        val token = matrixAuthService.getAccessToken()
            ?: return MatrixSendResult(false, error = "Not logged in")

        return try {
            val api = matrixClient.getApi()
            val txnId = "nexussms_${System.currentTimeMillis()}_${(Math.random() * 10000).toLong()}"
            val info = mutableMapOf<String, Any>()
            if (width != null) info["w"] = width
            if (height != null) info["h"] = height
            if (size != null) info["size"] = size
            info["mimetype"] = mimeType

            val imageBody = MatrixSendMessageImageBody(
                body = body,
                url = mxcUrl,
                info = info
            )
            val response = api.sendImageMessage("Bearer $token", roomId, txnId, imageBody)

            val message = Message(
                conversationId = conversationId,
                senderPhoneNumber = "self",
                recipientPhoneNumber = recipientId.ifEmpty { roomId },
                content = body,
                type = "IMAGE",
                timestamp = System.currentTimeMillis(),
                status = "SENT",
                sourcePlatform = "MATRIX",
                mediaUrls = mxcUrl,
                metadata = """{"eventId":"${response.eventId}","roomId":"$roomId","platform":"MATRIX","mxcUrl":"$mxcUrl","fileName":"$fileName"}"""
            )
            messageRepository.insertMessage(message)

            Timber.d("Matrix image sent: %s -> %s", response.eventId, roomId)
            MatrixSendResult(success = true, eventId = response.eventId)
        } catch (e: Exception) {
            Timber.e(e, "Matrix image send failed")
            MatrixSendResult(success = false, error = parseSendError(e))
        }
    }

    suspend fun sendFileMessage(
        roomId: String,
        mxcUrl: String,
        body: String,
        fileName: String,
        mimeType: String,
        size: Long? = null,
        conversationId: String,
        recipientId: String = ""
    ): MatrixSendResult {
        val token = matrixAuthService.getAccessToken()
            ?: return MatrixSendResult(false, error = "Not logged in")

        return try {
            val api = matrixClient.getApi()
            val txnId = "nexussms_${System.currentTimeMillis()}_${(Math.random() * 10000).toLong()}"
            val info = mutableMapOf<String, Any>()
            if (size != null) info["size"] = size
            info["mimetype"] = mimeType

            val fileBody = MatrixSendMessageFileBody(
                body = body,
                url = mxcUrl,
                info = info
            )
            val response = api.sendFileMessage("Bearer $token", roomId, txnId, fileBody)

            val message = Message(
                conversationId = conversationId,
                senderPhoneNumber = "self",
                recipientPhoneNumber = recipientId.ifEmpty { roomId },
                content = body,
                type = "FILE",
                timestamp = System.currentTimeMillis(),
                status = "SENT",
                sourcePlatform = "MATRIX",
                mediaUrls = mxcUrl,
                metadata = """{"eventId":"${response.eventId}","roomId":"$roomId","platform":"MATRIX","mxcUrl":"$mxcUrl","fileName":"$fileName"}"""
            )
            messageRepository.insertMessage(message)

            Timber.d("Matrix file sent: %s -> %s", response.eventId, roomId)
            MatrixSendResult(success = true, eventId = response.eventId)
        } catch (e: Exception) {
            Timber.e(e, "Matrix file send failed")
            MatrixSendResult(success = false, error = parseSendError(e))
        }
    }

    suspend fun uploadMedia(
        fileName: String,
        data: ByteArray,
        mimeType: String
    ): MatrixSendResult {
        val token = matrixAuthService.getAccessToken()
            ?: return MatrixSendResult(false, error = "Not logged in")

        return try {
            val api = matrixClient.getApi()
            val requestBody = data.toRequestBody(mimeType.toMediaType())
            val response = api.uploadMedia(
                "Bearer $token",
                mimeType,
                data.size.toLong(),
                requestBody
            )
            Timber.d("Matrix media uploaded: %s", response.contentUri)
            MatrixSendResult(success = true, eventId = response.contentUri)
        } catch (e: Exception) {
            Timber.e(e, "Matrix media upload failed")
            MatrixSendResult(success = false, error = parseSendError(e))
        }
    }

    suspend fun markAsRead(roomId: String, eventId: String) {
        val token = matrixAuthService.getAccessToken() ?: return
        try {
            val api = matrixClient.getApi()
            api.markAsRead("Bearer $token", roomId, mapOf(
                "m.fully_read" to eventId,
                "m.read" to eventId
            ))
        } catch (e: Exception) {
            Timber.w(e, "Failed to mark Matrix room as read")
        }
    }

    private fun parseSendError(e: Exception): String {
        val msg = e.message ?: return "Send failed"
        return when {
            msg.contains("M_UNKNOWN_TOKEN") || msg.contains("401") -> "Session expired. Please reconnect"
            msg.contains("M_FORBIDDEN") -> "Not allowed to send to this room"
            msg.contains("M_NOT_FOUND") -> "Room not found"
            msg.contains("M_CANNOT_OVERWRITE_EVENT") -> "Message already sent"
            msg.contains("Unable to resolve host") -> "No network connection"
            msg.contains("timeout") -> "Connection timed out"
            else -> msg
        }
    }
}
