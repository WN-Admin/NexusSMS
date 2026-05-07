package com.nexussms.features.backup

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import timber.log.Timber

@HiltWorker
class BackupWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val backupService: GoogleDriveBackupService
) : CoroutineWorker(context, params) {

    companion object {
        private const val TAG = "BackupWorker"
    }

    override suspend fun doWork(): Result {
        return try {
            Timber.d("Starting automatic backup")
            val result = backupService.createBackup(
                dataTypes = listOf("shortcuts", "signatures", "themes"),
                encrypt = true,
                isAutomatic = true
            )

            if (result.isSuccess) {
                Timber.d("Automatic backup completed successfully")
                Result.success()
            } else {
                Timber.e(TAG, "Automatic backup failed: ${result.exceptionOrNull()?.message}")
                Result.retry()
            }
        } catch (e: Exception) {
            Timber.e(TAG, "Automatic backup failed with exception: ${e.message}")
            if (runAttemptCount < 3) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }
}
