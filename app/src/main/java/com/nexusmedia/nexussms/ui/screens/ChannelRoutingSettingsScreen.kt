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
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.nexusmedia.nexussms.features.messaging.ChannelRoutingManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChannelRoutingSettingsScreen(
    routingManager: ChannelRoutingManager,
    onBack: () -> Unit
) {
    val globalFallbackEnabled by routingManager.globalFallbackEnabled.collectAsState(initial = true)
    val defaultDelay by routingManager.defaultFallbackDelay.collectAsState(initial = 2000L)
    val routingStats by routingManager.channelRouter.channelStats.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Channel Routing") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Global Settings", style = MaterialTheme.typography.titleMedium)

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Enable Fallback")
                                Text(
                                    "Automatically retry on another platform if the first fails",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = globalFallbackEnabled,
                                onCheckedChange = { routingManager.setGlobalFallbackEnabled(it) }
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text("Default Fallback Delay: ${defaultDelay / 1000}s")
                        Slider(
                            value = defaultDelay.toFloat(),
                            onValueChange = { routingManager.setDefaultFallbackDelay(it.toLong()) },
                            valueRange = 500f..5000f,
                            steps = 8
                        )
                    }
                }
            }

            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Channel Priority", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "Channels are tried in order from highest to lowest priority.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        val platforms = listOf(
                            "MATRIX" to "Matrix",
                            "TELEGRAM" to "Telegram",
                            "DISCORD" to "Discord",
                            "MESSENGER" to "Messenger",
                            "SMS" to "SMS (Last Resort)"
                        )

                        platforms.forEachIndexed { index, (platform, name) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.DragHandle,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Surface(
                                        modifier = Modifier.size(24.dp),
                                        shape = MaterialTheme.shapes.extraSmall,
                                        color = channelPlatformColor(platform)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = platform.take(1),
                                                color = Color.White,
                                                style = MaterialTheme.typography.labelSmall
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(name)
                                }
                                Text(
                                    "Priority ${index + 1}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            item {
                Text(
                    "Channel Statistics",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            items(routingStats.entries.toList()) { (platform, stats) ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(platform, style = MaterialTheme.typography.bodyLarge)
                            Text(
                                "${stats.totalAttempts} attempts, ${stats.successfulAttempts} successful",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Surface(
                            shape = MaterialTheme.shapes.small,
                            color = when {
                                stats.successRate >= 0.8 -> MaterialTheme.colorScheme.primary
                                stats.successRate >= 0.5 -> MaterialTheme.colorScheme.tertiary
                                else -> MaterialTheme.colorScheme.error
                            }
                        ) {
                            Text(
                                text = "${(stats.successRate * 100).toInt()}%",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                }
            }

            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("How Routing Works", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))

                        val rules = listOf(
                            "Messages are sent via the highest-priority available platform first",
                            "If sending fails, the system waits the fallback delay then tries the next platform",
                            "SMS is always used as the last resort",
                            "Per-contact overrides can be set in the contact's routing settings",
                            "Statistics track success rates to optimize future routing"
                        )

                        rules.forEach { rule ->
                            Row(
                                modifier = Modifier.padding(vertical = 4.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier
                                        .size(16.dp)
                                        .padding(top = 2.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    rule,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

internal fun channelPlatformColor(platform: String): Color {
    return when (platform) {
        "MATRIX" -> Color(0xFF0DBD8B)
        "TELEGRAM" -> Color(0xFF0088CC)
        "DISCORD" -> Color(0xFF5865F2)
        "MESSENGER" -> Color(0xFF0084FF)
        else -> Color(0xFF6750A4)
    }
}
