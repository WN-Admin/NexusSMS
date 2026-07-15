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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.nexusmedia.nexussms.features.security.VaultManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VaultSetupScreen(
    vaultManager: VaultManager,
    onBack: () -> Unit,
    onSetupComplete: () -> Unit
) {
    var step by remember { mutableIntStateOf(1) }
    var pin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var decoyPin by remember { mutableStateOf("") }
    var enableDecoy by remember { mutableStateOf(false) }
    var showPin by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Setup Vault") },
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
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.Lock,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = when (step) {
                    1 -> "Create Vault PIN"
                    2 -> "Confirm PIN"
                    3 -> "Decoy PIN (Optional)"
                    else -> "Setup Complete"
                },
                style = MaterialTheme.typography.headlineMedium
            )

            Text(
                text = when (step) {
                    1 -> "Set a PIN to access your hidden conversations"
                    2 -> "Re-enter your PIN to confirm"
                    3 -> "Set a decoy PIN that shows an empty vault"
                    else -> "Your vault is ready"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(32.dp))

            when (step) {
                1 -> {
                    OutlinedTextField(
                        value = pin,
                        onValueChange = { pin = it.filter { c -> c.isDigit() } },
                        label = { Text("Enter PIN") },
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = if (showPin) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { showPin = !showPin }) {
                                Icon(
                                    if (showPin) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = "Toggle PIN visibility"
                                )
                            }
                        },
                        supportingText = {
                            Text("${pin.length}/6 digits")
                        },
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (pin.length >= 4) {
                                step = 2
                                error = null
                            } else {
                                error = "PIN must be at least 4 digits"
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = pin.length >= 4
                    ) {
                        Text("Next")
                    }
                }

                2 -> {
                    OutlinedTextField(
                        value = confirmPin,
                        onValueChange = { confirmPin = it.filter { c -> c.isDigit() } },
                        label = { Text("Confirm PIN") },
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = if (showPin) VisualTransformation.None else PasswordVisualTransformation(),
                        singleLine = true,
                        isError = error != null
                    )

                    if (error != null) {
                        Text(
                            text = error!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { step = 1; confirmPin = ""; error = null },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Back")
                        }

                        Button(
                            onClick = {
                                if (confirmPin == pin) {
                                    step = 3
                                    error = null
                                } else {
                                    error = "PINs don't match"
                                }
                            },
                            modifier = Modifier.weight(1f),
                            enabled = confirmPin.isNotEmpty()
                        ) {
                            Text("Next")
                        }
                    }
                }

                3 -> {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Enable Decoy Vault")
                                    Text(
                                        "Show an empty vault when wrong PIN is entered",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Switch(
                                    checked = enableDecoy,
                                    onCheckedChange = { enableDecoy = it }
                                )
                            }

                            if (enableDecoy) {
                                Spacer(modifier = Modifier.height(12.dp))

                                OutlinedTextField(
                                    value = decoyPin,
                                    onValueChange = { decoyPin = it.filter { c -> c.isDigit() } },
                                    label = { Text("Decoy PIN") },
                                    modifier = Modifier.fillMaxWidth(),
                                    visualTransformation = if (showPin) VisualTransformation.None else PasswordVisualTransformation(),
                                    singleLine = true,
                                    supportingText = {
                                        Text("Must be different from main PIN")
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { step = 2; error = null },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Back")
                        }

                        Button(
                            onClick = {
                                val success = vaultManager.setupVault(
                                    pin = pin,
                                    decoyPin = if (enableDecoy && decoyPin.isNotEmpty() && decoyPin != pin) decoyPin else null
                                )
                                if (success) {
                                    onSetupComplete()
                                } else {
                                    error = "Failed to setup vault"
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Complete Setup")
                        }
                    }
                }
            }

            if (error != null && step != 2) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = error!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
