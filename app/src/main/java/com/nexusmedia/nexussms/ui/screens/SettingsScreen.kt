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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Shortcut
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.unit.sp
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
                SettingsItem(title = "Themes", subtitle = "Browse & create themes", icon = Icons.Default.Palette) { navController.navigate("themes") }
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
                SettingsItem(title = "Signatures", subtitle = "${signatures.size} signatures saved", icon = Icons.Default.EditNote) { navController.navigate("signatures") }
            }
            item { SettingsItem(title = "Scheduled Messages", subtitle = "Manage scheduled messages", icon = Icons.Default.Schedule) { navController.navigate("scheduled") } }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                SettingsSection(title = "Shortcuts")
            }
            item { SettingsItem(title = "Quick Shortcuts", subtitle = "Manage shortcodes", icon = Icons.Default.Shortcut) { navController.navigate("shortcuts") } }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                SettingsSection(title = "Security & Privacy")
            }
            item { SettingsItem(title = "Security Settings", subtitle = "App lock, biometrics, privacy", icon = Icons.Default.Security) { navController.navigate("security") } }
            item { SettingsItem(title = "Backup & Restore", subtitle = "Google Drive backup", icon = Icons.Default.Cloud) { navController.navigate("backup") } }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                SettingsSection(title = "Integrations")
            }
            item { SettingsItem(title = "Social Media Accounts", subtitle = "Connect platforms", icon = Icons.Default.People) { navController.navigate("social") } }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                SettingsSection(title = "About")
            }
            item { SettingsItem(title = "Version", subtitle = "1.0.3", icon = Icons.Default.Info) }
            item { SettingsItem(title = "Privacy Policy", icon = Icons.Default.Shield) { navController.navigate("privacy_policy") } }
            item { SettingsItem(title = "Terms of Service", icon = Icons.Default.Description) { navController.navigate("terms_of_service") } }

            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }
}

@Composable
fun SettingsSection(title: String) {
    Text(
        text = title.uppercase(),
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        letterSpacing = 1.5.sp
    )
}

@Composable
fun SettingsItem(
    title: String,
    subtitle: String = "",
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(16.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (subtitle.isNotEmpty()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
    HorizontalDivider(
        modifier = Modifier.padding(start = if (icon != null) 56.dp else 16.dp),
        color = MaterialTheme.colorScheme.outlineVariant
    )
}

@Composable
fun DarkModeToggle(isDarkMode: Boolean, onToggle: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle(!isDarkMode) }
            .padding(horizontal = 16.dp, vertical = 14.dp),
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
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "Dark Mode",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Switch(
            checked = isDarkMode,
            onCheckedChange = { onToggle(it) },
            colors = SwitchDefaults.colors(
                checkedTrackColor = MaterialTheme.colorScheme.primary,
                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        )
    }
    HorizontalDivider(
        modifier = Modifier.padding(start = 56.dp),
        color = MaterialTheme.colorScheme.outlineVariant
    )
}

private fun checkIsDefaultSmsApp(context: Context): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        Telephony.Sms.getDefaultSmsPackage(context) == context.packageName
    } else {
        @Suppress("DEPRECATION")
        Telephony.Sms.getDefaultSmsPackage(context) == context.packageName
    }
}
