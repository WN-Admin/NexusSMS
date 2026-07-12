package com.nexusmedia.nexussms.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.ScheduleSend
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nexusmedia.nexussms.data.models.Message
import com.nexusmedia.nexussms.ui.theme.LocalBubbleTheme
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MessageBubble(
    message: Message,
    modifier: Modifier = Modifier
) {
    val isOutgoing = message.senderPhoneNumber == "self"
    val bubbleTheme = LocalBubbleTheme.current
    val bubbleColor = if (isOutgoing) bubbleTheme.sentColor else bubbleTheme.receivedColor
    val contentColor = if (isOutgoing) bubbleTheme.sentTextColor else bubbleTheme.receivedTextColor
    val timestampColor = contentColor.copy(alpha = 0.6f)
    val cr = bubbleTheme.cornerRadius
    val bubbleShape = if (isOutgoing) {
        RoundedCornerShape(cr.dp, (cr / 4).dp.coerceAtLeast(2.dp), cr.dp, cr.dp)
    } else {
        RoundedCornerShape((cr / 4).dp.coerceAtLeast(2.dp), cr.dp, cr.dp, cr.dp)
    }

    val reactionsMap = remember(message.reactions) {
        try {
            val json = JSONObject(message.reactions)
            json.keys().asSequence().map { key ->
                val arr = json.getJSONArray(key)
                key to arr.length()
            }.toList()
        } catch (_: Exception) {
            emptyList()
        }
    }

    val mediaList = remember(message.mediaUrls) {
        try {
            val arr = JSONArray(message.mediaUrls)
            (0 until arr.length()).map { arr.getString(it) }
        } catch (_: Exception) {
            emptyList()
        }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 3.dp),
        horizontalArrangement = if (isOutgoing) Arrangement.End else Arrangement.Start
    ) {
        Column(
            horizontalAlignment = if (isOutgoing) Alignment.End else Alignment.Start,
            modifier = Modifier.widthIn(max = 300.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(bubbleShape)
                    .background(bubbleColor)
                    .padding(12.dp)
            ) {
                Column {
                    if (mediaList.isNotEmpty()) {
                        MediaAttachments(mediaList)
                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    Text(
                        text = message.content,
                        color = contentColor,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Normal
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = formatTimestamp(message.timestamp),
                            color = timestampColor,
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 11.sp
                        )

                        if (isOutgoing) {
                            StatusIcon(message.status)
                        } else if (message.isRead) {
                            Icon(
                                imageVector = Icons.Default.DoneAll,
                                contentDescription = "Read",
                                tint = timestampColor,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }

            if (reactionsMap.isNotEmpty()) {
                Row(
                    modifier = Modifier.padding(top = 2.dp, start = 8.dp, end = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    reactionsMap.forEach { (emoji, count) ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Text(text = emoji, fontSize = 14.sp)
                                if (count > 1) {
                                    Text(
                                        text = count.toString(),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MediaAttachments(mediaUrls: List<String>) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        mediaUrls.take(3).forEach { _ ->
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Image,
                    contentDescription = "Media",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
private fun StatusIcon(status: String) {
    val (icon, description) = when (status) {
        "SENDING" -> Icons.Default.AccessTime to "Sending"
        "SENT" -> Icons.Default.Check to "Sent"
        "DELIVERED" -> Icons.Default.DoneAll to "Delivered"
        "READ" -> Icons.Default.CheckCircle to "Read"
        "FAILED" -> Icons.Default.ErrorOutline to "Failed"
        "SCHEDULED" -> Icons.Default.ScheduleSend to "Scheduled"
        else -> Icons.Default.Check to "Sent"
    }
    val tint = when (status) {
        "FAILED" -> MaterialTheme.colorScheme.error
        else -> Color.White.copy(alpha = 0.7f)
    }
    Icon(
        imageVector = icon,
        contentDescription = description,
        tint = tint,
        modifier = Modifier.size(14.dp)
    )
}

private fun formatTimestamp(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    val sdf = if (diff < 86400000L) {
        SimpleDateFormat("HH:mm", Locale.getDefault())
    } else if (diff < 604800000L) {
        SimpleDateFormat("EEE", Locale.getDefault())
    } else {
        SimpleDateFormat("MMM d", Locale.getDefault())
    }
    return sdf.format(Date(timestamp))
}
