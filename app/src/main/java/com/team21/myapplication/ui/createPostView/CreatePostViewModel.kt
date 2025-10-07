package com.team21.myapplication.ui.createPostView

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.team21.myapplication.data.model.HousingPost
import com.team21.myapplication.data.repository.HousingPostRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import android.net.Uri
import com.google.firebase.Timestamp
import com.team21.myapplication.data.model.Ammenities
import com.team21.myapplication.data.model.Location
import com.team21.myapplication.ui.createPostView.state.CreatePostOperationState
import com.team21.myapplication.ui.createPostView.state.CreatePostUiState

/**
 * ViewModel to manage the logic of creating posts
 *
 * El ViewModel:
 * 1. Exposes observable states (StateFlow) for the UI to react to
 * 2. Validate the data before sending it to the Repository.
 * 3. Handle asynchronous operations with coroutines
 */
class CreatePostViewModel : ViewModel() {

    // Repository instance to access Firebase
    private val repository = HousingPostRepository()

    // StateFlow with all the state
    private val _uiState = MutableStateFlow(CreatePostUiState())
    val uiState: StateFlow<CreatePostUiState> = _uiState.asStateFlow()

// --- FUNCTIONS TO UPDATE TEXT FIELDS ---

    fun updateTitle(newTitle: String) {
        _uiState.value = _uiState.value.copy(title = newTitle)
    }

    fun updateDescription(newDescription: String) {
        _uiState.value = _uiState.value.copy(description = newDescription)
    }

    fun updatePrice(newPrice: String) {
        val filtered = newPrice.filter { it.isDigit() || it == '.' }
        _uiState.value = _uiState.value.copy(price = filtered)
    }

    fun updateAddress(newAddress: String) {
        _uiState.value = _uiState.value.copy(address = newAddress)
    }

    fun selectTag(tagId: String) {
        val newTagId = if (_uiState.value.selectedTagId == tagId) null else tagId
        _uiState.value = _uiState.value.copy(selectedTagId = newTagId)
    }

    // --- FUNCTION TO HANDLE AMENITIES ---

    /**
     * Updates the list of selected amenities
     */
    fun updateSelectedAmenities(amenities: List<Ammenities>) {
        _uiState.value = _uiState.value.copy(selectedAmenities = amenities)
    }

    // --- HANDLE IMAGES ---

    /**
     * Sets the main photo
     * There can only be one main photo
     */
    fun setMainPhoto(uri: Uri) {
        _uiState.value = _uiState.value.copy(mainPhoto = uri)
    }

    /**
     * Removes the main photo
     */
    fun removeMainPhoto() {
        _uiState.value = _uiState.value.copy(mainPhoto = null)
    }

    /**
     * Adds additional photos
     * Maximum 9 additional photos
     */
    fun addAdditionalPhotos(uris: List<Uri>) {
        val currentPhotos = _uiState.value.additionalPhotos.toMutableList()

        // Calculate how many photos can be added (maximum 9 in total)
        val remainingSlots = 9 - currentPhotos.size
        val photosToAdd = uris.take(remainingSlots)

        currentPhotos.addAll(photosToAdd)
        _uiState.value = _uiState.value.copy(additionalPhotos = currentPhotos)
    }

    /**
     * Removes a specific additional photo
     */
    fun removeAdditionalPhoto(uri: Uri) {
        val currentPhotos = _uiState.value.additionalPhotos.toMutableList()
        currentPhotos.remove(uri)
        _uiState.value = _uiState.value.copy(additionalPhotos = currentPhotos)
    }

    /**
     * Clears all additional photos
     */
    fun clearAdditionalPhotos() {
        _uiState.value = _uiState.value.copy(additionalPhotos = emptyList())
    }

    /**
     * Gets the total number of photos (main + additional)
     */
    fun getTotalPhotosCount(): Int {
        val mainPhotoCount = if (_uiState.value.mainPhoto != null) 1 else 0
        return mainPhotoCount + _uiState.value.additionalPhotos.size
    }

    // --- MAIN FUNCTION: CREATE POST ---

    fun createPost(onDone: (Boolean, String?) -> Unit = { _, _ -> }) {
        val state = _uiState.value

        // 1. DATA VALIDATION
        val validationError = validatePostState(state)
        if (validationError != null) {
            _uiState.value = state.copy(
                operationState = CreatePostOperationState.Error(validationError)
            )
            return
        }

        // 2. CHANGE STATE TO LOADING
        _uiState.value = state.copy(operationState = CreatePostOperationState.Loading)

        viewModelScope.launch {
            try {

                val allPhotos = buildList {
                    state.mainPhoto?.let { add(it) }
                    addAll(state.additionalPhotos)
                }

                if (allPhotos.isEmpty()) {
                    _uiState.value = _uiState.value.copy(
                        operationState = CreatePostOperationState.Error("You must attach at least one image")
                    )
                    return@launch
                }

                val post = HousingPost(
                    id = "",   // "" para autogenerar
                    address = state.address.trim(),
                    closureDate = null,
                    creationDate = Timestamp.now(),
                    description = state.description.trim(),
                    host = "temporary_user_${System.currentTimeMillis()}", //todo: fix user
                    location = Location(lat = 4.6097, lng = -74.0817), //todo: fix location
                    price = state.price.toDoubleOrNull() ?: 0.0,
                    rating = 5.0,
                    status = "Available",
                    statusChange = Timestamp.now(),
                    title = state.title.trim(),
                    updatedAt = Timestamp.now(),
                    thumbnail = ""
                )
                val res = repository.createHousingPost(
                    post,
                    state.selectedAmenities,
                    imageUris = allPhotos,
                    selectedTagId = state.selectedTagId)

                // UPDATE STATE ACCORDING TO RESULT
                _uiState.value = _uiState.value.copy(
                    operationState = if (res.isSuccess) {
                        CreatePostOperationState.Success(res.getOrNull()!!)
                    } else {
                        CreatePostOperationState.Error(
                            res.exceptionOrNull()?.message ?: "Unknown error while creating the post"
                        )
                    }
                )
            } catch (e: Exception) {
                onDone(false, e.message)
            }
        }
    }

    fun resetState() {
        _uiState.value = _uiState.value.copy(operationState = CreatePostOperationState.Idle)
    }

    /**
     * Clears all form fields including images
     */
    fun clearForm() {
        _uiState.value = CreatePostUiState()
    }

    // Función de validación
    private fun validatePostState(state: CreatePostUiState): String? {
        return when {
            state.title.isBlank() -> "The title is mandatory"
            state.title.length < 3 -> "The title must have at least 3 characters"
            state.description.isBlank() -> "The description is mandatory"
            state.price.toDoubleOrNull() == null || state.price.toDouble() <= 0 ->
                "Enter a valid price greater than 0"
            state.selectedTagId?.isBlank() ?: true -> "You must select a tag"
            state.selectedAmenities.isEmpty() -> "You must select at least one amenity"
            state.address.isBlank() -> "The address is mandatory"
            state.mainPhoto == null -> "You must add a main photo"
            else -> null
        }
    }
}
