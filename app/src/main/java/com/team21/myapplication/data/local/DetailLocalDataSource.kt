package com.team21.myapplication.data.local

import com.team21.myapplication.ui.detailView.state.DetailHousingUiState

/**
 * [LOCAL STORAGE]
 * Abstraction over local data access for the Detail screen.
 * Useful to swap Room for another storage, or to fake in tests.
 */
interface DetailLocalDataSource {
    suspend fun getSnapshot(housingId: String): DetailHousingUiState?
    suspend fun saveSnapshot(housingId: String, ui: DetailHousingUiState)
    suspend fun deleteSnapshot(housingId: String)
}
