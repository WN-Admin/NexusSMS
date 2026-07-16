package com.nexusmedia.nexussms.features.automation

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat
import com.nexusmedia.nexussms.MainActivity
import com.nexusmedia.nexussms.data.repository.ConversationRepository
import com.nexusmedia.nexussms.data.repository.MessageRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ActionExecutor @Inject constructor(
    @ApplicationContext private val context: Context,
    private val conversationRepository: ConversationRepository,
    private val messageRepository: MessageRepository
) {
    suspend fun execute(action: RuleAction, message: IncomingMessage): Result<Unit> {
        return try {
            when (action.type) {
                ActionType.COPY_TO_CLIPBOARD -> copyToClipboard(message.content)
                ActionType.EXTRACT_AND_COPY -> extractAndCopy(message.content, action.config)
                ActionType.FORWARD_TO_CONTACT -> forwardToContact(message, action.config)
                ActionType.FORWARD_TO_EMAIL -> forwardToEmail(message, action.config)
                ActionType.AUTO_REPLY -> autoReply(message, action.config)
                ActionType.ARCHIVE -> archiveConversation(message)
                ActionType.MARK_AS_READ -> markAsRead(message)
                ActionType.DELETE -> deleteMessage(message)
                ActionType.MUTE_CONVERSATION -> muteConversation(message)
                ActionType.LABEL -> addLabel(message, action.config)
                ActionType.NOTIFICATION -> showNotification(message, action.config)
                ActionType.WEBHOOK -> sendWebhook(message, action.config)
                ActionType.SOUND_ALERT -> playSound(action.config)
                ActionType.VIBRATE_PATTERN -> vibratePattern(action.config)
                ActionType.BLOCK_SENDER -> blockSender(message)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun copyToClipboard(text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("message", text)
        clipboard.setPrimaryClip(clip)
    }

    private fun extractAndCopy(text: String, config: Map<String, String>) {
        val pattern = config["pattern"] ?: return
        val regex = Regex(pattern)
        val matches = regex.findAll(text).map { it.value }.toList()

        if (matches.isNotEmpty()) {
            val extracted = matches.joinToString(", ")
            copyToClipboard(extracted)
        }
    }

    private fun forwardToContact(message: IncomingMessage, config: Map<String, String>) {
        val contactNumber = config["contactNumber"] ?: return
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("smsto:$contactNumber")
            putExtra("sms_body", "Fwd: ${message.senderNumber}: ${message.content}")
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    private fun forwardToEmail(message: IncomingMessage, config: Map<String, String>) {
        val email = config["email"] ?: return
        val subject = config["subject"] ?: "Forwarded SMS from ${message.senderNumber}"

        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")
            putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, message.content)
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    private suspend fun autoReply(message: IncomingMessage, config: Map<String, String>) {
        val replyMessage = config["message"] ?: return
        try {
            @Suppress("DEPRECATION")
            val smsManager = android.telephony.SmsManager.getDefault()
            smsManager.sendTextMessage(
                message.senderNumber, null, replyMessage, null, null
            )
            Timber.d("Auto-reply sent to %s", message.senderNumber)
        } catch (e: Exception) {
            Timber.e(e, "Auto-reply failed")
        }
    }

    private suspend fun archiveConversation(message: IncomingMessage) {
        val conversation = conversationRepository.findConversationWithParticipant(message.senderNumber)
        if (conversation != null) {
            conversationRepository.updateConversation(conversation.copy(isArchived = true))
            Timber.d("Archived conversation with %s", message.senderNumber)
        }
    }

    private suspend fun markAsRead(message: IncomingMessage) {
        if (message.conversationId.isNotEmpty()) {
            conversationRepository.markConversationAsRead(message.conversationId)
            Timber.d("Marked conversation %s as read", message.conversationId)
        }
    }

    private suspend fun deleteMessage(message: IncomingMessage) {
        if (message.id.isNotEmpty()) {
            messageRepository.deleteMessagesByIds(listOf(message.id))
            Timber.d("Deleted message %s", message.id)
        }
    }

    private suspend fun muteConversation(message: IncomingMessage) {
        val conversation = conversationRepository.findConversationWithParticipant(message.senderNumber)
        if (conversation != null) {
            conversationRepository.updateConversation(conversation.copy(isMuted = true))
            Timber.d("Muted conversation with %s", message.senderNumber)
        }
    }

    private fun addLabel(message: IncomingMessage, config: Map<String, String>) {
        val label = config["label"] ?: return
        Timber.d("Label action: would tag conversation with '%s' (label storage not yet implemented)", label)
    }

    private fun showNotification(message: IncomingMessage, config: Map<String, String>) {
        val title = config["title"] ?: "Rule Triggered"
        val text = config["text"] ?: message.content.take(100)

        val intent = Intent(context, MainActivity::class.java).apply {
            putExtra("openConversation", message.senderNumber)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context, message.senderNumber.hashCode(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val channelId = "automation_channel"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.sym_action_chat)
            .setContentTitle(title)
            .setContentText(text)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()
        notificationManager.notify(message.senderNumber.hashCode(), notification)
        Timber.d("Showed automation notification: %s", title)
    }

    private suspend fun sendWebhook(message: IncomingMessage, config: Map<String, String>) {
        val url = config["url"] ?: return
        withContext(Dispatchers.IO) {
            try {
                val client = OkHttpClient.Builder()
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .readTimeout(10, TimeUnit.SECONDS)
                    .build()

                val json = """
                    {
                        "sender": "${message.senderNumber}",
                        "content": "${message.content.replace("\"", "\\\"")}",
                        "timestamp": ${message.timestamp},
                        "conversationId": "${message.conversationId}"
                    }
                """.trimIndent()

                val body = json.toRequestBody("application/json".toMediaType())
                val request = Request.Builder()
                    .url(url)
                    .post(body)
                    .build()

                val response = client.newCall(request).execute()
                Timber.d("Webhook response: %d", response.code)
            } catch (e: Exception) {
                Timber.e(e, "Webhook failed: %s", url)
            }
        }
    }

    private fun playSound(config: Map<String, String>) {
        val soundUri = config["soundUri"]?.let { Uri.parse(it) }
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val ringtone = RingtoneManager.getRingtone(context, soundUri)
        ringtone?.play()
    }

    private fun vibratePattern(config: Map<String, String>) {
        val patternStr = config["pattern"] ?: "0,200,100,200"
        val pattern = patternStr.split(",").map { it.trim().toLong() }.toLongArray()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            val vibrator = vibratorManager.defaultVibrator
            vibrator.vibrate(VibrationEffect.createWaveform(pattern, 0))
        } else {
            @Suppress("DEPRECATION")
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            @Suppress("DEPRECATION")
            vibrator.vibrate(VibrationEffect.createWaveform(pattern, 0))
        }
    }

    private suspend fun blockSender(message: IncomingMessage) {
        val conversation = conversationRepository.findConversationWithParticipant(message.senderNumber)
        if (conversation != null) {
            conversationRepository.updateConversation(conversation.copy(isBlocked = true))
            Timber.d("Blocked sender %s", message.senderNumber)
        }
    }
}
