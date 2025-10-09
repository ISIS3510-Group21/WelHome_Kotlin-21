package com.team21.myapplication.ui.main

import com.team21.myapplication.data.model.HousingPreview

data class HomeState (
    val isLoading: Boolean = false,
    val recommendedHousings: List<HousingPreview> = emptyList(),
    val recentlySeenHousings: List<HousingPreview> = emptyList(),
    val currentUserId: String? = null,
)