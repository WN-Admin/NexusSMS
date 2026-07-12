package com.nexusmedia.nexussms.features.matrix

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MatrixClient @Inject constructor() {

    private var currentHomeserver: String? = null
    private var currentApi: MatrixApi? = null
    private var accessToken: String? = null

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor { chain ->
                val original = chain.request()
                val token = accessToken
                val request = if (token != null) {
                    original.newBuilder()
                        .header("Authorization", "Bearer $token")
                        .build()
                } else {
                    original
                }
                chain.proceed(request)
            }
            .addInterceptor(
                HttpLoggingInterceptor { msg -> Timber.d("MatrixAPI: %s", msg) }
                    .apply { level = HttpLoggingInterceptor.Level.BODY }
            )
            .build()
    }

    fun configure(homeserver: String, token: String?) {
        if (homeserver == currentHomeserver && token == accessToken) return
        currentHomeserver = homeserver
        accessToken = token
        currentApi = Retrofit.Builder()
            .baseUrl(normalizeHomeserver(homeserver))
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(MatrixApi::class.java)
    }

    fun getApi(): MatrixApi {
        return currentApi ?: throw IllegalStateException("Matrix client not configured. Call configure() first.")
    }

    fun setAccessToken(token: String) {
        accessToken = token
    }

    private fun normalizeHomeserver(url: String): String {
        var normalized = url.trim()
        if (!normalized.startsWith("http://") && !normalized.startsWith("https://")) {
            normalized = "https://$normalized"
        }
        if (!normalized.endsWith("/")) {
            normalized = "$normalized/"
        }
        return normalized
    }
}
