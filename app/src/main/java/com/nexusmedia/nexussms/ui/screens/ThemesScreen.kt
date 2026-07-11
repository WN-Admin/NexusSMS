package com.nexusmedia.nexussms.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexusmedia.nexussms.data.models.Theme
import com.nexusmedia.nexussms.data.repository.ThemeRepository
import com.nexusmedia.nexussms.features.theme.ThemeManager
import com.nexusmedia.nexussms.features.theme.ThemePreference
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class ThemeDialogState {
    object Hidden : ThemeDialogState()
    data class Delete(val theme: Theme) : ThemeDialogState()
}

@HiltViewModel
class ThemesViewModel @Inject constructor(
    private val themeRepository: ThemeRepository,
    private val themePreference: ThemePreference
) : ViewModel() {

    private val _allThemes = MutableStateFlow<List<Theme>>(emptyList())
    val allThemes: StateFlow<List<Theme>> = _allThemes.asStateFlow()

    private val _builtInThemes = MutableStateFlow<List<Theme>>(emptyList())
    val builtInThemes: StateFlow<List<Theme>> = _builtInThemes.asStateFlow()

    private val _customThemes = MutableStateFlow<List<Theme>>(emptyList())
    val customThemes: StateFlow<List<Theme>> = _customThemes.asStateFlow()

    private val _currentThemeId = MutableStateFlow<String?>(null)
    val currentThemeId: StateFlow<String?> = _currentThemeId.asStateFlow()

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
        _currentThemeId.value = theme.id
    }

    fun showDeleteDialog(theme: Theme) {
        _dialogState.value = ThemeDialogState.Delete(theme)
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

    fun isThemeActive(theme: Theme): Boolean = _currentThemeId.value == theme.id
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemesScreen(
    viewModel: ThemesViewModel = hiltViewModel()
) {
    val builtInThemes by viewModel.builtInThemes.collectAsState()
    val customThemes by viewModel.customThemes.collectAsState()
    val dialogState by viewModel.dialogState.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Themes") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("Loading themes...", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                item {
                    Text(
                        text = "Built-in Themes",
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(16.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                items(builtInThemes, key = { it.id }) { theme ->
                    ThemeItem(
                        theme = theme,
                        isActive = viewModel.isThemeActive(theme),
                        showDelete = false,
                        onApply = { viewModel.applyTheme(theme) },
                        onDelete = { viewModel.showDeleteDialog(theme) }
                    )
                }

                if (customThemes.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Custom Themes",
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(16.dp),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    items(customThemes, key = { it.id }) { theme ->
                        ThemeItem(
                            theme = theme,
                            isActive = viewModel.isThemeActive(theme),
                            showDelete = true,
                            onApply = { viewModel.applyTheme(theme) },
                            onDelete = { viewModel.showDeleteDialog(theme) }
                        )
                    }
                }

                if (builtInThemes.isEmpty() && customThemes.isEmpty()) {
                    item {
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

                item { Spacer(modifier = Modifier.height(16.dp)) }
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
        ThemeDialogState.Hidden -> { /* no dialog */ }
    }
}

@Composable
private fun ThemeItem(
    theme: Theme,
    isActive: Boolean,
    showDelete: Boolean,
    onApply: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable { onApply() },
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ColorPreview(theme = theme)
            Spacer(modifier = Modifier.width(16.dp))
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = theme.name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    if (theme.isCustom) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Custom",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Text(
                    text = if (theme.isDarkMode) "Dark theme" else "Light theme",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (isActive) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Active theme",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            if (showDelete) {
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
private fun ColorPreview(theme: Theme) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            ColorSwatch(color = Color(android.graphics.Color.parseColor(theme.primaryColor)))
            ColorSwatch(color = Color(android.graphics.Color.parseColor(theme.secondaryColor)))
            ColorSwatch(color = Color(android.graphics.Color.parseColor(theme.backgroundColor)))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            ColorSwatch(color = Color(android.graphics.Color.parseColor(theme.bubbleColorSent)))
            ColorSwatch(color = Color(android.graphics.Color.parseColor(theme.bubbleColorReceived)))
            ColorSwatch(color = Color(android.graphics.Color.parseColor(theme.textColor)))
        }
    }
}

@Composable
private fun ColorSwatch(color: Color) {
    Box(
        modifier = Modifier
            .size(16.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(color)
    )
}
