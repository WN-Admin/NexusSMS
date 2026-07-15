package com.nexusmedia.nexussms.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Reply
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nexusmedia.nexussms.features.automation.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AutomationScreen(
    ruleEngine: RuleEngine,
    onBack: () -> Unit
) {
    var rules by remember { mutableStateOf(emptyList<MessageRule>()) }
    var showCreateDialog by remember { mutableStateOf(false) }
    val stats by ruleEngine.stats.collectAsState()
    val logs by ruleEngine.executionLogs.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Automation Rules") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showCreateDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Rule")
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
                        Text("Statistics", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("${rules.size}", style = MaterialTheme.typography.headlineMedium)
                                Text("Rules", style = MaterialTheme.typography.labelSmall)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("${stats.totalExecutions}", style = MaterialTheme.typography.headlineMedium)
                                Text("Executions", style = MaterialTheme.typography.labelSmall)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("${(stats.successRate * 100).toInt()}%", style = MaterialTheme.typography.headlineMedium)
                                Text("Success", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            }

            item {
                Text("Quick Templates", style = MaterialTheme.typography.titleMedium)
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AssistChip(
                        onClick = {
                            val rule = ruleEngine.createOtpForwardRule("clipboard")
                            rules = rules + rule
                        },
                        label = { Text("Copy OTP") },
                        leadingIcon = { Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp)) }
                    )
                    AssistChip(
                        onClick = { showCreateDialog = true },
                        label = { Text("Auto-Reply") },
                        leadingIcon = { Icon(Icons.Default.Reply, contentDescription = null, modifier = Modifier.size(18.dp)) }
                    )
                }
            }

            item {
                Text("Your Rules", style = MaterialTheme.typography.titleMedium)
            }

            if (rules.isEmpty()) {
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.AutoAwesome,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No automation rules yet")
                            Text(
                                "Create rules to automate message handling",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(rules) { rule ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(rule.name, style = MaterialTheme.typography.bodyLarge)
                                    if (!rule.isEnabled) {
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Surface(
                                            shape = MaterialTheme.shapes.extraSmall,
                                            color = MaterialTheme.colorScheme.surfaceVariant
                                        ) {
                                            Text(
                                                "Disabled",
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                style = MaterialTheme.typography.labelSmall
                                            )
                                        }
                                    }
                                }
                                if (rule.description.isNotEmpty()) {
                                    Text(
                                        rule.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Text(
                                    "${rule.actions.size} action(s) \u2022 Priority ${rule.priority}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Switch(
                                checked = rule.isEnabled,
                                onCheckedChange = { enabled ->
                                    rules = rules.map {
                                        if (it.id == rule.id) it.copy(isEnabled = enabled) else it
                                    }
                                }
                            )
                        }
                    }
                }
            }

            item {
                Text("Recent Executions", style = MaterialTheme.typography.titleMedium)
            }

            items(logs.take(10)) { log ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            if (log.success) Icons.Default.CheckCircle else Icons.Default.Error,
                            contentDescription = null,
                            tint = if (log.success) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(log.ruleName, style = MaterialTheme.typography.bodyMedium)
                            Text(
                                log.messagePreview,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1
                            )
                            Text(
                                "${log.actionsExecuted.size} action(s) executed",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        var ruleName by remember { mutableStateOf("") }
        var senderPattern by remember { mutableStateOf("") }
        var contentPattern by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("Create Rule") },
            text = {
                Column {
                    OutlinedTextField(
                        value = ruleName,
                        onValueChange = { ruleName = it },
                        label = { Text("Rule Name") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = senderPattern,
                        onValueChange = { senderPattern = it },
                        label = { Text("Sender Pattern (regex)") },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("e.g., \\+1555.*") }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = contentPattern,
                        onValueChange = { contentPattern = it },
                        label = { Text("Content Pattern (regex)") },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("e.g., (?i)otp.*\\d{4,8}") }
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (ruleName.isNotEmpty()) {
                            val rule = ruleEngine.createRule(
                                name = ruleName,
                                senderPattern = senderPattern.ifEmpty { null },
                                contentPattern = contentPattern.ifEmpty { null },
                                actions = listOf(
                                    RuleAction(type = ActionType.COPY_TO_CLIPBOARD)
                                )
                            )
                            rules = rules + rule
                            showCreateDialog = false
                        }
                    }
                ) {
                    Text("Create")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
