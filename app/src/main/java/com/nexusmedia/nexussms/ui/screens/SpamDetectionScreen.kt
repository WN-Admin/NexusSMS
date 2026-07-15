package com.nexusmedia.nexussms.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import com.nexusmedia.nexussms.features.security.SpamCategory
import com.nexusmedia.nexussms.features.security.SpamDetector
import com.nexusmedia.nexussms.features.security.SpamBlocklistManager
import com.nexusmedia.nexussms.features.security.RiskLevel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpamDetectionScreen(
    spamDetector: SpamDetector,
    spamBlocklistManager: SpamBlocklistManager,
    onBack: () -> Unit
) {
    val stats by spamDetector.stats.collectAsState()
    val history by spamDetector.detectionHistory.collectAsState()
    val autoBlockEnabled by spamBlocklistManager.autoBlockEnabled.collectAsState(initial = true)
    val blockThreshold by spamBlocklistManager.blockThreshold.collectAsState(initial = 0.7f)
    val notificationEnabled by spamBlocklistManager.spamNotificationEnabled.collectAsState(initial = true)
    var blockedNumbers by remember { mutableStateOf(emptySet<String>()) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        blockedNumbers = spamBlocklistManager.getBlockedNumbers()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Spam Detection") },
                navigationIcon = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White,
                        modifier = Modifier.clickable { onBack() }
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White
                )
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
                        Text(
                            "Detection Statistics",
                            style = MaterialTheme.typography.titleMedium
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            StatItem(
                                label = "Scanned",
                                value = "${stats.totalScanned}",
                                icon = Icons.Default.Scanner
                            )
                            StatItem(
                                label = "Spam Detected",
                                value = "${stats.spamDetected}",
                                icon = Icons.Default.Warning,
                                isWarning = true
                            )
                            StatItem(
                                label = "Blocked",
                                value = "${blockedNumbers.size}",
                                icon = Icons.Default.Block
                            )
                        }
                    }
                }
            }

            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Settings",
                            style = MaterialTheme.typography.titleMedium
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Auto-block Spam")
                                Text(
                                    "Automatically block high-confidence spam senders",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = autoBlockEnabled,
                                onCheckedChange = { coroutineScope.launch { spamBlocklistManager.setAutoBlockEnabled(it) } }
                            )
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                        Text("Block Threshold: ${(blockThreshold * 100).toInt()}%")
                        Slider(
                            value = blockThreshold,
                            onValueChange = { coroutineScope.launch { spamBlocklistManager.setBlockThreshold(it) } },
                            valueRange = 0.5f..0.95f,
                            steps = 8
                        )

                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Spam Notifications")
                                Text(
                                    "Show notifications for detected spam",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = notificationEnabled,
                                onCheckedChange = { coroutineScope.launch { spamBlocklistManager.setSpamNotificationEnabled(it) } }
                            )
                        }
                    }
                }
            }

            item {
                Text(
                    "Detection Patterns",
                    style = MaterialTheme.typography.titleMedium
                )
            }

            val categories = SpamCategory.entries.filter { it != SpamCategory.UNKNOWN }
            items(categories) { category ->
                val patterns = spamDetector.getPatternsForCategory(category)
                if (patterns.isNotEmpty()) {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = formatCategoryName(category),
                                    style = MaterialTheme.typography.bodyLarge
                                )

                                Surface(
                                    shape = MaterialTheme.shapes.small,
                                    color = when (patterns.first().riskLevel) {
                                        RiskLevel.CRITICAL -> MaterialTheme.colorScheme.error
                                        RiskLevel.HIGH -> MaterialTheme.colorScheme.error
                                        RiskLevel.MEDIUM -> MaterialTheme.colorScheme.tertiary
                                        RiskLevel.LOW -> MaterialTheme.colorScheme.primary
                                    }
                                ) {
                                    Text(
                                        text = patterns.first().riskLevel.name,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = patterns.first().description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Text(
                                text = "${patterns.size} pattern(s) active",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            item {
                Text(
                    "Recent Detections",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            if (history.isEmpty()) {
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "No spam detected yet",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                "Incoming messages are being scanned for spam patterns",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(history.take(10)) { detection ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                if (detection.isSpam) Icons.Default.Warning else Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = if (detection.isSpam)
                                    MaterialTheme.colorScheme.error
                                else
                                    MaterialTheme.colorScheme.primary
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = detection.messagePreview,
                                    style = MaterialTheme.typography.bodyMedium,
                                    maxLines = 2
                                )
                                Text(
                                    text = "${detection.matchedPatterns.size} pattern(s) matched \u2022 ${(detection.confidence * 100).toInt()}% confidence",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Surface(
                                shape = MaterialTheme.shapes.small,
                                color = when (detection.riskLevel) {
                                    RiskLevel.CRITICAL -> MaterialTheme.colorScheme.error
                                    RiskLevel.HIGH -> MaterialTheme.colorScheme.error
                                    RiskLevel.MEDIUM -> MaterialTheme.colorScheme.tertiary
                                    RiskLevel.LOW -> MaterialTheme.colorScheme.primary
                                }
                            ) {
                                Text(
                                    text = detection.riskLevel.name,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    }
                }
            }

            item {
                Text(
                    "Blocked Numbers",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            if (blockedNumbers.isEmpty()) {
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "No blocked numbers",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            } else {
                items(blockedNumbers.toList()) { number ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(number)
                            IconButton(onClick = {
                                coroutineScope.launch {
                                    spamBlocklistManager.unblockNumber(number)
                                    blockedNumbers = spamBlocklistManager.getBlockedNumbers()
                                }
                            }) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Unblock",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }
}

@Composable
private fun StatItem(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isWarning: Boolean = false
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            icon,
            contentDescription = null,
            tint = if (isWarning) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun formatCategoryName(category: SpamCategory): String {
    return category.name.replace("_", " ").lowercase()
        .split(" ")
        .joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }
}
