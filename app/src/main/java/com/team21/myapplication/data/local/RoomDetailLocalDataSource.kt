package com.team21.myapplication.data.local

import android.content.Context
import com.team21.myapplication.local.db.AppDatabase
import com.team21.myapplication.local.db.entity.DetailSnapshotEntity
import com.team21.myapplication.ui.detailView.state.DetailHousingUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray

/**
 * [LOCAL STORAGE] [MULTITHREADING]
 * Room-backed implementation of DetailLocalDataSource.
 * All I/O happens on Dispatchers.IO.
 */
class RoomDetailLocalDataSource(
    private val appContext: Context
) : DetailLocalDataSource {

    private val db by lazy { AppDatabase.get(appContext) }

    override suspend fun getSnapshot(housingId: String): DetailHousingUiState? = withContext(Dispatchers.IO) {
        db.detailSnapshotDao().getById(housingId)?.toUi()
    }

    override suspend fun saveSnapshot(housingId: String, ui: DetailHousingUiState) = withContext(Dispatchers.IO) {
        db.detailSnapshotDao().upsert(ui.toEntity(housingId))
    }

    override suspend fun deleteSnapshot(housingId: String) = withContext(Dispatchers.IO) {
        db.detailSnapshotDao().deleteById(housingId)
    }

    // ---- Mappers ----
    private fun DetailSnapshotEntity.toUi(): DetailHousingUiState {
        return DetailHousingUiState(
            isLoading = false,
            error = null,
            title = title,
            rating = rating,
            pricePerMonthLabel = pricePerMonthLabel,
            address = address,
            ownerName = ownerName,
            imagesFromServer = imagesJson.toStringList(),
            amenityLabels = amenityLabelsJson.toStringList(),
            roommateNames = roommateNamesJson.toStringList(),
            roommateCount = roommateCount,
            reviewsCount = reviewsCount,
            latitude = latitude,
            longitude = longitude,
            status = status
        )
    }

    private fun DetailHousingUiState.toEntity(housingId: String): DetailSnapshotEntity {
        return DetailSnapshotEntity(
            housingId = housingId,
            title = title,
            rating = rating,
            pricePerMonthLabel = pricePerMonthLabel,
            address = address,
            ownerName = ownerName,
            imagesJson = imagesFromServer.toJsonArrayString(),
            amenityLabelsJson = amenityLabels.toJsonArrayString(),
            roommateNamesJson = roommateNames.toJsonArrayString(),
            roommateCount = roommateCount,
            reviewsCount = reviewsCount,
            latitude = latitude,
            longitude = longitude,
            status = status,
            isSaved = isSaved,
            updatedAtMs = System.currentTimeMillis()
        )
    }

    // ---- JSON helpers (store list<String> as JSON array string) ----
    private fun String.toStringList(): List<String> = try {
        val arr = JSONArray(this)
        List(arr.length()) { i -> arr.getString(i) }
    } catch (_: Exception) { emptyList() }

    private fun List<String>.toJsonArrayString(): String {
        val arr = JSONArray()
        forEach { arr.put(it) }
        return arr.toString()
    }
}
