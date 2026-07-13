package com.nexusmedia.nexussms.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LinkOff
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.nexusmedia.nexussms.data.models.SocialAccount
import com.nexusmedia.nexussms.ui.viewmodels.PlatformInfo
import com.nexusmedia.nexussms.ui.viewmodels.SocialPlatforms
import com.nexusmedia.nexussms.ui.viewmodels.SocialAccountsViewModel
import com.nexusmedia.nexussms.ui.viewmodels.SocialAccountDialogState
import com.nexusmedia.nexussms.ui.viewmodels.MatrixLoginUiState
import com.nexusmedia.nexussms.ui.viewmodels.TelegramLoginUiState
import com.nexusmedia.nexussms.ui.viewmodels.DiscordLoginUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SocialAccountsScreen(
    navController: NavController,
    viewModel: SocialAccountsViewModel = hiltViewModel()
) {
    val accounts by viewModel.accounts.collectAsState()
    val installedApps by viewModel.installedApps.collectAsState()
    val dialogState by viewModel.dialogState.collectAsState()
    val matrixLoginState by viewModel.matrixLoginState.collectAsState()
    val matrixSyncStatus by viewModel.matrixSyncStatus.collectAsState()
    val telegramLoginState by viewModel.telegramLoginState.collectAsState()
    val telegramSyncStatus by viewModel.telegramSyncStatus.collectAsState()
    val discordLoginState by viewModel.discordLoginState.collectAsState()
    val discordSyncStatus by viewModel.discordSyncStatus.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    val connectedPlatforms = accounts.filter { it.isConnected }.map { it.platform }

    LaunchedEffect(matrixSyncStatus) {
        matrixSyncStatus?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSyncStatus()
        }
    }

    LaunchedEffect(telegramSyncStatus) {
        telegramSyncStatus?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSyncStatus()
        }
    }

    LaunchedEffect(discordSyncStatus) {
        discordSyncStatus?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSyncStatus()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Connected Accounts") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            items(SocialPlatforms.all) { platform ->
                val isInstalled = installedApps[platform.id] == true
                val isConnected = connectedPlatforms.contains(platform.id)
                val account = accounts.find { it.platform == platform.id }

                PlatformCard(
                    platform = platform,
                    isInstalled = isInstalled,
                    isConnected = isConnected,
                    account = account,
                    onConnect = { viewModel.connectPlatform(platform) },
                    onDisconnect = { viewModel.disconnectPlatform(platform.id) },
                    onDelete = { account?.let { viewModel.showDeleteDialog(it) } },
                    onSync = when (platform.id) {
                        "MATRIX" -> if (isConnected) {{ viewModel.syncMatrixIncremental() }} else null
                        "TELEGRAM" -> if (isConnected) {{ viewModel.syncTelegramIncremental() }} else null
                        "DISCORD" -> if (isConnected) {{ viewModel.syncDiscordIncremental() }} else null
                        else -> null
                    }
                )
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Apps detected on this device:",
                    modifier = Modifier.padding(horizontal = 16.dp),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            val detectedPlatforms = SocialPlatforms.all.filter { installedApps[it.id] == true }
            if (detectedPlatforms.isEmpty()) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.PhoneAndroid,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "No supported apps detected",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(detectedPlatforms) { platform ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(Color(platform.color)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = platform.name.first().toString(),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = platform.name,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "installed",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF4CAF50)
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Connect via web login or API:",
                    modifier = Modifier.padding(horizontal = 16.dp),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            val apiPlatforms = SocialPlatforms.all.filter { it.supportsApi }
            items(apiPlatforms) { platform ->
                val isConnected = connectedPlatforms.contains(platform.id)
                val account = accounts.find { it.platform == platform.id }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Cloud,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(Color(platform.color)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = platform.name.first().toString(),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = platform.name,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = if (isConnected) "Connected — Tap to sync" else "Tap to log in with API",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isConnected) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (isConnected) {
                        IconButton(onClick = { viewModel.syncMatrixIncremental() }) {
                            Icon(
                                Icons.Default.Refresh,
                                contentDescription = "Sync",
                                tint = Color(0xFF4CAF50)
                            )
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(32.dp)) }
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
        SocialAccountDialogState.MatrixLogin -> MatrixLoginDialog(
            state = matrixLoginState,
            onHomeserverChange = viewModel::updateMatrixLoginHomeserver,
            onUsernameChange = viewModel::updateMatrixLoginUsername,
            onPasswordChange = viewModel::updateMatrixLoginPassword,
            onLogin = viewModel::submitMatrixLogin,
            onDismiss = viewModel::dismissMatrixLogin
        )
        SocialAccountDialogState.TelegramLogin -> TelegramLoginDialog(
            state = telegramLoginState,
            onTokenChange = viewModel::updateTelegramBotToken,
            onLogin = viewModel::submitTelegramLogin,
            onDismiss = viewModel::dismissTelegramLogin
        )
        SocialAccountDialogState.DiscordLogin -> DiscordLoginDialog(
            state = discordLoginState,
            onTokenChange = viewModel::updateDiscordBotToken,
            onLogin = viewModel::submitDiscordLogin,
            onDismiss = viewModel::dismissDiscordLogin
        )
        SocialAccountDialogState.Hidden -> {}
    }
}

@Composable
private fun MatrixLoginDialog(
    state: MatrixLoginUiState,
    onHomeserverChange: (String) -> Unit,
    onUsernameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onLogin: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF0DBD8B)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("M", color = Color.White, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text("Matrix Login")
            }
        },
        text = {
            Column {
                Text(
                    text = "Connect to your Matrix homeserver to sync messages.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = state.homeserver,
                    onValueChange = onHomeserverChange,
                    label = { Text("Homeserver URL") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !state.isLoading
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = state.username,
                    onValueChange = onUsernameChange,
                    label = { Text("Username") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !state.isLoading,
                    placeholder = { Text("@user:matrix.org") }
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = state.password,
                    onValueChange = onPasswordChange,
                    label = { Text("Password") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !state.isLoading,
                    visualTransformation = PasswordVisualTransformation()
                )
                if (state.error != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = state.error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onLogin,
                enabled = !state.isLoading && state.username.isNotBlank() && state.password.isNotBlank()
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                }
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

@Composable
private fun TelegramLoginDialog(
    state: TelegramLoginUiState,
    onTokenChange: (String) -> Unit,
    onLogin: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF0088CC)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("T", color = Color.White, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text("Telegram Bot")
            }
        },
        text = {
            Column {
                Text(
                    text = "Enter your bot token from @BotFather to connect.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = state.botToken,
                    onValueChange = onTokenChange,
                    label = { Text("Bot Token") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !state.isLoading,
                    placeholder = { Text("123456789:ABCdefGhIJKlmnOPQrSTUvwxYZ") }
                )
                if (state.botUsername != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Connected as @$state.botUsername",
                        color = Color(0xFF4CAF50),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                if (state.error != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = state.error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onLogin,
                enabled = !state.isLoading && state.botToken.isNotBlank()
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                }
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

@Composable
private fun DiscordLoginDialog(
    state: DiscordLoginUiState,
    onTokenChange: (String) -> Unit,
    onLogin: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF5865F2)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("D", color = Color.White, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text("Discord Bot")
            }
        },
        text = {
            Column {
                Text(
                    text = "Enter your bot token from the Discord Developer Portal.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = state.botToken,
                    onValueChange = onTokenChange,
                    label = { Text("Bot Token") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !state.isLoading,
                    placeholder = { Text("MTIzNDU2Nzg5...") }
                )
                if (state.botUsername != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Connected as ${state.botUsername}",
                        color = Color(0xFF4CAF50),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                if (state.error != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = state.error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onLogin,
                enabled = !state.isLoading && state.botToken.isNotBlank()
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                }
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

@Composable
private fun PlatformCard(
    platform: PlatformInfo,
    isInstalled: Boolean,
    isConnected: Boolean,
    account: SocialAccount?,
    onConnect: () -> Unit,
    onDisconnect: () -> Unit,
    onDelete: () -> Unit,
    onSync: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isConnected)
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            else
                MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                    .clip(CircleShape)
                    .background(Color(platform.color)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = platform.name.first().toString(),
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = platform.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isConnected) {
                        Icon(
                            Icons.Default.Link,
                            contentDescription = null,
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Connected",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF4CAF50)
                        )
                    } else {
                        Icon(
                            Icons.Default.LinkOff,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Not connected",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (isInstalled) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF4CAF50))
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "App detected",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF4CAF50)
                        )
                    }
                }
            }

            if (isConnected) {
                if (onSync != null) {
                    IconButton(onClick = onSync) {
                        Icon(
                            Icons.Default.Sync,
                            contentDescription = "Sync",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                TextButton(onClick = onDisconnect) {
                    Text("Disconnect")
                }
            } else {
                TextButton(onClick = onConnect) {
                    Text("Connect")
                }
            }
        }
    }
}
