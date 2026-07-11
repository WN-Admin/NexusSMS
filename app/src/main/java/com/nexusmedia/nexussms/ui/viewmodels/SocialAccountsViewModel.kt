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
    val packageName: String,
    val color: Long,
    val webLoginUrl: String
)

object SocialPlatforms {
    val all = listOf(
        PlatformInfo("DISCORD", "Discord", "com.discord", 0xFF5865F2, "https://discord.com/login"),
        PlatformInfo("TELEGRAM", "Telegram", "org.telegram.messenger", 0xFF0088CC, "https://web.telegram.org"),
        PlatformInfo("FACEBOOK_MESSENGER", "Messenger", "com.facebook.orca", 0xFF0084FF, "https://www.messenger.com/login"),
        PlatformInfo("VIBER", "Viber", "com.viber.voip", 0xFF7360F2, "https://www.viber.com"),
        PlatformInfo("MATRIX", "Matrix", "im.vector.app", 0xFF0DBD8B, "https://app.element.io")
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
            result[platform.id] = isAppInstalled(platform.packageName)
        }
        _installedApps.value = result
    }

    private fun isAppInstalled(packageName: String): Boolean {
        return try {
            context.packageManager.getPackageInfo(packageName, 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    fun connectPlatform(platformInfo: PlatformInfo) {
        val isInstalled = _installedApps.value[platformInfo.id] == true
        if (isInstalled) {
            val launchIntent = context.packageManager.getLaunchIntentForPackage(platformInfo.packageName)
            if (launchIntent != null) {
                context.startActivity(launchIntent)
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
