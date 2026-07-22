package com.nexusmedia.nexussms.ui.screens

import android.Manifest
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.pm.PackageManager
import android.content.Intent
import android.location.LocationManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Forward
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import com.nexusmedia.nexussms.ui.theme.LocalBubbleTheme
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.nexusmedia.nexussms.data.models.Message
import com.nexusmedia.nexussms.ui.components.EmojiPicker
import com.nexusmedia.nexussms.ui.components.NexusAvatar
import com.nexusmedia.nexussms.features.shortcodes.ShortcodeExpansionService
import com.nexusmedia.nexussms.ui.viewmodels.ChatViewModel
import androidx.compose.material.icons.filled.VerifiedUser
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatDetailScreen(
    conversationId: String,
    onNavigateToDetails: (String) -> Unit = {},
    onNavigateToSafetyNumber: (String) -> Unit = {},
    onNavigateToKeyVerification: (String) -> Unit = {},
    viewModel: ChatViewModel = hiltViewModel()
) {
    val messages by viewModel.messages.collectAsState()
    val conversation by viewModel.currentConversation.collectAsState()
    val messageText by viewModel.messageText.collectAsState()
    val isSending by viewModel.isSending.collectAsState()
    val selectedMessageType by viewModel.selectedMessageType.collectAsState()
    val shortcutSuggestions by viewModel.shortcutSuggestions.collectAsState()
    val contactAvatarUri by viewModel.contactAvatarUri.collectAsState()
    val conversationBubbleTheme by viewModel.conversationBubbleTheme.collectAsState()
    val smartReplies by viewModel.smartReplies.collectAsState()
    val templates by viewModel.templates.collectAsState()
    val showTemplatePicker by viewModel.showTemplatePicker.collectAsState()
    val keyChangeWarning by viewModel.keyChangeWarning.collectAsState()

    val context = LocalContext.current
    var showEmojiPicker by remember { mutableStateOf(false) }
    var showStickerPicker by remember { mutableStateOf(false) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var showLocationPermissionDenied by remember { mutableStateOf(false) }
    var showAttachments by remember { mutableStateOf(false) }
    var showWallpaperPicker by remember { mutableStateOf(false) }
    var showConversationMenu by remember { mutableStateOf(false) }
    var messageActionsMessageId by remember { mutableStateOf<String?>(null) }
    var forwardingMessage by remember { mutableStateOf<Message?>(null) }
    var showForwardDialog by remember { mutableStateOf(false) }
    var showSpamReportDialog by remember { mutableStateOf(false) }

    val multiSelectMode by viewModel.multiSelectMode.collectAsState()
    val selectedMessages by viewModel.selectedMessages.collectAsState()
    val sendDelaySeconds by viewModel.sendDelaySeconds.collectAsState()
    val isSendDelayed by viewModel.isSendDelayed.collectAsState()
    val sendDelayRemaining by viewModel.sendDelayRemaining.collectAsState()
    val allConversations by viewModel.allConversations.collectAsState()

    val detectedUrl = remember(messageText) {
        val urlRegex = Regex("""(https?://[^\s]+|www\.[^\s]+|[a-zA-Z0-9.-]+\.(com|org|net|io|ca|co|me|tv|gg)[^\s]*)""")
        urlRegex.find(messageText)?.value
    }

    val urlHost = remember(detectedUrl) {
        detectedUrl?.let {
            try {
                java.net.URL(if (it.startsWith("http")) it else "https://$it").host
            } catch (_: Exception) { it }
        }
    }

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
            if (multiSelectMode) {
                TopAppBar(
                    title = {
                        Text(
                            text = context.getString(com.nexusmedia.nexussms.R.string.selected_count, selectedMessages.size),
                            style = MaterialTheme.typography.titleMedium
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = Color.White
                    ),
                    navigationIcon = {
                        IconButton(onClick = { viewModel.exitMultiSelect() }) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                        }
                    },
                    actions = {
                        IconButton(onClick = {
                            viewModel.copySelectedMessages()
                            Toast.makeText(context, context.getString(com.nexusmedia.nexussms.R.string.message_copied), Toast.LENGTH_SHORT).show()
                            viewModel.exitMultiSelect()
                        }) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = Color.White)
                        }
                        IconButton(onClick = {
                            val firstId = selectedMessages.firstOrNull()
                            val msg = firstId?.let { viewModel.forwardMessage(it) }
                            if (msg != null && selectedMessages.size == 1) {
                                forwardingMessage = msg
                                showForwardDialog = true
                            } else if (msg != null) {
                                val combined = selectedMessages.mapNotNull { id -> viewModel.forwardMessage(id) }
                                    .joinToString("\n") { it.content }
                                forwardingMessage = msg.copy(content = combined)
                                showForwardDialog = true
                            }
                            viewModel.exitMultiSelect()
                        }) {
                            Icon(Icons.Default.Forward, contentDescription = "Forward", tint = Color.White)
                        }
                        IconButton(onClick = { viewModel.lockSelectedMessages() }) {
                            Icon(Icons.Default.Lock, contentDescription = "Lock", tint = Color.White)
                        }
                        IconButton(onClick = { viewModel.deleteSelectedMessages() }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.White)
                        }
                    }
                )
            } else {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        NexusAvatar(
                            photoUri = contactAvatarUri,
                            fallbackName = conversation?.displayName ?: "Chat",
                            size = 36.dp
                        )
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
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White
                ),
                actions = {
                    IconButton(onClick = { showConversationMenu = true }) {
                        Icon(
                            Icons.Default.MoreVert,
                            contentDescription = "More options",
                            tint = Color.White
                        )
                    }
                    DropdownMenu(
                        expanded = showConversationMenu,
                        onDismissRequest = { showConversationMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Call") },
                            onClick = {
                                showConversationMenu = false
                                conversation?.participantPhoneNumbers?.let { phone ->
                                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${phone.take(10)}"))
                                    context.startActivity(intent)
                                }
                            },
                            leadingIcon = { Icon(Icons.Default.Call, contentDescription = null) }
                        )
                        DropdownMenuItem(
                            text = { Text("View Contact") },
                            onClick = {
                                showConversationMenu = false
                                conversation?.participantPhoneNumbers?.let { phone ->
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("content://contacts/people/"))
                                    try { context.startActivity(intent) } catch (_: Exception) {}
                                }
                            },
                            leadingIcon = { Icon(Icons.Default.Info, contentDescription = null) }
                        )
                        DropdownMenuItem(
                            text = { Text("Wallpaper") },
                            onClick = {
                                showConversationMenu = false
                                showWallpaperPicker = true
                            },
                            leadingIcon = { Icon(Icons.Default.Wallpaper, contentDescription = null) }
                        )
                        DropdownMenuItem(
                            text = { Text("Search") },
                            onClick = { showConversationMenu = false },
                            leadingIcon = { Icon(Icons.Default.SwapHoriz, contentDescription = null) }
                        )
                        DropdownMenuItem(
                            text = { Text(if (conversation?.isMuted == true) "Unmute" else "Mute") },
                            onClick = {
                                showConversationMenu = false
                                viewModel.toggleMute(conversationId)
                            },
                            leadingIcon = {
                                Icon(
                                    if (conversation?.isMuted == true) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                                    contentDescription = null
                                )
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(if (conversation?.isBlocked == true) "Unblock" else "Block") },
                            onClick = {
                                showConversationMenu = false
                                viewModel.toggleBlock(conversationId)
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Block,
                                    contentDescription = null,
                                    tint = if (conversation?.isBlocked == true) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                                )
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete") },
                            onClick = {
                                showConversationMenu = false
                            },
                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) }
                        )
                        DropdownMenuItem(
                            text = { Text("Verify Encryption") },
                            onClick = {
                                showConversationMenu = false
                                onNavigateToSafetyNumber(conversationId)
                            },
                            leadingIcon = { Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = MaterialTheme.colorScheme.primary) }
                        )
                        DropdownMenuItem(
                            text = { Text("Report Spam") },
                            onClick = {
                                showConversationMenu = false
                                showSpamReportDialog = true
                            },
                            leadingIcon = { Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error) }
                        )
                    }
                }
            )
            }
        }
    ) { paddingValues ->
        val effectiveBubbleTheme = conversationBubbleTheme ?: LocalBubbleTheme.current
        CompositionLocalProvider(LocalBubbleTheme provides effectiveBubbleTheme) {
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            AnimatedVisibility(visible = keyChangeWarning) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    shape = RoundedCornerShape(0.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(24.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Security Alert",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "The encryption key for ${conversation?.displayName ?: "this contact"} has changed. This could mean they reinstalled the app, or someone may be intercepting your messages.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                        TextButton(onClick = {
                            viewModel.dismissKeyChangeWarning()
                            onNavigateToSafetyNumber(conversationId)
                        }) {
                            Text("Verify", color = MaterialTheme.colorScheme.error)
                        }
                        IconButton(onClick = { viewModel.dismissKeyChangeWarning() }) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Dismiss",
                                tint = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            val wallpaperUrl = conversation?.wallpaperUrl
            val gradientColors = when (wallpaperUrl) {
                "nexussms://gradient/sunset" -> listOf(Color(0xFFFF6B35), Color(0xFFFFD166))
                "nexussms://gradient/ocean" -> listOf(Color(0xFF0077B6), Color(0xFF90E0EF))
                "nexussms://gradient/forest" -> listOf(Color(0xFF2D6A4F), Color(0xFF52B788))
                "nexussms://gradient/night" -> listOf(Color(0xFF10002B), Color(0xFF3C096C))
                "nexussms://gradient/lavender" -> listOf(Color(0xFF7B2CBF), Color(0xFFC77DFF))
                "nexussms://gradient/coral" -> listOf(Color(0xFFFF006E), Color(0xFFFFBE0B))
                else -> null
            }
            if (gradientColors != null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Brush.verticalGradient(gradientColors))
                )
            } else if (!wallpaperUrl.isNullOrBlank()) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(Uri.parse(wallpaperUrl))
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.25f))
                )
            }

            Column(modifier = Modifier.fillMaxSize()) {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    state = listState,
                    reverseLayout = true
                ) {
                    items(messages, key = { it.id }) { message ->
                        val avatarUri = if (message.senderPhoneNumber != "self") {
                            val normalized = message.senderPhoneNumber.replace(Regex("[^+\\d]"), "")
                            contactAvatarUri
                        } else null
                        AnimatedVisibility(
                            visible = true,
                            enter = fadeIn(tween(300)) + slideInVertically(tween(300)) { it / 4 }
                        ) {
                            ChatMessageBubble(
                                message = message,
                                onLongClick = {
                                    if (multiSelectMode) {
                                        viewModel.toggleMultiSelect(message.id)
                                    } else {
                                        messageActionsMessageId = message.id
                                    }
                                },
                                onClick = {
                                    if (multiSelectMode) {
                                        viewModel.toggleMultiSelect(message.id)
                                    }
                                },
                                isSelected = message.id in selectedMessages,
                                multiSelectMode = multiSelectMode,
                                messageActionsMessageId = messageActionsMessageId,
                                onDismissMessageActions = { messageActionsMessageId = null },
                                onCopy = {
                                    viewModel.copyMessage(message.id)
                                    Toast.makeText(context, context.getString(com.nexusmedia.nexussms.R.string.message_copied), Toast.LENGTH_SHORT).show()
                                    messageActionsMessageId = null
                                },
                                onForward = {
                                    forwardingMessage = message
                                    showForwardDialog = true
                                    messageActionsMessageId = null
                                },
                                onLock = {
                                    viewModel.toggleLockMessage(message.id)
                                    messageActionsMessageId = null
                                },
                                onDelete = {
                                    viewModel.deleteMessage(message)
                                    messageActionsMessageId = null
                                },
                                onEnterMultiSelect = {
                                    viewModel.enterMultiSelect(message.id)
                                    messageActionsMessageId = null
                                },
                                onReact = { messageId, emoji ->
                                    viewModel.addReaction(messageId, emoji)
                                    messageActionsMessageId = null
                                },
                                onDetails = {
                                    messageActionsMessageId = null
                                    onNavigateToDetails(message.id)
                                },
                                avatarPhotoUri = if (message.senderPhoneNumber != "self") avatarUri else null
                            )
                        }
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

            if (smartReplies.isNotEmpty() && shortcutSuggestions.isEmpty()) {
                SmartReplyBar(
                    suggestions = smartReplies,
                    onSuggestionClick = { suggestion ->
                        viewModel.applySmartReply(suggestion.text)
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

            if (showWallpaperPicker) {
                WallpaperPickerDialog(
                    onWallpaperSelected = { url ->
                        viewModel.setWallpaper(url)
                        showWallpaperPicker = false
                    },
                    onClearWallpaper = {
                        viewModel.setWallpaper(null)
                        showWallpaperPicker = false
                    },
                    onDismiss = { showWallpaperPicker = false }
                )
            }

            if (showSpamReportDialog) {
                AlertDialog(
                    onDismissRequest = { showSpamReportDialog = false },
                    title = { Text("Report Spam") },
                    text = { Text("Are you sure you want to report this conversation as spam? It will be blocked.") },
                    confirmButton = {
                        TextButton(onClick = {
                            viewModel.toggleBlock(conversationId)
                            val lastMsg = messages.firstOrNull()?.content ?: ""
                            try {
                                val spamPrefs = context.getSharedPreferences("spam_prefs", android.content.Context.MODE_PRIVATE)
                                val reports = com.google.gson.Gson().fromJson(
                                    spamPrefs.getString("reports", "[]") ?: "[]",
                                    object : com.google.gson.reflect.TypeToken<MutableList<Map<String, Any>>>() {}.type
                                ) ?: mutableListOf<Map<String, Any>>()
                                reports.add(mapOf(
                                    "conversationId" to conversationId,
                                    "content" to lastMsg,
                                    "timestamp" to System.currentTimeMillis()
                                ))
                                spamPrefs.edit().putString("reports", com.google.gson.Gson().toJson(reports)).apply()
                            } catch (_: Exception) {}
                            android.widget.Toast.makeText(context, "Conversation blocked and reported as spam", android.widget.Toast.LENGTH_SHORT).show()
                            showSpamReportDialog = false
                        }) {
                            Text("Report", color = MaterialTheme.colorScheme.error)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showSpamReportDialog = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }

            if (showTemplatePicker) {
                TemplatePickerDialog(
                    templates = templates,
                    onTemplateSelected = { template ->
                        viewModel.applyTemplate(template)
                    },
                    onDismiss = { viewModel.hideTemplatePicker() }
                )
            }

            if (showForwardDialog && forwardingMessage != null) {
                AlertDialog(
                    onDismissRequest = { showForwardDialog = false; forwardingMessage = null },
                    title = { Text(context.getString(com.nexusmedia.nexussms.R.string.forward_to)) },
                    text = {
                        LazyColumn {
                            items(allConversations.filter { it.id != conversationId }) { conv ->
            Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            viewModel.updateMessageText(forwardingMessage!!.content)
                                            viewModel.sendMessage(conv.id, conv.participantPhoneNumbers)
                                            showForwardDialog = false
                                            forwardingMessage = null
                                        }
                                        .padding(vertical = 10.dp, horizontal = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    NexusAvatar(
                                        photoUri = null,
                                        fallbackName = conv.displayName,
                                        size = 36.dp
                                    )
                                    Column {
                                        Text(
                                            text = conv.displayName,
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Text(
                                            text = conv.participantPhoneNumbers,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {},
                    dismissButton = {
                        TextButton(onClick = { showForwardDialog = false; forwardingMessage = null }) {
                            Text("Cancel")
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
                    AttachmentButton(
                        icon = Icons.Default.Wallpaper,
                        label = "Wallpaper",
                        onClick = { showWallpaperPicker = true }
                    )
                    AttachmentButton(
                        icon = Icons.Default.NoteAdd,
                        label = "Template",
                        onClick = { viewModel.showTemplatePicker() }
                    )
                }
            }

            if (detectedUrl != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AssistChip(
                        onClick = {
                            try {
                                val url = if (detectedUrl.startsWith("http")) detectedUrl else "https://$detectedUrl"
                                val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, Uri.parse(url))
                                context.startActivity(intent)
                            } catch (_: Exception) {}
                        },
                        label = {
                            Text(
                                text = urlHost ?: detectedUrl,
                                style = MaterialTheme.typography.labelSmall,
                                maxLines = 1
                            )
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.AttachFile,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                        )
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = detectedUrl.take(40) + if (detectedUrl.length > 40) "..." else "",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            AnimatedVisibility(visible = isSendDelayed) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f))
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.Timer,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = context.getString(com.nexusmedia.nexussms.R.string.send_delay_seconds, sendDelayRemaining),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    TextButton(onClick = { viewModel.cancelPendingSend() }) {
                        Text(
                            "Cancel",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
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
                        containerColor = if (messageText.isNotEmpty()) LocalBubbleTheme.current.sentColor
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
                } // FilledIconButton
            } // Row
        } // Column (compose bar)
        } // Box
        } // outer Column (warning + content)
    } // CompositionLocalProvider
    } // Scaffold content
} // ChatDetailScreen

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
private fun ChatMessageBubble(
    message: Message,
    onLongClick: () -> Unit = {},
    onClick: () -> Unit = {},
    isSelected: Boolean = false,
    multiSelectMode: Boolean = false,
    messageActionsMessageId: String? = null,
    onDismissMessageActions: () -> Unit = {},
    onCopy: () -> Unit = {},
    onForward: () -> Unit = {},
    onLock: () -> Unit = {},
    onDelete: () -> Unit = {},
    onEnterMultiSelect: () -> Unit = {},
    onReact: (String, String) -> Unit = { _, _ -> },
    onDetails: () -> Unit = {},
    avatarPhotoUri: String? = null
) {
    val isIncoming = message.senderPhoneNumber != "self"
    val bubbleTheme = LocalBubbleTheme.current
    val bubbleColor = if (isIncoming) bubbleTheme.receivedColor else bubbleTheme.sentColor
    val textColor = if (isIncoming) bubbleTheme.receivedTextColor else bubbleTheme.sentTextColor
    val cr = bubbleTheme.cornerRadius
    val bubbleShape = if (isIncoming) {
        RoundedCornerShape((cr / 4).dp.coerceAtLeast(2.dp), cr.dp, cr.dp, cr.dp)
    } else {
        RoundedCornerShape(cr.dp, (cr / 4).dp.coerceAtLeast(2.dp), cr.dp, cr.dp)
    }

    val selectionColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color.Transparent

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp, horizontal = 4.dp),
        horizontalArrangement = if (isIncoming) Arrangement.Start else Arrangement.End
    ) {
        if (isIncoming) {
            NexusAvatar(
                photoUri = avatarPhotoUri,
                fallbackName = message.senderPhoneNumber,
                size = 28.dp
            )
            Spacer(modifier = Modifier.width(6.dp))
        }

        Column(
            horizontalAlignment = if (isIncoming) Alignment.Start else Alignment.End,
            modifier = Modifier.widthIn(max = 300.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(elevation = bubbleTheme.elevation.dp, shape = bubbleShape, ambientColor = Color.Black.copy(alpha = 0.15f))
                    .clip(bubbleShape)
                    .background(color = selectionColor)
                    .then(
                        if (isSelected) Modifier.border(2.dp, MaterialTheme.colorScheme.primary, bubbleShape)
                        else Modifier
                    )
                    .background(color = bubbleColor)
                    .combinedClickable(
                        onClick = onClick,
                        onLongClick = onLongClick
                    )
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                Column {
                    Text(
                        text = message.content,
                        color = textColor,
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
                        if (message.isLocked) {
                            Icon(
                                Icons.Default.Lock,
                                contentDescription = "Locked",
                                modifier = Modifier.size(12.dp),
                                tint = textColor.copy(alpha = 0.5f)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                        }
                        Text(
                            text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(message.timestamp),
                            color = textColor.copy(alpha = 0.6f),
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
                                    color = when (message.status) {
                                        "READ" -> Color(0xFF4FC3F7)
                                        "FAILED" -> MaterialTheme.colorScheme.error
                                        else -> textColor.copy(alpha = 0.7f)
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 11.sp,
                                    textAlign = TextAlign.End
                                )
                            }
                        }
                    }
                }
            }

            if (messageActionsMessageId == message.id && !multiSelectMode) {
                MessageActionsPopup(
                    isLocked = message.isLocked,
                    onCopy = onCopy,
                    onForward = onForward,
                    onLock = onLock,
                    onDelete = onDelete,
                    onSelect = onEnterMultiSelect,
                    onReact = { onReact(message.id, it) },
                    onDetails = onDetails,
                    onDismiss = onDismissMessageActions
                )
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
private fun MessageActionsPopup(
    isLocked: Boolean,
    onCopy: () -> Unit,
    onForward: () -> Unit,
    onLock: () -> Unit,
    onDelete: () -> Unit,
    onSelect: () -> Unit,
    onReact: (String) -> Unit,
    onDetails: () -> Unit = {},
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(4.dp)) {
            val emojis = listOf("\uD83D\uDC4D", "\u2764\uFE0F", "\uD83D\uDE02", "\uD83D\uDE2E", "\uD83D\uDE22", "\uD83D\uDE4F")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                emojis.forEach { emoji ->
                    Text(
                        text = emoji,
                        modifier = Modifier
                            .clickable {
                                onReact(emoji)
                                onDismiss()
                            }
                            .padding(6.dp),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
            HorizontalDivider(modifier = Modifier.padding(horizontal = 8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                MessageActionChip(Icons.Default.ContentCopy, "Copy") { onCopy(); onDismiss() }
                MessageActionChip(Icons.Default.Forward, "Forward") { onForward(); onDismiss() }
                MessageActionChip(
                    if (isLocked) Icons.Default.LockOpen else Icons.Default.Lock,
                    if (isLocked) "Unlock" else "Lock"
                ) { onLock(); onDismiss() }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                MessageActionChip(Icons.Default.Delete, "Delete", tint = MaterialTheme.colorScheme.error) { onDelete(); onDismiss() }
                MessageActionChip(Icons.Default.SelectAll, "Select") { onSelect() }
                MessageActionChip(Icons.Default.Info, "Details") { onDetails(); onDismiss() }
            }
        }
    }
}

@Composable
private fun MessageActionChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    tint: Color = MaterialTheme.colorScheme.onSurface,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            modifier = Modifier.size(22.dp),
            tint = tint
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontSize = 10.sp,
            color = tint
        )
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

@Composable
private fun SmartReplyBar(
    suggestions: List<com.nexusmedia.nexussms.features.smartreply.SmartReplyService.SmartReplySuggestion>,
    onSuggestionClick: (com.nexusmedia.nexussms.features.smartreply.SmartReplyService.SmartReplySuggestion) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
    ) {
        suggestions.forEach { suggestion ->
            Card(
                modifier = Modifier.clickable { onSuggestionClick(suggestion) },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Text(
                    text = suggestion.text,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TemplatePickerDialog(
    templates: List<com.nexusmedia.nexussms.data.models.Template>,
    onTemplateSelected: (com.nexusmedia.nexussms.data.models.Template) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Pick a Template") },
        text = {
            if (templates.isEmpty()) {
                Text(
                    "No templates yet. Create one in Settings > Templates.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                LazyColumn {
                    val grouped = templates.groupBy { it.category }
                    grouped.forEach { (category, categoryTemplates) ->
                        item {
                            Text(
                                text = category.uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(vertical = 6.dp)
                            )
                        }
                        items(categoryTemplates) { template ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp)
                                    .clickable { onTemplateSelected(template) },
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                )
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = template.name,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = template.content,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun WallpaperPickerDialog(
    onWallpaperSelected: (String) -> Unit,
    onClearWallpaper: () -> Unit,
    onDismiss: () -> Unit
) {
    val builtInWallpapers = listOf(
        "Gradient: Sunset" to "nexussms://gradient/sunset",
        "Gradient: Ocean" to "nexussms://gradient/ocean",
        "Gradient: Forest" to "nexussms://gradient/forest",
        "Gradient: Night" to "nexussms://gradient/night",
        "Gradient: Lavender" to "nexussms://gradient/lavender",
        "Gradient: Coral" to "nexussms://gradient/coral"
    )

    val gradientColors = mapOf(
        "nexussms://gradient/sunset" to listOf(Color(0xFFFF6B35), Color(0xFFFF8C61), Color(0xFFFFD166)),
        "nexussms://gradient/ocean" to listOf(Color(0xFF0077B6), Color(0xFF00B4D8), Color(0xFF90E0EF)),
        "nexussms://gradient/forest" to listOf(Color(0xFF2D6A4F), Color(0xFF40916C), Color(0xFF52B788)),
        "nexussms://gradient/night" to listOf(Color(0xFF10002B), Color(0xFF240046), Color(0xFF3C096C)),
        "nexussms://gradient/lavender" to listOf(Color(0xFF7B2CBF), Color(0xFF9D4EDD), Color(0xFFC77DFF)),
        "nexussms://gradient/coral" to listOf(Color(0xFFFF006E), Color(0xFFFF5A5F), Color(0xFFFFBE0B))
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Chat Wallpaper") },
        text = {
            Column {
                Text(
                    "Select a wallpaper for this conversation",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                LazyColumn {
                    items(builtInWallpapers.size) { index ->
                        val (name, url) = builtInWallpapers[index]
                        val colors = gradientColors[url] ?: listOf(Color.Gray, Color.DarkGray)
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable { onWallpaperSelected(url) },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(60.dp)
                                    .background(Brush.horizontalGradient(colors)),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Text(
                                    text = name,
                                    color = Color.White,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onClearWallpaper) {
                Text("Clear Wallpaper", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
