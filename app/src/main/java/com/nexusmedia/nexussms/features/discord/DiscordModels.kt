package com.nexusmedia.nexussms.features.discord

import com.google.gson.annotations.SerializedName

data class DiscordUser(
    @SerializedName("id") val id: String,
    @SerializedName("username") val username: String,
    @SerializedName("discriminator") val discriminator: String?,
    @SerializedName("global_name") val globalName: String?,
    @SerializedName("bot") val bot: Boolean?
) {
    val displayName: String get() = globalName ?: username
}

data class DiscordGuild(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("icon") val icon: String?
)

data class DiscordChannel(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String?,
    @SerializedName("type") val type: Int,
    @SerializedName("guild_id") val guildId: String?,
    @SerializedName("topic") val topic: String?
) {
    val isTextChannel: Boolean get() = type == 0
}

data class DiscordMessage(
    @SerializedName("id") val id: String,
    @SerializedName("content") val content: String,
    @SerializedName("author") val author: DiscordUser,
    @SerializedName("timestamp") val timestamp: String,
    @SerializedName("channel_id") val channelId: String,
    @SerializedName("attachments") val attachments: List<DiscordAttachment>?,
    @SerializedName("embeds") val embeds: List<DiscordEmbed>?
)

data class DiscordAttachment(
    @SerializedName("id") val id: String,
    @SerializedName("filename") val filename: String,
    @SerializedName("url") val url: String,
    @SerializedName("size") val size: Int,
    @SerializedName("content_type") val contentType: String?
)

data class DiscordEmbed(
    @SerializedName("title") val title: String?,
    @SerializedName("description") val description: String?,
    @SerializedName("url") val url: String?
)

data class DiscordSendMessageRequest(
    @SerializedName("content") val content: String
)

data class DiscordGatewayEvent(
    @SerializedName("op") val op: Int,
    @SerializedName("d") val d: Any?,
    @SerializedName("s") val s: Int?,
    @SerializedName("t") val t: String?
)
