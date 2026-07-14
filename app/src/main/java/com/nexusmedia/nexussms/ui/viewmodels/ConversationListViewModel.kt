package com.nexusmedia.nexussms.ui.viewmodels

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexusmedia.nexussms.data.models.Conversation
import com.nexusmedia.nexussms.data.repository.ContactAvatarRepository
import com.nexusmedia.nexussms.data.repository.ConversationRepository
import com.nexusmedia.nexussms.data.repository.SmsImporter
import com.nexusmedia.nexussms.features.matrix.MatrixAuthService
import com.nexusmedia.nexussms.features.matrix.MatrixSyncService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ConversationListViewModel @Inject constructor(
    private val conversationRepository: ConversationRepository,
    private val smsImporter: SmsImporter,
    private val contactAvatarRepository: ContactAvatarRepository,
    private val matrixAuthService: MatrixAuthService,
    private val matrixSyncService: MatrixSyncService,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _conversationList = MutableStateFlow<List<Conversation>>(emptyList())
    val conversationList: StateFlow<List<Conversation>> = _conversationList.asStateFlow()

    private val _pinnedConversations = MutableStateFlow<List<Conversation>>(emptyList())
    val pinnedConversations: StateFlow<List<Conversation>> = _pinnedConversations.asStateFlow()

    private val _blockedConversations = MutableStateFlow<List<Conversation>>(emptyList())
    val blockedConversations: StateFlow<List<Conversation>> = _blockedConversations.asStateFlow()

    private val _archivedConversations = MutableStateFlow<List<Conversation>>(emptyList())
    val archivedConversations: StateFlow<List<Conversation>> = _archivedConversations.asStateFlow()

    private val _selectedConversation = MutableStateFlow<Conversation?>(null)
    val selectedConversation: StateFlow<Conversation?> = _selectedConversation.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isImporting = MutableStateFlow(false)
    val isImporting: StateFlow<Boolean> = _isImporting.asStateFlow()

    private val _importResult = MutableStateFlow<String?>(null)
    val importResult: StateFlow<String?> = _importResult.asStateFlow()

    private val _needsPermission = MutableStateFlow(false)
    val needsPermission: StateFlow<Boolean> = _needsPermission.asStateFlow()

    private val _selectedPlatform = MutableStateFlow("ALL")
    val selectedPlatform: StateFlow<String> = _selectedPlatform.asStateFlow()

    private val _availablePlatforms = MutableStateFlow<List<String>>(emptyList())
    val availablePlatforms: StateFlow<List<String>> = _availablePlatforms.asStateFlow()

    private val _avatarCache = MutableStateFlow<Map<String, String?>>(emptyMap())
    val avatarCache: StateFlow<Map<String, String?>> = _avatarCache.asStateFlow()

    private val _multiSelectMode = MutableStateFlow(false)
    val multiSelectMode: StateFlow<Boolean> = _multiSelectMode.asStateFlow()

    private val _selectedIds = MutableStateFlow<Set<String>>(emptySet())
    val selectedIds: StateFlow<Set<String>> = _selectedIds.asStateFlow()

    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences("nexus_sms_prefs", Context.MODE_PRIVATE)
    }

    init {
        conversationRepository.getAllConversations()
            .onEach {
                _conversationList.value = it
                _isLoading.value = false
            }
            .launchIn(viewModelScope)

        conversationRepository.getPinnedConversations()
            .onEach { _pinnedConversations.value = it }
            .launchIn(viewModelScope)

        conversationRepository.getBlockedConversations()
            .onEach { _blockedConversations.value = it }
            .launchIn(viewModelScope)

        conversationRepository.getArchivedConversations()
            .onEach { _archivedConversations.value = it }
            .launchIn(viewModelScope)

        conversationRepository.getActivePlatforms()
            .onEach { _availablePlatforms.value = it }
            .launchIn(viewModelScope)

        contactAvatarRepository.getAll()
            .onEach { avatars ->
                _avatarCache.value = avatars.associate { it.normalizedPhone to it.photoUri }
            }
            .launchIn(viewModelScope)
    }

    fun getAvatarUri(phoneNumber: String): String? {
        val normalized = phoneNumber.replace(Regex("[^+\\d]"), "")
        return _avatarCache.value[normalized]
    }

    fun selectConversation(conversation: Conversation) {
        _selectedConversation.value = conversation
    }

    fun deleteConversation(conversationId: String) {
        viewModelScope.launch {
            conversationRepository.deleteConversationById(conversationId)
        }
    }

    fun pinConversation(conversationId: String) {
        viewModelScope.launch {
            val conversation = conversationRepository.getConversationById(conversationId)
            if (conversation != null) {
                conversationRepository.updateConversation(conversation.copy(isPinned = true))
            }
        }
    }

    fun unpinConversation(conversationId: String) {
        viewModelScope.launch {
            val conversation = conversationRepository.getConversationById(conversationId)
            if (conversation != null) {
                conversationRepository.updateConversation(conversation.copy(isPinned = false))
            }
        }
    }

    fun muteConversation(conversationId: String) {
        viewModelScope.launch {
            val conversation = conversationRepository.getConversationById(conversationId)
            if (conversation != null) {
                conversationRepository.updateConversation(conversation.copy(isMuted = true))
            }
        }
    }

    fun unmuteConversation(conversationId: String) {
        viewModelScope.launch {
            val conversation = conversationRepository.getConversationById(conversationId)
            if (conversation != null) {
                conversationRepository.updateConversation(conversation.copy(isMuted = false))
            }
        }
    }

    fun blockConversation(conversationId: String) {
        viewModelScope.launch {
            val conversation = conversationRepository.getConversationById(conversationId)
            if (conversation != null) {
                conversationRepository.updateConversation(conversation.copy(isBlocked = true))
            }
        }
    }

    fun unblockConversation(conversationId: String) {
        viewModelScope.launch {
            val conversation = conversationRepository.getConversationById(conversationId)
            if (conversation != null) {
                conversationRepository.updateConversation(conversation.copy(isBlocked = false))
            }
        }
    }

    fun archiveConversation(conversationId: String) {
        viewModelScope.launch {
            val conversation = conversationRepository.getConversationById(conversationId)
            if (conversation != null) {
                conversationRepository.updateConversation(conversation.copy(isArchived = true))
            }
        }
    }

    fun unarchiveConversation(conversationId: String) {
        viewModelScope.launch {
            val conversation = conversationRepository.getConversationById(conversationId)
            if (conversation != null) {
                conversationRepository.updateConversation(conversation.copy(isArchived = false))
            }
        }
    }

    fun toggleMultiSelect() {
        _multiSelectMode.value = !_multiSelectMode.value
        if (!_multiSelectMode.value) {
            _selectedIds.value = emptySet()
        }
    }

    fun toggleSelection(conversationId: String) {
        _selectedIds.value = _selectedIds.value.toMutableSet().also {
            if (it.contains(conversationId)) it.remove(conversationId) else it.add(conversationId)
        }
        if (_selectedIds.value.isEmpty()) {
            _multiSelectMode.value = false
        }
    }

    fun selectAll() {
        _selectedIds.value = getFilteredConversations().map { it.id }.toSet()
    }

    fun deleteSelected() {
        viewModelScope.launch {
            _selectedIds.value.forEach { id ->
                conversationRepository.deleteConversationById(id)
            }
            _selectedIds.value = emptySet()
            _multiSelectMode.value = false
        }
    }

    fun blockSelected() {
        viewModelScope.launch {
            _selectedIds.value.forEach { id ->
                val conversation = conversationRepository.getConversationById(id)
                if (conversation != null) {
                    conversationRepository.updateConversation(conversation.copy(isBlocked = true))
                }
            }
            _selectedIds.value = emptySet()
            _multiSelectMode.value = false
        }
    }

    fun muteSelected() {
        viewModelScope.launch {
            _selectedIds.value.forEach { id ->
                val conversation = conversationRepository.getConversationById(id)
                if (conversation != null) {
                    conversationRepository.updateConversation(conversation.copy(isMuted = true))
                }
            }
            _selectedIds.value = emptySet()
            _multiSelectMode.value = false
        }
    }

    fun archiveSelected() {
        viewModelScope.launch {
            _selectedIds.value.forEach { id ->
                val conversation = conversationRepository.getConversationById(id)
                if (conversation != null) {
                    conversationRepository.updateConversation(conversation.copy(isArchived = true))
                }
            }
            _selectedIds.value = emptySet()
            _multiSelectMode.value = false
        }
    }

    fun markAsRead(conversationId: String) {
        viewModelScope.launch {
            conversationRepository.clearUnreadCount(conversationId)
        }
    }

    fun setPlatform(platform: String) {
        _selectedPlatform.value = platform
    }

    fun getFilteredConversations(): List<Conversation> {
        val platform = _selectedPlatform.value
        val all = _conversationList.value
        return if (platform == "ALL") all else all.filter { it.sourcePlatform == platform }
    }

    fun getFilteredPinnedConversations(): List<Conversation> {
        val platform = _selectedPlatform.value
        val pinned = _pinnedConversations.value
        return if (platform == "ALL") pinned else pinned.filter { it.sourcePlatform == platform }
    }

    fun checkAndAutoImport() {
        val importDone = prefs.getBoolean("sms_import_done", false)
        if (importDone) {
            Timber.d("SMS import already completed, skipping")
            return
        }
        Timber.d("First launch - triggering auto import")
        importSms()
    }

    fun importSms() {
        viewModelScope.launch {
            _isImporting.value = true
            try {
                val result = smsImporter.importAllSms()
                if (result.error != null) {
                    _importResult.value = result.error
                } else if (result.messagesImported > 0) {
                    prefs.edit().putBoolean("sms_import_done", true).apply()
                    smsImporter.importContactAvatars()
                    _importResult.value = "Imported ${result.messagesImported} messages from ${result.conversationsImported} conversations"
                } else {
                    smsImporter.importContactAvatars()
                    prefs.edit().putBoolean("sms_import_done", true).apply()
                    _importResult.value = "No SMS messages found to import"
                }
            } catch (e: Exception) {
                Timber.e(e, "Import failed")
                _importResult.value = "Import failed: ${e.message}"
            } finally {
                _isImporting.value = false
            }
        }
    }

    fun setNeedsPermission(needed: Boolean) {
        _needsPermission.value = needed
    }

    fun clearImportResult() {
        _importResult.value = null
    }

    fun syncMatrix() {
        viewModelScope.launch {
            _isImporting.value = true
            try {
                if (!matrixAuthService.isLoggedIn()) {
                    _importResult.value = "Matrix not connected"
                    return@launch
                }
                val result = matrixSyncService.incrementalSync()
                if (result.error != null) {
                    _importResult.value = result.error
                } else {
                    _importResult.value = "Matrix synced: ${result.messagesImported} new messages"
                }
            } catch (e: Exception) {
                Timber.e(e, "Matrix sync failed")
                _importResult.value = "Matrix sync failed: ${e.message}"
            } finally {
                _isImporting.value = false
            }
        }
    }

    fun resyncSms() {
        viewModelScope.launch {
            _isImporting.value = true
            try {
                val result = smsImporter.resyncFromDevice()
                if (result.error != null) {
                    _importResult.value = result.error
                } else {
                    _importResult.value = "Re-synced: removed ${result.messagesRemoved} messages deleted from device"
                }
            } catch (e: Exception) {
                Timber.e(e, "Re-sync failed")
                _importResult.value = "Re-sync failed: ${e.message}"
            } finally {
                _isImporting.value = false
            }
        }
    }
}
