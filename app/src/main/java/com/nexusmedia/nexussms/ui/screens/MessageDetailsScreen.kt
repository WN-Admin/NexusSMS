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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexusmedia.nexussms.data.models.Message
import com.nexusmedia.nexussms.data.repository.ConversationRepository
import com.nexusmedia.nexussms.data.repository.MessageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class MessageDetailsViewModel @Inject constructor(
    private val messageRepository: MessageRepository,
    private val conversationRepository: ConversationRepository
) : ViewModel() {

    private val _message = MutableStateFlow<Message?>(null)
    val message: StateFlow<Message?> = _message.asStateFlow()

    private val _conversationName = MutableStateFlow("")
    val conversationName: StateFlow<String> = _conversationName.asStateFlow()

    fun loadMessage(messageId: String) {
        viewModelScope.launch {
            _message.value = messageRepository.getMessageById(messageId)
            _message.value?.let { msg ->
                val conversation = conversationRepository.getConversationById(msg.conversationId)
                _conversationName.value = conversation?.displayName ?: ""
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageDetailsScreen(
    messageId: String,
    onBack: () -> Unit,
    viewModel: MessageDetailsViewModel = hiltViewModel()
) {
    val message by viewModel.message.collectAsState()
    val conversationName by viewModel.conversationName.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(messageId) {
        viewModel.loadMessage(messageId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Message Details") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        message?.let { msg ->
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Message", msg.content)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "Message copied", Toast.LENGTH_SHORT).show()
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy",
                            tint = Color.White
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
        val msg = message
        if (msg == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Loading...",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Content",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = msg.content,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }

                HorizontalDivider()

                DetailRow(label = "Message ID", value = msg.id.take(8) + "...")
                DetailRow(label = "Conversation", value = conversationName.ifBlank { msg.conversationId.take(8) + "..." })

                HorizontalDivider()

                if (msg.senderPhoneNumber == "self") {
                    DetailRow(label = "Direction", value = "Outgoing")
                    DetailRow(label = "Recipient", value = msg.recipientPhoneNumber)
                } else {
                    DetailRow(label = "Direction", value = "Incoming")
                    DetailRow(label = "Sender", value = msg.senderPhoneNumber)
                }

                HorizontalDivider()

                val fullDateFormat = remember { SimpleDateFormat("EEEE, MMMM d, yyyy 'at' HH:mm:ss", Locale.getDefault()) }
                val shortDateFormat = remember { SimpleDateFormat("MMM d, yyyy HH:mm:ss", Locale.getDefault()) }
                DetailRow(label = "Timestamp", value = shortDateFormat.format(Date(msg.timestamp)))
                DetailRow(label = "Full Date", value = fullDateFormat.format(Date(msg.timestamp)))

                HorizontalDivider()

                val statusDisplay = when (msg.status) {
                    "SENDING" -> "Sending" to MaterialTheme.colorScheme.onSurfaceVariant
                    "SENT" -> "Sent" to MaterialTheme.colorScheme.primary
                    "DELIVERED" -> "Delivered" to MaterialTheme.colorScheme.primary
                    "READ" -> "Read" to Color(0xFF4FC3F7)
                    "FAILED" -> "Failed" to MaterialTheme.colorScheme.error
                    else -> msg.status to MaterialTheme.colorScheme.onSurfaceVariant
                }
                DetailRow(
                    label = "Status",
                    value = statusDisplay.first,
                    valueColor = statusDisplay.second
                )

                val typeIcon: ImageVector = when (msg.type) {
                    "IMAGE" -> Icons.Default.Image
                    "VIDEO" -> Icons.Default.PlayArrow
                    "AUDIO" -> Icons.Default.PlayArrow
                    "FILE" -> Icons.Default.InsertDriveFile
                    "LOCATION" -> Icons.Default.LocationOn
                    "STICKER" -> Icons.Default.Image
                    else -> Icons.Default.Link
                }
                DetailRow(label = "Type", value = msg.type, icon = typeIcon)
                DetailRow(label = "Platform", value = msg.sourcePlatform)

                HorizontalDivider()

                DetailRow(
                    label = "Encryption",
                    value = if (msg.isEncrypted) "Encrypted (${msg.encryptionAlgorithm ?: "Unknown"})" else "Not encrypted",
                    icon = Icons.Default.Lock
                )
                DetailRow(
                    label = "Locked",
                    value = if (msg.isLocked) "Yes" else "No",
                    icon = Icons.Default.Lock
                )

                if (msg.deliveryReport.isNotBlank()) {
                    HorizontalDivider()
                    DetailRow(label = "Delivery Report", value = msg.deliveryReport)
                }

                HorizontalDivider()

                val charCount = msg.content.length
                val smsSegments = if (charCount <= 160) 1 else {
                    val multipart = (charCount - 1) / 153 + 1
                    multipart
                }
                DetailRow(label = "Characters", value = "$charCount")
                DetailRow(label = "SMS Segments", value = "$smsSegments")

                if (msg.scheduledTime != null) {
                    HorizontalDivider()
                    DetailRow(label = "Scheduled For", value = shortDateFormat.format(Date(msg.scheduledTime)))
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.onSurface,
    icon: ImageVector? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = valueColor,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(start = 16.dp)
        )
    }
}
