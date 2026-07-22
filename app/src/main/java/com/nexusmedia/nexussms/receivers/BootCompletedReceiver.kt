package com.nexusmedia.nexussms.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.nexusmedia.nexussms.data.repository.ScheduledMessageRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class BootCompletedReceiver : BroadcastReceiver() {

    @Inject lateinit var scheduledMessageRepository: ScheduledMessageRepository

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Timber.d("Boot completed, re-scheduling pending alarms")
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    ScheduledMessageAlarmReceiver.rescheduleAllPendingAlarms(
                        context, scheduledMessageRepository
                    )
                } catch (e: Exception) {
                    Timber.e(e, "Failed to reschedule alarms after boot")
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
