package com.team21.myapplication.ui.createPostView

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.team21.myapplication.data.model.Ammenities
import com.team21.myapplication.data.repository.AmenityRepository
import com.team21.myapplication.ui.createPostView.state.AmenitiesUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AmenitiesViewModel : ViewModel() {
    private val repository = AmenityRepository()

    private val _uiState = MutableStateFlow(AmenitiesUiState())
    val uiState: StateFlow<AmenitiesUiState> = _uiState.asStateFlow()

    // Exponer propiedades individuales para compatibilidad
    val amenitiesList = _uiState.map { it.amenitiesList }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val selectedAmenitiesIds = _uiState.map { it.selectedAmenitiesIds }.stateIn(viewModelScope, SharingStarted.Eagerly, emptySet())
    val isLoading = _uiState.map { it.isLoading }.stateIn(viewModelScope, SharingStarted.Eagerly, false)

    init {
        loadAmenities()
    }

    private fun loadAmenities() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val amenities = repository.getAmenities()
                _uiState.value = _uiState.value.copy(
                    amenitiesList = amenities,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    fun toggleAmenity(amenityId: String) {
        val current = _uiState.value.selectedAmenitiesIds.toMutableSet()
        if (current.contains(amenityId)) {
            current.remove(amenityId)
        } else {
            current.add(amenityId)
        }
        _uiState.value = _uiState.value.copy(selectedAmenitiesIds = current)
    }

    fun setInitialSelection(amenities: List<Ammenities>) {
        _uiState.value = _uiState.value.copy(
            selectedAmenitiesIds = amenities.map { it.id }.toSet()
        )
    }

    fun getSelectedAmenities(): List<Ammenities> {
        val state = _uiState.value
        return state.amenitiesList.filter { state.selectedAmenitiesIds.contains(it.id) }
    }
}