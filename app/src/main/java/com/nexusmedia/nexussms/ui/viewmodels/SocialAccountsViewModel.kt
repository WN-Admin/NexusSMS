package com.nexusmedia.nexussms.ui.viewmodels

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexusmedia.nexussms.data.models.SocialAccount
import com.nexusmedia.nexussms.data.repository.SocialAccountRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PlatformInfo(
    val id: String,
    val name: String,
    val packageNames: List<String>,
    val color: Long,
    val webLoginUrl: String
)

object SocialPlatforms {
    val all = listOf(
        PlatformInfo("DISCORD", "Discord", listOf("com.discord"), 0xFF5865F2, "https://discord.com/login"),
        PlatformInfo("TELEGRAM", "Telegram", listOf("org.telegram.messenger", "org.telegram.messenger.web"), 0xFF0088CC, "https://web.telegram.org"),
        PlatformInfo("FACEBOOK_MESSENGER", "Messenger", listOf("com.facebook.orca", "com.facebook.mlite"), 0xFF0084FF, "https://www.messenger.com/login"),
        PlatformInfo("VIBER", "Viber", listOf("com.viber.voip"), 0xFF7360F2, "https://www.viber.com"),
        PlatformInfo("SIGNAL", "Signal", listOf("org.thoughtcrime.securesms"), 0xFF3A76F0, "https://signal.org"),
        PlatformInfo("WHATSAPP", "WhatsApp", listOf("com.whatsapp", "com.whatsapp.w4b"), 0xFF25D366, "https://web.whatsapp.com"),
        PlatformInfo("MATRIX", "Matrix", listOf("im.vector.app", "im.vector.app.x"), 0xFF0DBD8B, "https://app.element.io"),
        PlatformInfo("SLACK", "Slack", listOf("com.Slack"), 0xFF4A154B, "https://slack.com/signin")
    )
}

sealed class SocialAccountDialogState {
    object Hidden : SocialAccountDialogState()
    data class Delete(val account: SocialAccount) : SocialAccountDialogState()
}

@HiltViewModel
class SocialAccountsViewModel @Inject constructor(
    private val socialAccountRepository: SocialAccountRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _accounts = MutableStateFlow<List<SocialAccount>>(emptyList())
    val accounts: StateFlow<List<SocialAccount>> = _accounts.asStateFlow()

    private val _installedApps = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val installedApps: StateFlow<Map<String, Boolean>> = _installedApps.asStateFlow()

    private val _dialogState = MutableStateFlow<SocialAccountDialogState>(SocialAccountDialogState.Hidden)
    val dialogState: StateFlow<SocialAccountDialogState> = _dialogState.asStateFlow()

    init {
        socialAccountRepository.getAllAccounts()
            .onEach { _accounts.value = it }
            .launchIn(viewModelScope)
        checkInstalledApps()
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

    fun disconnectPlatform(platform: String) {
        viewModelScope.launch {
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
            socialAccountRepository.deleteAccount(account)
            hideDialog()
        }
    }
}
