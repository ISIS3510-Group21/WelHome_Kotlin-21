package com.team21.myapplication.ui.visits

import com.team21.myapplication.data.model.Booking

data class VisitsState(
    val visits: List<Booking> = emptyList(),
    val isLoading: Boolean = false,
    // Map of visitId to the pending rating value
    val pendingRatings: Map<String, Float> = emptyMap(),
    val error: String? = null
)
