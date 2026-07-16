package com.nexusmedia.nexussms.features.matrix

import com.google.gson.annotations.SerializedName

data class MatrixLoginRequest(
    @SerializedName("type") val type: String = "m.login.password",
    @SerializedName("identifier") val identifier: MatrixIdentifier,
    @SerializedName("password") val password: String
)

data class MatrixIdentifier(
    @SerializedName("type") val type: String = "m.id.user",
    @SerializedName("user") val user: String
)

data class MatrixLoginResponse(
    @SerializedName("user_id") val userId: String,
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("device_id") val deviceId: String?,
    @SerializedName("well_known") val wellKnown: MatrixWellKnown?
)

data class MatrixWellKnown(
    @SerializedName("m.homeserver") val homeserver: MatrixHomeserver?
)

data class MatrixHomeserver(
    @SerializedName("base_url") val baseUrl: String
)

data class MatrixSyncResponse(
    @SerializedName("next_batch") val nextBatch: String,
    @SerializedName("rooms") val rooms: MatrixRooms?
)

data class MatrixRooms(
    @SerializedName("join") val join: Map<String, MatrixRoomSync>?,
    @SerializedName("invite") val invite: Map<String, MatrixRoomSync>?
)

data class MatrixRoomSync(
    @SerializedName("timeline") val timeline: MatrixTimeline?,
    @SerializedName("state") val state: MatrixState?,
    @SerializedName("unread_notifications") val unreadNotifications: MatrixUnreadNotifications?
)

data class MatrixUnreadNotifications(
    @SerializedName("notification_count") val notificationCount: Int?,
    @SerializedName("highlight_count") val highlightCount: Int?
)

data class MatrixTimeline(
    @SerializedName("events") val events: List<MatrixEvent>?,
    @SerializedName("limited") val limited: Boolean = false,
    @SerializedName("prev_batch") val prevBatch: String?
)

data class MatrixState(
    @SerializedName("events") val events: List<MatrixEvent>?
)

data class MatrixEvent(
    @SerializedName("event_id") val eventId: String,
    @SerializedName("type") val type: String,
    @SerializedName("sender") val sender: String,
    @SerializedName("origin_server_ts") val originServerTs: Long,
    @SerializedName("content") val content: Map<String, Any>,
    @SerializedName("state_key") val stateKey: String? = null
)

data class MatrixSendMessageResponse(
    @SerializedName("event_id") val eventId: String
)

data class MatrixJoinedRoomsResponse(
    @SerializedName("joined_rooms") val joinedRooms: List<String>
)

data class MatrixError(
    @SerializedName("errcode") val errcode: String,
    @SerializedName("error") val error: String
)

data class MatrixSendMessageBody(
    @SerializedName("msgtype") val msgtype: String = "m.text",
    @SerializedName("body") val body: String
)

data class MatrixSendMessageImageBody(
    @SerializedName("msgtype") val msgtype: String = "m.image",
    @SerializedName("body") val body: String,
    @SerializedName("url") val url: String,
    @SerializedName("info") val info: Map<String, Any>? = null
)

data class MatrixSendMessageFileBody(
    @SerializedName("msgtype") val msgtype: String = "m.file",
    @SerializedName("body") val body: String,
    @SerializedName("url") val url: String,
    @SerializedName("info") val info: Map<String, Any>? = null
)

data class MatrixUploadResponse(
    @SerializedName("content_uri") val contentUri: String
)

data class MatrixMessagesResponse(
    @SerializedName("start") val start: String?,
    @SerializedName("end") val end: String?,
    @SerializedName("chunk") val chunk: List<MatrixEvent>?,
    @SerializedName("state") val state: List<MatrixEvent>?
)
