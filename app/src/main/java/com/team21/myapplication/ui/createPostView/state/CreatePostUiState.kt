package com.team21.myapplication.ui.createPostView.state

import android.net.Uri
import com.team21.myapplication.data.model.Ammenities
import com.team21.myapplication.data.model.HousingPost

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
    val operationState: CreatePostOperationState = CreatePostOperationState.Idle
)

sealed class CreatePostOperationState {
    object Idle : CreatePostOperationState()
    object Loading : CreatePostOperationState()
    data class Success(val post: HousingPost) : CreatePostOperationState()
    data class Error(val message: String) : CreatePostOperationState()
}