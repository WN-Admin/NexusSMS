package com.nexusmedia.nexussms.ui.screens

import android.content.Context
import android.os.Build
import android.provider.Telephony
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.nexusmedia.nexussms.ui.viewmodels.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val themes by viewModel.themes.collectAsState()
    val signatures by viewModel.signatures.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val context = LocalContext.current
    val isDefaultSmsApp = remember { checkIsDefaultSmsApp(context) }
    var showDefaultSmsDialog by remember { mutableStateOf(false) }

    if (showDefaultSmsDialog) {
        AlertDialog(
            onDismissRequest = { showDefaultSmsDialog = false },
            title = { Text("Set as Default SMS App") },
            text = {
                Text("To send and receive SMS messages, NexusSMS needs to be set as your default SMS app. This allows it to handle incoming messages and send replies.")
            },
            confirmButton = {
                TextButton(onClick = {
                    val intent = android.content.Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT)
                    context.startActivity(intent)
                    showDefaultSmsDialog = false
                }) {
                    Text("Set Default")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDefaultSmsDialog = false }) {
                    Text("Later")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White,
                        modifier = Modifier.clickable { navController.popBackStack() }
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            item { SettingsSection(title = "Appearance") }

            item {
                DarkModeToggle(
                    isDarkMode = isDarkMode,
                    onToggle = { viewModel.toggleDarkMode(it) }
                )
            }

            item {
                Text(
                    text = "Themes",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            items(themes) { theme ->
                ThemeItem(
                    themeName = theme.name,
                    themeColor = theme.primaryColor,
                    isCustom = theme.isCustom,
                    onClick = { viewModel.setCurrentTheme(theme) }
                )
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                SettingsSection(title = "Messages & Notifications")
            }

            if (!isDefaultSmsApp) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.errorContainer)
                            .clickable { showDefaultSmsDialog = true }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Set as Default SMS App",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Text(
                                text = "Required to send and receive SMS messages",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
                item { HorizontalDivider() }
            }

            item {
                SettingsItem(title = "Signatures", subtitle = "${signatures.size} signatures saved") { navController.navigate("signatures") }
            }
            item { SettingsItem(title = "Scheduled Messages", subtitle = "Manage scheduled messages") { navController.navigate("scheduled") } }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                SettingsSection(title = "Shortcuts")
            }
            item { SettingsItem(title = "Quick Shortcuts", subtitle = "Manage shortcodes") { navController.navigate("shortcuts") } }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                SettingsSection(title = "Security & Privacy")
            }
            item { SettingsItem(title = "Security Settings", subtitle = "App lock, biometrics, privacy") { navController.navigate("security") } }
            item { SettingsItem(title = "Backup & Restore", subtitle = "Google Drive backup") { navController.navigate("backup") } }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                SettingsSection(title = "Integrations")
            }
            item { SettingsItem(title = "Social Media Accounts", subtitle = "Connect platforms") { navController.navigate("social") } }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                SettingsSection(title = "About")
            }
            item { SettingsItem(title = "Version", subtitle = "1.0.3") }
            item { SettingsItem(title = "Privacy Policy", subtitle = "") { navController.navigate("privacy_policy") } }
            item { SettingsItem(title = "Terms of Service", subtitle = "") { navController.navigate("terms_of_service") } }

            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }
}

@Composable
fun SettingsSection(title: String) {
    Text(
        text = title,
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(16.dp),
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
fun SettingsItem(
    title: String,
    subtitle: String = "",
    onClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        if (subtitle.isNotEmpty()) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
    HorizontalDivider()
}

@Composable
fun DarkModeToggle(isDarkMode: Boolean, onToggle: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isDarkMode) Icons.Default.DarkMode else Icons.Default.LightMode,
                contentDescription = "Dark Mode",
                modifier = Modifier.width(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = if (isDarkMode) "Dark Mode" else "Light Mode",
                style = MaterialTheme.typography.bodyMedium
            )
        }
        Switch(checked = isDarkMode, onCheckedChange = { onToggle(it) })
    }
    HorizontalDivider()
}

@Composable
fun ThemeItem(
    themeName: String,
    themeColor: String,
    isCustom: Boolean = false,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(
                    color = try {
                        Color(android.graphics.Color.parseColor(themeColor))
                    } catch (e: Exception) {
                        MaterialTheme.colorScheme.primary
                    }
                )
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(themeName, style = MaterialTheme.typography.bodyMedium)
            if (isCustom) {
                Text(
                    "Custom",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
    HorizontalDivider()
}

private fun checkIsDefaultSmsApp(context: Context): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        Telephony.Sms.getDefaultSmsPackage(context) == context.packageName
    } else {
        @Suppress("DEPRECATION")
        Telephony.Sms.getDefaultSmsPackage(context) == context.packageName
    }
}
