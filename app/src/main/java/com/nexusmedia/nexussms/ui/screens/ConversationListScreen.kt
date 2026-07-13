package com.nexusmedia.nexussms.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.Tab
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberSwipeToDismissBoxState
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.nexusmedia.nexussms.data.models.Conversation
import com.nexusmedia.nexussms.ui.components.NexusAvatar
import com.nexusmedia.nexussms.ui.viewmodels.ConversationListViewModel
import com.nexusmedia.nexussms.ui.viewmodels.SocialPlatforms

private val AvatarColors = listOf(
    Color(0xFF5C6BC0),
    Color(0xFF26A69A),
    Color(0xFFEF5350),
    Color(0xFFAB47BC),
    Color(0xFF42A5F5),
    Color(0xFF66BB6A),
    Color(0xFFFFA726),
    Color(0xFFEC407A),
    Color(0xFF5C6BC0),
    Color(0xFF8D6E63),
    Color(0xFF78909C),
    Color(0xFF7E57C2),
)

private fun avatarGradient(name: String): Brush {
    val hash = name.hashCode()
    val c1 = AvatarColors[((hash % AvatarColors.size) + AvatarColors.size) % AvatarColors.size]
    val c2 = AvatarColors[((hash / AvatarColors.size % AvatarColors.size) + AvatarColors.size) % AvatarColors.size]
    return Brush.verticalGradient(listOf(c1, c2))
}

private val platformColors = mapOf(
    "SMS" to Color(0xFF4CAF50),
    "RCS" to Color(0xFF2196F3),
    "TELEGRAM" to Color(0xFF0088CC),
    "DISCORD" to Color(0xFF5865F2),
    "WHATSAPP" to Color(0xFF25D366),
    "SIGNAL" to Color(0xFF3A76F0),
    "SLACK" to Color(0xFF4A154B),
    "MATRIX" to Color(0xFF0DBD8B),
    "MESSENGER" to Color(0xFF0084FF),
    "VIBER" to Color(0xFF7360F2),
)

