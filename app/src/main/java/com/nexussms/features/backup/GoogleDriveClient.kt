package com.nexussms.features.backup

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoogleDriveClient @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "GoogleDriveClient"
    }

    suspend fun authenticate(accountName: String): Boolean = true
    suspend fun uploadFile(fileName: String, content: String, mimeType: String = "application/json"): String? = "mock_file_id"
    suspend fun downloadFile(fileId: String): String? = null
    suspend fun listBackupFiles(): List<Any> = emptyList()
    suspend fun deleteFile(fileId: String): Boolean = true
    fun isAuthenticated(): Boolean = true
}
