package com.nexusmedia.nexussms.features.automation

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
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ActionExecutor @Inject constructor(
    @ApplicationContext private val context: Context
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
        // Would use SmsSender in real implementation
    }

    private fun archiveConversation(message: IncomingMessage) {
        // Would update conversation in database
    }

    private fun markAsRead(message: IncomingMessage) {
        // Would mark message as read
    }

    private fun deleteMessage(message: IncomingMessage) {
        // Would delete the message
    }

    private fun muteConversation(message: IncomingMessage) {
        // Would mute the conversation
    }

    private fun addLabel(message: IncomingMessage, config: Map<String, String>) {
        val label = config["label"] ?: return
        // Would add label to message
    }

    private fun showNotification(message: IncomingMessage, config: Map<String, String>) {
        val title = config["title"] ?: "Rule Triggered"
        val text = config["text"] ?: message.content.take(100)
        // Would show notification
    }

    private suspend fun sendWebhook(message: IncomingMessage, config: Map<String, String>) {
        val url = config["url"] ?: return
        // Would make HTTP POST request
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

    private fun blockSender(message: IncomingMessage) {
        // Would add sender to blocklist
    }
}