private fun platformDisplayName(platform: String): String {
    return when (platform) {
        "SMS" -> "SMS"
        "RCS" -> "RCS"
        "DISCORD" -> "Discord"
        "TELEGRAM" -> "Telegram"
        "WHATSAPP" -> "WhatsApp"
        "SIGNAL" -> "Signal"
        "SLACK" -> "Slack"
        "MATRIX" -> "Matrix"
        "MESSENGER" -> "Messenger"
        "VIBER" -> "Viber"
        else -> platform
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    return when {
        diff < 60_000L -> "Now"
        diff < 3_600_000L -> "${diff / 60_000}m"
        diff < 86_400_000L -> {
            val sdf = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
            sdf.format(java.util.Date(timestamp))
        }
        diff < 172_800_000L -> "Yesterday"
        diff < 604_800_000L -> {
            val sdf = java.text.SimpleDateFormat("EEE", java.util.Locale.getDefault())
            sdf.format(java.util.Date(timestamp))
        }
        else -> {
            val sdf = java.text.SimpleDateFormat("MMM d", java.util.Locale.getDefault())
            sdf.format(java.util.Date(timestamp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationListScreen(
    onConversationClick: (String) -> Unit = {},
    onNewConversationClick: () -> Unit = {},
    viewModel: ConversationListViewModel = hiltViewModel()
) {
    val conversationList by viewModel.conversationList.collectAsState()
    val pinnedConversations by viewModel.pinnedConversations.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isImporting by viewModel.isImporting.collectAsState()
    val importResult by viewModel.importResult.collectAsState()
    val needsPermission by viewModel.needsPermission.collectAsState()
    val selectedPlatform by viewModel.selectedPlatform.collectAsState()
    val availablePlatforms by viewModel.availablePlatforms.collectAsState()
    val avatarCache by viewModel.avatarCache.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    val importPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (allGranted) {
            viewModel.importSms()
        } else {
            viewModel.setNeedsPermission(true)
        }
    }

    LaunchedEffect(Unit) {
        val readSms = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED
        val readContacts = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED
        if (readSms && readContacts) {
            viewModel.checkAndAutoImport()
        } else {
            viewModel.setNeedsPermission(true)
        }
    }

    LaunchedEffect(importResult) {
        importResult?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearImportResult()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Messages") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White
                ),
                actions = {
                    IconButton(onClick = { }) {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = "Search",
                            tint = Color.White
                        )
                    }
                    IconButton(onClick = { viewModel.syncMatrix() }) {
                        Icon(
                            Icons.Default.CloudSync,
                            contentDescription = "Sync Matrix",
                            tint = Color.White
                        )
                    }
                    IconButton(onClick = { viewModel.resyncSms() }) {
                        Icon(
                            Icons.Default.Sync,
                            contentDescription = "Re-sync from device",
                            tint = Color.White
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNewConversationClick,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "New Message", tint = Color.White)
            }
        }
    ) { paddingValues ->
        val filteredPinned = viewModel.getFilteredPinnedConversations()
        val filteredAll = viewModel.getFilteredConversations().filter { !it.isPinned }
        val tabPlatforms = listOf("ALL") + availablePlatforms.distinct()

        Column(modifier = Modifier.padding(paddingValues)) {
            if (tabPlatforms.size > 1) {
                ScrollableTabRow(
                    selectedTabIndex = tabPlatforms.indexOf(selectedPlatform).coerceAtLeast(0),
                    edgePadding = 8.dp,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary,
                    divider = {}
                ) {
                    tabPlatforms.forEach { platform ->
                        Tab(
                            selected = selectedPlatform == platform,
                            onClick = { viewModel.setPlatform(platform) },
                            text = {
                                Text(
                                    text = if (platform == "ALL") "All" else platformDisplayName(platform),
                                    fontSize = 12.sp
                                )
                            }
                        )
                    }
                }
            }

            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Loading conversations...",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else if (conversationList.isEmpty() && pinnedConversations.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "No conversations yet",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Tap + to start a new one",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    if (filteredPinned.isNotEmpty()) {
                        item {
                            Text(
                                text = "PINNED",
                                modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        }
                        items(
                            items = filteredPinned,
                            key = { it.id }
                        ) { conversation ->
                            val normalizedPhone = conversation.participantPhoneNumbers.replace(Regex("[^+\\d]"), "")
                            SwipeableConversationItem(
                                conversation = conversation,
                                onClick = { onConversationClick(conversation.id) },
                                onDeleteClick = { viewModel.deleteConversation(conversation.id) },
                                showPinAction = false,
                                onUnpinClick = { viewModel.unpinConversation(conversation.id) },
                                onBlockClick = { viewModel.blockConversation(conversation.id) },
                                onMuteClick = {
                                    if (conversation.isMuted) viewModel.unmuteConversation(conversation.id)
                                    else viewModel.muteConversation(conversation.id)
                                },
                                onCallClick = { /* TODO: launch dialer */ },
                                onViewInfoClick = { /* TODO: show contact info */ },
                                avatarPhotoUri = avatarCache[normalizedPhone]
                            )
                        }
                        item { Spacer(modifier = Modifier.height(8.dp)) }
                    }

                    if (filteredAll.isNotEmpty()) {
                        item {
                            Text(
                                text = "RECENT",
                                modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        }
                        items(
                            items = filteredAll,
                            key = { it.id }
                        ) { conversation ->
                            val normalizedPhone = conversation.participantPhoneNumbers.replace(Regex("[^+\\d]"), "")
                            SwipeableConversationItem(
                                conversation = conversation,
                                onClick = { onConversationClick(conversation.id) },
                                onDeleteClick = { viewModel.deleteConversation(conversation.id) },
                                showPinAction = true,
                                onPinClick = { viewModel.pinConversation(conversation.id) },
                                onBlockClick = { viewModel.blockConversation(conversation.id) },
                                onMuteClick = {
                                    if (conversation.isMuted) viewModel.unmuteConversation(conversation.id)
                                    else viewModel.muteConversation(conversation.id)
                                },
                                onCallClick = { /* TODO: launch dialer */ },
                                onViewInfoClick = { /* TODO: show contact info */ },
                                avatarPhotoUri = avatarCache[normalizedPhone]
                            )
                        }
                    }
                }
            }
        }
    }

    if (needsPermission) {
        AlertDialog(
            onDismissRequest = { viewModel.setNeedsPermission(false) },
            title = { Text("Import Messages") },
            text = {
                Text("SMS and Contacts permissions are needed to import your existing messages and show contact names.")
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.setNeedsPermission(false)
                    val needed = mutableListOf<String>()
                    val readSms = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED
                    val readContacts = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED
                    if (readSms) needed.add(Manifest.permission.READ_SMS)
                    if (readContacts) needed.add(Manifest.permission.READ_CONTACTS)
                    if (needed.isNotEmpty()) {
                        importPermissionLauncher.launch(needed.toTypedArray())
                    }
                }) {
                    Text("Grant Permissions")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.setNeedsPermission(false) }) {
                    Text("Later")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeableConversationItem(
    conversation: Conversation,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit,
    showPinAction: Boolean = false,
    onPinClick: (() -> Unit)? = null,
    onUnpinClick: (() -> Unit)? = null,
    onBlockClick: (() -> Unit)? = null,
    onMuteClick: (() -> Unit)? = null,
    onCallClick: (() -> Unit)? = null,
    onViewInfoClick: (() -> Unit)? = null,
    avatarPhotoUri: String? = null
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            when (value) {
                SwipeToDismissBoxValue.StartToEnd -> {
                    if (showPinAction && onPinClick != null) onPinClick()
                    false
                }
                SwipeToDismissBoxValue.EndToStart -> {
                    showDeleteConfirm = true
                    false
                }
                else -> false
            }
        }
    )

    val dismissDirection = dismissState.dismissDirection

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            val color by animateColorAsState(
                when (dismissDirection) {
                    SwipeToDismissBoxValue.StartToEnd -> MaterialTheme.colorScheme.primary
                    SwipeToDismissBoxValue.EndToStart -> MaterialTheme.colorScheme.error
                    else -> Color.Transparent
                },
                label = "swipe_bg"
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(8.dp))
                    .background(color)
                    .padding(horizontal = 20.dp),
                contentAlignment = when (dismissDirection) {
                    SwipeToDismissBoxValue.StartToEnd -> Alignment.CenterStart
                    else -> Alignment.CenterEnd
                }
            ) {
                if (dismissDirection == SwipeToDismissBoxValue.StartToEnd && showPinAction) {
                    Icon(
                        Icons.Default.PushPin,
                        contentDescription = "Pin",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                } else if (dismissDirection == SwipeToDismissBoxValue.EndToStart) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        },
        enableDismissFromEndToStart = true,
        enableDismissFromStartToEnd = showPinAction,
        content = {
            ConversationItemRow(
                conversation = conversation,
                onClick = onClick,
                onUnpinClick = onUnpinClick,
                onBlockClick = onBlockClick,
                onMuteClick = onMuteClick,
                onCallClick = onCallClick,
                onViewInfoClick = onViewInfoClick,
                avatarPhotoUri = avatarPhotoUri
            )
        }
    )

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = {
                showDeleteConfirm = false
            },
            title = { Text("Delete Conversation") },
            text = {
                Text("Delete all messages with ${conversation.displayName.ifBlank { conversation.participantPhoneNumbers }}?")
            },
            confirmButton = {
                TextButton(onClick = {
                    onDeleteClick()
                    showDeleteConfirm = false
                }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ConversationItemRow(
    conversation: Conversation,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit = {},
    onUnpinClick: (() -> Unit)? = null,
    onBlockClick: (() -> Unit)? = null,
    onMuteClick: (() -> Unit)? = null,
    onCallClick: (() -> Unit)? = null,
    onViewInfoClick: (() -> Unit)? = null,
    avatarPhotoUri: String? = null
) {
    val displayName = conversation.displayName.ifBlank {
        conversation.participantPhoneNumbers
    }
    var showContextMenu by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = { onClick() },
                onLongClick = { showContextMenu = true }
            )
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box {
            NexusAvatar(
                photoUri = avatarPhotoUri,
                fallbackName = displayName,
                size = 52.dp
            )

            val badgeColor = platformColors[conversation.sourcePlatform] ?: Color.Gray
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(badgeColor)
                    .padding(2.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = conversation.sourcePlatform.first().toString(),
                    color = Color.White,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    if (conversation.isPinned) {
                        Icon(
                            imageVector = Icons.Default.PushPin,
                            contentDescription = "Pinned",
                            modifier = Modifier.size(12.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    if (conversation.isBlocked) {
                        Icon(
                            imageVector = Icons.Default.Block,
                            contentDescription = "Blocked",
                            modifier = Modifier.size(12.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                    if (conversation.isMuted) {
                        Icon(
                            imageVector = Icons.Default.VolumeOff,
                            contentDescription = "Muted",
                            modifier = Modifier.size(12.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = displayName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = if (conversation.unreadCount > 0) FontWeight.Bold else FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = formatTimestamp(conversation.lastMessageTime),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (conversation.unreadCount > 0) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = if (conversation.unreadCount > 0) FontWeight.SemiBold else FontWeight.Normal,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.padding(vertical = 2.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = conversation.lastMessage.ifBlank { "No messages yet" },
                        style = MaterialTheme.typography.bodySmall,
                        color = if (conversation.unreadCount > 0) MaterialTheme.colorScheme.onSurface
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = if (conversation.unreadCount > 0) FontWeight.Medium else FontWeight.Normal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (conversation.unreadCount > 0) {
                    Box(
                        modifier = Modifier
                            .size(minOf(24.dp, (12 + conversation.unreadCount.toString().length * 8).dp))
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (conversation.unreadCount > 99) "99+" else conversation.unreadCount.toString(),
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    }
                }

                if (onUnpinClick != null && conversation.isPinned) {
                    IconButton(
                        onClick = onUnpinClick,
                        modifier = Modifier
                            .padding(start = 4.dp)
                            .size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.PushPin,
                            contentDescription = "Unpin",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        DropdownMenu(
            expanded = showContextMenu,
            onDismissRequest = { showContextMenu = false }
        ) {
            if (onCallClick != null) {
                DropdownMenuItem(
                    text = { Text("Call") },
                    onClick = { showContextMenu = false; onCallClick() },
                    leadingIcon = { Icon(Icons.Default.Call, contentDescription = null) }
                )
            }
            if (onViewInfoClick != null) {
                DropdownMenuItem(
                    text = { Text("View Contact") },
                    onClick = { showContextMenu = false; onViewInfoClick() },
                    leadingIcon = { Icon(Icons.Default.Info, contentDescription = null) }
                )
            }
            DropdownMenuItem(
                text = { Text(if (conversation.isMuted) "Unmute" else "Mute") },
                onClick = {
                    showContextMenu = false
                    onMuteClick?.invoke()
                },
                leadingIcon = {
                    Icon(
                        if (conversation.isMuted) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                        contentDescription = null
                    )
                }
            )
            DropdownMenuItem(
                text = { Text(if (conversation.isBlocked) "Unblock" else "Block") },
                onClick = {
                    showContextMenu = false
                    onBlockClick?.invoke()
                },
                leadingIcon = {
                    Icon(
                        Icons.Default.Block,
                        contentDescription = null,
                        tint = if (conversation.isBlocked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                    )
                }
            )
            DropdownMenuItem(
                text = { Text("Delete") },
                onClick = { showContextMenu = false; onDeleteClick() },
                leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) }
            )
        }
    }

    HorizontalDivider(
        modifier = Modifier.padding(start = 82.dp),
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
        thickness = 0.5.dp
    )
}
