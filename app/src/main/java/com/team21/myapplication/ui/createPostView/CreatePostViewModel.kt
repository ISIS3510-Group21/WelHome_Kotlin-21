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

/**
 * ViewModel para manejar la lógica de creación de posts
 *
 * El ViewModel:
 * 1. Expone estados observables (StateFlow) para que la UI reaccione
 * 2. Valida los datos antes de enviarlos al Repository
 * 3. Maneja las operaciones asíncronas con coroutines
 */
class CreatePostViewModel : ViewModel() {

    // Instancia del Repository para acceder a Firebase
    private val repository = HousingPostRepository()

    // --- ESTADOS OBSERVABLES ---

    // Estados para los campos del formulario
    private val _title = MutableStateFlow("")
    val title: StateFlow<String> = _title.asStateFlow()

    private val _description = MutableStateFlow("")
    val description: StateFlow<String> = _description.asStateFlow()

    private val _price = MutableStateFlow("")
    val price: StateFlow<String> = _price.asStateFlow()

    private val _address = MutableStateFlow("")
    val address: StateFlow<String> = _address.asStateFlow()

    // Tag seleccionado (solo uno)
    private val _selectedTagId = MutableStateFlow<String?>(null)
    val selectedTagId: StateFlow<String?> = _selectedTagId.asStateFlow()

    // --- NUEVOS ESTADOS PARA IMÁGENES ---

    // Foto principal (obligatoria)
    private val _mainPhoto = MutableStateFlow<Uri?>(null)
    val mainPhoto: StateFlow<Uri?> = _mainPhoto.asStateFlow()

    // Fotos adicionales (opcionales, máximo 9)
    private val _additionalPhotos = MutableStateFlow<List<Uri>>(emptyList())
    val additionalPhotos: StateFlow<List<Uri>> = _additionalPhotos.asStateFlow()

    // Estado de creación del post
    private val _createPostState = MutableStateFlow<CreatePostState>(CreatePostState.Idle)
    val createPostState: StateFlow<CreatePostState> = _createPostState.asStateFlow()

// --- FUNCIONES PARA ACTUALIZAR CAMPOS DE TEXTO ---

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

    fun selectTag(tagId: String) {
        _selectedTagId.value = if (_selectedTagId.value == tagId) {
            null // Si ya está seleccionado, deseleccionar
        } else {
            tagId // Seleccionar el nuevo tag
        }
    }

    // --- NUEVAS FUNCIONES PARA MANEJAR IMÁGENES ---

    /**
     * Establece la foto principal
     * Solo puede haber una foto principal
     */
    fun setMainPhoto(uri: Uri) {
        _mainPhoto.value = uri
    }

    /**
     * Elimina la foto principal
     */
    fun removeMainPhoto() {
        _mainPhoto.value = null
    }

    /**
     * Agrega fotos adicionales
     * Máximo 9 fotos adicionales
     */
    fun addAdditionalPhotos(uris: List<Uri>) {
        val currentPhotos = _additionalPhotos.value.toMutableList()

        // Calcular cuántas fotos se pueden agregar (máximo 9 en total)
        val remainingSlots = 9 - currentPhotos.size
        val photosToAdd = uris.take(remainingSlots)

        currentPhotos.addAll(photosToAdd)
        _additionalPhotos.value = currentPhotos
    }

    /**
     * Elimina una foto adicional específica
     */
    fun removeAdditionalPhoto(uri: Uri) {
        val currentPhotos = _additionalPhotos.value.toMutableList()
        currentPhotos.remove(uri)
        _additionalPhotos.value = currentPhotos
    }

    /**
     * Limpia todas las fotos adicionales
     */
    fun clearAdditionalPhotos() {
        _additionalPhotos.value = emptyList()
    }

    /**
     * Obtiene el total de fotos (principal + adicionales)
     */
    fun getTotalPhotosCount(): Int {
        val mainPhotoCount = if (_mainPhoto.value != null) 1 else 0
        return mainPhotoCount + _additionalPhotos.value.size
    }

    // --- FUNCIÓN PRINCIPAL: CREAR POST ---

    fun createPost() {
        // 1. VALIDACIÓN DE DATOS

        if (_title.value.isBlank()) {
            _createPostState.value = CreatePostState.Error("El título es obligatorio")
            return
        }

        if (_title.value.length < 3) {
            _createPostState.value = CreatePostState.Error("El título debe tener al menos 3 caracteres")
            return
        }

        if (_address.value.isBlank()) {
            _createPostState.value = CreatePostState.Error("La dirección es obligatoria")
            return
        }

        val priceValue = _price.value.toDoubleOrNull()
        if (priceValue == null || priceValue <= 0) {
            _createPostState.value = CreatePostState.Error("Ingresa un precio válido mayor a 0")
            return
        }

        // NUEVA VALIDACIÓN: Foto principal obligatoria
        if (_mainPhoto.value == null) {
            _createPostState.value = CreatePostState.Error("Debes agregar una foto principal")
            return
        }

        // 2. CAMBIAR ESTADO A LOADING
        _createPostState.value = CreatePostState.Loading

        // 3. EJECUTAR OPERACIÓN ASÍNCRONA
        viewModelScope.launch {
            // Preparar todas las fotos (principal + adicionales)
            val allPhotos = mutableListOf<Uri>()
            _mainPhoto.value?.let { allPhotos.add(it) }
            allPhotos.addAll(_additionalPhotos.value)

            // Llamar al Repository con las imágenes
            val result = repository.createHousingPost(
                title = _title.value.trim(),
                description = _description.value.trim(),
                price = priceValue,
                address = _address.value.trim(),
                imageUris = allPhotos, // Pasar las URIs de las imágenes
                selectedTagId = _selectedTagId.value
            )

            // 4. ACTUALIZAR ESTADO SEGÚN RESULTADO
            _createPostState.value = if (result.isSuccess) {
                val createdPost = result.getOrNull()!!
                CreatePostState.Success(createdPost)
            } else {
                val errorMessage = result.exceptionOrNull()?.message
                    ?: "Error desconocido al crear el post"
                CreatePostState.Error(errorMessage)
            }
        }
    }

    fun resetState() {
        _createPostState.value = CreatePostState.Idle
    }

    /**
     * Limpia todos los campos del formulario incluyendo las imágenes
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
    }
}

sealed class CreatePostState {
    object Idle : CreatePostState()
    object Loading : CreatePostState()
    data class Success(val post: HousingPost) : CreatePostState()
    data class Error(val message: String) : CreatePostState()
}