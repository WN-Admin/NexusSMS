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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.nexusmedia.nexussms.data.models.UnifiedContact
import com.nexusmedia.nexussms.data.models.PlatformIdentity
import com.nexusmedia.nexussms.ui.viewmodels.UnifiedContactViewModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

private fun platformColor(platform: String): Color = when (platform) {
    "MATRIX" -> Color(0xFF0DBD8B)
    "TELEGRAM" -> Color(0xFF0088CC)
    "DISCORD" -> Color(0xFF5865F2)
    "MESSENGER" -> Color(0xFF0084FF)
    "WHATSAPP" -> Color(0xFF25D366)
    "SIGNAL" -> Color(0xFF3A76F0)
    "SLACK" -> Color(0xFF4A154B)
    "VIBER" -> Color(0xFF7360F2)
    else -> Color(0xFF4CAF50)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnifiedContactScreen(
    contactId: String,
    viewModel: UnifiedContactViewModel,
    onBack: () -> Unit,
    onConversationClick: (String) -> Unit
) {
    val contact by viewModel.contact.collectAsState()
    val conversations by viewModel.linkedConversations.collectAsState()
    val isEditing by viewModel.isEditing.collectAsState()
    val editName by viewModel.editName.collectAsState()
    val showMergeDialog by viewModel.showMergeDialog.collectAsState()
    val allContacts by viewModel.allContacts.collectAsState()
    val gson = remember { Gson() }

    LaunchedEffect(contactId) {
        viewModel.loadContact(contactId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) "Edit Contact" else "Contact") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (isEditing) {
                        IconButton(onClick = { viewModel.saveEdits() }) {
                            Icon(Icons.Default.Check, contentDescription = "Save")
                        }
                        IconButton(onClick = { viewModel.cancelEdit() }) {
                            Icon(Icons.Default.Close, contentDescription = "Cancel")
                        }
                    } else {
                        IconButton(onClick = { viewModel.startEdit() }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit")
                        }
                        IconButton(onClick = { viewModel.toggleFavorite() }) {
                            Icon(
                                if (contact?.isFavorite == true) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Favorite",
                                tint = if (contact?.isFavorite == true) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                            )
                        }
                        var showMenu by remember { mutableStateOf(false) }
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "More")
                        }
                        DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                            DropdownMenuItem(
                                text = { Text("Merge with another contact") },
                                onClick = {
                                    showMenu = false
                                    viewModel.showMergeDialog.value = true
                                },
                                leadingIcon = { Icon(Icons.Default.Merge, contentDescription = null) }
                            )
                            DropdownMenuItem(
                                text = { Text("Hide from main list") },
                                onClick = {
                                    showMenu = false
                                    viewModel.setHidden(true)
                                },
                                leadingIcon = { Icon(Icons.Default.VisibilityOff, contentDescription = null) }
                            )
                            DropdownMenuItem(
                                text = { Text("Delete contact") },
                                onClick = {
                                    showMenu = false
                                    viewModel.deleteContact()
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        contact?.let { c ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Card(
                            modifier = Modifier.size(100.dp),
                            shape = MaterialTheme.shapes.extraLarge
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = c.displayName.take(1).uppercase(),
                                    style = MaterialTheme.typography.headlineLarge,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        if (isEditing) {
                            OutlinedTextField(
                                value = editName,
                                onValueChange = { viewModel.editName.value = it },
                                label = { Text("Display Name") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        } else {
                            Text(
                                text = c.displayName,
                                style = MaterialTheme.typography.headlineMedium
                            )
                        }

                        if (c.isFavorite) {
                            Icon(
                                Icons.Default.Favorite,
                                contentDescription = "Favorite",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }

                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Phone Numbers", style = MaterialTheme.typography.titleMedium)
                            Spacer(modifier = Modifier.height(8.dp))
                            val phones: List<String> = gson.fromJson(
                                c.phoneNumbers,
                                object : TypeToken<List<String>>() {}.type
                            )
                            if (phones.isEmpty()) {
                                Text(
                                    "No phone numbers",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            } else {
                                phones.forEach { phone ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(text = phone)
                                        Row {
                                            IconButton(onClick = { /* Call */ }) {
                                                Icon(Icons.Default.Phone, contentDescription = "Call")
                                            }
                                            IconButton(onClick = { /* SMS */ }) {
                                                Icon(Icons.Default.Chat, contentDescription = "Message")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Connected Platforms", style = MaterialTheme.typography.titleMedium)
                            Spacer(modifier = Modifier.height(8.dp))
                            val identities: List<PlatformIdentity> = gson.fromJson(
                                c.platformIdentities,
                                object : TypeToken<List<PlatformIdentity>>() {}.type
                            )
                            if (identities.isEmpty()) {
                                Text(
                                    "No platforms connected",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            } else {
                                identities.forEach { identity ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Surface(
                                                modifier = Modifier.size(32.dp),
                                                shape = MaterialTheme.shapes.small,
                                                color = platformColor(identity.platform)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Text(
                                                        text = identity.platform.take(1),
                                                        color = Color.White,
                                                        style = MaterialTheme.typography.labelMedium
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Column {
                                                Text(
                                                    text = identity.displayName
                                                        ?: identity.username
                                                        ?: identity.id
                                                )
                                                Text(
                                                    text = "${identity.platform} \u2022 ${identity.username ?: identity.id}",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                        IconButton(onClick = { /* Open in platform */ }) {
                                            Icon(Icons.Default.OpenInNew, contentDescription = "Open")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    Text(
                        "Unified Timeline",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                items(conversations) { conversation ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { onConversationClick(conversation.id) }
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                modifier = Modifier.size(24.dp),
                                shape = MaterialTheme.shapes.extraSmall,
                                color = platformColor(conversation.sourcePlatform)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = conversation.sourcePlatform.take(1),
                                        color = Color.White,
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
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
                                if (conversation.unreadCount > 0) {
                                    Badge { Text("${conversation.unreadCount}") }
                                }
                            }
                        }
                    }
                }

                if (c.notes != null || isEditing) {
                    item {
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Notes", style = MaterialTheme.typography.titleMedium)
                                Spacer(modifier = Modifier.height(8.dp))
                                if (isEditing) {
                                    OutlinedTextField(
                                        value = c.notes ?: "",
                                        onValueChange = { viewModel.updateNotes(it) },
                                        modifier = Modifier.fillMaxWidth(),
                                        minLines = 3
                                    )
                                } else {
                                    Text(
                                        text = c.notes ?: "No notes",
                                        color = if (c.notes == null) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        } ?: Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }

    if (showMergeDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.showMergeDialog.value = false },
            title = { Text("Merge Contact") },
            text = {
                Column {
                    Text("Select a contact to merge with ${contact?.displayName}:")
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyColumn {
                        items(allContacts.filter { it.id != contactId }) { other ->
                            ListItem(
                                headlineContent = { Text(other.displayName) },
                                leadingContent = {
                                    Icon(Icons.Default.Person, contentDescription = null)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { viewModel.showMergeDialog.value = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
