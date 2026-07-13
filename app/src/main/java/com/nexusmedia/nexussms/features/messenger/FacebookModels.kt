package com.nexusmedia.nexussms.features.messenger

import com.google.gson.annotations.SerializedName

data class FacebookMeResponse(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String?,
    @SerializedName("category") val category: String?
)

data class FacebookConversationsResponse(
    @SerializedName("data") val data: List<FacebookConversation>?,
    @SerializedName("paging") val paging: FacebookPaging?
)

data class FacebookConversation(
    @SerializedName("id") val id: String,
    @SerializedName("snippet") val snippet: String?,
    @SerializedName("updated_time") val updatedTime: String?,
    @SerializedName("participants") val participants: FacebookParticipants?,
    @SerializedName("message_count") val messageCount: Int?,
    @SerializedName("unread_count") val unreadCount: Int?
)

data class FacebookParticipants(
    @SerializedName("data") val data: List<FacebookParticipant>?
)

data class FacebookParticipant(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String?
)

data class FacebookMessagesResponse(
    @SerializedName("data") val data: List<FacebookMessage>?,
    @SerializedName("paging") val paging: FacebookPaging?
)

data class FacebookMessage(
    @SerializedName("id") val id: String,
    @SerializedName("message") val message: String?,
    @SerializedName("from") val from: FacebookParticipant?,
    @SerializedName("created_time") val createdTime: String?,
    @SerializedName("attachments") val attachments: FacebookAttachments?
)

data class FacebookAttachments(
    @SerializedName("data") val data: List<FacebookAttachment>?
)

data class FacebookAttachment(
    @SerializedName("type") val type: String?,
    @SerializedName("payload") val payload: FacebookPayload?
)

data class FacebookPayload(
    @SerializedName("url") val url: String?
)

data class FacebookPaging(
    @SerializedName("cursors") val cursors: FacebookCursors?,
    @SerializedName("next") val next: String?,
    @SerializedName("previous") val previous: String?
)

data class FacebookCursors(
    @SerializedName("before") val before: String?,
    @SerializedName("after") val after: String?
)

data class FacebookSendRequest(
    @SerializedName("recipient") val recipient: FacebookRecipient,
    @SerializedName("message") val message: FacebookMessageBody
)

data class FacebookRecipient(
    @SerializedName("id") val id: String
)

data class FacebookMessageBody(
    @SerializedName("text") val text: String
)

data class FacebookSendResponse(
    @SerializedName("recipient_id") val recipientId: String?,
    @SerializedName("message_id") val messageId: String?
)
