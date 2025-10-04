package com.team21.myapplication.ui.createPostView.state

import com.team21.myapplication.data.model.Ammenities

data class AmenitiesUiState(
    val amenitiesList: List<Ammenities> = emptyList(),
    val selectedAmenitiesIds: Set<String> = emptySet(),
    val isLoading: Boolean = false
)