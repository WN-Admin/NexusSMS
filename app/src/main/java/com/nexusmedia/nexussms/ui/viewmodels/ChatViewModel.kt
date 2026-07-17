package com.nexusmedia.nexussms.ui.viewmodels

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
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
import androidx.compose.ui.graphics.Color
import com.nexusmedia.nexussms.features.shortcodes.ShortcodeExpansionService
import com.nexusmedia.nexussms.features.matrix.MatrixMessageService
import com.nexusmedia.nexussms.features.matrix.MatrixSyncService
import com.nexusmedia.nexussms.features.telegram.TelegramService
import com.nexusmedia.nexussms.features.discord.DiscordService
import com.nexusmedia.nexussms.features.messenger.MessengerService
import com.nexusmedia.nexussms.features.messaging.SimSelector
import com.nexusmedia.nexussms.features.messaging.ChannelRoutingManager
import com.nexusmedia.nexussms.features.smartreply.SmartReplyService
import com.nexusmedia.nexussms.data.models.Template
import com.nexusmedia.nexussms.data.repository.TemplateRepository
import com.nexusmedia.nexussms.data.repository.ReactionRepository
import com.nexusmedia.nexussms.data.models.Reaction
import com.nexusmedia.nexussms.features.messaging.MessagingPreferences
import com.nexusmedia.nexussms.security.EncryptionManager
import com.nexusmedia.nexussms.services.SmsSender
import com.nexusmedia.nexussms.services.ScheduledMessageScheduler
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
    private val themeRepository: ThemeRepository,
    private val matrixMessageService: MatrixMessageService,
    private val matrixSyncService: MatrixSyncService,
    private val telegramService: TelegramService,
    private val discordService: DiscordService,
    private val messengerService: MessengerService,
    private val simSelector: SimSelector,
    private val smartReplyService: SmartReplyService,
    private val templateRepository: TemplateRepository,
    private val channelRoutingManager: ChannelRoutingManager,
    private val smsSender: SmsSender,
    private val reactionRepository: ReactionRepository,
    private val messagingPreferences: MessagingPreferences,
    private val scheduledMessageScheduler: ScheduledMessageScheduler
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

    private val _allConversations = MutableStateFlow<List<Conversation>>(emptyList())
    val allConversations: StateFlow<List<Conversation>> = _allConversations.asStateFlow()

    private val _smartReplies = MutableStateFlow<List<SmartReplyService.SmartReplySuggestion>>(emptyList())
    val smartReplies: StateFlow<List<SmartReplyService.SmartReplySuggestion>> = _smartReplies.asStateFlow()

    private val _templates = MutableStateFlow<List<Template>>(emptyList())
    val templates: StateFlow<List<Template>> = _templates.asStateFlow()

    private val _showTemplatePicker = MutableStateFlow(false)
    val showTemplatePicker: StateFlow<Boolean> = _showTemplatePicker.asStateFlow()

    private var conversationJob: Job? = null
    private var messagesJob: Job? = null
    private var messagePageOffset = 0
    private val messagePageSize = 50
    private var allMessagesLoaded = false

    val hasMoreMessages: StateFlow<Boolean>
        get() = _hasMoreMessages
    private val _hasMoreMessages = MutableStateFlow(true)

    fun loadConversation(conversationId: String) {
        conversationJob?.cancel()
        messagesJob?.cancel()

        _sendDelaySeconds.value = messagingPreferences.sendDelaySeconds
        messagePageOffset = 0
        allMessagesLoaded = false
        _hasMoreMessages.value = true

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
            .onEach { msgs ->
                val initial = if (messagePageOffset == 0) {
                    msgs.take(messagePageSize)
                } else {
                    _messages.value + msgs.drop(messagePageOffset)
                }
                _messages.value = initial
                _hasMoreMessages.value = initial.size < msgs.size
                messagePageOffset = initial.size
                generateSmartReplies(initial)
            }
            .launchIn(viewModelScope)

        viewModelScope.launch {
            conversationRepository.getAllConversations()
                .onEach { list -> _allConversations.value = list }
                .launchIn(viewModelScope)
        }

        if (_currentConversation.value?.sourcePlatform == "MATRIX") {
            viewModelScope.launch {
                try { matrixSyncService.syncForRoom(_currentConversation.value?.sourceAccountId ?: "") } catch (e: Exception) { Timber.w(e, "Matrix sync failed") }
            }
        }
    }

    fun loadMoreMessages() {
        if (allMessagesLoaded) return
        viewModelScope.launch {
            val convId = _currentConversation.value?.id ?: return@launch
            val all = messageRepository.getConversationMessages(convId).first()
            val nextEnd = (messagePageOffset + messagePageSize).coerceAtMost(all.size)
            if (nextEnd >= all.size) allMessagesLoaded = true
            _messages.value = all.take(nextEnd)
            _hasMoreMessages.value = !allMessagesLoaded
            messagePageOffset = nextEnd
        }
    }

    fun updateMessageText(text: String) {
        _messageText.value = text
        checkForShortcutTriggers(text)
    }

    fun toggleMute(conversationId: String) {
        viewModelScope.launch {
            val conv = conversationRepository.getConversationById(conversationId) ?: return@launch
            conversationRepository.updateConversation(conv.copy(isMuted = !conv.isMuted))
        }
    }

    fun toggleBlock(conversationId: String) {
        viewModelScope.launch {
            val conv = conversationRepository.getConversationById(conversationId) ?: return@launch
            conversationRepository.updateConversation(conv.copy(isBlocked = !conv.isBlocked))
        }
    }

    fun sendMessage(conversationId: String, recipientPhone: String) {
        viewModelScope.launch {
            _isSending.value = true
            try {
                val delay = _sendDelaySeconds.value
                if (delay > 0) {
                    kotlinx.coroutines.delay(delay * 1000L)
                }

                var messageContent = _messageText.value
                messageContent = shortcodeExpansionService.expandMessage(messageContent)
                messageContent = encryptionManager.generateMessageSignature(messageContent)

                val attachments = _pendingAttachments.value
                val mediaUrlsStr = attachments.joinToString(",")

                val platform = _currentConversation.value?.sourcePlatform ?: "SMS"

                // Use ChannelRouter for non-SMS platform conversations to enable fallback
                if (platform != "SMS" && platform != "RCS") {
                    val contactId = conversationId
                    val availablePlatforms = mutableListOf(platform, "SMS")
                    val routingResult = channelRoutingManager.routeMessage(
                        contactId = contactId,
                        message = messageContent,
                        availablePlatforms = availablePlatforms.distinct()
                    ) { sendPlatform, msg ->
                        when (sendPlatform) {
                            "MATRIX" -> {
                                val roomId = _currentConversation.value?.sourceAccountId ?: ""
                                if (roomId.isBlank()) return@routeMessage Result.failure(Exception("No roomId"))
                                val result = matrixMessageService.sendTextMessage(
                                    roomId = roomId,
                                    content = msg,
                                    conversationId = conversationId,
                                    recipientId = roomId
                                )
                                if (result.success) Result.success(Unit) else Result.failure(Exception(result.error ?: "Matrix send failed"))
                            }
                            "TELEGRAM" -> {
                                val chatId = _currentConversation.value?.sourceAccountId?.toLongOrNull()
                                    ?: return@routeMessage Result.failure(Exception("No Telegram chatId"))
                                if (telegramService.sendMessage(chatId, msg)) Result.success(Unit) else Result.failure(Exception("Telegram send failed"))
                            }
                            "DISCORD" -> {
                                val channelId = _currentConversation.value?.sourceAccountId ?: ""
                                if (channelId.isBlank()) return@routeMessage Result.failure(Exception("No Discord channelId"))
                                if (discordService.sendMessage(channelId, msg)) Result.success(Unit) else Result.failure(Exception("Discord send failed"))
                            }
                            "MESSENGER" -> {
                                val recipientId = _currentConversation.value?.sourceAccountId ?: ""
                                if (recipientId.isBlank()) return@routeMessage Result.failure(Exception("No Messenger recipientId"))
                                if (messengerService.sendMessage(recipientId, msg)) Result.success(Unit) else Result.failure(Exception("Messenger send failed"))
                            }
                            "SMS" -> {
                                val simSubId = _selectedSim.value?.subscriptionId
                                smsSender.sendTextMessage(
                                    conversationId = conversationId,
                                    recipientPhone = recipientPhone,
                                    content = msg,
                                    persistToDb = true,
                                    subscriptionId = simSubId
                                )
                                Result.success(Unit)
                            }
                            else -> Result.failure(Exception("Unknown platform: $sendPlatform"))
                        }
                    }
                    if (!routingResult.success) {
                        Timber.e("Channel routing failed: %s", routingResult.error)
                    }
                } else {
                    when (_selectedMessageType.value) {
                    "SMS" -> {
                        val messageId = java.util.UUID.randomUUID().toString()
                        val simSubId = _selectedSim.value?.subscriptionId
                        smsSender.sendTextMessage(
                            conversationId = conversationId,
                            recipientPhone = recipientPhone,
                            content = messageContent,
                            existingMessageId = messageId,
                            persistToDb = true,
                            subscriptionId = simSubId
                        )
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
                            val simSubId = _selectedSim.value?.subscriptionId
                            smsSender.sendTextMessage(
                                conversationId = conversationId,
                                recipientPhone = recipientPhone,
                                content = messageContent,
                                persistToDb = true,
                                subscriptionId = simSubId
                            )
                        }
                    }
                    }
                }

                _messageText.value = ""
                _pendingAttachments.value = emptyList()
            } catch (e: Exception) {
                Timber.e(e, "Message send failed")
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
            val senderPhone = messagingPreferences.mobileNumber.ifEmpty { "local" }
            val existing = reactionRepository.getReactionsByMessage(messageId).first()
                .find { it.emoji == reaction && it.senderPhoneNumber == senderPhone }
            if (existing != null) {
                reactionRepository.deleteReaction(existing)
            } else {
                reactionRepository.insertReaction(
                    Reaction(
                        messageId = messageId,
                        emoji = reaction,
                        senderPhoneNumber = senderPhone
                    )
                )
            }
        }
    }

    fun copyMessage(messageId: String) {
        val msg = _messages.value.find { it.id == messageId } ?: return
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("message", msg.content)
        clipboard.setPrimaryClip(clip)
    }

    fun toggleLockMessage(messageId: String) {
        viewModelScope.launch {
            val msg = messageRepository.getMessageById(messageId) ?: return@launch
            messageRepository.setMessageLocked(messageId, !msg.isLocked)
        }
    }

    fun forwardMessage(messageId: String): Message? {
        return _messages.value.find { it.id == messageId }
    }

    private val _selectedMessages = MutableStateFlow<Set<String>>(emptySet())
    val selectedMessages: StateFlow<Set<String>> = _selectedMessages.asStateFlow()

    private val _multiSelectMode = MutableStateFlow(false)
    val multiSelectMode: StateFlow<Boolean> = _multiSelectMode.asStateFlow()

    fun toggleMultiSelect(messageId: String) {
        val current = _selectedMessages.value.toMutableSet()
        if (current.contains(messageId)) {
            current.remove(messageId)
        } else {
            current.add(messageId)
        }
        _selectedMessages.value = current
        _multiSelectMode.value = current.isNotEmpty()
    }

    fun enterMultiSelect(messageId: String) {
        _selectedMessages.value = setOf(messageId)
        _multiSelectMode.value = true
    }

    fun exitMultiSelect() {
        _selectedMessages.value = emptySet()
        _multiSelectMode.value = false
    }

    fun deleteSelectedMessages() {
        viewModelScope.launch {
            val ids = _selectedMessages.value.toList()
            messageRepository.deleteMessagesByIds(ids)
            exitMultiSelect()
        }
    }

    fun lockSelectedMessages() {
        viewModelScope.launch {
            _selectedMessages.value.forEach { id ->
                messageRepository.setMessageLocked(id, true)
            }
            exitMultiSelect()
        }
    }

    private val _sendDelaySeconds = MutableStateFlow(0)
    val sendDelaySeconds: StateFlow<Int> = _sendDelaySeconds.asStateFlow()

    fun setSendDelay(seconds: Int) {
        _sendDelaySeconds.value = seconds.coerceIn(0, 9)
    }

    fun copySelectedMessages() {
        val texts = _messages.value
            .filter { it.id in _selectedMessages.value }
            .joinToString("\n") { it.content }
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("messages", texts)
        clipboard.setPrimaryClip(clip)
    }

    private val _pendingAttachments = MutableStateFlow<List<String>>(emptyList())
    val pendingAttachments: StateFlow<List<String>> = _pendingAttachments.asStateFlow()

    private val _availableSims = MutableStateFlow<List<SimSelector.SimInfo>>(emptyList())
    val availableSims: StateFlow<List<SimSelector.SimInfo>> = _availableSims.asStateFlow()

    private val _selectedSim = MutableStateFlow<SimSelector.SimInfo?>(null)
    val selectedSim: StateFlow<SimSelector.SimInfo?> = _selectedSim.asStateFlow()

    init {
        loadAvailableSims()
    }

    private fun loadAvailableSims() {
        val sims = simSelector.getAvailableSims()
        _availableSims.value = sims
        if (sims.size > 1 && _selectedSim.value == null) {
            _selectedSim.value = sims.first()
        }
    }

    fun selectSim(sim: SimSelector.SimInfo) {
        _selectedSim.value = sim
    }

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

    fun sendGifAsMessage(conversationId: String, recipientPhone: String, gifText: String) {
        viewModelScope.launch {
            _messageText.value = gifText
            sendMessage(conversationId, recipientPhone)
        }
    }

    fun sendMessageWithSticker(conversationId: String, recipientPhone: String, stickerId: String) {
        viewModelScope.launch {
            _isSending.value = true
            try {
                rcsService.shareSticker(recipientPhone, stickerId, conversationId)
            } catch (e: Exception) {
                Timber.e(e, "Sticker send failed")
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
            val delayMs = scheduleAt - System.currentTimeMillis()
            if (delayMs in 1 until 15 * 60 * 1000L) {
                scheduledMessageScheduler.scheduleExactAlarm(
                    scheduledMsgId = scheduledMessage.id,
                    conversationId = conversationId,
                    recipientPhone = recipientPhone,
                    content = content,
                    triggerAtMillis = scheduleAt,
                    repeatType = scheduledMessage.repeatType,
                    repeatUntil = scheduledMessage.repeatUntil ?: -1L
                )
            }
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

    private fun generateSmartReplies(messages: List<Message>) {
        val lastIncoming = messages.lastOrNull { it.senderPhoneNumber != "self" }
        if (lastIncoming != null) {
            _smartReplies.value = smartReplyService.getSuggestions(lastIncoming.content, isIncoming = true)
        } else {
            _smartReplies.value = emptyList()
        }
    }

    fun applySmartReply(text: String) {
        _messageText.value = text
        _smartReplies.value = emptyList()
    }

    fun loadTemplates() {
        viewModelScope.launch {
            templateRepository.getAllTemplates()
                .onEach { _templates.value = it }
                .launchIn(viewModelScope)
        }
    }

    fun showTemplatePicker() {
        loadTemplates()
        _showTemplatePicker.value = true
    }

    fun hideTemplatePicker() {
        _showTemplatePicker.value = false
    }

    fun applyTemplate(template: Template) {
        _messageText.value = template.content
        viewModelScope.launch {
            templateRepository.incrementUsage(template.id)
        }
        _showTemplatePicker.value = false
    }

    companion object {
        private val SHORTCODE_PATTERN = listOf("!", "@")
    }
}
