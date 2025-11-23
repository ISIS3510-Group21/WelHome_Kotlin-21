package com.team21.myapplication.ui.createPostView

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.team21.myapplication.data.model.HousingPost
import com.team21.myapplication.data.repository.HousingPostRepository
import com.team21.myapplication.data.repository.AuthRepository
import com.team21.myapplication.data.repository.OwnerUserRepository
import com.team21.myapplication.data.model.BasicHousingPost
import kotlinx.coroutines.flow.MutableStateFlow
import com.team21.myapplication.ui.createPostView.state.SuggestedPrice
import com.team21.myapplication.data.repository.PricingRepository
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import android.net.Uri
import com.google.firebase.Timestamp
import com.team21.myapplication.data.model.Ammenities
import com.team21.myapplication.data.model.Location
import com.team21.myapplication.data.repository.AiRepository
import com.team21.myapplication.data.repository.AiPrompts
import com.team21.myapplication.data.repository.HousingTagRepository
import kotlinx.coroutines.launch
import com.team21.myapplication.ui.createPostView.state.CreatePostOperationState
import com.team21.myapplication.ui.createPostView.state.CreatePostUiState
import android.content.Context
import com.team21.myapplication.data.local.AppDatabase
import com.team21.myapplication.data.local.entity.DraftPostEntity
import com.team21.myapplication.data.local.entity.DraftImageEntity
import com.team21.myapplication.data.local.files.DraftFileStore
import com.team21.myapplication.utils.ImageCompressor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import java.util.UUID


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
    private val authRepo = AuthRepository()
    private val ownerRepo = OwnerUserRepository()
    private val pricingRepo = PricingRepository()
    private val aiRepo = AiRepository()

    private val tagRepo = HousingTagRepository()

    // New: Tag name "cache"
    private val tagNameCache = mutableMapOf<String, String>()

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
        val current = _uiState.value

        if (current.selectedTagId == tagId) {
            _uiState.value = current.copy(selectedTagId = null, selectedTagLabel = null)
            return
        }

        viewModelScope.launch {
            val tagName = tagNameCache.getOrPut(tagId) {
                try {
                    tagRepo.getTagNameById(tagId) ?: "Home"
                } catch (e: Exception) {
                    "Home"
                }
            }
            _uiState.value = current.copy(selectedTagId = tagId, selectedTagLabel = tagName)
        }
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

    fun createPost(context: Context, onDone: (Boolean, String?) -> Unit = { _, _ -> }) {
        val state = _uiState.value

        // 1. DATA VALIDATION
        val validationError = validatePostState(state)
        if (validationError != null) {
            _uiState.value = state.copy(
                operationState = CreatePostOperationState.Error(validationError)
            )
            return
        }

        // New: Prevent duplicate calls
        if (state.operationState is CreatePostOperationState.Loading) {
            return
        }

        // 2. CHANGE STATE TO LOADING
        _uiState.value = state.copy(operationState = CreatePostOperationState.Loading)

        // corrutina principal: general en Dispatcher.IO
        viewModelScope.launch(Dispatchers.IO) {
            try {

                // corrutina anidada 1 - Default: Procesar imágenes en memoria
                val allPhotos = withContext(Dispatchers.Default) {
                    val photosToProcess = buildList {
                        state.mainPhoto?.let { add(it) }
                        addAll(state.additionalPhotos)
                    }
                    // new: Compress in parallel
                    photosToProcess.map { uri ->
                        async {
                            try {
                                ImageCompressor.compressImage(
                                    context = context,
                                    uri = uri,
                                    quality = 85
                                )
                            } catch (e: Exception) {
                                uri // If compression fails, use original
                            }
                        }
                    }.awaitAll()
                }

                if (allPhotos.isEmpty()) {
                    // Cambiar a Main para actualizar UI
                    withContext(Dispatchers.Main) {
                        _uiState.value = _uiState.value.copy(
                            operationState = CreatePostOperationState.Error("You must attach at least one image")
                        )
                    }
                    return@launch
                }

                val ownerId = authRepo.getCurrentUserId()
                if (ownerId == null) {
                    _uiState.value = _uiState.value.copy(
                        operationState = CreatePostOperationState.Error("Error obtaining user")
                    )
                    return@launch
                }

                // corrutina anidada 2 - Default: Construir objeto HousingPost
                val post = withContext(Dispatchers.Default) {
                    HousingPost(
                        id = "",   // "" para autogenerar
                        address = state.address.trim(),
                        closureDate = null,
                        creationDate = Timestamp.now(),
                        description = state.description.trim(),
                        host = ownerId,
                        location = Location(lat = 4.6097, lng = -74.0817), //todo: fix location
                        price = state.price.toDoubleOrNull() ?: 0.0,
                        rating = 5.0,
                        status = "Available",
                        statusChange = Timestamp.now(),
                        title = state.title.trim(),
                        updatedAt = Timestamp.now(),
                        thumbnail = ""
                    )
                }

                // corrutina anidada 3 - IO: Crear el post en Firebase - op mas pesada
                val res = withContext(Dispatchers.IO) {
                    repository.createHousingPost(
                        post,
                        state.selectedAmenities,
                        imageUris = allPhotos,
                        selectedTagId = state.selectedTagId
                    )
                }

                val result =  res.getOrNull()!!

                // corrutina anidada 4 - IO: Guardar referencia en el owner (si fue exitoso)
                if (res.isSuccess) {
                    withContext(Dispatchers.IO) {
                        val postId = result.postId
                        val mainUrl = result.mainPhotoUrl ?: ""

                        // Mapea a BasicHousingPost para la subcolección del owner.
                        val basicPost = BasicHousingPost(
                            id = postId,
                            housing = postId,
                            title = state.title.trim(),
                            photoPath = mainUrl,
                            price = state.price.toDoubleOrNull() ?: 0.0,
                        )
                        ownerRepo.addOwnerHousingPost(ownerId, postId, basicPost)
                    }
                }

                // UPDATE STATE ACCORDING TO RESULT
                withContext(Dispatchers.Main) {
                    _uiState.value = _uiState.value.copy(
                        operationState = if (res.isSuccess) {
                            CreatePostOperationState.Success(result.postId)
                        } else {
                            CreatePostOperationState.Error(
                                res.exceptionOrNull()?.message ?: "Unknown error while creating the post"
                            )
                        }
                    )
                    onDone(res.isSuccess, res.exceptionOrNull()?.message)
                }
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


    //Funciones de Recomendacion

    fun suggestPrice() {
        val state = _uiState.value

        if (state.selectedTagId.isNullOrBlank()) {
            _uiState.value = state.copy(
                suggestPriceError = "Please fill at least the 'Type of Housing' section to reccomend you a price"
            )
            return
        }

        // Evitar clicks repetidos mientras carga
        if (state.isSuggestingPrice) return

        _uiState.value = state.copy(
            isSuggestingPrice = true,
            suggestPriceError = null,
            suggestedPrice = null
        )

        viewModelScope.launch {
            val res = pricingRepo.suggestPrice(
                selectedTagId = state.selectedTagId!!,
                selectedAmenities = state.selectedAmenities
            )

            // Si no hay resultados, devolvemos el valor por defecto de 9,500
            val suggested = res.getOrNull() ?: SuggestedPrice(
                value = 9500.0,
                low = 9500.0,
                high = 9500.0,
                compsCount = 0,
                note = "Default recommendation."
            )

            _uiState.value = _uiState.value.copy(
                isSuggestingPrice = false,
                suggestedPrice = suggested,
                suggestPriceError = null,
                price = suggested.value.toInt().toString()
            )
        }
    }


    /** Aplicar el valor sugerido al campo price */
    fun useSuggestedPrice() {
        val s = _uiState.value.suggestedPrice ?: return
        _uiState.value = _uiState.value.copy(price = s.value.toInt().toString())
    }



    // Prompt Description
    fun generateOrRewriteDescription(
        city: String? = null,
        neighborhood: String? = null
    ) {
        val s = _uiState.value
        if (s.isDescGenerating) return

        val hasExisting = !s.description.isNullOrBlank()

        // Validación según tu regla:
        // - Si YA hay descripción: no exigimos nada más (reescritura).
        // - Si NO hay descripción: exigir mainPhoto + tag + amenities.
        val missing = mutableListOf<String>()
        if (!hasExisting) {
            // TODO: cambia "mainPhotoUri" si tu campo se llama distinto (coverImageUri / mainImageUri)
            val hasMainPhoto = s.mainPhoto != null
            val hasTag = !s.selectedTagId.isNullOrBlank()
            val hasAmenities = s.selectedAmenities.isNotEmpty()
            if (!hasMainPhoto) missing += "main photo"
            if (!hasTag) missing += "housing type"
            if (!hasAmenities) missing += "amenities"
            if (missing.isNotEmpty()) {
                _uiState.value = s.copy(
                    descError = "Please complete: ${missing.joinToString(", ")} before generating a description."
                )
                return
            }
        }

        // Limpio mensajes previos y marco loading
        _uiState.value = s.copy(
            isDescGenerating = true,
            descError = null
        )

        viewModelScope.launch {
            val prompt = if (hasExisting) {
                AiPrompts.makeRewritePrompt(s.description.orEmpty())
            } else {
                val amenitiesNames = s.selectedAmenities.map { it.name }
                // Puedes pasar city/neighborhood si los tienes en tu estado
                val housingTypeLabel = s.selectedTagLabel ?: "Home"
                AiPrompts.makeCreatePrompt(city, neighborhood, housingTypeLabel, amenitiesNames)
            }

            val result = aiRepo.generateListingDescription(prompt)
            if (result.isSuccess) {
                val generated = result.getOrNull()?.takeIf { it.isNotBlank() } ?: s.description.orEmpty()
                _uiState.value = _uiState.value.copy(
                    isDescGenerating = false,
                    previousDescription = s.description,
                    description = generated,
                    showDescReviewControls = true,
                    descError = null
                )
            } else {
                // Error de red o similar: mensaje neutro
                val msg = result.exceptionOrNull()?.message ?: "Service unavailable."
                _uiState.value = _uiState.value.copy(
                    isDescGenerating = false,
                    descError = msg // <- para ver si es "Invalid or unauthorized API key." etc.
                )
            }
        }
    }

    /** Acepta el texto generado: oculta controles y limpia previousDescription */
    fun acceptGeneratedDescription() {
        val s = _uiState.value
        _uiState.value = s.copy(
            showDescReviewControls = false,
            previousDescription = null,
            descError = null
        )
    }

    /** Rechaza cambios: restaura texto previo y oculta controles */
    fun revertGeneratedDescription() {
        val s = _uiState.value
        _uiState.value = s.copy(
            description = s.previousDescription.orEmpty(),
            showDescReviewControls = false,
            previousDescription = null,
            descError = null
        )
    }

    //Guardar borrador desde el UiState
    suspend fun saveDraftFromUiState(context: Context, ownerId: String): String {
        val s = _uiState.value
        val draftId = UUID.randomUUID().toString()

        // 1) Copiar imágenes a almacenamiento interno app
        val allUris = buildList {
            s.mainPhoto?.let { add(it) }
            addAll(s.additionalPhotos)
        }
        val imageEntities = allUris.mapIndexed { idx, uri ->
            // DraftFileStore -> copia la imagen al almacenamiento interno
            val path = DraftFileStore.copyIntoDraft(context, draftId, uri, idx)
            DraftImageEntity(
                draftId = draftId,
                isMain = (idx == 0),
                localPath = path
            )
        }

        // 2) Persistir borrador + imágenes en Room
        val db = AppDatabase.getDatabase(context)
        val draftDao = db.draftPostDao()
        val amenitiesIds = s.selectedAmenities.joinToString(",") { it.id }

        val draft = DraftPostEntity(
            id = draftId,
            title = s.title.trim(),
            description = s.description.trim(),
            price = s.price.toDoubleOrNull() ?: 0.0,
            address = s.address.trim(),
            selectedTagId = s.selectedTagId,
            amenitiesIdsCsv = amenitiesIds,
            createdAtMillis = System.currentTimeMillis(),
            ownerId = ownerId
        )
        // guarda en room
        draftDao.upsertWithImages(draft, imageEntities)
        return draftId
    }

    // Decidir create vs draft
    fun createOrDraft(
        isOnline: Boolean,
        context: Context,
        onDone: (Boolean, String?) -> Unit = { _, _ -> }
    ) {
        val state = _uiState.value
        val validationError = validatePostState(state)
        if (validationError != null) {
            _uiState.value = state.copy(
                operationState = CreatePostOperationState.Error(validationError)
            )
            return
        }

        viewModelScope.launch {
            val ownerId = authRepo.getCurrentUserId()
            if (ownerId == null) {
                _uiState.value = _uiState.value.copy(
                    operationState = CreatePostOperationState.Error("Error obtaining user")
                )
                return@launch
            }

            if (!isOnline) {
                // Guardar borrador y encolar Worker
                val draftId = saveDraftFromUiState(context, ownerId)
                com.team21.myapplication.workers.enqueueUploadDraft(context, draftId)
                _uiState.value = _uiState.value.copy(
                    operationState = CreatePostOperationState.Success("DRAFT_SAVED")
                )
                onDone(true, "draft")
            } else {
                // Flujo online: crear post
                createPost(context, onDone)
            }
        }
    }

    // === OFFLINE FALLBACKS ===

    /** Precio fijo offline: 950,000 (promedio global). Actualiza UI. */
    fun setOfflineSuggestedPrice() {
        val s = _uiState.value
        _uiState.value = s.copy(
            isSuggestingPrice = false,
            suggestedPrice = SuggestedPrice(
                value = 950000.0,
                low = 950000.0,
                high = 950000.0,
                compsCount = 0,
                note = "Offline default average."
            ),
            price = "950000"
        )
    }

    /** Descripción offline: usa plantillas de AiPrompts con amenities del usuario. */
    fun generateOfflineDescription() {
        val s = _uiState.value
        val amenitiesNames = s.selectedAmenities.map { it.name } // usa tus entidades actuales
        val offlineDesc = com.team21.myapplication.data.repository.AiPrompts
            .makeOfflineTemplate(amenitiesNames)

        _uiState.value = s.copy(
            isDescGenerating = false,
            previousDescription = s.description,
            description = offlineDesc,
            showDescReviewControls = true,
            descError = null
        )
    }




}
