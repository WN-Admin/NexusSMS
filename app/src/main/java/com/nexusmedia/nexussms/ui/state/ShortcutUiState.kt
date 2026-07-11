package com.nexusmedia.nexussms.ui.state

import com.nexusmedia.nexussms.data.models.Shortcut

sealed class ShortcutUiState {
    data object Loading : ShortcutUiState()
    data class Success(val shortcuts: List<Shortcut>) : ShortcutUiState()
    data class Error(val message: String) : ShortcutUiState()
}
