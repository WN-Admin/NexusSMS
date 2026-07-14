package com.nexusmedia.nexussms

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import androidx.work.Configuration
import androidx.hilt.work.HiltWorkerFactory
import com.nexusmedia.nexussms.BuildConfig
import com.nexusmedia.nexussms.services.ScheduledMessageScheduler
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class NexusSMSApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var scheduledMessageScheduler: ScheduledMessageScheduler

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        createNotificationChannels()
        scheduledMessageScheduler.schedulePeriodicCheck()
    }

    private fun createNotificationChannels() {
        val notificationManager = getSystemService(NotificationManager::class.java)

        val smsChannel = NotificationChannel(
            CHANNEL_SMS,
            "SMS Messages",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Incoming SMS message notifications"
        }

        val scheduledChannel = NotificationChannel(
            CHANNEL_SCHEDULED,
            "Scheduled Messages",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Scheduled message notifications"
        }

        val backupChannel = NotificationChannel(
            CHANNEL_BACKUP,
            "Backup",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Backup status notifications"
        }

        notificationManager.createNotificationChannels(
            listOf(smsChannel, scheduledChannel, backupChannel)
        )
    }

    companion object {
        const val CHANNEL_SMS = "nexussms_sms"
        const val CHANNEL_SCHEDULED = "nexussms_scheduled"
        const val CHANNEL_BACKUP = "nexussms_backup"
    }
}
