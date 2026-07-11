package com.nexusmedia.nexussms.ui.screens

import android.Manifest
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.EmojiEmotions
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.nexusmedia.nexussms.data.models.Message
import com.nexusmedia.nexussms.ui.components.EmojiPicker
import com.nexusmedia.nexussms.features.shortcodes.ShortcodeExpansionService
import com.nexusmedia.nexussms.ui.viewmodels.ChatViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
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
    var showStickerPicker by remember { mutableStateOf(false) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var reactingMessageId by remember { mutableStateOf<String?>(null) }
    var showLocationPermissionDenied by remember { mutableStateOf(false) }
    var showAttachments by remember { mutableStateOf(false) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            viewModel.attachImage(it.toString())
        }
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
        AlertDialog(
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

    if (showStickerPicker) {
        StickerPicker(
            onStickerSelected = { stickerId ->
                conversation?.let { conv ->
                    viewModel.sendMessageWithSticker(conversationId, conv.participantPhoneNumbers, stickerId)
                }
                showStickerPicker = false
            },
            onDismiss = { showStickerPicker = false }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            conversation?.displayName ?: "Chat",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = conversation?.participantPhoneNumbers ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.8f)
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

            if (shortcutSuggestions.isNotEmpty()) {
                ShortcutSuggestionsBar(
                    suggestions = shortcutSuggestions,
                    onInsert = { trigger, expansion ->
                        viewModel.applyShortcutSuggestion(trigger, expansion)
                    }
                )
            }

            var showScheduleDialog by remember { mutableStateOf(false) }

            if (showScheduleDialog) {
                val calendar = remember { Calendar.getInstance() }
                var scheduleTime by remember { mutableStateOf(System.currentTimeMillis() + 3600000L) }
                val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()) }

                AlertDialog(
                    onDismissRequest = { showScheduleDialog = false },
                    title = { Text("Schedule Message") },
                    text = {
                        Column {
                            Text("Send this message later.")
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Scheduled for: ${dateFormat.format(Date(scheduleTime))}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = {
                                        DatePickerDialog(
                                            context,
                                            { _, year, month, day ->
                                                calendar.set(year, month, day)
                                                scheduleTime = calendar.timeInMillis
                                            },
                                            calendar.get(Calendar.YEAR),
                                            calendar.get(Calendar.MONTH),
                                            calendar.get(Calendar.DAY_OF_MONTH)
                                        ).show()
                                    },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Date", style = MaterialTheme.typography.labelSmall)
                                }
                                Button(
                                    onClick = {
                                        TimePickerDialog(
                                            context,
                                            { _, hour, minute ->
                                                calendar.set(Calendar.HOUR_OF_DAY, hour)
                                                calendar.set(Calendar.MINUTE, minute)
                                                scheduleTime = calendar.timeInMillis
                                            },
                                            calendar.get(Calendar.HOUR_OF_DAY),
                                            calendar.get(Calendar.MINUTE),
                                            true
                                        ).show()
                                    },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Time", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(
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
                        TextButton(
                            onClick = { showScheduleDialog = false }
                        ) { Text("Cancel") }
                    }
                )
            }

            if (showLocationPermissionDenied) {
                AlertDialog(
                    onDismissRequest = { showLocationPermissionDenied = false },
                    title = { Text("Location Permission Needed") },
                    text = { Text("Please grant location permission to share your location.") },
                    confirmButton = {
                        TextButton(onClick = { showLocationPermissionDenied = false }) {
                            Text("OK")
                        }
                    }
                )
            }

            if (showAttachments && messageText.isEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    AttachmentButton(
                        icon = Icons.Default.Image,
                        label = "Photo",
                        onClick = { imagePickerLauncher.launch("image/*") }
                    )
                    AttachmentButton(
                        icon = Icons.Default.AttachFile,
                        label = "File",
                        onClick = { filePickerLauncher.launch("*/*") }
                    )
                    AttachmentButton(
                        icon = Icons.Default.EmojiEmotions,
                        label = "Sticker",
                        onClick = { showStickerPicker = true }
                    )
                    AttachmentButton(
                        icon = Icons.Default.Schedule,
                        label = "Schedule",
                        onClick = { showScheduleDialog = true }
                    )
                    AttachmentButton(
                        icon = Icons.Default.MyLocation,
                        label = "Location",
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
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 6.dp, vertical = 6.dp),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            if (selectedMessageType == "RCS") MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                        .clickable {
                            viewModel.setMessageType(
                                if (selectedMessageType == "SMS") "RCS" else "SMS"
                            )
                        }
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = selectedMessageType,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = if (selectedMessageType == "RCS") MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Icon(
                            imageVector = Icons.Default.SwapHoriz,
                            contentDescription = "Switch",
                            modifier = Modifier.size(14.dp),
                            tint = if (selectedMessageType == "RCS") MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                OutlinedTextField(
                    value = messageText,
                    onValueChange = { viewModel.updateMessageText(it) },
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 44.dp, max = 120.dp),
                    placeholder = {
                        Text(
                            "Message",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(
                        onSend = {
                            conversation?.let { conv ->
                                viewModel.sendMessage(conversationId, conv.participantPhoneNumbers)
                            }
                        }
                    ),
                    maxLines = 5,
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    textStyle = MaterialTheme.typography.bodyMedium
                )

                IconButton(onClick = { showEmojiPicker = true }) {
                    Icon(
                        Icons.Default.EmojiEmotions,
                        contentDescription = "Emoji",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                FilledIconButton(
                    onClick = {
                        conversation?.let { conv ->
                            viewModel.sendMessage(conversationId, conv.participantPhoneNumbers)
                        }
                    },
                    enabled = !isSending && messageText.isNotEmpty(),
                    modifier = Modifier.size(40.dp),
                    shape = CircleShape,
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = if (messageText.isNotEmpty()) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        modifier = Modifier.size(20.dp),
                        tint = if (messageText.isNotEmpty()) Color.White
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun AttachmentButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 11.sp
        )
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
            .padding(vertical = 3.dp, horizontal = 4.dp),
        horizontalArrangement = if (isIncoming) Arrangement.Start else Arrangement.End
    ) {
        if (isIncoming) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = message.senderPhoneNumber.firstOrNull()?.uppercase() ?: "?",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
            Spacer(modifier = Modifier.width(6.dp))
        }

        Column(
            horizontalAlignment = if (isIncoming) Alignment.Start else Alignment.End,
            modifier = Modifier.widthIn(max = 300.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(
                        RoundedCornerShape(
                            topStart = if (isIncoming) 4.dp else 16.dp,
                            topEnd = if (isIncoming) 16.dp else 4.dp,
                            bottomStart = 16.dp,
                            bottomEnd = 16.dp
                        )
                    )
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
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                Column {
                    Text(
                        text = message.content,
                        color = if (isIncoming) {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        } else {
                            Color.White
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        lineHeight = 20.sp
                    )
                    if (message.reactions.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = message.reactions,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(message.timestamp),
                            color = if (isIncoming) {
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            } else {
                                Color.White.copy(alpha = 0.7f)
                            },
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 11.sp
                        )
                        if (!isIncoming) {
                            Spacer(modifier = Modifier.width(4.dp))
                            val statusIcon = when (message.status) {
                                "SENT" -> "\u2713"
                                "DELIVERED" -> "\u2713\u2713"
                                "READ" -> "\u2713\u2713"
                                "FAILED" -> "\u2717"
                                else -> ""
                            }
                            if (statusIcon.isNotEmpty()) {
                                Text(
                                    text = statusIcon,
                                    color = if (message.status == "READ") Color(0xFF4FC3F7)
                                    else Color.White.copy(alpha = 0.7f),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 11.sp,
                                    textAlign = TextAlign.End
                                )
                            }
                        }
                    }
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

@Composable
private fun StickerPicker(
    onStickerSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val stickerCategories = listOf(
        "Smileys" to listOf("\uD83D\uDE00", "\uD83D\uDE02", "\uD83D\uDE0D", "\uD83D\uDE18", "\uD83D\uDE0E", "\uD83E\uDD29"),
        "Gestures" to listOf("\uD83D\uDC4D", "\uD83D\uDC4E", "\uD83D\uDC4B", "\uD83D\uDC4F", "\uD83D\uDE4F", "\uD83D\uDC4C"),
        "Hearts" to listOf("\u2764\uFE0F", "\uD83D\uDC94", "\uD83D\uDC95", "\uD83D\uDC96", "\uD83D\uDC97", "\uD83D\uDC99"),
        "Animals" to listOf("\uD83D\uDC36", "\uD83D\uDC31", "\uD83D\uDC3B", "\uD83D\uDC3C", "\uD83E\uDD8A", "\uD83D\uDC30"),
        "Food" to listOf("\uD83C\uDF55", "\uD83C\uDF54", "\uD83C\uDF69", "\uD83C\uDF70", "\uD83C\uDF53", "\uD83E\uDD66"),
        "Objects" to listOf("\u2B50", "\uD83D\uDCA1", "\uD83C\uDF08", "\uD83C\uDF3B", "\uD83D\uDD25", "\uD83C\uDF1F")
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Stickers") },
        text = {
            LazyColumn {
                stickerCategories.forEach { (category, stickers) ->
                    item {
                        Text(
                            text = category,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            stickers.forEach { sticker ->
                                Text(
                                    text = sticker,
                                    modifier = Modifier
                                        .clickable { onStickerSelected(sticker) }
                                        .padding(8.dp),
                                    style = MaterialTheme.typography.headlineMedium
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
