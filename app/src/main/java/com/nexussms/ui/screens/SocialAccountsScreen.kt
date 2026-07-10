package com.nexussms.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LinkOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
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

sealed class SocialAccountDialogState {
    object Hidden : SocialAccountDialogState()
    data class Delete(val account: SocialAccount) : SocialAccountDialogState()
    object Connect : SocialAccountDialogState()
}

@HiltViewModel
class SocialAccountsViewModel @Inject constructor(
    private val socialAccountRepository: SocialAccountRepository
) : ViewModel() {

    private val _accounts = MutableStateFlow<List<SocialAccount>>(emptyList())
    val accounts: StateFlow<List<SocialAccount>> = _accounts.asStateFlow()

    private val _dialogState = MutableStateFlow<SocialAccountDialogState>(SocialAccountDialogState.Hidden)
    val dialogState: StateFlow<SocialAccountDialogState> = _dialogState.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        socialAccountRepository.getAllAccounts()
            .onEach {
                _accounts.value = it
                _isLoading.value = false
            }
            .launchIn(viewModelScope)
    }

    fun showDeleteDialog(account: SocialAccount) {
        _dialogState.value = SocialAccountDialogState.Delete(account)
    }

    fun hideDialog() {
        _dialogState.value = SocialAccountDialogState.Hidden
    }

    fun showConnectDialog() {
        _dialogState.value = SocialAccountDialogState.Connect
    }

    fun connectAccount(platform: String, username: String, displayName: String) {
        viewModelScope.launch {
            socialAccountRepository.insertAccount(
                SocialAccount(
                    platform = platform,
                    userId = username,
                    username = username,
                    displayName = displayName,
                    isConnected = true,
                    accessToken = "mock_token_${System.currentTimeMillis()}"
                )
            )
            hideDialog()
        }
    }

    fun toggleConnection(account: SocialAccount) {
        viewModelScope.launch {
            socialAccountRepository.updateAccount(
                account.copy(
                    isConnected = !account.isConnected,
                    updatedAt = System.currentTimeMillis()
                )
            )
        }
    }

    fun deleteAccount(account: SocialAccount) {
        viewModelScope.launch {
            socialAccountRepository.deleteAccount(account)
            hideDialog()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SocialAccountsScreen(
    viewModel: SocialAccountsViewModel = hiltViewModel()
) {
    val accounts by viewModel.accounts.collectAsState()
    val dialogState by viewModel.dialogState.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Social Accounts") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.showConnectDialog() }) {
                Icon(Icons.Default.Add, contentDescription = "Connect Account")
            }
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("Loading accounts...", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else if (accounts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.LinkOff,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "No connected accounts",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                item {
                    Text(
                        text = "Connected Accounts",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                items(accounts, key = { it.id }) { account ->
                    SocialAccountItem(
                        account = account,
                        onToggleConnection = { viewModel.toggleConnection(account) },
                        onDelete = { viewModel.showDeleteDialog(account) }
                    )
                }
                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }
    }

    when (val state = dialogState) {
        is SocialAccountDialogState.Delete -> AlertDialog(
            onDismissRequest = { viewModel.hideDialog() },
            title = { Text("Remove Account") },
            text = {
                Text("Are you sure you want to remove ${state.account.displayName} from ${state.account.platform}?")
            },
            confirmButton = {
                TextButton(onClick = { viewModel.deleteAccount(state.account) }) {
                    Text("Remove", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.hideDialog() }) {
                    Text("Cancel")
                }
            }
        )
        is SocialAccountDialogState.Connect -> ConnectAccountDialog(
            onConnect = { platform, username, displayName ->
                viewModel.connectAccount(platform, username, displayName)
            },
            onDismiss = { viewModel.hideDialog() }
        )
        SocialAccountDialogState.Hidden -> { /* no dialog */ }
    }
}

@Composable
private fun SocialAccountItem(
    account: SocialAccount,
    onToggleConnection: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (account.isConnected) Icons.Default.Link else Icons.Default.LinkOff,
                    contentDescription = null,
                    tint = if (account.isConnected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(32.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = account.displayName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = account.platform,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "@${account.username}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (account.isConnected && account.lastSyncTime != null) {
                    Text(
                        text = "Last synced: ${java.text.SimpleDateFormat("MMM dd, HH:mm", java.util.Locale.getDefault()).format(java.util.Date(account.lastSyncTime))}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Switch(
                checked = account.isConnected,
                onCheckedChange = { onToggleConnection() }
            )
            Spacer(modifier = Modifier.width(4.dp))
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Remove",
                    tint = MaterialTheme.colorScheme.error
                )
            }
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ConnectAccountDialog(
    onConnect: (platform: String, username: String, displayName: String) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedPlatform by remember { mutableStateOf("DISCORD") }
    var username by remember { mutableStateOf("") }
    var displayName by remember { mutableStateOf("") }
    var usernameError by remember { mutableStateOf(false) }

    val platforms = listOf("DISCORD", "TELEGRAM", "FACEBOOK_MESSENGER", "VIBER", "MATRIX")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Connect Account") },
        text = {
            Column {
                Text(
                    "Select platform and enter your account details:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))

                Text("Platform", style = MaterialTheme.typography.labelMedium)
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    platforms.forEach { platform ->
                        FilterChip(
                            selected = selectedPlatform == platform,
                            onClick = { selectedPlatform = platform },
                            label = {
                                Text(
                                    text = platform.replace("_", " "),
                                    style = MaterialTheme.typography.labelSmall
                                )
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = username,
                    onValueChange = {
                        username = it
                        usernameError = false
                    },
                    label = { Text("Username") },
                    isError = usernameError,
                    supportingText = if (usernameError) {
                        { Text("Username cannot be empty") }
                    } else null,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = displayName,
                    onValueChange = { displayName = it },
                    label = { Text("Display Name (optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Text(
                        text = "Note: Real platform integration requires API keys and app registration. This creates a local account entry for framework readiness.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    usernameError = username.isBlank()
                    if (!usernameError) {
                        onConnect(selectedPlatform, username.trim(), displayName.trim().ifEmpty { username.trim() })
                    }
                }
            ) {
                Text("Connect")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    androidx.compose.material3.FilterChip(
        selected = selected,
        onClick = onClick,
        label = label,
        modifier = modifier
    )
}
