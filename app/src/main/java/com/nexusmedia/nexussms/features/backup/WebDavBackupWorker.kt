package com.nexusmedia.nexussms.features.backup

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkerParameters
import androidx.work.WorkManager
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import timber.log.Timber
import java.util.concurrent.TimeUnit

@HiltWorker
class WebDavBackupWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val webDavBackupService: WebDavBackupService
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        private const val TAG = "WebDavBackupWorker"
        const val WORK_NAME = "nexussms_webdav_auto_backup"

        fun schedule(context: Context, frequency: BackupFrequency) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresBatteryNotLow(true)
                .build()

            val (interval, timeUnit) = when (frequency) {
                BackupFrequency.HOURLY -> 1L to TimeUnit.HOURS
                BackupFrequency.DAILY -> 1L to TimeUnit.DAYS
                BackupFrequency.WEEKLY -> 7L to TimeUnit.DAYS
                BackupFrequency.MONTHLY -> 30L to TimeUnit.DAYS
            }

            val workRequest = PeriodicWorkRequestBuilder<WebDavBackupWorker>(interval, timeUnit)
                .setConstraints(constraints)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 10, TimeUnit.SECONDS)
                .build()

            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(
                    WORK_NAME,
                    ExistingPeriodicWorkPolicy.UPDATE,
                    workRequest
                )

            Timber.d("WebDAV auto backup scheduled: $frequency")
        }

        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
            Timber.d("WebDAV auto backup cancelled")
        }
    }

    override suspend fun doWork(): Result {
        return try {
            Timber.d("Starting WebDAV automatic backup")

            if (!webDavBackupService.authenticateFromStoredCredentials()) {
                Timber.e(TAG, "WebDAV: no stored credentials or re-authentication failed")
                return Result.failure()
            }

            val result = webDavBackupService.createBackup(
                dataTypes = listOf("shortcuts", "signatures", "themes", "conversations", "messages"),
                encrypt = true,
                isAutomatic = true
            )

            if (result.isSuccess) {
                Timber.d("WebDAV automatic backup completed successfully")
                Result.success()
            } else {
                Timber.e(TAG, "WebDAV automatic backup failed: ${result.exceptionOrNull()?.message}")
                if (runAttemptCount < 3) Result.retry() else Result.failure()
            }
        } catch (e: Exception) {
            Timber.e(TAG, "WebDAV automatic backup failed with exception: ${e.message}")
            if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
    }
}

enum class BackupFrequency {
    HOURLY,
    DAILY,
    WEEKLY,
    MONTHLY
}
