package com.team21.myapplication.ui.createPostView

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.team21.myapplication.data.model.Ammenities
import com.team21.myapplication.data.repository.AmenityRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AmenitiesViewModel : ViewModel() {
    private val repository = AmenityRepository()

    // List of all available amenities
    private val _amenitiesList = MutableStateFlow<List<Ammenities>>(emptyList())
    val amenitiesList: StateFlow<List<Ammenities>> = _amenitiesList.asStateFlow()

    // Selected amenities (IDs)
    private val _selectedAmenitiesIds = MutableStateFlow<Set<String>>(emptySet())
    val selectedAmenitiesIds: StateFlow<Set<String>> = _selectedAmenitiesIds.asStateFlow()

    // Loading state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadAmenities()
    }

    private fun loadAmenities() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val amenities = repository.getHousingPosts() // Uses the method you already have
                _amenitiesList.value = amenities
            } catch (e: Exception) {
                println("Error loading amenities: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun toggleAmenity(amenityId: String) {
        val current = _selectedAmenitiesIds.value.toMutableSet()
        if (current.contains(amenityId)) {
            current.remove(amenityId)
        } else {
            current.add(amenityId)
        }
        _selectedAmenitiesIds.value = current
    }

    fun setInitialSelection(amenities: List<Ammenities>) {
        _selectedAmenitiesIds.value = amenities.map { it.id }.toSet()
    }

    fun getSelectedAmenities(): List<Ammenities> {
        return _amenitiesList.value.filter {
            _selectedAmenitiesIds.value.contains(it.id)
        }
    }
}