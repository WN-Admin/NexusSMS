package com.nexusmedia.nexussms.ui.viewmodels

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexusmedia.nexussms.data.models.SocialAccount
import com.nexusmedia.nexussms.data.repository.SocialAccountRepository
import com.nexusmedia.nexussms.features.matrix.MatrixAuthService
import com.nexusmedia.nexussms.features.matrix.MatrixSyncService
import com.nexusmedia.nexussms.features.telegram.TelegramService
import com.nexusmedia.nexussms.features.discord.DiscordService
import com.nexusmedia.nexussms.features.messenger.MessengerService
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

data class PlatformInfo(
    val id: String,
    val name: String,
    val packageNames: List<String>,
    val color: Long,
    val webLoginUrl: String,
    val supportsApi: Boolean = false
)

object SocialPlatforms {
    val all = listOf(
        PlatformInfo("DISCORD", "Discord", listOf("com.discord"), 0xFF5865F2, "https://discord.com/login", supportsApi = true),
        PlatformInfo("TELEGRAM", "Telegram", listOf("org.telegram.messenger", "org.telegram.messenger.web"), 0xFF0088CC, "https://web.telegram.org", supportsApi = true),
        PlatformInfo("FACEBOOK_MESSENGER", "Messenger", listOf("com.facebook.orca", "com.facebook.mlite"), 0xFF0084FF, "https://www.messenger.com/login"),
        PlatformInfo("VIBER", "Viber", listOf("com.viber.voip"), 0xFF7360F2, "https://www.viber.com"),
        PlatformInfo("SIGNAL", "Signal", listOf("org.thoughtcrime.securesms"), 0xFF3A76F0, "https://signal.org"),
        PlatformInfo("WHATSAPP", "WhatsApp", listOf("com.whatsapp", "com.whatsapp.w4b"), 0xFF25D366, "https://web.whatsapp.com"),
        PlatformInfo("MATRIX", "Matrix", listOf("im.vector.app", "im.vector.app.x"), 0xFF0DBD8B, "https://app.element.io", supportsApi = true),
        PlatformInfo("SLACK", "Slack", listOf("com.Slack"), 0xFF4A154B, "https://slack.com/signin")
    )
}

sealed class SocialAccountDialogState {
    object Hidden : SocialAccountDialogState()
    data class Delete(val account: SocialAccount) : SocialAccountDialogState()
    object MatrixLogin : SocialAccountDialogState()
    object TelegramLogin : SocialAccountDialogState()
    object DiscordLogin : SocialAccountDialogState()
    object MessengerLogin : SocialAccountDialogState()
}

data class MatrixLoginUiState(
    val homeserver: String = "https://matrix.org",
    val username: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)

data class TelegramLoginUiState(
    val botToken: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val botUsername: String? = null
)

data class DiscordLoginUiState(
    val botToken: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val botUsername: String? = null
)

data class MessengerLoginUiState(
    val pageToken: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val pageName: String? = null
)

