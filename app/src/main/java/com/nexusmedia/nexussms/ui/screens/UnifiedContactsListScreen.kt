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
import com.nexusmedia.nexussms.ui.viewmodels.UnifiedContactsListViewModel
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnifiedContactsListScreen(
    viewModel: UnifiedContactsListViewModel,
    onContactClick: (String) -> Unit,
    onBack: () -> Unit
) {
    val contacts by viewModel.contacts.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val showHidden by viewModel.showHidden.collectAsState()
    val favorites by viewModel.favorites.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Contacts") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.showHidden.value = !showHidden }) {
                        Icon(
                            if (showHidden) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = "Toggle hidden"
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
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.searchQuery.value = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Search contacts...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.searchQuery.value = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true
            )

            if (favorites.isNotEmpty() && searchQuery.isEmpty()) {
                Text(
                    "Favorites",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    color = MaterialTheme.colorScheme.primary
                )
                favorites.forEach { contact ->
                    UnifiedContactItem(
                        contact = contact,
                        onClick = { onContactClick(contact.id) },
                        onToggleFavorite = { viewModel.toggleFavorite(contact.id) }
                    )
                }
            }

            Text(
                if (showHidden) "All Contacts" else "Contacts",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                color = MaterialTheme.colorScheme.primary
            )

            LazyColumn(modifier = Modifier.weight(1f)) {
                items(contacts) { contact ->
                    UnifiedContactItem(
                        contact = contact,
                        onClick = { onContactClick(contact.id) },
                        onToggleFavorite = { viewModel.toggleFavorite(contact.id) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UnifiedContactItem(
    contact: UnifiedContact,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    val gson = remember { Gson() }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Card(
                modifier = Modifier.size(48.dp),
                shape = MaterialTheme.shapes.extraLarge
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = contact.displayName.take(1).uppercase(),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = contact.displayName,
                    style = MaterialTheme.typography.bodyLarge
                )

                val identities: List<PlatformIdentity> = gson.fromJson(
                    contact.platformIdentities,
                    object : TypeToken<List<PlatformIdentity>>() {}.type
                )
                if (identities.isNotEmpty()) {
                    Row {
                        identities.take(3).forEach { identity ->
                            Surface(
                                modifier = Modifier
                                    .size(16.dp)
                                    .padding(end = 2.dp),
                                shape = MaterialTheme.shapes.extraSmall,
                                color = platformColor(identity.platform)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = identity.platform.take(1),
                                        color = Color.White,
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }
                        }
                        if (identities.size > 3) {
                            Text(
                                "+${identities.size - 3}",
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }

                val phones: List<String> = gson.fromJson(
                    contact.phoneNumbers,
                    object : TypeToken<List<String>>() {}.type
                )
                if (phones.isNotEmpty()) {
                    Text(
                        text = phones.first(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            IconButton(onClick = onToggleFavorite) {
                Icon(
                    if (contact.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Favorite",
                    tint = if (contact.isFavorite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
