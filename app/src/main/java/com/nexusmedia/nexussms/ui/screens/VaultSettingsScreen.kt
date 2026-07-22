package com.nexusmedia.nexussms.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nexusmedia.nexussms.features.security.VaultManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VaultSettingsScreen(
    vaultManager: VaultManager,
    onBack: () -> Unit
) {
    val isVaultEnabled = remember { mutableStateOf(false) }
    val hideFromRecents = remember { mutableStateOf(false) }
    val autoLockTimeout = remember { mutableStateOf(0L) }

    LaunchedEffect(Unit) {
        isVaultEnabled.value = vaultManager.isVaultEnabled()
        hideFromRecents.value = vaultManager.shouldHideFromRecents()
        autoLockTimeout.value = vaultManager.getAutoLockTimeout()
    }
    var showChangePinDialog by remember { mutableStateOf(false) }
    var showDisableDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Vault Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (isVaultEnabled.value)
                        MaterialTheme.colorScheme.primaryContainer
                    else
                        MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        if (isVaultEnabled.value) Icons.Default.Lock else Icons.Default.LockOpen,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = if (isVaultEnabled.value)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Text(
                            text = if (isVaultEnabled.value) "Vault Enabled" else "Vault Disabled",
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Text(
                            text = "Hidden conversations are ${if (isVaultEnabled.value) "protected" else "not protected"}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isVaultEnabled.value)
                                MaterialTheme.colorScheme.onPrimaryContainer
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Settings", style = MaterialTheme.typography.titleMedium)

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Hide from Recents")
                            Text(
                                "Hide vault from recent apps",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = hideFromRecents.value,
                            onCheckedChange = {
                                hideFromRecents.value = it
                                vaultManager.setHideFromRecents(it)
                            }
                        )
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    Text("Auto-lock Timeout: ${formatTimeout(autoLockTimeout.value)}")
                    Slider(
                        value = autoLockTimeout.value.toFloat(),
                        onValueChange = {
                            autoLockTimeout.value = it.toLong()
                            vaultManager.updateAutoLockTimeout(it.toLong())
                        },
                        valueRange = 60000f..600000f,
                        steps = 8
                    )
                }
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Actions", style = MaterialTheme.typography.titleMedium)

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = { showChangePinDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Key, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Change Vault PIN")
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (isVaultEnabled.value) {
                        Button(
                            onClick = { showDisableDialog = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Icon(Icons.Default.LockOpen, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Disable Vault")
                        }
                    }
                }
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "About Vault",
                            style = MaterialTheme.typography.titleSmall
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        "The vault provides a separate encrypted space for sensitive conversations. Hidden conversations are stored locally and never appear in the main conversation list.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        "You can set a decoy PIN that shows an empty vault when entered, providing plausible deniability.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }

    if (showChangePinDialog) {
        var oldPin by remember { mutableStateOf("") }
        var newPin by remember { mutableStateOf("") }
        var confirmNewPin by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showChangePinDialog = false },
            title = { Text("Change Vault PIN") },
            text = {
                Column {
                    OutlinedTextField(
                        value = oldPin,
                        onValueChange = { oldPin = it },
                        label = { Text("Current PIN") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newPin,
                        onValueChange = { newPin = it },
                        label = { Text("New PIN") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = confirmNewPin,
                        onValueChange = { confirmNewPin = it },
                        label = { Text("Confirm New PIN") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newPin == confirmNewPin && newPin.isNotEmpty()) {
                            vaultManager.changePin(oldPin, newPin)
                            showChangePinDialog = false
                        }
                    },
                    enabled = oldPin.isNotEmpty() && newPin.isNotEmpty() && newPin == confirmNewPin
                ) {
                    Text("Change PIN")
                }
            },
            dismissButton = {
                TextButton(onClick = { showChangePinDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showDisableDialog) {
        var pin by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showDisableDialog = false },
            title = { Text("Disable Vault") },
            text = {
                Column {
                    Text("Enter your vault PIN to disable it. All hidden conversations will be moved back to the main list.")
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = pin,
                        onValueChange = { pin = it },
                        label = { Text("Vault PIN") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (vaultManager.disableVault(pin)) {
                            isVaultEnabled.value = false
                            showDisableDialog = false
                        }
                    },
                    enabled = pin.isNotEmpty(),
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Disable")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDisableDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

private fun formatTimeout(timeout: Long): String {
    return when {
        timeout < 60000 -> "${timeout / 1000}s"
        timeout < 3600000 -> "${timeout / 60000}m"
        else -> "${timeout / 3600000}h"
    }
}
