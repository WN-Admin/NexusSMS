package com.nexusmedia.nexussms.features.matrix

import android.content.Context
import android.content.SharedPreferences
import com.nexusmedia.nexussms.data.repository.SocialAccountRepository
import com.nexusmedia.nexussms.security.EncryptionManager
import com.nexusmedia.nexussms.features.matrix.MatrixClient
import com.nexusmedia.nexussms.data.models.SocialAccount
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

data class MatrixAuthResult(
    val success: Boolean,
    val userId: String? = null,
    val accessToken: String? = null,
    val deviceId: String? = null,
    val homeserver: String? = null,
    val error: String? = null
)

@Singleton
class MatrixAuthService @Inject constructor(
    private val matrixClient: MatrixClient,
    private val socialAccountRepository: SocialAccountRepository,
    private val encryptionManager: EncryptionManager,
    @ApplicationContext private val context: Context
) {
    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences("matrix_auth", Context.MODE_PRIVATE)
    }

    suspend fun login(homeserver: String, username: String, password: String): MatrixAuthResult {
        return try {
            matrixClient.configure(homeserver, null)
            val api = matrixClient.getApi()

            val request = MatrixLoginRequest(
                identifier = MatrixIdentifier(user = username),
                password = password
            )

            val response = api.login(request)

            val actualHomeserver = response.wellKnown?.homeserver?.baseUrl ?: homeserver
            matrixClient.configure(actualHomeserver, response.accessToken)

            saveAuth(actualHomeserver, response.userId, response.accessToken, response.deviceId)

            Timber.d("Matrix login successful: %s on %s", response.userId, actualHomeserver)

            MatrixAuthResult(
                success = true,
                userId = response.userId,
                accessToken = response.accessToken,
                deviceId = response.deviceId,
                homeserver = actualHomeserver
            )
        } catch (e: Exception) {
            Timber.e(e, "Matrix login failed")
            MatrixAuthResult(
                success = false,
                error = parseMatrixError(e)
            )
        }
    }

    suspend fun restoreSession(): Boolean {
        val homeserver = prefs.getString("homeserver", null) ?: return false
        val encryptedToken = prefs.getString("access_token", null) ?: return false
        val token = encryptionManager.decryptToken(encryptedToken)
        val userId = prefs.getString("user_id", null) ?: return false

        matrixClient.configure(homeserver, token)

        val account = socialAccountRepository.getAccountByPlatform("MATRIX")
        if (account != null && account.isConnected) {
            Timber.d("Matrix session restored for %s", userId)
            return true
        }

        Timber.d("Matrix token found but account not connected, saving account")
        saveAccount(userId, username = userId.substringBefore(":").removePrefix("@"), token, homeserver)
        return true
    }

    suspend fun logout() {
        prefs.edit().clear().apply()
        socialAccountRepository.getAccountByPlatform("MATRIX")?.let { account ->
            socialAccountRepository.updateAccount(
                account.copy(
                    isConnected = false,
                    accessToken = "",
                    refreshToken = null,
                    updatedAt = System.currentTimeMillis()
                )
            )
        }
        Timber.d("Matrix logged out")
    }

    fun isLoggedIn(): Boolean {
        return prefs.getString("access_token", null) != null
    }

    fun getUserId(): String? = prefs.getString("user_id", null)

    fun getAccessToken(): String? {
        val encrypted = prefs.getString("access_token", null) ?: return null
        return encryptionManager.decryptToken(encrypted)
    }

    fun getHomeserver(): String? = prefs.getString("homeserver", null)

    fun getDeviceId(): String? = prefs.getString("device_id", null)

    private suspend fun saveAuth(homeserver: String, userId: String, accessToken: String, deviceId: String?) {
        prefs.edit()
            .putString("homeserver", homeserver)
            .putString("user_id", userId)
            .putString("access_token", encryptionManager.encryptToken(accessToken))
            .putString("device_id", deviceId)
            .apply()

        saveAccount(userId, username = userId.substringBefore(":").removePrefix("@"), accessToken, homeserver)
    }

    private suspend fun saveAccount(userId: String, username: String, accessToken: String, homeserver: String) {
        val existing = socialAccountRepository.getAccountByPlatform("MATRIX")
        val account = SocialAccount(
            id = existing?.id ?: java.util.UUID.randomUUID().toString(),
            platform = "MATRIX",
            userId = userId,
            username = username,
            displayName = userId.substringBefore(":").removePrefix("@"),
            accessToken = encryptionManager.encryptToken(accessToken),
            isConnected = true,
            settings = """{"homeserver":"$homeserver","deviceId":"${getDeviceId() ?: ""}"}""",
            updatedAt = System.currentTimeMillis(),
            createdAt = existing?.createdAt ?: System.currentTimeMillis()
        )
        if (existing != null) {
            socialAccountRepository.updateAccount(account)
        } else {
            socialAccountRepository.insertAccount(account)
        }
    }

    private fun parseMatrixError(e: Exception): String {
        val msg = e.message ?: return "Unknown error"
        return when {
            msg.contains("M_FORBIDDEN") || msg.contains("Invalid credentials") -> "Invalid username or password"
            msg.contains("M_USER_DEACTIVATED") -> "Account is deactivated"
            msg.contains("M_THREEDAY_RETRY") || msg.contains("rate_limited") -> "Too many attempts. Try again later"
            msg.contains("Unable to resolve host") || msg.contains("timeout") -> "Cannot reach homeserver"
            msg.contains("Connection") -> "Network error. Check your connection"
            else -> msg
        }
    }
}
