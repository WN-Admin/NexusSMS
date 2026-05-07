package com.nexussms.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun LockScreenOverlay(
    isBiometricAvailable: Boolean = false,
    onUnlocked: () -> Unit,
    onBiometricRequest: (() -> Unit)? = null,
    onPinSubmit: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var pin by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf<String?>(null) }
    var showPinEntry by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = "App Locked",
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "App Locked",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Unlock to access your messages",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            if (!showPinEntry && isBiometricAvailable) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(36.dp)
                        )
                        .clickable { onBiometricRequest?.invoke() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Fingerprint,
                        contentDescription = "Biometric unlock",
                        modifier = Modifier.size(40.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                TextButton(onClick = { showPinEntry = true }) {
                    Text("Use PIN instead")
                }
            }

            if (showPinEntry || !isBiometricAvailable) {
                OutlinedTextField(
                    value = pin,
                    onValueChange = { input ->
                        val filtered = input.filter { it.isDigit() }.take(6)
                        pin = filtered
                        pinError = null
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("PIN") },
                    placeholder = { Text("Enter your PIN") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    singleLine = true,
                    isError = pinError != null,
                    supportingText = pinError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(
                    onClick = {
                        if (pin.length >= 4) {
                            onPinSubmit?.invoke(pin) ?: onUnlocked()
                        } else {
                            pinError = "PIN must be at least 4 digits"
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(12.dp)
                        )
                ) {
                    Text(
                        text = "Unlock",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (isBiometricAvailable) {
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(onClick = { showPinEntry = false }) {
                        Text("Use biometric")
                    }
                }
            }
        }
    }
}
