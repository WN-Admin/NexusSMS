package com.nexussms.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp

private val presetColors = listOf(
    Color(0xFFF44336),
    Color(0xFFE91E63),
    Color(0xFF9C27B0),
    Color(0xFF673AB7),
    Color(0xFF3F51B5),
    Color(0xFF2196F3),
    Color(0xFF03A9F4),
    Color(0xFF00BCD4),
    Color(0xFF009688),
    Color(0xFF4CAF50),
    Color(0xFF8BC34A),
    Color(0xFFCDDC39),
    Color(0xFFFFEB3B),
    Color(0xFFFFC107),
    Color(0xFFFF9800),
    Color(0xFFFF5722),
    Color(0xFF795548),
    Color(0xFF9E9E9E),
    Color(0xFF607D8B),
    Color(0xFF000000)
)

@Composable
fun ThemeColorPicker(
    selectedColor: Color,
    onColorSelected: (Color) -> Unit,
    modifier: Modifier = Modifier
) {
    var hexInput by remember { mutableStateOf("") }
    var hexError by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Preset Colors",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(5),
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(4.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(presetColors) { color ->
                val isSelected = color == selectedColor
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(color, CircleShape)
                        .then(
                            if (isSelected) {
                                Modifier.border(3.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                            } else {
                                Modifier.border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape)
                            }
                        )
                        .clickable { onColorSelected(color) },
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Selected",
                            tint = if (color == Color.Black) Color.White else Color.Black,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        Text(
            text = "Custom Color",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
        )

        OutlinedTextField(
            value = hexInput,
            onValueChange = { input ->
                hexInput = input
                if (input.length == 6 || input.length == 7) {
                    val hex = if (input.startsWith("#")) input else "#$input"
                    try {
                        val color = Color(android.graphics.Color.parseColor(hex))
                        onColorSelected(color)
                        hexError = false
                    } catch (_: Exception) {
                        hexError = true
                    }
                } else {
                    hexError = false
                }
            },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("#FF5722") },
            label = { Text("Hex Color") },
            singleLine = true,
            isError = hexError,
            supportingText = if (hexError) {
                { Text("Invalid hex color") }
            } else null,
            shape = RoundedCornerShape(12.dp)
        )

        if (selectedColor != Color.Black || selectedColor != Color.White) {
            Box(
                modifier = Modifier
                    .padding(top = 8.dp)
                    .fillMaxWidth()
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(selectedColor)
            )
        }
    }
}
