package com.nexusmedia.nexussms.ui.screens

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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.nexusmedia.nexussms.features.messaging.ChannelPriority
import com.nexusmedia.nexussms.features.messaging.ChannelRoutingManager
import com.nexusmedia.nexussms.features.messaging.ContactRoutingConfig

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactRoutingScreen(
    contactId: String,
    contactName: String,
    routingManager: ChannelRoutingManager,
    onBack: () -> Unit
) {
    val existingConfig = remember { routingManager.getRoutingConfig(contactId) }
    var fallbackEnabled by remember { mutableStateOf(existingConfig?.fallbackEnabled ?: true) }
    var autoSelectBest by remember { mutableStateOf(existingConfig?.autoSelectBest ?: true) }
    val channels = remember {
        mutableStateListOf<ChannelPriority>().apply {
            addAll(existingConfig?.channels ?: listOf(
                ChannelPriority(platform = "MATRIX", priority = 1),
                ChannelPriority(platform = "TELEGRAM", priority = 2),
                ChannelPriority(platform = "DISCORD", priority = 3),
                ChannelPriority(platform = "MESSENGER", priority = 4),
                ChannelPriority(platform = "SMS", priority = 5)
            ))
        }
    }
    var saved by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Routing: $contactName") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            channels.clear()
                            channels.addAll(listOf(
                                ChannelPriority(platform = "MATRIX", priority = 1),
                                ChannelPriority(platform = "TELEGRAM", priority = 2),
                                ChannelPriority(platform = "DISCORD", priority = 3),
                                ChannelPriority(platform = "MESSENGER", priority = 4),
                                ChannelPriority(platform = "SMS", priority = 5)
                            ))
                        }
                    ) {
                        Text("Reset")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.padding(16.dp),
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
                                    text = contactName.take(1).uppercase(),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(contactName, style = MaterialTheme.typography.bodyLarge)
                            Text(
                                "Configure message routing preferences",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Enable Fallback")
                                Text(
                                    "Try next platform if sending fails",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = fallbackEnabled,
                                onCheckedChange = { fallbackEnabled = it }
                            )
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Auto-select Best Platform")
                                Text(
                                    "Automatically choose the most reliable platform",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = autoSelectBest,
                                onCheckedChange = { autoSelectBest = it }
                            )
                        }
                    }
                }
            }

            item {
                Column {
                    Text(
                        "Platform Priority Order",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        "Platforms are tried in order from top to bottom",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            itemsIndexed(channels) { index, channel ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                modifier = Modifier.size(24.dp),
                                shape = MaterialTheme.shapes.extraSmall,
                                color = MaterialTheme.colorScheme.primary
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "${index + 1}",
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Surface(
                                modifier = Modifier.size(24.dp),
                                shape = MaterialTheme.shapes.extraSmall,
                                color = channelPlatformColor(channel.platform)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = channel.platform.take(1),
                                        color = Color.White,
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Text(channel.platform)
                                Text(
                                    "Delay: ${channel.fallbackDelayMs / 1000}s",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = {
                                    if (index > 0) {
                                        val temp = channels[index]
                                        channels[index] = channels[index - 1]
                                        channels[index - 1] = temp
                                    }
                                },
                                enabled = index > 0
                            ) {
                                Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Move up")
                            }

                            IconButton(
                                onClick = {
                                    if (index < channels.size - 1) {
                                        val temp = channels[index]
                                        channels[index] = channels[index + 1]
                                        channels[index + 1] = temp
                                    }
                                },
                                enabled = index < channels.size - 1
                            ) {
                                Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Move down")
                            }

                            Switch(
                                checked = channel.enabled,
                                onCheckedChange = { enabled ->
                                    channels[index] = channel.copy(enabled = enabled)
                                }
                            )
                        }
                    }
                }
            }

            item {
                Button(
                    onClick = {
                        val config = ContactRoutingConfig(
                            contactId = contactId,
                            channels = channels.mapIndexed { index, channel ->
                                channel.copy(priority = index + 1)
                            },
                            fallbackEnabled = fallbackEnabled,
                            autoSelectBest = autoSelectBest
                        )
                        routingManager.saveRoutingConfig(config)
                        saved = true
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (saved) "Saved!" else "Save Routing Settings")
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}
