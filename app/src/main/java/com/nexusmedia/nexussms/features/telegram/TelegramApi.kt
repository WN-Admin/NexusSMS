package com.nexusmedia.nexussms.features.telegram

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface TelegramBotApi {

    @GET("getMe")
    suspend fun getMe(): TelegramGetMeResponse

    @GET("getUpdates")
    suspend fun getUpdates(
        @Query("offset") offset: Long? = null,
        @Query("limit") limit: Int? = null,
        @Query("timeout") timeout: Int? = null
    ): TelegramGetUpdatesResponse

    @POST("sendMessage")
    suspend fun sendMessage(@Body request: TelegramSendMessageRequest): TelegramSendResponse
}
