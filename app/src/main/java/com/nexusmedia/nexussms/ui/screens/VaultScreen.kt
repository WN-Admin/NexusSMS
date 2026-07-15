package com.nexusmedia.nexussms.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nexusmedia.nexussms.features.security.HiddenConversation
import com.nexusmedia.nexussms.features.security.VaultManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VaultScreen(
    vaultManager: VaultManager,
    onBack: () -> Unit,
    onConversationClick: (String) -> Unit
) {
    val hiddenConversations by vaultManager.hiddenConversations.collectAsState()
    var showUnhideDialog by remember { mutableStateOf<HiddenConversation?>(null) }
    var showClearDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Vault") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (hiddenConversations.isNotEmpty()) {
                        IconButton(onClick = { showClearDialog = true }) {
                            Icon(
                                Icons.Default.DeleteSweep,
                                contentDescription = "Clear all",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        if (hiddenConversations.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Default.LockOpen,
                    contentDescription = null,
                    modifier = Modifier.size(80.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Vault is Empty",
                    style = MaterialTheme.typography.headlineMedium
                )

                Text(
                    text = "Hide conversations from the main list by long-pressing and selecting 'Hide in Vault'",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    Text(
                        text = "${hiddenConversations.size} hidden conversation(s)",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                items(hiddenConversations) { conversation ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { onConversationClick(conversation.originalConversationId) }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Card(
                                modifier = Modifier.size(48.dp),
                                shape = MaterialTheme.shapes.extraLarge
                            ) {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.Lock,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = conversation.displayName,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Text(
                                    text = conversation.lastMessage.take(50),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = formatTimestamp(conversation.lastMessageTime),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                IconButton(
                                    onClick = { showUnhideDialog = conversation }
                                ) {
                                    Icon(
                                        Icons.Default.VisibilityOff,
                                        contentDescription = "Unhide",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    showUnhideDialog?.let { conversation ->
        AlertDialog(
            onDismissRequest = { showUnhideDialog = null },
            title = { Text("Unhide Conversation") },
            text = {
                Text("Move '${conversation.displayName}' back to the main list?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        vaultManager.unhideConversation(conversation.id)
                        showUnhideDialog = null
                    }
                ) {
                    Text("Unhide")
                }
            },
            dismissButton = {
                TextButton(onClick = { showUnhideDialog = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("Clear All Hidden Conversations") },
            text = {
                Text("This will unhide all conversations. This action cannot be undone.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        vaultManager.deleteAllHiddenConversations()
                        showClearDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Clear All")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    return when {
        diff < 60_000 -> "now"
        diff < 3_600_000 -> "${diff / 60_000}m"
        diff < 86_400_000 -> "${diff / 3_600_000}h"
        diff < 604_800_000 -> "${diff / 86_400_000}d"
        else -> {
            val sdf = java.text.SimpleDateFormat("MMM d", java.util.Locale.getDefault())
            sdf.format(java.util.Date(timestamp))
        }
    }
}
