package com.nexusmedia.nexussms.services

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.RemoteInput
import com.nexusmedia.nexussms.MainActivity
import com.nexusmedia.nexussms.NexusSMSApplication
import com.nexusmedia.nexussms.R
import com.nexusmedia.nexussms.data.database.AppSecuritySettingsDao
import com.nexusmedia.nexussms.data.models.Conversation
import com.nexusmedia.nexussms.features.notifications.PerContactNotificationSettings
import com.nexusmedia.nexussms.receivers.NotificationActionReceiver
import com.nexusmedia.nexussms.ui.screens.QuickReplyActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SmsNotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context,
    private val appSecuritySettingsDao: AppSecuritySettingsDao,
    private val perContactNotificationSettings: PerContactNotificationSettings
) {
    fun showIncomingMessageNotification(
        conversation: Conversation,
        senderPhone: String,
        messageBody: String,
        messageId: String
    ) {
        if (conversation.isBlocked || conversation.isMuted) return

        val hideContent = runBlocking {
            appSecuritySettingsDao.getSecuritySettingsSync()?.hideNotificationContent == true
        }

        val perContactPrivacy = perContactNotificationSettings.getPrivacyLevel(conversation.id)
        val effectiveHideContent = when (perContactPrivacy) {
            PerContactNotificationSettings.PRIVACY_HIDDEN -> true
            PerContactNotificationSettings.PRIVACY_NONE -> return
            else -> hideContent
        }

        val displayName = conversation.displayName.ifBlank { senderPhone }
        val text = if (effectiveHideContent) "New message" else messageBody

        val replyLabel = context.getString(R.string.notification_reply)
        val remoteInput = RemoteInput.Builder(NotificationActionReceiver.KEY_REPLY_TEXT)
            .setLabel(replyLabel)
            .build()

        val replyIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = NotificationActionReceiver.ACTION_REPLY
            putExtra(NotificationActionReceiver.EXTRA_CONVERSATION_ID, conversation.id)
            putExtra(NotificationActionReceiver.EXTRA_SENDER_PHONE, senderPhone)
        }
        val replyPending = PendingIntent.getBroadcast(
            context,
            (conversation.id + "reply").hashCode(),
            replyIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )

        val markReadIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = NotificationActionReceiver.ACTION_MARK_READ
            putExtra(NotificationActionReceiver.EXTRA_CONVERSATION_ID, conversation.id)
            putExtra(NotificationActionReceiver.EXTRA_NOTIFICATION_ID, conversation.id.hashCode())
        }
        val markReadPending = PendingIntent.getBroadcast(
            context,
            (conversation.id + "read").hashCode(),
            markReadIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val quickReplyIntent = Intent(context, QuickReplyActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(QuickReplyActivity.EXTRA_CONVERSATION_ID, conversation.id)
            putExtra(QuickReplyActivity.EXTRA_SENDER_PHONE, senderPhone)
            putExtra(QuickReplyActivity.EXTRA_DISPLAY_NAME, displayName)
        }
        val quickReplyPending = PendingIntent.getActivity(
            context,
            (conversation.id + "qr").hashCode(),
            quickReplyIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val callIntent = Intent(Intent.ACTION_DIAL).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            data = Uri.parse("tel:$senderPhone")
        }
        val callPending = PendingIntent.getActivity(
            context,
            (conversation.id + "call").hashCode(),
            callIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val contentIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(MainActivity.EXTRA_OPEN_CONVERSATION_ID, conversation.id)
        }
        val contentPending = PendingIntent.getActivity(
            context,
            conversation.id.hashCode(),
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val visibility = if (effectiveHideContent) {
            NotificationCompat.VISIBILITY_SECRET
        } else {
            NotificationCompat.VISIBILITY_PUBLIC
        }

        val vibratePattern = perContactNotificationSettings.getVibratePattern(conversation.id)
            ?: perContactNotificationSettings.getDisplayHashVibratePattern(conversation.id)

        val builder = NotificationCompat.Builder(context, NexusSMSApplication.CHANNEL_SMS)
            .setSmallIcon(android.R.drawable.sym_action_chat)
            .setContentTitle(displayName)
            .setContentText(text)
            .setStyle(
                NotificationCompat.MessagingStyle(displayName)
                    .setConversationTitle(displayName)
                    .addMessage(text, System.currentTimeMillis(), senderPhone)
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setVisibility(visibility)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setVibrate(vibratePattern)
            .setAutoCancel(true)
            .setContentIntent(contentPending)
            .addAction(
                NotificationCompat.Action.Builder(
                    android.R.drawable.ic_menu_send,
                    replyLabel,
                    replyPending
                ).addRemoteInput(remoteInput).build()
            )
            .addAction(
                android.R.drawable.ic_menu_view,
                context.getString(R.string.notification_quick_reply),
                quickReplyPending
            )
            .addAction(
                android.R.drawable.ic_menu_call,
                context.getString(R.string.notification_call),
                callPending
            )
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                context.getString(R.string.notification_mark_read),
                markReadPending
            )

        val ringtoneUri = perContactNotificationSettings.getRingtone(conversation.id)
        if (ringtoneUri != null) {
            builder.setSound(Uri.parse(ringtoneUri))
        }

        val repeatEnabled = perContactNotificationSettings.isRepeatNotification(conversation.id)
        if (repeatEnabled) {
            builder.setOngoing(true)
        }

        val manager = context.getSystemService(NotificationManager::class.java)
        manager.notify(conversation.id.hashCode(), builder.build())
    }

    fun cancelNotification(conversationId: String) {
        context.getSystemService(NotificationManager::class.java)
            .cancel(conversationId.hashCode())
    }
}
