package com.team21.myapplication.ui.ownerPostsDetail

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.team21.myapplication.data.repository.HousingPostRepository
import com.team21.myapplication.data.repository.StudentUserRepository
import com.team21.myapplication.ui.ownerPostsDetail.state.OwnerPostDetailUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class OwnerPostDetailViewModel(app: Application) : AndroidViewModel(app) {

    private val housingRepo = HousingPostRepository()
    private val studentRepo = StudentUserRepository()

    private val _state = MutableStateFlow(OwnerPostDetailUiState())
    val state: StateFlow<OwnerPostDetailUiState> = _state

    fun load(housingId: String) {
        if (housingId.isBlank()) {
            _state.value = _state.value.copy(
                isLoading = false,
                error = "Invalid housing id"
            )
            return
        }

        _state.value = _state.value.copy(isLoading = true, error = null)

        viewModelScope.launch {
            try {
                // 1) Traer el post completo (base + listas embebidas)
                val full = withContext(Dispatchers.IO) {
                    housingRepo.getHousingPostById(housingId)
                }

                if (full == null) {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = "Post not found"
                    )
                    return@launch
                }

                val post = full.post

                // Imágenes
                val images = full.pictures
                    .map { it.PhotoPath }
                    .filter { it.isNotBlank() }

                // Amenities
                val amenities = full.ammenities
                    .map { it.name }
                    .filter { it.isNotBlank() }

                // Rating + reviews
                val rating = post.rating
                val reviewsCount = post.reviews.toIntOrNull() ?: 0

                // Precio
                val priceLabel = if (post.price > 0.0) {
                    "$${post.price.toInt()}/month"
                } else {
                    ""
                }

                // 2) Sacar fotos de roommates usando StudentUserID
                val roommatePhotoUrls = withContext(Dispatchers.IO) {
                    full.roomateProfile.mapNotNull { roomie ->
                        val studentId = roomie.StudentUserID
                        if (studentId.isBlank()) return@mapNotNull null

                        // (name, photoPath)
                        val (_, photoPath) = studentRepo.getStudentBasicInfo(studentId)
                        photoPath?.takeIf { it.isNotBlank() }
                    }
                }

                // 3) Actualizar estado final
                _state.value = OwnerPostDetailUiState(
                    isLoading = false,
                    error = null,
                    title = post.title,
                    address = post.address,
                    rating = rating,
                    reviewsCount = reviewsCount,
                    pricePerMonthLabel = priceLabel,
                    images = images,
                    amenities = amenities,
                    roommatesPhotoUrls = roommatePhotoUrls
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message ?: "Error loading post"
                )
            }
        }
    }
}
