package com.nexusmedia.nexussms.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.nexusmedia.nexussms.data.models.Theme
import com.nexusmedia.nexussms.data.repository.ThemeRepository
import com.nexusmedia.nexussms.features.theme.ThemeManager
import com.nexusmedia.nexussms.features.theme.ThemePreference
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ThemeCreatorViewModel @Inject constructor(
    private val themeManager: ThemeManager,
    private val themeRepository: ThemeRepository,
    private val themePreference: ThemePreference
) : ViewModel() {

    fun createAndApply(
        name: String,
        primaryColor: String,
        secondaryColor: String,
        bubbleColorSent: String,
        bubbleColorReceived: String,
        textColor: String,
        backgroundColor: String,
        surfaceColor: String,
        textColorSecondary: String,
        bubbleTextColorSent: String,
        bubbleTextColorReceived: String,
        bubbleStyle: String,
        bubbleCornerRadius: Int,
        bubbleElevation: Float,
        isDarkMode: Boolean,
        onDone: () -> Unit
    ) {
        viewModelScope.launch {
            val theme = Theme(
                name = name,
                isCustom = true,
                isDefault = false,
                primaryColor = primaryColor,
                secondaryColor = secondaryColor,
                bubbleColorSent = bubbleColorSent,
                bubbleColorReceived = bubbleColorReceived,
                textColor = textColor,
                backgroundColor = backgroundColor,
                surfaceColor = surfaceColor,
                textColorSecondary = textColorSecondary,
                bubbleTextColorSent = bubbleTextColorSent,
                bubbleTextColorReceived = bubbleTextColorReceived,
                bubbleStyle = bubbleStyle,
                bubbleCornerRadius = bubbleCornerRadius,
                bubbleElevation = bubbleElevation,
                isDarkMode = isDarkMode
            )
            themeRepository.insertTheme(theme)
            themePreference.setTheme(theme)
            themeManager.applyTheme(theme)
            onDone()
        }
    }
}

