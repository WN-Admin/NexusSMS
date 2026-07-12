package com.nexusmedia.nexussms.ui.components

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.nexusmedia.nexussms.ui.theme.TonalPalette

@Composable
fun NexusAvatar(
    photoUri: String?,
    fallbackName: String,
    size: Dp = 52.dp,
    modifier: Modifier = Modifier
) {
    val initials = remember(fallbackName) {
        fallbackName.trim().split("\\s+".toRegex())
            .take(2)
            .mapNotNull { it.firstOrNull()?.uppercase() }
            .joinToString("")
            .ifEmpty { "?" }
    }

    val gradient = remember(fallbackName) {
        val hash = fallbackName.hashCode()
        val hue = ((hash % 360) + 360) % 360.toDouble()
        val chroma = 48.0 + (hash and 0xFF).toDouble() / 255.0 * 40.0
        val palette = TonalPalette.fromSeed(hue, chroma)
        Brush.verticalGradient(listOf(palette.primary(40), palette.primary(70)))
    }

    val fontSize = remember(size) {
        when {
            size <= 28.dp -> 10.sp
            size <= 40.dp -> 13.sp
            size <= 56.dp -> 18.sp
            else -> 22.sp
        }
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(gradient),
        contentAlignment = Alignment.Center
    ) {
        if (!photoUri.isNullOrBlank()) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(Uri.parse(photoUri))
                    .crossfade(true)
                    .build(),
                contentDescription = fallbackName,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(size)
                    .clip(CircleShape)
            )
        } else {
            Text(
                text = initials,
                color = Color.White,
                fontSize = fontSize,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun NexusAvatarSmall(
    photoUri: String?,
    fallbackName: String,
    size: Dp = 24.dp,
    modifier: Modifier = Modifier
) {
    NexusAvatar(
        photoUri = photoUri,
        fallbackName = fallbackName,
        size = size,
        modifier = modifier
    )
}
