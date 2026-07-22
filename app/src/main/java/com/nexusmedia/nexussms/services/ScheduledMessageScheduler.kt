package com.nexusmedia.nexussms.services

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.nexusmedia.nexussms.receivers.ScheduledMessageAlarmReceiver
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScheduledMessageScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun schedulePeriodicCheck() {
        val request = PeriodicWorkRequestBuilder<ScheduledMessageWorker>(15, TimeUnit.MINUTES)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }

    fun scheduleExactAlarm(
        scheduledMsgId: String,
        triggerAtMillis: Long,
        repeatType: String? = null,
        repeatUntil: Long = -1L
    ) {
        ScheduledMessageAlarmReceiver.scheduleExactAlarm(
            context, scheduledMsgId, triggerAtMillis, repeatType, repeatUntil
        )
    }

    companion object {
        private const val WORK_NAME = "scheduled_message_worker"
    }
}
