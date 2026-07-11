package com.nexusmedia.nexussms.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.nexusmedia.nexussms.ui.viewmodels.SecuritySettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecuritySettingsScreen(
    navController: NavController,
    viewModel: SecuritySettingsViewModel = hiltViewModel()
) {
    val settings by viewModel.settings.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Security") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            SecuritySection(
                title = "App Lock"
            )
            SettingToggle(
                title = "App Lock",
                subtitle = "Require PIN/pattern to open app",
                checked = settings?.appLockEnabled == true,
                onCheckedChange = { viewModel.toggleAppLock(it) }
            )
            if (settings?.appLockEnabled == true) {
                LockTypeSelector(
                    currentType = settings?.appLockType ?: "PIN",
                    onTypeSelected = { viewModel.setLockType(it) }
                )
            }

            HorizontalDivider()
            Spacer(modifier = Modifier.height(8.dp))

            SecuritySection(
                title = "Biometric Authentication"
            )
            SettingToggle(
                title = "Require on Startup",
                checked = settings?.requireBiometricOnStartup == true,
                onCheckedChange = { viewModel.toggleBiometricOnStartup(it) }
            )
            SettingToggle(
                title = "Require to Read Messages",
                checked = settings?.requireBiometricForRead == true,
                onCheckedChange = { viewModel.toggleBiometricForRead(it) }
            )
            SettingToggle(
                title = "Require to Send Messages",
                checked = settings?.requireBiometricForSend == true,
                onCheckedChange = { viewModel.toggleBiometricForSend(it) }
            )
            SettingToggle(
                title = "Require to Delete Messages",
                checked = settings?.requireBiometricForDelete == true,
                onCheckedChange = { viewModel.toggleBiometricForDelete(it) }
            )
            SettingToggle(
                title = "Require to Forward Messages",
                checked = settings?.requireBiometricForForward == true,
                onCheckedChange = { viewModel.toggleBiometricForForward(it) }
            )

            HorizontalDivider()
            Spacer(modifier = Modifier.height(8.dp))

            SecuritySection(
                title = "Privacy"
            )
            SettingToggle(
                title = "Hide Messages in Recents",
                subtitle = "Prevent message content from appearing in the recent apps screen",
                checked = settings?.hideMessages == true,
                onCheckedChange = { viewModel.toggleHideMessages(it) }
            )
            SettingToggle(
                title = "Hide Notification Content",
                subtitle = "Hide message content in notifications",
                checked = settings?.hideNotificationContent == true,
                onCheckedChange = { viewModel.toggleHideNotificationContent(it) }
            )
            SettingToggle(
                title = "Disable Screenshots",
                subtitle = "Prevent screenshots within the app",
                checked = settings?.disableScreenshots == true,
                onCheckedChange = { viewModel.toggleDisableScreenshots(it) }
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SettingToggle(
    title: String,
    subtitle: String = "",
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
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
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun LockTypeSelector(
    currentType: String,
    onTypeSelected: (String) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    val lockTypes = listOf("PIN", "PATTERN", "PASSWORD")
    val typeLabels = mapOf(
        "PIN" to "PIN (4-6 digits)",
        "PATTERN" to "Pattern",
        "PASSWORD" to "Password"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { showDialog = true }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = "Lock Type",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = typeLabels[currentType] ?: currentType,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = "Change",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary
        )
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Select Lock Type") },
            text = {
                Column {
                    lockTypes.forEach { type ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onTypeSelected(type)
                                    showDialog = false
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = currentType == type,
                                onClick = {
                                    onTypeSelected(type)
                                    showDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.padding(start = 8.dp))
                            Text(
                                text = typeLabels[type] ?: type,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun SecuritySection(title: String) {
    Text(
        text = title,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, top = 16.dp, bottom = 8.dp),
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}
