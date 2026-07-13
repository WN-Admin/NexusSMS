package com.nexusmedia.nexussms.features.telegram

import com.google.gson.annotations.SerializedName

data class TelegramGetMeResponse(
    @SerializedName("ok") val ok: Boolean,
    @SerializedName("result") val result: TelegramBot?
)

data class TelegramBot(
    @SerializedName("id") val id: Long,
    @SerializedName("username") val username: String,
    @SerializedName("first_name") val firstName: String?,
    @SerializedName("can_join_groups") val canJoinGroups: Boolean?,
    @SerializedName("can_read_all_group_messages") val canReadAllGroupMessages: Boolean?
)

data class TelegramGetUpdatesResponse(
    @SerializedName("ok") val ok: Boolean,
    @SerializedName("result") val result: List<TelegramUpdate>?
)

data class TelegramUpdate(
    @SerializedName("update_id") val updateId: Long,
    @SerializedName("message") val message: TelegramMessage?,
    @SerializedName("channel_post") val channelPost: TelegramMessage?
)

data class TelegramMessage(
    @SerializedName("message_id") val messageId: Long,
    @SerializedName("from") val from: TelegramUser?,
    @SerializedName("chat") val chat: TelegramChat,
    @SerializedName("date") val date: Long,
    @SerializedName("text") val text: String?,
    @SerializedName("photo") val photo: List<TelegramPhotoSize>?,
    @SerializedName("document") val document: TelegramDocument?,
    @SerializedName("voice") val voice: TelegramVoice?,
    @SerializedName("caption") val caption: String?
)

data class TelegramUser(
    @SerializedName("id") val id: Long,
    @SerializedName("is_bot") val isBot: Boolean,
    @SerializedName("first_name") val firstName: String?,
    @SerializedName("last_name") val lastName: String?,
    @SerializedName("username") val username: String?
) {
    val displayName: String get() {
        val parts = mutableListOf<String>()
        firstName?.let { parts.add(it) }
        lastName?.let { parts.add(it) }
        return parts.joinToString(" ").ifEmpty { username ?: "Unknown" }
    }
}

data class TelegramChat(
    @SerializedName("id") val id: Long,
    @SerializedName("type") val type: String,
    @SerializedName("title") val title: String?,
    @SerializedName("username") val username: String?,
    @SerializedName("first_name") val firstName: String?,
    @SerializedName("last_name") val lastName: String?
) {
    val displayName: String get() {
        return title ?: username ?: run {
            val parts = mutableListOf<String>()
            firstName?.let { parts.add(it) }
            lastName?.let { parts.add(it) }
            parts.joinToString(" ").ifEmpty { "Unknown" }
        }
    }
}

data class TelegramPhotoSize(
    @SerializedName("file_id") val fileId: String,
    @SerializedName("width") val width: Int,
    @SerializedName("height") val height: Int
)

data class TelegramDocument(
    @SerializedName("file_id") val fileId: String,
    @SerializedName("file_name") val fileName: String?
)

data class TelegramVoice(
    @SerializedName("file_id") val fileId: String
)

data class TelegramSendMessageRequest(
    @SerializedName("chat_id") val chatId: Long,
    @SerializedName("text") val text: String,
    @SerializedName("parse_mode") val parseMode: String? = null
)

data class TelegramSendResponse(
    @SerializedName("ok") val ok: Boolean,
    @SerializedName("result") val result: TelegramMessage?
)
