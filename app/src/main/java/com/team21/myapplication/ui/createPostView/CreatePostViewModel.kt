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
import com.team21.myapplication.data.model.Ammenities

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

    // OBSERVABLE STATES

    // States for the form fields
    private val _title = MutableStateFlow("")
    val title: StateFlow<String> = _title.asStateFlow()

    private val _description = MutableStateFlow("")
    val description: StateFlow<String> = _description.asStateFlow()

    private val _price = MutableStateFlow("")
    val price: StateFlow<String> = _price.asStateFlow()

    private val _address = MutableStateFlow("")
    val address: StateFlow<String> = _address.asStateFlow()

    // Selected tag (only one)
    private val _selectedTagId = MutableStateFlow<String?>(null)
    val selectedTagId: StateFlow<String?> = _selectedTagId.asStateFlow()

    // Selected amenities
    private val _selectedAmenities = MutableStateFlow<List<Ammenities>>(emptyList())
    val selectedAmenities: StateFlow<List<Ammenities>> = _selectedAmenities.asStateFlow()


    // --- NEW STATES FOR IMAGES ---

    // Main photo (mandatory)
    private val _mainPhoto = MutableStateFlow<Uri?>(null)
    val mainPhoto: StateFlow<Uri?> = _mainPhoto.asStateFlow()

    // Additional photos (optional, maximum 9)
    private val _additionalPhotos = MutableStateFlow<List<Uri>>(emptyList())
    val additionalPhotos: StateFlow<List<Uri>> = _additionalPhotos.asStateFlow()

    // Post creation state
    private val _createPostState = MutableStateFlow<CreatePostState>(CreatePostState.Idle)
    val createPostState: StateFlow<CreatePostState> = _createPostState.asStateFlow()

// --- FUNCTIONS TO UPDATE TEXT FIELDS ---

    fun updateTitle(newTitle: String) {
        _title.value = newTitle
    }

    fun updateDescription(newDescription: String) {
        _description.value = newDescription
    }

    fun updatePrice(newPrice: String) {
        val filtered = newPrice.filter { it.isDigit() || it == '.' }
        _price.value = filtered
    }

    fun updateAddress(newAddress: String) {
        _address.value = newAddress
    }

    // --- FUNCTION TO HANDLE TAGS ---
    fun selectTag(tagId: String) {
        _selectedTagId.value = if (_selectedTagId.value == tagId) {
            null // If already selected, deselect
        } else {
            tagId // Select the new tag
        }
    }

    // --- FUNCTION TO HANDLE AMENITIES ---

    /**
     * Updates the list of selected amenities
     */
    fun updateSelectedAmenities(amenities: List<Ammenities>) {
        _selectedAmenities.value = amenities
    }

    // --- NEW FUNCTIONS TO HANDLE IMAGES ---

    /**
     * Sets the main photo
     * There can only be one main photo
     */
    fun setMainPhoto(uri: Uri) {
        _mainPhoto.value = uri
    }

    /**
     * Removes the main photo
     */
    fun removeMainPhoto() {
        _mainPhoto.value = null
    }

    /**
     * Adds additional photos
     * Maximum 9 additional photos
     */
    fun addAdditionalPhotos(uris: List<Uri>) {
        val currentPhotos = _additionalPhotos.value.toMutableList()

        // Calculate how many photos can be added (maximum 9 in total)
        val remainingSlots = 9 - currentPhotos.size
        val photosToAdd = uris.take(remainingSlots)

        currentPhotos.addAll(photosToAdd)
        _additionalPhotos.value = currentPhotos
    }

    /**
     * Removes a specific additional photo
     */
    fun removeAdditionalPhoto(uri: Uri) {
        val currentPhotos = _additionalPhotos.value.toMutableList()
        currentPhotos.remove(uri)
        _additionalPhotos.value = currentPhotos
    }

    /**
     * Clears all additional photos
     */
    fun clearAdditionalPhotos() {
        _additionalPhotos.value = emptyList()
    }

    /**
     * Gets the total number of photos (main + additional)
     */
    fun getTotalPhotosCount(): Int {
        val mainPhotoCount = if (_mainPhoto.value != null) 1 else 0
        return mainPhotoCount + _additionalPhotos.value.size
    }

    // --- MAIN FUNCTION: CREATE POST ---

    fun createPost() {
        // 1. DATA VALIDATION

        if (_title.value.isBlank()) {
            _createPostState.value = CreatePostState.Error("The title is mandatory")
            return
        }

        if (_title.value.length < 3) {
            _createPostState.value = CreatePostState.Error("The title must have at least 3 characters")
            return
        }

        if (_address.value.isBlank()) {
            _createPostState.value = CreatePostState.Error("The address is mandatory")
            return
        }

        val priceValue = _price.value.toDoubleOrNull()
        if (priceValue == null || priceValue <= 0) {
            _createPostState.value = CreatePostState.Error("Enter a valid price greater than 0")
            return
        }

        // NEW VALIDATION: Main photo is mandatory
        if (_mainPhoto.value == null) {
            _createPostState.value = CreatePostState.Error("You must add a main photo")
            return
        }

        // 2. CHANGE STATE TO LOADING
        _createPostState.value = CreatePostState.Loading

        // 3. EXECUTE ASYNCHRONOUS OPERATION
        viewModelScope.launch {
            // Prepare all photos (main + additional)
            val allPhotos = mutableListOf<Uri>()
            _mainPhoto.value?.let { allPhotos.add(it) }
            allPhotos.addAll(_additionalPhotos.value)

            // Call the Repository with the images
            val result = repository.createHousingPost(
                title = _title.value.trim(),
                description = _description.value.trim(),
                price = priceValue,
                address = _address.value.trim(),
                imageUris = allPhotos, // Pass the image URIs
                selectedTagId = _selectedTagId.value,
                selectedAmenities = _selectedAmenities.value
            )

            // 4. UPDATE STATE ACCORDING TO RESULT
            _createPostState.value = if (result.isSuccess) {
                val createdPost = result.getOrNull()!!
                CreatePostState.Success(createdPost)
            } else {
                val errorMessage = result.exceptionOrNull()?.message
                    ?: "Unknown error while creating the post"
                CreatePostState.Error(errorMessage)
            }
        }
    }

    fun resetState() {
        _createPostState.value = CreatePostState.Idle
    }

    /**
     * Clears all form fields including images
     */
    fun clearForm() {
        _title.value = ""
        _description.value = ""
        _price.value = ""
        _address.value = ""
        _mainPhoto.value = null
        _additionalPhotos.value = emptyList()
        _createPostState.value = CreatePostState.Idle
        _selectedTagId.value = null
        _selectedAmenities.value = emptyList()
    }
}

sealed class CreatePostState {
    object Idle : CreatePostState()
    object Loading : CreatePostState()
    data class Success(val post: HousingPost) : CreatePostState()
    data class Error(val message: String) : CreatePostState()
}