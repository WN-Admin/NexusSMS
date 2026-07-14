package com.nexusmedia.nexussms.receivers

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.nexusmedia.nexussms.services.SmsSender
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SmsStatusReceiver : BroadcastReceiver() {

    @Inject lateinit var smsSender: SmsSender

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent == null) return
        val messageId = intent.getStringExtra(SmsSender.EXTRA_MESSAGE_ID) ?: return
        val pendingResult = goAsync()
        val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

        scope.launch {
            try {
                when (intent.action) {
                    SmsSender.ACTION_SMS_SENT -> {
                        smsSender.applySentResult(messageId, resultCode == Activity.RESULT_OK)
                    }
                    SmsSender.ACTION_SMS_DELIVERED -> {
                        smsSender.applyDeliveredResult(messageId, resultCode == Activity.RESULT_OK)
                    }
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
