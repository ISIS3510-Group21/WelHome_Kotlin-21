package com.team21.myapplication.ui.ownerMainView.state

import com.team21.myapplication.data.model.HousingPreview

data class OwnerHomeState(
    val isLoading: Boolean = true,
    val currentUserId: String? = null,
    val ownedHousings: List<HousingPreview> = emptyList(),

    val recentlySeen: List<HousingPreview> = emptyList(),
    val defaultTop: List<HousingPreview> = emptyList(),
    val isOnline: Boolean = true
)