@HiltViewModel
class SocialAccountsViewModel @Inject constructor(
    private val socialAccountRepository: SocialAccountRepository,
    private val matrixAuthService: MatrixAuthService,
    private val matrixSyncService: MatrixSyncService,
    private val telegramService: TelegramService,
    private val discordService: DiscordService,
    private val messengerService: MessengerService,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _accounts = MutableStateFlow<List<SocialAccount>>(emptyList())
    val accounts: StateFlow<List<SocialAccount>> = _accounts.asStateFlow()

    private val _installedApps = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val installedApps: StateFlow<Map<String, Boolean>> = _installedApps.asStateFlow()

    private val _dialogState = MutableStateFlow<SocialAccountDialogState>(SocialAccountDialogState.Hidden)
    val dialogState: StateFlow<SocialAccountDialogState> = _dialogState.asStateFlow()

    private val _matrixLoginState = MutableStateFlow(MatrixLoginUiState())
    val matrixLoginState: StateFlow<MatrixLoginUiState> = _matrixLoginState.asStateFlow()

    private val _matrixSyncStatus = MutableStateFlow<String?>(null)
    val matrixSyncStatus: StateFlow<String?> = _matrixSyncStatus.asStateFlow()

    private val _telegramLoginState = MutableStateFlow(TelegramLoginUiState())
    val telegramLoginState: StateFlow<TelegramLoginUiState> = _telegramLoginState.asStateFlow()

    private val _telegramSyncStatus = MutableStateFlow<String?>(null)
    val telegramSyncStatus: StateFlow<String?> = _telegramSyncStatus.asStateFlow()

    private val _discordLoginState = MutableStateFlow(DiscordLoginUiState())
    val discordLoginState: StateFlow<DiscordLoginUiState> = _discordLoginState.asStateFlow()

    private val _discordSyncStatus = MutableStateFlow<String?>(null)
    val discordSyncStatus: StateFlow<String?> = _discordSyncStatus.asStateFlow()

    private val _messengerLoginState = MutableStateFlow(MessengerLoginUiState())
    val messengerLoginState: StateFlow<MessengerLoginUiState> = _messengerLoginState.asStateFlow()

    private val _messengerSyncStatus = MutableStateFlow<String?>(null)
    val messengerSyncStatus: StateFlow<String?> = _messengerSyncStatus.asStateFlow()

    init {
        socialAccountRepository.getAllAccounts()
            .onEach { _accounts.value = it }
            .launchIn(viewModelScope)
        checkInstalledApps()
        restoreMatrixSession()
    }

    private fun restoreMatrixSession() {
        viewModelScope.launch {
            val restored = matrixAuthService.restoreSession()
            if (restored) {
                Timber.d("Matrix session restored, starting sync")
                syncMatrix()
            }
        }
    }

    fun checkInstalledApps() {
        val result = mutableMapOf<String, Boolean>()
        SocialPlatforms.all.forEach { platform ->
            result[platform.id] = platform.packageNames.any { isAppInstalled(it) }
        }
        _installedApps.value = result
    }

    private fun isAppInstalled(packageName: String): Boolean {
        return try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(packageName, android.content.pm.PackageManager.PackageInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(packageName, 0)
            }
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    fun connectPlatform(platformInfo: PlatformInfo) {
        if (platformInfo.id == "MATRIX") {
            if (matrixAuthService.isLoggedIn()) {
                syncMatrix()
            } else {
                _dialogState.value = SocialAccountDialogState.MatrixLogin
            }
            return
        }
        if (platformInfo.id == "TELEGRAM") {
            val existing = accounts.value.find { it.platform == "TELEGRAM" && it.isConnected }
            if (existing != null) {
                syncTelegram()
            } else {
                _dialogState.value = SocialAccountDialogState.TelegramLogin
            }
            return
        }
        if (platformInfo.id == "DISCORD") {
            val existing = accounts.value.find { it.platform == "DISCORD" && it.isConnected }
            if (existing != null) {
                syncDiscord()
            } else {
                _dialogState.value = SocialAccountDialogState.DiscordLogin
            }
            return
        }
        if (platformInfo.id == "FACEBOOK_MESSENGER") {
            val existing = accounts.value.find { it.platform == "FACEBOOK_MESSENGER" && it.isConnected }
            if (existing != null) {
                syncMessenger()
            } else {
                _dialogState.value = SocialAccountDialogState.MessengerLogin
            }
            return
        }

        val isInstalled = _installedApps.value[platformInfo.id] == true
        if (isInstalled) {
            val installedPackage = platformInfo.packageNames.firstOrNull { isAppInstalled(it) }
            if (installedPackage != null) {
                val launchIntent = context.packageManager.getLaunchIntentForPackage(installedPackage)
                if (launchIntent != null) {
                    context.startActivity(launchIntent)
                }
            }
        } else {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(platformInfo.webLoginUrl))
            browserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(browserIntent)
        }
        viewModelScope.launch {
            val existing = socialAccountRepository.getAccountByPlatform(platformInfo.id)
            if (existing == null) {
                socialAccountRepository.insertAccount(
                    SocialAccount(
                        platform = platformInfo.id,
                        userId = platformInfo.id.lowercase(),
                        username = platformInfo.name.lowercase(),
                        displayName = platformInfo.name,
                        isConnected = true,
                        accessToken = "token_${System.currentTimeMillis()}"
                    )
                )
            } else if (!existing.isConnected) {
                socialAccountRepository.updateAccount(
                    existing.copy(isConnected = true, updatedAt = System.currentTimeMillis())
                )
            }
        }
    }

    fun updateMatrixLoginHomeserver(value: String) {
        _matrixLoginState.value = _matrixLoginState.value.copy(homeserver = value, error = null)
    }

    fun updateMatrixLoginUsername(value: String) {
        _matrixLoginState.value = _matrixLoginState.value.copy(username = value, error = null)
    }

    fun updateMatrixLoginPassword(value: String) {
        _matrixLoginState.value = _matrixLoginState.value.copy(password = value, error = null)
    }

    fun submitMatrixLogin() {
        val state = _matrixLoginState.value
        if (state.homeserver.isBlank() || state.username.isBlank() || state.password.isBlank()) {
            _matrixLoginState.value = state.copy(error = "All fields are required")
            return
        }

        _matrixLoginState.value = state.copy(isLoading = true, error = null)

        viewModelScope.launch {
            val result = matrixAuthService.login(state.homeserver, state.username, state.password)
            if (result.success) {
                _matrixLoginState.value = MatrixLoginUiState()
                _dialogState.value = SocialAccountDialogState.Hidden
                Timber.d("Matrix login successful, starting sync")
                syncMatrix()
            } else {
                _matrixLoginState.value = _matrixLoginState.value.copy(
                    isLoading = false,
                    error = result.error ?: "Login failed"
                )
            }
        }
    }

    fun dismissMatrixLogin() {
        _matrixLoginState.value = MatrixLoginUiState()
        _dialogState.value = SocialAccountDialogState.Hidden
    }

    private fun syncMatrix() {
        viewModelScope.launch {
            _matrixSyncStatus.value = "Syncing Matrix messages..."
            try {
                val result = matrixSyncService.initialSync()
                _matrixSyncStatus.value = if (result.success) {
                    if (result.messagesImported > 0) {
                        "Imported ${result.messagesImported} messages from Matrix"
                    } else {
                        "Matrix synced — no new messages"
                    }
                } else {
                    "Matrix sync failed: ${result.error}"
                }
            } catch (e: Exception) {
                _matrixSyncStatus.value = "Matrix sync error: ${e.message}"
            }
        }
    }

    fun syncMatrixIncremental() {
        viewModelScope.launch {
            try {
                val result = matrixSyncService.incrementalSync()
                if (result.success && result.messagesImported > 0) {
                    _matrixSyncStatus.value = "New Matrix messages imported"
                }
            } catch (e: Exception) {
                Timber.w(e, "Matrix incremental sync failed")
            }
        }
    }

    fun clearSyncStatus() {
        _matrixSyncStatus.value = null
        _telegramSyncStatus.value = null
        _discordSyncStatus.value = null
        _messengerSyncStatus.value = null
    }

    // --- Telegram ---

    fun updateTelegramBotToken(value: String) {
        _telegramLoginState.value = _telegramLoginState.value.copy(botToken = value, error = null)
    }

    fun submitTelegramLogin() {
        val state = _telegramLoginState.value
        if (state.botToken.isBlank()) {
            _telegramLoginState.value = state.copy(error = "Bot token is required")
            return
        }
        _telegramLoginState.value = state.copy(isLoading = true, error = null)
        viewModelScope.launch {
            val result = telegramService.connectBot(state.botToken)
            if (result.success) {
                _telegramLoginState.value = TelegramLoginUiState()
                _dialogState.value = SocialAccountDialogState.Hidden
                Timber.d("Telegram bot connected: @%s", result.botUsername)
                syncTelegram()
            } else {
                _telegramLoginState.value = _telegramLoginState.value.copy(
                    isLoading = false,
                    error = result.error ?: "Connection failed"
                )
            }
        }
    }

    fun dismissTelegramLogin() {
        _telegramLoginState.value = TelegramLoginUiState()
        _dialogState.value = SocialAccountDialogState.Hidden
    }

    private fun syncTelegram() {
        viewModelScope.launch {
            _telegramSyncStatus.value = "Syncing Telegram messages..."
            try {
                val result = telegramService.sync()
                _telegramSyncStatus.value = if (result.success) {
                    if (result.messagesImported > 0) {
                        "Imported ${result.messagesImported} Telegram messages"
                    } else {
                        "Telegram synced — no new messages"
                    }
                } else {
                    "Telegram sync failed: ${result.error}"
                }
            } catch (e: Exception) {
                _telegramSyncStatus.value = "Telegram sync error: ${e.message}"
            }
        }
    }

    fun syncTelegramIncremental() {
        viewModelScope.launch {
            try {
                val result = telegramService.sync()
                if (result.success && result.messagesImported > 0) {
                    _telegramSyncStatus.value = "New Telegram messages imported"
                }
            } catch (e: Exception) {
                Timber.w(e, "Telegram incremental sync failed")
            }
        }
    }

    // --- Discord ---

    fun updateDiscordBotToken(value: String) {
        _discordLoginState.value = _discordLoginState.value.copy(botToken = value, error = null)
    }

    fun submitDiscordLogin() {
        val state = _discordLoginState.value
        if (state.botToken.isBlank()) {
            _discordLoginState.value = state.copy(error = "Bot token is required")
            return
        }
        _discordLoginState.value = state.copy(isLoading = true, error = null)
        viewModelScope.launch {
            val result = discordService.connectBot(state.botToken)
            if (result.success) {
                _discordLoginState.value = DiscordLoginUiState()
                _dialogState.value = SocialAccountDialogState.Hidden
                Timber.d("Discord bot connected: %s", result.username)
                syncDiscord()
            } else {
                _discordLoginState.value = _discordLoginState.value.copy(
                    isLoading = false,
                    error = result.error ?: "Connection failed"
                )
            }
        }
    }

    fun dismissDiscordLogin() {
        _discordLoginState.value = DiscordLoginUiState()
        _dialogState.value = SocialAccountDialogState.Hidden
    }

    private fun syncDiscord() {
        viewModelScope.launch {
            _discordSyncStatus.value = "Syncing Discord messages..."
            try {
                val result = discordService.sync()
                _discordSyncStatus.value = if (result.success) {
                    if (result.messagesImported > 0) {
                        "Imported ${result.messagesImported} Discord messages"
                    } else {
                        "Discord synced — no new messages"
                    }
                } else {
                    "Discord sync failed: ${result.error}"
                }
            } catch (e: Exception) {
                _discordSyncStatus.value = "Discord sync error: ${e.message}"
            }
        }
    }

    fun syncDiscordIncremental() {
        viewModelScope.launch {
            try {
                val result = discordService.sync()
                if (result.success && result.messagesImported > 0) {
                    _discordSyncStatus.value = "New Discord messages imported"
                }
            } catch (e: Exception) {
                Timber.w(e, "Discord incremental sync failed")
            }
        }
    }

    // --- Facebook Messenger ---

    fun updateMessengerPageToken(value: String) {
        _messengerLoginState.value = _messengerLoginState.value.copy(pageToken = value, error = null)
    }

    fun submitMessengerLogin() {
        val state = _messengerLoginState.value
        if (state.pageToken.isBlank()) {
            _messengerLoginState.value = state.copy(error = "Page access token is required")
            return
        }
        _messengerLoginState.value = state.copy(isLoading = true, error = null)
        viewModelScope.launch {
            val result = messengerService.connectPage(state.pageToken)
            if (result.success) {
                _messengerLoginState.value = MessengerLoginUiState()
                _dialogState.value = SocialAccountDialogState.Hidden
                Timber.d("Facebook page connected: %s", result.pageName)
                syncMessenger()
            } else {
                _messengerLoginState.value = _messengerLoginState.value.copy(
                    isLoading = false,
                    error = result.error ?: "Connection failed"
                )
            }
        }
    }

    fun dismissMessengerLogin() {
        _messengerLoginState.value = MessengerLoginUiState()
        _dialogState.value = SocialAccountDialogState.Hidden
    }

    private fun syncMessenger() {
        viewModelScope.launch {
            _messengerSyncStatus.value = "Syncing Messenger conversations..."
            try {
                val result = messengerService.sync()
                _messengerSyncStatus.value = if (result.success) {
                    if (result.messagesImported > 0) {
                        "Imported ${result.messagesImported} Messenger messages"
                    } else {
                        "Messenger synced — no new messages"
                    }
                } else {
                    "Messenger sync failed: ${result.error}"
                }
            } catch (e: Exception) {
                _messengerSyncStatus.value = "Messenger sync error: ${e.message}"
            }
        }
    }

    fun syncMessengerIncremental() {
        viewModelScope.launch {
            try {
                val result = messengerService.sync()
                if (result.success && result.messagesImported > 0) {
                    _messengerSyncStatus.value = "New Messenger messages imported"
                }
            } catch (e: Exception) {
                Timber.w(e, "Messenger incremental sync failed")
            }
        }
    }

    fun disconnectPlatform(platform: String) {
        viewModelScope.launch {
            if (platform == "MATRIX") {
                matrixAuthService.logout()
                socialAccountRepository.getAccountByPlatform("MATRIX")?.let { account ->
                    socialAccountRepository.updateAccount(
                        account.copy(
                            isConnected = false,
                            accessToken = "",
                            refreshToken = null,
                            updatedAt = System.currentTimeMillis()
                        )
                    )
                }
                return@launch
            }
            if (platform == "TELEGRAM") {
                telegramService.disconnect()
                return@launch
            }
            if (platform == "DISCORD") {
                discordService.disconnect()
                return@launch
            }
            if (platform == "FACEBOOK_MESSENGER") {
                messengerService.disconnect()
                return@launch
            }

            val account = socialAccountRepository.getAccountByPlatform(platform)
            if (account != null) {
                socialAccountRepository.updateAccount(
                    account.copy(
                        isConnected = false,
                        accessToken = "",
                        refreshToken = null,
                        updatedAt = System.currentTimeMillis()
                    )
                )
            }
        }
    }

    fun showDeleteDialog(account: SocialAccount) {
        _dialogState.value = SocialAccountDialogState.Delete(account)
    }

    fun hideDialog() {
        _dialogState.value = SocialAccountDialogState.Hidden
    }

    fun deleteAccount(account: SocialAccount) {
        viewModelScope.launch {
            when (account.platform) {
                "MATRIX" -> matrixAuthService.logout()
                "TELEGRAM" -> telegramService.disconnect()
                "DISCORD" -> discordService.disconnect()
                "FACEBOOK_MESSENGER" -> messengerService.disconnect()
            }
            socialAccountRepository.deleteAccount(account)
            hideDialog()
        }
    }
}
