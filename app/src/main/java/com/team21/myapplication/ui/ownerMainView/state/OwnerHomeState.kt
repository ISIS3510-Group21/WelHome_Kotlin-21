package com.team21.myapplication.ui.ownerMainView.state

import com.team21.myapplication.data.model.HousingPreview

data class OwnerHomeState(
    val isLoading: Boolean = true,
    val currentUserId: String? = null,
    val ownedHousings: List<HousingPreview> = emptyList(),
)