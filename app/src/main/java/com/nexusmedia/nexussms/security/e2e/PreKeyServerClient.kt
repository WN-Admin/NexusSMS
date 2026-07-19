package com.nexusmedia.nexussms.security.e2e

import android.content.Context
import android.util.Base64
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.OkHttpClient
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

data class ServerRegisterRequest(
    val identityKey: String,
    val signedPrekey: ServerSignedPrekey,
    val oneTimePrekeys: List<ServerOneTimePrekey>
)

data class ServerSignedPrekey(
    val id: Int,
    val publicKey: String,
    val signature: String
)

data class ServerOneTimePrekey(
    val id: Int,
    val publicKey: String
)

data class ServerRegisterResponse(
    val apiKey: String,
    val deviceCount: Int
)

data class ServerPreKeyBundleResponse(
    val identityKey: String,
    val signedPrekey: ServerSignedPrekey,
    val oneTimePrekey: ServerOneTimePrekey? = null
)

data class ServerTopUpRequest(
    val oneTimePrekeys: List<ServerOneTimePrekey>
)

data class ServerStatusResponse(
    val identifier: String,
    val oneTimePrekeyCount: Int,
    val signedPrekeyId: Int? = null,
    val registrationDate: String? = null
)

private data class ErrorResponse(
    val error: String
)

@Singleton
class PreKeyServerClient @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    private val gson = Gson()

    private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()

    fun register(serverUrl: String, identity: LocalIdentity): Result<ServerRegisterResponse> {
        return try {
            val otkIdStart = identity.oneTimePrekeyStartId
            val request = ServerRegisterRequest(
                identityKey = Base64.encodeToString(identity.identityKeyPair.publicKey, Base64.NO_WRAP),
                signedPrekey = ServerSignedPrekey(
                    id = identity.signedPrekeyId,
                    publicKey = Base64.encodeToString(identity.signedPrekey.publicKey, Base64.NO_WRAP),
                    signature = Base64.encodeToString(CryptoPrimitives.randomBytes(64), Base64.NO_WRAP)
                ),
                oneTimePrekeys = identity.oneTimePrekeys.mapIndexed { index, keyPair ->
                    ServerOneTimePrekey(
                        id = otkIdStart + index,
                        publicKey = Base64.encodeToString(keyPair.publicKey, Base64.NO_WRAP)
                    )
                }
            )

            val body = gson.toJson(request).toRequestBody(JSON_MEDIA_TYPE)
            val httpRequest = Request.Builder()
                .url("${serverUrl.trimEnd('/')}/v1/keys/register")
                .post(body)
                .build()

            val response = executeRequest(httpRequest)
            parseResponse(response)
        } catch (e: Exception) {
            Timber.e(e, "Failed to register with pre-key server")
            Result.failure(e)
        }
    }

    fun fetchBundle(serverUrl: String, identifier: String): Result<ServerPreKeyBundleResponse> {
        return try {
            val httpRequest = Request.Builder()
                .url("${serverUrl.trimEnd('/')}/v1/keys/bundle/$identifier")
                .get()
                .build()

            val response = executeRequest(httpRequest)
            parseResponse(response)
        } catch (e: Exception) {
            Timber.e(e, "Failed to fetch key bundle for $identifier")
            Result.failure(e)
        }
    }

    fun topUpPrekeys(
        serverUrl: String,
        apiKey: String,
        prekeys: List<Pair<Int, ByteArray>>
    ): Result<Unit> {
        return try {
            val request = ServerTopUpRequest(
                oneTimePrekeys = prekeys.map { (id, publicKey) ->
                    ServerOneTimePrekey(
                        id = id,
                        publicKey = Base64.encodeToString(publicKey, Base64.NO_WRAP)
                    )
                }
            )

            val body = gson.toJson(request).toRequestBody(JSON_MEDIA_TYPE)
            val httpRequest = Request.Builder()
                .url("${serverUrl.trimEnd('/')}/v1/keys/prekeys")
                .put(body)
                .addHeader("Authorization", "Bearer $apiKey")
                .build()

            val response = executeRequest(httpRequest)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(parseErrorResponse(response))
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to top up prekeys")
            Result.failure(e)
        }
    }

    fun rotateSignedPrekey(
        serverUrl: String,
        apiKey: String,
        publicKey: ByteArray,
        signature: ByteArray
    ): Result<Unit> {
        return try {
            val request = ServerSignedPrekey(
                id = 0,
                publicKey = Base64.encodeToString(publicKey, Base64.NO_WRAP),
                signature = Base64.encodeToString(signature, Base64.NO_WRAP)
            )

            val body = gson.toJson(request).toRequestBody(JSON_MEDIA_TYPE)
            val httpRequest = Request.Builder()
                .url("${serverUrl.trimEnd('/')}/v1/keys/signed-prekey")
                .put(body)
                .addHeader("Authorization", "Bearer $apiKey")
                .build()

            val response = executeRequest(httpRequest)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(parseErrorResponse(response))
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to rotate signed prekey")
            Result.failure(e)
        }
    }

    fun getStatus(serverUrl: String, apiKey: String): Result<ServerStatusResponse> {
        return try {
            val httpRequest = Request.Builder()
                .url("${serverUrl.trimEnd('/')}/v1/keys/status")
                .get()
                .addHeader("Authorization", "Bearer $apiKey")
                .build()

            val response = executeRequest(httpRequest)
            parseResponse(response)
        } catch (e: Exception) {
            Timber.e(e, "Failed to get server status")
            Result.failure(e)
        }
    }

    private fun executeRequest(request: Request): Response {
        return httpClient.newCall(request).execute()
    }

    private inline fun <reified T> parseResponse(response: Response): Result<T> {
        return try {
            if (response.isSuccessful) {
                val body = response.body?.string() ?: return Result.failure(
                    IllegalStateException("Empty response body")
                )
                val parsed = gson.fromJson(body, T::class.java)
                Result.success(parsed)
            } else {
                Result.failure(parseErrorResponse(response))
            }
        } catch (e: Exception) {
            Result.failure(e)
        } finally {
            response.close()
        }
    }

    private fun parseErrorResponse(response: Response): Exception {
        return try {
            val body = response.body?.string()
            if (body != null) {
                val error = gson.fromJson(body, ErrorResponse::class.java)
                ServerException(response.code, error.error)
            } else {
                ServerException(response.code, "HTTP ${response.code}")
            }
        } catch (e: Exception) {
            ServerException(response.code, "HTTP ${response.code}")
        }
    }

    class ServerException(val httpCode: Int, message: String) : Exception("Server error ($httpCode): $message")
}
