package com.team21.myapplication.ui.saved.state

import com.team21.myapplication.ui.filterView.state.PreviewCardUi

/**
 * UI state for the Saved Posts screen.
 */
data class SavedPostsUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val items: List<PreviewCardUi> = emptyList()
)
