package com.nexusmedia.nexussms.features.matrix

import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface MatrixApi {

    @POST("_matrix/client/v3/login")
    suspend fun login(@Body request: MatrixLoginRequest): MatrixLoginResponse

    @GET("_matrix/client/v3/login")
    suspend fun getLoginFlows(): Map<String, Any>

    @GET("_matrix/client/v3/sync")
    suspend fun sync(
        @Header("Authorization") auth: String,
        @Query("since") since: String? = null,
        @Query("timeout") timeout: Long? = null,
        @Query("filter") filter: String? = null
    ): MatrixSyncResponse

    @PUT("_matrix/client/v3/rooms/{roomId}/send/m.room.message/{txnId}")
    suspend fun sendMessage(
        @Header("Authorization") auth: String,
        @Path("roomId") roomId: String,
        @Path("txnId") txnId: String,
        @Body content: MatrixSendMessageBody
    ): MatrixSendMessageResponse

    @PUT("_matrix/client/v3/rooms/{roomId}/send/m.room.image/{txnId}")
    suspend fun sendImageMessage(
        @Header("Authorization") auth: String,
        @Path("roomId") roomId: String,
        @Path("txnId") txnId: String,
        @Body content: MatrixSendMessageImageBody
    ): MatrixSendMessageResponse

    @PUT("_matrix/client/v3/rooms/{roomId}/send/m.room.file/{txnId}")
    suspend fun sendFileMessage(
        @Header("Authorization") auth: String,
        @Path("roomId") roomId: String,
        @Path("txnId") txnId: String,
        @Body content: MatrixSendMessageFileBody
    ): MatrixSendMessageResponse

    @GET("_matrix/client/v3/joined_rooms")
    suspend fun getJoinedRooms(
        @Header("Authorization") auth: String
    ): MatrixJoinedRoomsResponse

    @GET("_matrix/client/v3/rooms/{roomId}/state")
    suspend fun getRoomState(
        @Header("Authorization") auth: String,
        @Path("roomId") roomId: String
    ): List<MatrixEvent>

    @POST("_matrix/media/r0/upload")
    suspend fun uploadMedia(
        @Header("Authorization") auth: String,
        @Header("Content-Type") contentType: String,
        @Header("Content-Length") contentLength: Long,
        @Body body: RequestBody
    ): MatrixUploadResponse

    @POST("_matrix/client/v3/rooms/{roomId}/read_markers")
    suspend fun markAsRead(
        @Header("Authorization") auth: String,
        @Path("roomId") roomId: String,
        @Body body: Map<String, String>
    )
}
