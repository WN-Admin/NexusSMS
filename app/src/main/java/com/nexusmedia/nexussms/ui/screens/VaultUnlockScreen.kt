package com.nexusmedia.nexussms.ui.screens

import androidx.compose.foundation.layout.*
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
import com.nexusmedia.nexussms.features.security.VaultUnlockResult

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VaultUnlockScreen(
    vaultManager: VaultManager,
    onUnlock: () -> Unit,
    onBack: () -> Unit
) {
    var pin by remember { mutableStateOf("") }
    var showPin by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var attempts by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Unlock Vault") },
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
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                Icons.Default.Lock,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Enter Vault PIN",
                style = MaterialTheme.typography.headlineMedium
            )

            Text(
                text = "Access your hidden conversations",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = pin,
                onValueChange = { pin = it.filter { c -> c.isDigit() } },
                label = { Text("PIN") },
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
                singleLine = true,
                isError = error != null
            )

            if (error != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = error!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    val result = vaultManager.unlockVault(pin)
                    when (result) {
                        is VaultUnlockResult.SUCCESS -> onUnlock()
                        is VaultUnlockResult.DECOY -> onUnlock()
                        is VaultUnlockResult.INCORRECT_PIN -> {
                            attempts++
                            error = "Incorrect PIN. ${3 - attempts} attempts remaining."
                            pin = ""
                            if (attempts >= 3) {
                                onUnlock()
                            }
                        }
                        is VaultUnlockResult.ERROR -> {
                            error = result.message
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = pin.isNotEmpty()
            ) {
                Text("Unlock")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Hint: Your vault PIN is different from your app lock PIN",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
