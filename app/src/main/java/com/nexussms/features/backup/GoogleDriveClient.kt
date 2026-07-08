package com.nexussms.features.backup

import android.content.Context
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.api.services.drive.model.File
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.BufferedReader
import java.io.InputStreamReader
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoogleDriveClient @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "GoogleDriveClient"
        private const val BACKUP_FOLDER = "NexusSMS_Backups"
    }

    private var credential: GoogleAccountCredential? = null
    private var driveService: Drive? = null

    suspend fun authenticate(accountName: String): Boolean = withContext(Dispatchers.IO) {
        try {
            credential = GoogleAccountCredential.usingOAuth2(
                context,
                listOf(DriveScopes.DRIVE_FILE)
            ).apply { selectedAccountName = accountName }

            driveService = Drive.Builder(
                AndroidHttp.newCompatibleTransport(),
                GsonFactory.getDefaultInstance(),
                credential
            )
                .setApplicationName("NexusSMS")
                .build()

            driveService?.files()?.list()?.setPageSize(1)?.execute()
            Timber.d("Google Drive authentication successful")
            true
        } catch (e: Exception) {
            Timber.e(TAG, "Authentication failed: ${e.message}")
            false
        }
    }

    suspend fun uploadFile(fileName: String, content: String, mimeType: String = "application/json"): String? = withContext(Dispatchers.IO) {
        try {
            val service = driveService ?: return@withContext null

            val folderId = getOrCreateBackupFolder(service)

            val fileMetadata = File()
            fileMetadata.setName(fileName)
            fileMetadata.setParents(listOf(folderId))
            fileMetadata.setMimeType(mimeType)

            val uploadedFile = service.files().create(
                fileMetadata,
                com.google.api.client.http.ByteArrayContent(mimeType, content.toByteArray())
            ).execute()

            Timber.d("File uploaded: $fileName (${uploadedFile.id})")
            uploadedFile.id
        } catch (e: Exception) {
            Timber.e(TAG, "Upload failed: ${e.message}")
            null
        }
    }

    suspend fun downloadFile(fileId: String): String? = withContext(Dispatchers.IO) {
        try {
            val service = driveService ?: return@withContext null
            val response = service.files().get(fileId).executeMediaAsInputStream()
            val reader = BufferedReader(InputStreamReader(response))
            val content = reader.readText()
            reader.close()
            content
        } catch (e: Exception) {
            Timber.e(TAG, "Download failed: ${e.message}")
            null
        }
    }

    suspend fun listBackupFiles(): List<Any> = withContext(Dispatchers.IO) {
        try {
            val service = driveService ?: return@withContext emptyList<Any>()
            val folderId = getOrCreateBackupFolder(service)

            val result = service.files().list()
                .setQ("'$folderId' in parents and mimeType='application/json'")
                .setOrderBy("createdTime desc")
                .execute()

            result.files ?: emptyList()
        } catch (e: Exception) {
            Timber.e(TAG, "List failed: ${e.message}")
            emptyList<Any>()
        }
    }

    suspend fun deleteFile(fileId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val service = driveService ?: return@withContext false
            service.files().delete(fileId).execute()
            true
        } catch (e: Exception) {
            Timber.e(TAG, "Delete failed: ${e.message}")
            false
        }
    }

    fun isAuthenticated(): Boolean = credential?.selectedAccountName != null

    private suspend fun getOrCreateBackupFolder(service: Drive): String = withContext(Dispatchers.IO) {
        try {
            val query = service.files().list()
                .setQ("name='$BACKUP_FOLDER' and mimeType='application/vnd.google-apps.folder' and trashed=false")
                .setPageSize(1)
                .execute()

            query.files?.firstOrNull()?.id ?: run {
                val folder = File().apply {
                    name = BACKUP_FOLDER
                    mimeType = "application/vnd.google-apps.folder"
                }
                service.files().create(folder).execute().id
            }
        } catch (e: Exception) {
            Timber.e(TAG, "Folder operation failed: ${e.message}")
            throw e
        }
    }
}
