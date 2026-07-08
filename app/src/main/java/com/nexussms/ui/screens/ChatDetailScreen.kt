package com.nexussms.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.EmojiEmotions
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nexussms.data.models.Message
import com.nexussms.ui.components.EmojiPicker
import com.nexussms.features.shortcodes.ShortcodeExpansionService
import com.nexussms.ui.viewmodels.ChatViewModel
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatDetailScreen(
    conversationId: String,
    viewModel: ChatViewModel = hiltViewModel()
) {
    val messages by viewModel.messages.collectAsState()
    val conversation by viewModel.currentConversation.collectAsState()
    val messageText by viewModel.messageText.collectAsState()
    val isSending by viewModel.isSending.collectAsState()
    val selectedMessageType by viewModel.selectedMessageType.collectAsState()
    val shortcutSuggestions by viewModel.shortcutSuggestions.collectAsState()

    val context = LocalContext.current
    var showEmojiPicker by remember { mutableStateOf(false) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var reactingMessageId by remember { mutableStateOf<String?>(null) }
    var showLocationPermissionDenied by remember { mutableStateOf(false) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
    }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.attachFile(it.toString()) }
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                ?: locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            if (location != null) {
                val mapsUrl = "https://maps.google.com/?q=${location.latitude},${location.longitude}"
                viewModel.sendLocation(conversationId, mapsUrl)
            } else {
                Toast.makeText(context, "Unable to get location. Try again.", Toast.LENGTH_SHORT).show()
            }
        } else {
            showLocationPermissionDenied = true
        }
    }

    LaunchedEffect(conversationId) {
        viewModel.loadConversation(conversationId)
    }

    val listState = rememberLazyListState()

    if (showEmojiPicker) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showEmojiPicker = false },
            title = { Text("Pick an emoji") },
            text = {
                EmojiPicker(
                    onEmojiSelected = { emoji ->
                        viewModel.updateMessageText(messageText + emoji)
                        showEmojiPicker = false
                    },
                    onDismiss = { showEmojiPicker = false }
                )
            },
            confirmButton = {}
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(conversation?.displayName ?: "Chat")
                        Text(
                            text = conversation?.participantPhoneNumbers ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                state = listState,
                reverseLayout = true
            ) {
                items(messages) { message ->
                    MessageBubble(
                        message = message,
                        onLongClick = { reactingMessageId = message.id },
                        reactingMessageId = reactingMessageId,
                        onReact = { messageId, emoji ->
                            viewModel.addReaction(messageId, emoji)
                            reactingMessageId = null
                        }
                    )
                }
            }

            // Message input
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MessageTypeButton(
                        label = "SMS",
                        isSelected = selectedMessageType == "SMS",
                        onClick = { viewModel.setMessageType("SMS") }
                    )
                    MessageTypeButton(
                        label = "RCS",
                        isSelected = selectedMessageType == "RCS",
                        onClick = { viewModel.setMessageType("RCS") }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(onClick = { imagePickerLauncher.launch("image/*") }) {
                        Icon(Icons.Default.Image, contentDescription = "Image")
                    }
                    IconButton(onClick = { filePickerLauncher.launch("*/*") }) {
                        Icon(Icons.Default.AttachFile, contentDescription = "Attachment")
                    }
                    IconButton(onClick = { showEmojiPicker = true }) {
                        Icon(Icons.Default.EmojiEmotions, contentDescription = "Emoji")
                    }

                    var showScheduleDialog by remember { mutableStateOf(false) }

                    if (showScheduleDialog) {
                        var scheduleTime by remember { mutableStateOf(System.currentTimeMillis() + 3600000L) }
                        androidx.compose.material3.AlertDialog(
                            onDismissRequest = { showScheduleDialog = false },
                            title = { Text("Schedule Message") },
                            text = {
                                Column {
                                    Text("Send this message later.")
                                    Spacer(modifier = Modifier.height(8.dp))
                                    androidx.compose.material3.OutlinedTextField(
                                        value = scheduleTime.toString(),
                                        onValueChange = {
                                            it.toLongOrNull()?.let { v -> scheduleTime = v }
                                        },
                                        label = { Text("Time (epoch ms)") },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            },
                            confirmButton = {
                                androidx.compose.material3.TextButton(
                                    onClick = {
                                        viewModel.scheduleMessage(
                                            conversationId = conversationId,
                                            recipientPhone = conversation?.participantPhoneNumbers ?: "",
                                            scheduleAt = scheduleTime
                                        )
                                        showScheduleDialog = false
                                    }
                                ) { Text("Schedule") }
                            },
                            dismissButton = {
                                androidx.compose.material3.TextButton(
                                    onClick = { showScheduleDialog = false }
                                ) { Text("Cancel") }
                            }
                        )
                    }

                    if (showLocationPermissionDenied) {
                        androidx.compose.material3.AlertDialog(
                            onDismissRequest = { showLocationPermissionDenied = false },
                            title = { Text("Location Permission Needed") },
                            text = { Text("Please grant location permission to share your location.") },
                            confirmButton = {
                                androidx.compose.material3.TextButton(onClick = { showLocationPermissionDenied = false }) {
                                    Text("OK")
                                }
                            }
                        )
                    }

                    TextField(
                        value = messageText,
                        onValueChange = { viewModel.updateMessageText(it) },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Message...") },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(
                            onSend = {
                                conversation?.let { conv ->
                                    viewModel.sendMessage(conversationId, conv.participantPhoneNumbers)
                                }
                            }
                        ),
                        singleLine = true
                    )

                    IconButton(
                        onClick = { showScheduleDialog = true },
                        enabled = messageText.isNotEmpty()
                    ) {
                        Icon(Icons.Default.Schedule, contentDescription = "Schedule")
                    }

                    IconButton(
                        onClick = {
                            if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                                val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
                                val location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                                    ?: locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                                if (location != null) {
                                    val mapsUrl = "https://maps.google.com/?q=${location.latitude},${location.longitude}"
                                    viewModel.sendLocation(conversationId, mapsUrl)
                                } else {
                                    Toast.makeText(context, "Unable to get location. Try again.", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                            }
                        }
                    ) {
                        Icon(Icons.Default.Place, contentDescription = "Location")
                    }

                    IconButton(
                        onClick = {
                            conversation?.let { conv ->
                                viewModel.sendMessage(conversationId, conv.participantPhoneNumbers)
                            }
                        },
                        enabled = !isSending && messageText.isNotEmpty()
                    ) {
                        Icon(Icons.Default.Send, contentDescription = "Send")
                    }
                }

                if (shortcutSuggestions.isNotEmpty()) {
                    ShortcutSuggestionsBar(
                        suggestions = shortcutSuggestions,
                        onInsert = { trigger, expansion ->
                            viewModel.applyShortcutSuggestion(trigger, expansion)
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MessageBubble(
    message: Message,
    onLongClick: () -> Unit = {},
    reactingMessageId: String? = null,
    onReact: (String, String) -> Unit = { _, _ -> }
) {
    val isIncoming = message.senderPhoneNumber != "self"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = if (isIncoming) Arrangement.Start else Arrangement.End
    ) {
        Column(
            horizontalAlignment = if (isIncoming) Alignment.Start else Alignment.End
        ) {
            Box(
                modifier = Modifier
                    .widthIn(max = 280.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        color = if (isIncoming) {
                            MaterialTheme.colorScheme.surfaceVariant
                        } else {
                            MaterialTheme.colorScheme.primary
                        }
                    )
                    .combinedClickable(
                        onClick = {},
                        onLongClick = onLongClick
                    )
                    .padding(12.dp)
            ) {
                Column {
                    Text(
                        text = message.content,
                        color = if (isIncoming) {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        } else {
                            Color.White
                        },
                        style = MaterialTheme.typography.bodyMedium
                    )
                    if (message.reactions.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = message.reactions,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(message.timestamp),
                        color = if (isIncoming) {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        } else {
                            Color.White.copy(alpha = 0.7f)
                        },
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }

            if (reactingMessageId == message.id) {
                ReactionBar { emoji ->
                    onReact(message.id, emoji)
                }
            }
        }
    }
}

@Composable
private fun ReactionBar(onReact: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp, bottom = 4.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        val emojis = listOf("\uD83D\uDC4D", "\u2764\uFE0F", "\uD83D\uDE02", "\uD83D\uDE2E", "\uD83D\uDE22", "\uD83D\uDE4F")
        emojis.forEach { emoji ->
            Text(
                text = emoji,
                modifier = Modifier
                    .clickable { onReact(emoji) }
                    .padding(4.dp),
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

@Composable
private fun ShortcutSuggestionsBar(
    suggestions: List<ShortcodeExpansionService.ShortcutPreview>,
    onInsert: (String, String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp)
    ) {
        suggestions.forEach { preview ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp)
                    .clickable { onInsert(preview.trigger, preview.expansion) },
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = preview.trigger,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = preview.expansion,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = "Insert",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun MessageTypeButton(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(
                color = if (isSelected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.surfaceVariant
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium
        )
    }
}
