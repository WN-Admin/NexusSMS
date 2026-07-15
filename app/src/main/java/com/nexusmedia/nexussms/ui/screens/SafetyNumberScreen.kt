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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nexusmedia.nexussms.security.SafetyNumber
import com.nexusmedia.nexussms.security.SafetyNumberManager
import com.nexusmedia.nexussms.security.VerificationMethod

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SafetyNumberScreen(
    contactId: String,
    contactName: String,
    safetyNumberManager: SafetyNumberManager,
    onBack: () -> Unit
) {
    val safetyNumber by remember { mutableStateOf(safetyNumberManager.getSafetyNumber(contactId)) }
    var showQrScanner by remember { mutableStateOf(false) }
    var showSafetyNumberCompare by remember { mutableStateOf(false) }
    var verificationComplete by remember { mutableStateOf(false) }
    var isVerified by remember { mutableStateOf(safetyNumberManager.isVerified(contactId)) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Verify Encryption") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (isVerified) {
                        Icon(
                            Icons.Default.Verified,
                            contentDescription = "Verified",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(16.dp)
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
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (isVerified)
                        MaterialTheme.colorScheme.primaryContainer
                    else
                        MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        if (isVerified) Icons.Default.Verified else Icons.Default.Security,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = if (isVerified)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = if (isVerified) "Verified" else "Not Verified",
                        style = MaterialTheme.typography.headlineSmall,
                        color = if (isVerified)
                            MaterialTheme.colorScheme.onPrimaryContainer
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = if (isVerified)
                            "Your conversation with $contactName is end-to-end encrypted and verified"
                        else
                            "Verify your encryption with $contactName to ensure no one is intercepting your messages",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = if (isVerified)
                            MaterialTheme.colorScheme.onPrimaryContainer
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            safetyNumber?.let { sn ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Safety Number",
                            style = MaterialTheme.typography.titleMedium
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        val formattedNumber = sn.safetyNumber.chunked(5).joinToString(" ")

                        Text(
                            text = formattedNumber,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 20.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(8.dp)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            "Compare this number with $contactName. If they match, your conversation is secure.",
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "QR Code",
                            style = MaterialTheme.typography.titleMedium
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Surface(
                            modifier = Modifier.size(200.dp),
                            shape = MaterialTheme.shapes.medium,
                            color = Color.White
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        Icons.Default.QrCode,
                                        contentDescription = "QR Code",
                                        modifier = Modifier.size(150.dp),
                                        tint = Color.Black
                                    )
                                    Text(
                                        text = "Scan to verify",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.Black
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            "Scan this QR code with $contactName's device to verify your encryption.",
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { showQrScanner = true },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.QrCodeScanner, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Scan QR")
                    }

                    Button(
                        onClick = { showSafetyNumberCompare = true },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Compare, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Compare Number")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Key Fingerprints",
                            style = MaterialTheme.typography.titleMedium
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "Your Key",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = formatFingerprint(sn.myFingerprint),
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "Their Key",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.tertiary
                                )
                                Text(
                                    text = formatFingerprint(sn.theirFingerprint),
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            } ?: Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.ErrorOutline,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.error
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        "No Safety Number Available",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Text(
                        "Start an encrypted conversation with $contactName to generate a safety number.",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }

    if (showQrScanner) {
        AlertDialog(
            onDismissRequest = { showQrScanner = false },
            title = { Text("Scan QR Code") },
            text = {
                Column {
                    Text("Point your camera at $contactName's QR code to verify your encryption.")
                    Spacer(modifier = Modifier.height(16.dp))
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        shape = MaterialTheme.shapes.medium,
                        color = Color.Black
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                "Camera Preview",
                                color = Color.White
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    safetyNumber?.let { sn ->
                        safetyNumberManager.markAsVerified(contactId, VerificationMethod.QR_SCAN)
                        isVerified = true
                    }
                    showQrScanner = false
                }) {
                    Text("Simulate Scan")
                }
            },
            dismissButton = {
                TextButton(onClick = { showQrScanner = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showSafetyNumberCompare) {
        var theirNumber by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showSafetyNumberCompare = false },
            title = { Text("Compare Safety Number") },
            text = {
                Column {
                    Text("Ask $contactName to read their safety number, then enter it here:")
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = theirNumber,
                        onValueChange = { theirNumber = it.filter { c -> c.isDigit() } },
                        label = { Text("Their Safety Number") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (safetyNumberManager.verifySafetyNumber(contactId, theirNumber)) {
                            safetyNumberManager.markAsVerified(contactId, VerificationMethod.SAFETY_NUMBER_COMPARE)
                            isVerified = true
                        }
                        showSafetyNumberCompare = false
                    },
                    enabled = theirNumber.length == 30
                ) {
                    Text("Verify")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSafetyNumberCompare = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

private fun formatFingerprint(fingerprint: String): String {
    return fingerprint.chunked(4).joinToString(" ")
}
