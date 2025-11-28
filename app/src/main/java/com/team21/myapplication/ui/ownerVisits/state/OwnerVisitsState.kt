package com.team21.myapplication.ui.ownerVisits.state

import com.team21.myapplication.data.model.OwnerScheduledVisit

data class OwnerVisitsState(
    val visits: List<OwnerScheduledVisit> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)