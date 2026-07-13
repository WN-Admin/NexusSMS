package com.nexusmedia.nexussms.features.messenger

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface FacebookApi {

    @GET("me")
    suspend fun getMe(
        @Query("access_token") accessToken: String,
        @Query("fields") fields: String = "id,name,category"
    ): FacebookMeResponse

    @GET("me/conversations")
    suspend fun getConversations(
        @Query("access_token") accessToken: String,
        @Query("fields") fields: String = "id,snippet,updated_time,participants,message_count,unread_count",
        @Query("limit") limit: Int = 25,
        @Query("after") after: String? = null
    ): FacebookConversationsResponse

    @GET("{conversation-id}/messages")
    suspend fun getMessages(
        @Path("conversation-id") conversationId: String,
        @Query("access_token") accessToken: String,
        @Query("fields") fields: String = "id,message,from,created_time,attachments",
        @Query("limit") limit: Int = 50,
        @Query("after") after: String? = null
    ): FacebookMessagesResponse

    @POST("me/messages")
    suspend fun sendMessage(
        @Query("access_token") accessToken: String,
        @Body request: FacebookSendRequest
    ): FacebookSendResponse
}
