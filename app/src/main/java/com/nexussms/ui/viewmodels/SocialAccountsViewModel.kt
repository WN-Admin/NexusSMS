package com.nexussms.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexussms.data.models.SocialAccount
import com.nexussms.data.repository.SocialAccountRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SocialAccountsViewModel @Inject constructor(
    private val socialAccountRepository: SocialAccountRepository
) : ViewModel() {

    private val _accounts = MutableStateFlow<List<SocialAccount>>(emptyList())
    val accounts: StateFlow<List<SocialAccount>> = _accounts.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        socialAccountRepository.getAllAccounts()
            .onEach { _accounts.value = it }
            .launchIn(viewModelScope)
    }

    fun load() {
        socialAccountRepository.getAllAccounts()
            .onEach { _accounts.value = it }
            .launchIn(viewModelScope)
    }

    fun connect(
        platform: String,
        userId: String,
        username: String,
        displayName: String,
        accessToken: String
    ) {
        viewModelScope.launch {
            socialAccountRepository.insertAccount(
                SocialAccount(
                    platform = platform,
                    userId = userId,
                    username = username,
                    displayName = displayName,
                    accessToken = accessToken,
                    isConnected = true
                )
            )
        }
    }

    fun disconnect(account: SocialAccount) {
        viewModelScope.launch {
            socialAccountRepository.updateAccount(account.copy(isConnected = false))
        }
    }

    fun delete(account: SocialAccount) {
        viewModelScope.launch {
            socialAccountRepository.deleteAccount(account)
        }
    }
}
