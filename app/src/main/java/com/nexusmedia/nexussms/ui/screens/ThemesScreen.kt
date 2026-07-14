package com.nexusmedia.nexussms.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.nexusmedia.nexussms.data.models.Theme
import com.nexusmedia.nexussms.data.repository.ThemeRepository
import com.nexusmedia.nexussms.features.theme.ThemeManager
import com.nexusmedia.nexussms.features.theme.ThemePreference
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class ThemeDialogState {
    data object Hidden : ThemeDialogState()
    data class Delete(val theme: Theme) : ThemeDialogState()
    data class Import(val json: String = "") : ThemeDialogState()
    data class Export(val theme: Theme) : ThemeDialogState()
}

@HiltViewModel
class ThemesViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val themeRepository: ThemeRepository,
    private val themeManager: ThemeManager,
    private val themePreference: ThemePreference
) : ViewModel() {

    private val _allThemes = MutableStateFlow<List<Theme>>(emptyList())
    val allThemes: StateFlow<List<Theme>> = _allThemes.asStateFlow()

    private val _builtInThemes = MutableStateFlow<List<Theme>>(emptyList())
    val builtInThemes: StateFlow<List<Theme>> = _builtInThemes.asStateFlow()

    private val _customThemes = MutableStateFlow<List<Theme>>(emptyList())
    val customThemes: StateFlow<List<Theme>> = _customThemes.asStateFlow()

    private val _currentThemeId = MutableStateFlow("")
    val currentThemeId: StateFlow<String> = _currentThemeId.asStateFlow()

    private val _dialogState = MutableStateFlow<ThemeDialogState>(ThemeDialogState.Hidden)
    val dialogState: StateFlow<ThemeDialogState> = _dialogState.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        _currentThemeId.value = themePreference.currentThemeId.value

        themeRepository.getAllThemes()
            .onEach { _allThemes.value = it }
            .launchIn(viewModelScope)

        themeRepository.getBuiltInThemes()
            .onEach {
                _builtInThemes.value = it
                _isLoading.value = false
            }
            .launchIn(viewModelScope)

        themeRepository.getCustomThemes()
            .onEach { _customThemes.value = it }
            .launchIn(viewModelScope)
    }

    fun applyTheme(theme: Theme) {
        themePreference.setTheme(theme)
        themeManager.applyTheme(theme)
        _currentThemeId.value = theme.id
    }

    fun showDeleteDialog(theme: Theme) {
        _dialogState.value = ThemeDialogState.Delete(theme)
    }

    fun showExportDialog(theme: Theme) {
        _dialogState.value = ThemeDialogState.Export(theme)
    }

    fun showImportDialog() {
        _dialogState.value = ThemeDialogState.Import()
    }

    fun hideDialog() {
        _dialogState.value = ThemeDialogState.Hidden
    }

    fun deleteTheme(theme: Theme) {
        viewModelScope.launch {
            themeRepository.deleteTheme(theme)
            hideDialog()
        }
    }

    fun exportTheme(theme: Theme) {
        val map = mapOf(
            "name" to theme.name,
            "primaryColor" to theme.primaryColor,
            "secondaryColor" to theme.secondaryColor,
            "backgroundColor" to theme.backgroundColor,
            "surfaceColor" to theme.surfaceColor,
            "textColor" to theme.textColor,
            "bubbleColorSent" to theme.bubbleColorSent,
            "bubbleColorReceived" to theme.bubbleColorReceived,
            "bubbleTextColorSent" to theme.bubbleTextColorSent,
            "bubbleTextColorReceived" to theme.bubbleTextColorReceived,
            "bubbleCornerRadius" to theme.bubbleCornerRadius,
            "bubbleElevation" to theme.bubbleElevation,
            "bubbleStyle" to theme.bubbleStyle,
            "isDarkMode" to theme.isDarkMode,
            "textColorSecondary" to theme.textColorSecondary,
            "errorColor" to theme.errorColor,
            "fontFamily" to theme.fontFamily
        )
        val json = Gson().toJson(map)
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Theme JSON", json)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "Theme exported to clipboard", Toast.LENGTH_SHORT).show()
        hideDialog()
    }

    fun importTheme(json: String) {
        try {
            val type = object : TypeToken<Map<String, Any>>() {}.type
            val map: Map<String, Any> = Gson().fromJson(json, type)

            val theme = Theme(
                name = (map["name"] as? String) ?: "Imported Theme",
                isCustom = true,
                isDefault = false,
                primaryColor = (map["primaryColor"] as? String) ?: "#35AAD8",
                secondaryColor = (map["secondaryColor"] as? String) ?: "#35AAD8",
                backgroundColor = (map["backgroundColor"] as? String) ?: "#282828",
                surfaceColor = (map["surfaceColor"] as? String) ?: "#303030",
                textColor = (map["textColor"] as? String) ?: "#FFFFFF",
                textColorSecondary = (map["textColorSecondary"] as? String) ?: "#BDBDBD",
                bubbleColorSent = (map["bubbleColorSent"] as? String) ?: "#35AAD8",
                bubbleColorReceived = (map["bubbleColorReceived"] as? String) ?: "#303030",
                bubbleTextColorSent = (map["bubbleTextColorSent"] as? String) ?: "#FFFFFF",
                bubbleTextColorReceived = (map["bubbleTextColorReceived"] as? String) ?: "#FFFFFF",
                bubbleCornerRadius = (map["bubbleCornerRadius"] as? Double)?.toInt() ?: 16,
                bubbleElevation = (map["bubbleElevation"] as? Double)?.toFloat() ?: 4f,
                bubbleStyle = (map["bubbleStyle"] as? String) ?: "ROUNDED",
                isDarkMode = (map["isDarkMode"] as? Boolean) ?: true,
                errorColor = (map["errorColor"] as? String) ?: "#B00020",
                fontFamily = (map["fontFamily"] as? String) ?: "Inter"
            )

            viewModelScope.launch {
                themeRepository.insertTheme(theme)
                applyTheme(theme)
                Toast.makeText(context, "Theme imported successfully", Toast.LENGTH_SHORT).show()
                hideDialog()
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Invalid theme data", Toast.LENGTH_SHORT).show()
        }
    }

    fun createTheme(
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
        isDarkMode: Boolean
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
            applyTheme(theme)
        }
    }

    fun isThemeActive(theme: Theme): Boolean = _currentThemeId.value == theme.id
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemesScreen(
    navController: NavController,
    viewModel: ThemesViewModel = hiltViewModel()
) {
    val builtInThemes by viewModel.builtInThemes.collectAsState()
    val customThemes by viewModel.customThemes.collectAsState()
    val dialogState by viewModel.dialogState.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val currentThemeId by viewModel.currentThemeId.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Themes") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.showImportDialog() }) {
                        Icon(
                            imageVector = Icons.Default.FileDownload,
                            contentDescription = "Import Theme",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("theme_creator") },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Create Theme",
                    tint = Color.White
                )
            }
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Loading themes...",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            val allDisplayThemes = builtInThemes + customThemes

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (builtInThemes.isNotEmpty()) {
                    item(span = { GridItemSpan(2) }) {
                        SectionHeader(title = "Built-in Themes")
                    }
                    items(builtInThemes, key = { it.id }) { theme ->
                        ThemePreviewCard(
                            theme = theme,
                            isActive = theme.id == currentThemeId,
                            isCustom = false,
                            onApply = { viewModel.applyTheme(theme) },
                            onLongPress = {},
                            onDelete = {},
                            onExport = { viewModel.showExportDialog(theme) }
                        )
                    }
                }

                if (customThemes.isNotEmpty()) {
                    item(span = { GridItemSpan(2) }) {
                        SectionHeader(title = "Custom Themes")
                    }
                    items(customThemes, key = { it.id }) { theme ->
                        ThemePreviewCard(
                            theme = theme,
                            isActive = theme.id == currentThemeId,
                            isCustom = true,
                            onApply = { viewModel.applyTheme(theme) },
                            onLongPress = { viewModel.showDeleteDialog(theme) },
                            onDelete = { viewModel.showDeleteDialog(theme) },
                            onExport = { viewModel.showExportDialog(theme) }
                        )
                    }
                }

                if (allDisplayThemes.isEmpty()) {
                    item(span = { GridItemSpan(2) }) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "No themes available",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                item(span = { GridItemSpan(2) }) {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }

    when (val state = dialogState) {
        is ThemeDialogState.Delete -> AlertDialog(
            onDismissRequest = { viewModel.hideDialog() },
            title = { Text("Delete Theme") },
            text = { Text("Are you sure you want to delete \"${state.theme.name}\"?") },
            confirmButton = {
                TextButton(onClick = { viewModel.deleteTheme(state.theme) }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.hideDialog() }) {
                    Text("Cancel")
                }
            }
        )
        is ThemeDialogState.Export -> AlertDialog(
            onDismissRequest = { viewModel.hideDialog() },
            title = { Text("Export Theme") },
            text = { Text("Copy \"${state.theme.name}\" JSON to clipboard?") },
            confirmButton = {
                TextButton(onClick = { viewModel.exportTheme(state.theme) }) {
                    Text("Export")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.hideDialog() }) {
                    Text("Cancel")
                }
            }
        )
        is ThemeDialogState.Import -> {
            var importJson by remember { mutableStateOf("") }
            AlertDialog(
                onDismissRequest = { viewModel.hideDialog() },
                title = { Text("Import Theme") },
                text = {
                    Column {
                        Text(
                            "Paste theme JSON below",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = importJson,
                            onValueChange = { importJson = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp),
                            placeholder = { Text("{\"name\": \"...\", ...}") },
                            textStyle = MaterialTheme.typography.bodySmall
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = { viewModel.importTheme(importJson) },
                        enabled = importJson.isNotBlank()
                    ) {
                        Text("Import")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.hideDialog() }) {
                        Text("Cancel")
                    }
                }
            )
        }
        ThemeDialogState.Hidden -> { /* no dialog */ }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ThemePreviewCard(
    theme: Theme,
    isActive: Boolean,
    isCustom: Boolean,
    onApply: () -> Unit,
    onLongPress: () -> Unit,
    onDelete: () -> Unit,
    onExport: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .combinedClickable(
                onClick = onApply,
                onLongClick = {
                    if (isCustom) onLongPress()
                }
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isActive) 4.dp else 2.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp)
        ) {
            ThemePreview(theme = theme)

            Spacer(modifier = Modifier.weight(1f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = theme.name,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                if (isActive) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Active",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
                if (!isActive) {
                    IconButton(
                        onClick = onExport,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FileUpload,
                            contentDescription = "Export",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
                if (isCustom && !isActive) {
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Text(
                text = if (theme.isDarkMode) "Dark" else "Light",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun ThemePreview(theme: Theme) {
    val bgColor = themeManagerHexToColor(theme.backgroundColor)
    val sentBubbleColor = themeManagerHexToColor(theme.bubbleColorSent)
    val receivedBubbleColor = themeManagerHexToColor(theme.bubbleColorReceived)
    val sentTextColor = themeManagerHexToColor(theme.bubbleTextColorSent)
    val receivedTextColor = themeManagerHexToColor(theme.bubbleTextColorReceived)

    val cornerRadius = when (theme.bubbleStyle) {
        "SHARP" -> 2.dp
        "SQUARE" -> 4.dp
        "MODERN" -> 12.dp
        else -> 16.dp
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            MessageBubble(
                text = "Hey there!",
                textColor = sentTextColor,
                backgroundColor = sentBubbleColor,
                cornerRadius = cornerRadius,
                isOutgoing = true
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            MessageBubble(
                text = "Hi!",
                textColor = receivedTextColor,
                backgroundColor = receivedBubbleColor,
                cornerRadius = cornerRadius,
                isOutgoing = false
            )
        }
    }
}

@Composable
private fun MessageBubble(
    text: String,
    textColor: Color,
    backgroundColor: Color,
    cornerRadius: androidx.compose.ui.unit.Dp,
    isOutgoing: Boolean
) {
    val shape = if (isOutgoing) {
        RoundedCornerShape(
            topStart = cornerRadius,
            topEnd = cornerRadius,
            bottomStart = cornerRadius,
            bottomEnd = 2.dp
        )
    } else {
        RoundedCornerShape(
            topStart = cornerRadius,
            topEnd = cornerRadius,
            bottomStart = 2.dp,
            bottomEnd = cornerRadius
        )
    }

    Text(
        text = text,
        color = textColor,
        style = MaterialTheme.typography.labelSmall,
        modifier = Modifier
            .clip(shape)
            .background(backgroundColor)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        maxLines = 1
    )
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 4.dp),
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun themeManagerHexToColor(hex: String): Color {
    return try {
        Color(android.graphics.Color.parseColor(hex))
    } catch (_: Exception) {
        Color.Gray
    }
}
