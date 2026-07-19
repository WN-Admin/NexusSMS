package com.nexusmedia.nexussms.security.e2e

import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class E2ERegistrationManager @Inject constructor(
    private val e2eKeyManager: E2EKeyManager,
    private val preKeyServerClient: PreKeyServerClient
) {
    suspend fun ensureRegistered(serverUrl: String): Boolean = withContext(Dispatchers.IO) {
        val identity = e2eKeyManager.getOrCreateIdentity()
        if (identity.registeredWithServer && identity.serverUrl == serverUrl) {
            return@withContext true
        }
        register(serverUrl)
    }

    suspend fun register(serverUrl: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val identity = e2eKeyManager.getOrCreateIdentity()

            if (identity.oneTimePrekeys.isEmpty()) {
                val newOtks = e2eKeyManager.generateOneTimePrekeys(10)
                preKeyServerClient.topUpPrekeys(
                    serverUrl, identity.serverApiKey ?: "",
                    newOtks.map { it.publicKey }.mapIndexed { idx, pub -> (identity.oneTimePrekeyStartId + idx) to pub }
                )
            }

            val result = preKeyServerClient.register(serverUrl, identity)
            if (result.isSuccess) {
                val response = result.getOrThrow()
                e2eKeyManager.markRegistered(response.apiKey, serverUrl)
                Timber.i("Registered with E2E server, apiKey=%s...", response.apiKey.take(8))
                true
            } else {
                Timber.w(result.exceptionOrNull(), "E2E server registration failed")
                false
            }
        } catch (e: Exception) {
            Timber.e(e, "E2E registration error")
            false
        }
    }

    suspend fun topUpPrekeys(serverUrl: String, count: Int = 10): Boolean = withContext(Dispatchers.IO) {
        try {
            val identity = e2eKeyManager.getOrCreateIdentity()
            val apiKey = identity.serverApiKey ?: return@withContext false
            val newOtks = e2eKeyManager.generateOneTimePrekeys(count)
            val prekeyPairs = newOtks.mapIndexed { idx, kp ->
                (identity.oneTimePrekeyStartId + identity.oneTimePrekeys.size + idx) to kp.publicKey
            }
            val result = preKeyServerClient.topUpPrekeys(serverUrl, apiKey, prekeyPairs)
            result.isSuccess
        } catch (e: Exception) {
            Timber.e(e, "OTK top-up failed")
            false
        }
    }

    suspend fun getStatus(serverUrl: String): ServerStatusResponse? = withContext(Dispatchers.IO) {
        val identity = e2eKeyManager.getOrCreateIdentity()
        val apiKey = identity.serverApiKey ?: return@withContext null
        val result = preKeyServerClient.getStatus(serverUrl, apiKey)
        result.getOrNull()
    }
}
