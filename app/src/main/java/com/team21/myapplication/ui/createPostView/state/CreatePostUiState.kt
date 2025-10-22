package com.team21.myapplication.ui.createPostView.state

import android.net.Uri
import com.team21.myapplication.data.model.Ammenities

data class CreatePostUiState(
    // Form fields
    val title: String = "",
    val description: String = "",
    val price: String = "",
    val address: String = "",
    val selectedTagId: String? = null,
    val selectedAmenities: List<Ammenities> = emptyList(),

    // Photos
    val mainPhoto: Uri? = null,
    val additionalPhotos: List<Uri> = emptyList(),

    // Operation state
    val operationState: CreatePostOperationState = CreatePostOperationState.Idle,

    // Recommendation
    val isSuggestingPrice: Boolean = false,
    val suggestedPrice: SuggestedPrice? = null,
    val suggestPriceError: String? = null,
    val pricePlaceholder: String = "Ex: 9500",

    val isDescGenerating: Boolean = false,
    val descError: String? = null,
    val previousDescription: String? = null,
    val showDescReviewControls: Boolean = false,
    val selectedTagLabel: String? = null
)

sealed class CreatePostOperationState {
    object Idle : CreatePostOperationState()
    object Loading : CreatePostOperationState()
    data class Success(val postId: String) : CreatePostOperationState()
    data class Error(val message: String) : CreatePostOperationState()
}