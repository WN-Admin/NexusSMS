package com.nexusmedia.nexussms.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ContactPhone
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

enum class MediaType {
    CAMERA, GALLERY, VIDEO, AUDIO, DOCUMENT, LOCATION, CONTACT
}

private data class MediaOption(
    val type: MediaType,
    val label: String,
    val icon: ImageVector
)

private val mediaOptions = listOf(
    MediaOption(MediaType.CAMERA, "Camera", Icons.Default.CameraAlt),
    MediaOption(MediaType.GALLERY, "Gallery", Icons.Default.PhotoLibrary),
    MediaOption(MediaType.VIDEO, "Video", Icons.Default.Videocam),
    MediaOption(MediaType.AUDIO, "Audio", Icons.Default.Audiotrack),
    MediaOption(MediaType.DOCUMENT, "Document", Icons.Default.Description),
    MediaOption(MediaType.LOCATION, "Location", Icons.Default.LocationOn),
    MediaOption(MediaType.CONTACT, "Contact", Icons.Default.ContactPhone)
)

@Composable
fun MediaPicker(
    onMediaTypeSelected: (MediaType) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(mediaOptions) { option ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onMediaTypeSelected(option.type) },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = option.icon,
                        contentDescription = option.label,
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = option.label,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }
    }
}