private val presetColors = listOf(
    Color(0xFF2196F3) to "#2196F3",
    Color(0xFF03DAC6) to "#03DAC6",
    Color(0xFFBB86FC) to "#BB86FC",
    Color(0xFF000000) to "#000000",
    Color(0xFFFFFFFF) to "#FFFFFF",
    Color(0xFFF44336) to "#F44336",
    Color(0xFFE91E63) to "#E91E63",
    Color(0xFF9C27B0) to "#9C27B0",
    Color(0xFF673AB7) to "#673AB7",
    Color(0xFF3F51B5) to "#3F51B5",
    Color(0xFF00BCD4) to "#00BCD4",
    Color(0xFF4CAF50) to "#4CAF50",
    Color(0xFF8BC34A) to "#8BC34A",
    Color(0xFFFFEB3B) to "#FFEB3B",
    Color(0xFFFF9800) to "#FF9800",
    Color(0xFF795548) to "#795548",
    Color(0xFF9E9E9E) to "#9E9E9E",
    Color(0xFF607D8B) to "#607D8B",
    Color(0xFF0077B6) to "#0077B6",
    Color(0xFF00B4D8) to "#00B4D8",
    Color(0xFF2D6A4F) to "#2D6A4F",
    Color(0xFF52B788) to "#52B788",
    Color(0xFFE85D04) to "#E85D04",
    Color(0xFFFAA307) to "#FAA307",
    Color(0xFF7B2CBF) to "#7B2CBF",
    Color(0xFFC77DFF) to "#C77DFF",
    Color(0xFFB76E79) to "#B76E79",
    Color(0xFFE8A0BF) to "#E8A0BF",
    Color(0xFFFF006E) to "#FF006E",
    Color(0xFF3A86FF) to "#3A86FF",
    Color(0xFF00C49A) to "#00C49A",
    Color(0xFF7C4DFF) to "#7C4DFF",
    Color(0xFFF5F5F5) to "#F5F5F5",
    Color(0xFFE0E0E0) to "#E0E0E0",
    Color(0xFF333333) to "#333333",
    Color(0xFF121212) to "#121212",
    Color(0xFF1E1E1E) to "#1E1E1E",
    Color(0xFF2C2C2C) to "#2C2C2C"
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ThemeCreatorScreen(
    navController: NavController,
    viewModel: ThemeCreatorViewModel = hiltViewModel()
) {
    var themeName by remember { mutableStateOf("") }
    var primaryColor by remember { mutableStateOf("#2196F3") }
    var secondaryColor by remember { mutableStateOf("#03DAC6") }
    var backgroundColor by remember { mutableStateOf("#FFFFFF") }
    var surfaceColor by remember { mutableStateOf("#FFFFFF") }
    var bubbleColorSent by remember { mutableStateOf("#2196F3") }
    var bubbleColorReceived by remember { mutableStateOf("#E8E8E8") }
    var textColor by remember { mutableStateOf("#212121") }
    var textColorSecondary by remember { mutableStateOf("#757575") }
    var bubbleTextColorSent by remember { mutableStateOf("#FFFFFF") }
    var bubbleTextColorReceived by remember { mutableStateOf("#000000") }
    var isDarkMode by remember { mutableStateOf(false) }
    var bubbleStyle by remember { mutableStateOf("ROUNDED") }
    var bubbleCornerRadius by remember { mutableIntStateOf(16) }
    var bubbleElevation by remember { mutableFloatStateOf(4f) }

    var showColorPicker by remember { mutableStateOf(false) }
    var colorPickerTarget by remember { mutableStateOf("") }
    var colorPickerLabel by remember { mutableStateOf("") }

    fun openColorPicker(target: String, label: String) {
        colorPickerTarget = target
        colorPickerLabel = label
        showColorPicker = true
    }

    fun applyColor(hex: String) {
        when (colorPickerTarget) {
            "primary" -> primaryColor = hex
            "secondary" -> secondaryColor = hex
            "background" -> backgroundColor = hex
            "surface" -> surfaceColor = hex
            "bubbleSent" -> bubbleColorSent = hex
            "bubbleReceived" -> bubbleColorReceived = hex
            "text" -> textColor = hex
            "textSecondary" -> textColorSecondary = hex
            "bubbleTextSent" -> bubbleTextColorSent = hex
            "bubbleTextReceived" -> bubbleTextColorReceived = hex
        }
        showColorPicker = false
    }

    val livePreviewTheme = remember(
        primaryColor, secondaryColor, backgroundColor, surfaceColor,
        bubbleColorSent, bubbleColorReceived, textColor, textColorSecondary,
        bubbleTextColorSent, bubbleTextColorReceived, bubbleStyle,
        bubbleCornerRadius, bubbleElevation, isDarkMode
    ) {
        Theme(
            name = themeName.ifBlank { "Preview" },
            primaryColor = primaryColor,
            secondaryColor = secondaryColor,
            backgroundColor = backgroundColor,
            surfaceColor = surfaceColor,
            bubbleColorSent = bubbleColorSent,
            bubbleColorReceived = bubbleColorReceived,
            textColor = textColor,
            textColorSecondary = textColorSecondary,
            bubbleTextColorSent = bubbleTextColorSent,
            bubbleTextColorReceived = bubbleTextColorReceived,
            bubbleStyle = bubbleStyle,
            bubbleCornerRadius = bubbleCornerRadius,
            bubbleElevation = bubbleElevation,
            isDarkMode = isDarkMode,
            isCustom = true
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Theme") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = themeName,
                onValueChange = { themeName = it },
                label = { Text("Theme Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Live Preview",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    ThemePreview(theme = livePreviewTheme)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Colors",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            ColorPickerRow(
                label = "Primary Color",
                hex = primaryColor,
                onClick = { openColorPicker("primary", "Primary Color") }
            )
            ColorPickerRow(
                label = "Secondary Color",
                hex = secondaryColor,
                onClick = { openColorPicker("secondary", "Secondary Color") }
            )
            ColorPickerRow(
                label = "Background Color",
                hex = backgroundColor,
                onClick = { openColorPicker("background", "Background Color") }
            )
            ColorPickerRow(
                label = "Sent Bubble Color",
                hex = bubbleColorSent,
                onClick = { openColorPicker("bubbleSent", "Sent Bubble Color") }
            )
            ColorPickerRow(
                label = "Received Bubble Color",
                hex = bubbleColorReceived,
                onClick = { openColorPicker("bubbleReceived", "Received Bubble Color") }
            )
            ColorPickerRow(
                label = "Sent Text Color",
                hex = bubbleTextColorSent,
                onClick = { openColorPicker("bubbleTextSent", "Sent Text Color") }
            )
            ColorPickerRow(
                label = "Received Text Color",
                hex = bubbleTextColorReceived,
                onClick = { openColorPicker("bubbleTextReceived", "Received Text Color") }
            )
            ColorPickerRow(
                label = "Text Color",
                hex = textColor,
                onClick = { openColorPicker("text", "Text Color") }
            )
            ColorPickerRow(
                label = "Text Color Secondary",
                hex = textColorSecondary,
                onClick = { openColorPicker("textSecondary", "Text Color Secondary") }
            )
            ColorPickerRow(
                label = "Surface Color",
                hex = surfaceColor,
                onClick = { openColorPicker("surface", "Surface Color") }
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Dark Mode",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (isDarkMode) "Dark Mode" else "Light Mode",
                    style = MaterialTheme.typography.bodyMedium
                )
                Switch(
                    checked = isDarkMode,
                    onCheckedChange = {
                        isDarkMode = it
                        if (it) {
                            backgroundColor = "#121212"
                            surfaceColor = "#1E1E1E"
                            textColor = "#E1E1E1"
                            textColorSecondary = "#9E9E9E"
                            bubbleColorReceived = "#2C2C2C"
                            bubbleTextColorReceived = "#E1E1E1"
                        } else {
                            backgroundColor = "#FFFFFF"
                            surfaceColor = "#FFFFFF"
                            textColor = "#212121"
                            textColorSecondary = "#757575"
                            bubbleColorReceived = "#E8E8E8"
                            bubbleTextColorReceived = "#000000"
                        }
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.primary
                    )
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Bubble Style",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val styles = listOf(
                    "ROUNDED" to "Rounded",
                    "SQUARE" to "Square",
                    "MODERN" to "Modern",
                    "SHARP" to "Sharp"
                )
                styles.forEach { (styleValue, styleLabel) ->
                    FilterChip(
                        selected = bubbleStyle == styleValue,
                        onClick = {
                            bubbleStyle = styleValue
                            bubbleCornerRadius = when (styleValue) {
                                "SHARP" -> 2
                                "SQUARE" -> 4
                                "MODERN" -> 12
                                else -> 16
                            }
                        },
                        label = { Text(styleLabel) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    if (themeName.isNotBlank()) {
                        viewModel.createAndApply(
                            name = themeName.trim(),
                            primaryColor = primaryColor,
                            secondaryColor = secondaryColor,
                            bubbleColorSent = bubbleColorSent,
                            bubbleColorReceived = bubbleColorReceived,
                            textColor = textColor,
                            backgroundColor = backgroundColor,
                            surfaceColor = surfaceColor,
                            textColorSecondary = textColorSecondary,
                            bubbleTextColorSent = bubbleTextColorSent,
                            bubbleTextColorReceived = bubbleTextColorReceived,
                            bubbleStyle = bubbleStyle,
                            bubbleCornerRadius = bubbleCornerRadius,
                            bubbleElevation = bubbleElevation,
                            isDarkMode = isDarkMode,
                            onDone = { navController.popBackStack() }
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = themeName.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    "Save & Apply",
                    modifier = Modifier.padding(vertical = 4.dp),
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showColorPicker) {
        AlertDialog(
            onDismissRequest = { showColorPicker = false },
            title = { Text(colorPickerLabel) },
            text = {
                Column {
                    Text(
                        text = "Preset Colors",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        presetColors.forEach { (color, hex) ->
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .then(
                                        if (hex == when (colorPickerTarget) {
                                                "primary" -> primaryColor
                                                "secondary" -> secondaryColor
                                                "background" -> backgroundColor
                                                "surface" -> surfaceColor
                                                "bubbleSent" -> bubbleColorSent
                                                "bubbleReceived" -> bubbleColorReceived
                                                "text" -> textColor
                                                "textSecondary" -> textColorSecondary
                                                "bubbleTextSent" -> bubbleTextColorSent
                                                "bubbleTextReceived" -> bubbleTextColorReceived
                                                else -> ""
                                            }
                                        ) Modifier.border(3.dp, MaterialTheme.colorScheme.primary, CircleShape)
                                        else Modifier
                                    )
                                    .clickable { applyColor(hex) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showColorPicker = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun ColorPickerRow(
    label: String,
    hex: String,
    onClick: () -> Unit
) {
    val color = try {
        Color(android.graphics.Color.parseColor(hex))
    } catch (_: Exception) {
        Color.Gray
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = hex,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(end = 8.dp)
            )
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(color)
                    .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
            )
        }
    }
}
