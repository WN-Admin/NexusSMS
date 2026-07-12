package com.nexusmedia.nexussms.ui.viewmodels

import android.app.Activity
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.telephony.SmsManager
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexusmedia.nexussms.data.models.Conversation
import com.nexusmedia.nexussms.data.models.Message
import com.nexusmedia.nexussms.data.repository.ContactAvatarRepository
import com.nexusmedia.nexussms.data.repository.ConversationRepository
import com.nexusmedia.nexussms.data.repository.MessageRepository
import com.nexusmedia.nexussms.data.repository.ScheduledMessageRepository
import com.nexusmedia.nexussms.data.repository.ThemeRepository
import com.nexusmedia.nexussms.features.rcs.RcsService
import com.nexusmedia.nexussms.ui.theme.BubbleTheme
import com.nexusmedia.nexussms.ui.theme.TonalPalette
import androidx.compose.ui.graphics.Color
import com.nexusmedia.nexussms.features.shortcodes.ShortcodeExpansionService
import com.nexusmedia.nexussms.security.EncryptionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val messageRepository: MessageRepository,
    private val conversationRepository: ConversationRepository,
    private val scheduledMessageRepository: ScheduledMessageRepository,
    private val shortcodeExpansionService: ShortcodeExpansionService,
    private val rcsService: RcsService,
    private val encryptionManager: EncryptionManager,
    private val contactAvatarRepository: ContactAvatarRepository,
    private val themeRepository: ThemeRepository
) : ViewModel() {

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    private val _currentConversation = MutableStateFlow<Conversation?>(null)
    val currentConversation: StateFlow<Conversation?> = _currentConversation.asStateFlow()

    private val _messageText = MutableStateFlow("")
    val messageText: StateFlow<String> = _messageText.asStateFlow()

    private val _isSending = MutableStateFlow(false)
    val isSending: StateFlow<Boolean> = _isSending.asStateFlow()

    private val _selectedMessageType = MutableStateFlow("SMS")
    val selectedMessageType: StateFlow<String> = _selectedMessageType.asStateFlow()

    private val _shortcutSuggestions = MutableStateFlow<List<ShortcodeExpansionService.ShortcutPreview>>(emptyList())
    val shortcutSuggestions: StateFlow<List<ShortcodeExpansionService.ShortcutPreview>> = _shortcutSuggestions.asStateFlow()

    private val _contactAvatarUri = MutableStateFlow<String?>(null)
    val contactAvatarUri: StateFlow<String?> = _contactAvatarUri.asStateFlow()

    private val _conversationBubbleTheme = MutableStateFlow<BubbleTheme?>(null)
    val conversationBubbleTheme: StateFlow<BubbleTheme?> = _conversationBubbleTheme.asStateFlow()

    private var conversationJob: Job? = null
    private var messagesJob: Job? = null

    fun loadConversation(conversationId: String) {
        conversationJob?.cancel()
        messagesJob?.cancel()

        conversationJob = conversationRepository.getAllConversations()
            .onEach { list ->
                val conv = list.find { it.id == conversationId }
                _currentConversation.value = conv
                if (conv != null) {
                    val normalized = conv.participantPhoneNumbers.replace(Regex("[^+\\d]"), "")
                    _contactAvatarUri.value = contactAvatarRepository.getByPhone(normalized)?.photoUri
                    val themeId = conv.themeId
                    _conversationBubbleTheme.value = if (themeId != null) {
                        val theme = themeRepository.getThemeById(themeId)
                        theme?.let {
                            val parseColor = { hex: String -> try { Color(android.graphics.Color.parseColor(hex)) } catch (_: Exception) { Color.Unspecified } }
                            BubbleTheme(
                                sentColor = parseColor(it.bubbleColorSent),
                                receivedColor = parseColor(it.bubbleColorReceived),
                                sentTextColor = parseColor(it.bubbleTextColorSent),
                                receivedTextColor = parseColor(it.bubbleTextColorReceived),
                                cornerRadius = it.bubbleCornerRadius,
                                elevation = it.bubbleElevation
                            )
                        }
                    } else null
                }
            }
            .launchIn(viewModelScope)

        messagesJob = messageRepository.getConversationMessages(conversationId)
            .onEach { msgs -> _messages.value = msgs.reversed() }
            .launchIn(viewModelScope)
    }

    fun updateMessageText(text: String) {
        _messageText.value = text
        checkForShortcutTriggers(text)
    }

    fun sendMessage(conversationId: String, recipientPhone: String) {
        viewModelScope.launch {
            _isSending.value = true
            try {
                var messageContent = _messageText.value
                messageContent = shortcodeExpansionService.expandMessage(messageContent)
                messageContent = encryptionManager.generateMessageSignature(messageContent)

                val attachments = _pendingAttachments.value
                val mediaUrlsStr = attachments.joinToString(",")

                when (_selectedMessageType.value) {
                    "SMS" -> {
                        val messageId = java.util.UUID.randomUUID().toString()
                        val message = Message(
                            id = messageId,
                            conversationId = conversationId,
                            senderPhoneNumber = "self",
                            recipientPhoneNumber = recipientPhone,
                            content = messageContent,
                            timestamp = System.currentTimeMillis(),
                            type = if (attachments.isNotEmpty()) "MMS" else "TEXT",
                            status = "SENDING",
                            isEncrypted = true,
                            encryptionAlgorithm = "AES256",
                            mediaUrls = mediaUrlsStr
                        )
                        messageRepository.insertMessage(message)

                        try {
                            val smsManager: SmsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                context.getSystemService(SmsManager::class.java)
                            } else {
                                @Suppress("DEPRECATION")
                                SmsManager.getDefault()
                            }

                            val parts = smsManager.divideMessage(messageContent)
                            val sentIntents = ArrayList<PendingIntent>()
                            val deliveredIntents = ArrayList<PendingIntent>()

                            for (i in parts.indices) {
                                val sentIntent = PendingIntent.getBroadcast(
                                    context,
                                    messageId.hashCode() + i,
                                    Intent("com.nexusmedia.nexussms.SMS_SENT").setPackage(context.packageName),
                                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                                )
                                sentIntents.add(sentIntent)

                                val deliveredIntent = PendingIntent.getBroadcast(
                                    context,
                                    messageId.hashCode() + i + 10000,
                                    Intent("com.nexusmedia.nexussms.SMS_DELIVERED").setPackage(context.packageName),
                                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                                )
                                deliveredIntents.add(deliveredIntent)
                            }

                            val sentReceiver = object : BroadcastReceiver() {
                                override fun onReceive(ctx: Context?, intent: Intent?) {
                                    val resultCode = resultCode
                                    viewModelScope.launch {
                                        val updatedMessage = message.copy(
                                            status = if (resultCode == Activity.RESULT_OK) "SENT" else "FAILED"
                                        )
                                        messageRepository.updateMessage(updatedMessage)
                                    }
                                    try { context.unregisterReceiver(this) } catch (_: Exception) {}
                                }
                            }
                            context.registerReceiver(
                                sentReceiver,
                                IntentFilter("com.nexusmedia.nexussms.SMS_SENT"),
                                Context.RECEIVER_NOT_EXPORTED
                            )

                            val deliveredReceiver = object : BroadcastReceiver() {
                                override fun onReceive(ctx: Context?, intent: Intent?) {
                                    try { context.unregisterReceiver(this) } catch (_: Exception) {}
                                }
                            }
                            context.registerReceiver(
                                deliveredReceiver,
                                IntentFilter("com.nexusmedia.nexussms.SMS_DELIVERED"),
                                Context.RECEIVER_NOT_EXPORTED
                            )

                            smsManager.sendMultipartTextMessage(
                                recipientPhone,
                                null,
                                parts,
                                sentIntents,
                                deliveredIntents
                            )
                        } catch (e: Exception) {
                            messageRepository.updateMessage(message.copy(status = "FAILED"))
                            e.printStackTrace()
                        }
                    }
                    "RCS" -> {
                        if (rcsService.isRcsAvailable()) {
                            rcsService.sendRcsMessage(
                                recipientPhone,
                                messageContent,
                                attachments,
                                conversationId
                            )
                        } else {
                            Timber.w("RCS requested but not available, falling back to SMS")
                            val fallbackMessage = Message(
                                conversationId = conversationId,
                                senderPhoneNumber = "self",
                                recipientPhoneNumber = recipientPhone,
                                content = messageContent,
                                timestamp = System.currentTimeMillis(),
                                type = "TEXT",
                                status = "SENT",
                                isEncrypted = false,
                                mediaUrls = mediaUrlsStr
                            )
                            messageRepository.insertMessage(fallbackMessage)
                            try {
                                val smsManager: SmsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                    context.getSystemService(SmsManager::class.java)
                                } else {
                                    @Suppress("DEPRECATION")
                                    SmsManager.getDefault()
                                }
                                val parts = smsManager.divideMessage(messageContent)
                                smsManager.sendMultipartTextMessage(recipientPhone, null, parts, null, null)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                }

                _messageText.value = ""
                _pendingAttachments.value = emptyList()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isSending.value = false
            }
        }
    }

    fun sendLocation(conversationId: String, locationUrl: String) {
        viewModelScope.launch {
            _messageText.value = locationUrl
            val recipient = _currentConversation.value?.participantPhoneNumbers ?: ""
            sendMessage(conversationId, recipient)
        }
    }

    fun deleteMessage(message: Message) {
        viewModelScope.launch {
            messageRepository.deleteMessage(message)
        }
    }

    fun markAsRead(conversationId: String) {
        viewModelScope.launch {
            conversationRepository.markConversationAsRead(conversationId)
        }
    }

    fun setMessageType(type: String) {
        _selectedMessageType.value = type
    }

    fun addReaction(messageId: String, reaction: String) {
        viewModelScope.launch {
            val message = messageRepository.getMessageById(messageId)
            if (message != null) {
                val updatedMessage = message.copy(
                    reactions = reaction
                )
                messageRepository.updateMessage(updatedMessage)
            }
        }
    }

    private val _pendingAttachments = MutableStateFlow<List<String>>(emptyList())
    val pendingAttachments: StateFlow<List<String>> = _pendingAttachments.asStateFlow()

    fun attachFile(uri: String) {
        viewModelScope.launch {
            _pendingAttachments.value = _pendingAttachments.value + uri
        }
    }

    fun attachImage(uri: String) {
        viewModelScope.launch {
            _pendingAttachments.value = _pendingAttachments.value + uri
        }
    }

    fun removeAttachment(uri: String) {
        _pendingAttachments.value = _pendingAttachments.value - uri
    }

    fun setWallpaper(url: String?) {
        viewModelScope.launch {
            val conv = _currentConversation.value ?: return@launch
            conversationRepository.updateConversation(conv.copy(wallpaperUrl = url))
        }
    }

    fun sendMessageWithSticker(conversationId: String, recipientPhone: String, stickerId: String) {
        viewModelScope.launch {
            _isSending.value = true
            try {
                rcsService.shareSticker(recipientPhone, stickerId, conversationId)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isSending.value = false
            }
        }
    }

    fun scheduleMessage(conversationId: String, recipientPhone: String, scheduleAt: Long) {
        viewModelScope.launch {
            val content = _messageText.value
            if (content.isBlank()) return@launch
            val scheduledMessage = com.nexusmedia.nexussms.data.models.ScheduledMessage(
                conversationId = conversationId,
                recipientPhoneNumber = recipientPhone,
                content = content,
                scheduledTime = scheduleAt,
                status = "PENDING"
            )
            scheduledMessageRepository.insertScheduledMessage(scheduledMessage)
            _messageText.value = ""
            _shortcutSuggestions.value = emptyList()
        }
    }

    private fun checkForShortcutTriggers(text: String) {
        viewModelScope.launch {
            val words = text.split(" ")
            val lastWord = words.lastOrNull() ?: ""
            if (lastWord.startsWith("!") || lastWord.startsWith("@")) {
                _shortcutSuggestions.value = shortcodeExpansionService.previewExpansions(lastWord)
            } else {
                _shortcutSuggestions.value = emptyList()
            }
        }
    }

    fun applyShortcutSuggestion(trigger: String, expansion: String) {
        val text = _messageText.value
        val words = text.split(" ")
        val lastWord = words.lastOrNull() ?: ""
        if (lastWord == trigger) {
            val prefix = text.substringBeforeLast(lastWord)
            _messageText.value = prefix + expansion
        }
        _shortcutSuggestions.value = emptyList()
    }

    companion object {
        private val SHORTCODE_PATTERN = listOf("!", "@")
    }
}
