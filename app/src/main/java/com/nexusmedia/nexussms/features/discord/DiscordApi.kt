package com.nexusmedia.nexussms.features.discord

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface DiscordApi {

    @GET("users/@me")
    suspend fun getMe(@Header("Authorization") auth: String): DiscordUser

    @GET("users/@me/guilds")
    suspend fun getGuilds(@Header("Authorization") auth: String): List<DiscordGuild>

    @GET("guilds/{guildId}/channels")
    suspend fun getChannels(
        @Header("Authorization") auth: String,
        @Path("guildId") guildId: String
    ): List<DiscordChannel>

    @GET("channels/{channelId}/messages")
    suspend fun getMessages(
        @Header("Authorization") auth: String,
        @Path("channelId") channelId: String,
        @Query("limit") limit: Int = 50,
        @Query("after") after: String? = null
    ): List<DiscordMessage>

    @POST("channels/{channelId}/messages")
    suspend fun sendMessage(
        @Header("Authorization") auth: String,
        @Path("channelId") channelId: String,
        @Body request: DiscordSendMessageRequest
    ): DiscordMessage
}
