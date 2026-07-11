package com.nexusmedia.nexussms.ui.state

/**
 * Minimal state model for chat screen rendering.
 *
 * Phase 1A requests sealed classes for state management scaffolding.
 */
sealed class ChatUiState {
    data object Loading : ChatUiState()
    data object Idle : ChatUiState()
    data class Error(val message: String) : ChatUiState()
}

