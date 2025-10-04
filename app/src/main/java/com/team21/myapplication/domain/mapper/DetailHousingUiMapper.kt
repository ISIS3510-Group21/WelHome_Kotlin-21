package com.team21.myapplication.domain.mapper

import com.team21.myapplication.data.model.HousingPostFull
import com.team21.myapplication.ui.detailView.state.DetailHousingUiState
/**
 * MVVM (Presentación):
 * - Mapea tu modelo de datos (Firestore) a un UiState listo para pintar.
 * - No hace llamadas a red/bd; es puro y testeable.
 */
object DetailHousingUiMapper {

    fun toUiState(full: HousingPostFull): DetailHousingUiState {
        val post = full.post

        val images = full.pictures
            .mapNotNull { it.PhotoPath }
            .filter { it.isNotBlank() }

        val amenities = full.ammenities
            .mapNotNull { it.name }
            .filter { it.isNotBlank() }

        val roommates = full.roomateProfile
            .mapNotNull { it.name }
            .filter { it.isNotBlank() }

        val lat: Double? = post.location?.lat
        val lng: Double? = post.location?.lng

        return DetailHousingUiState(
            isLoading = false,
            error = null,
            title = post.title,
            rating = post.rating,
            pricePerMonthLabel = if (post.price > 0) "$${post.price}/month" else "",
            address = post.address,
            ownerName = post.host,
            imagesFromServer = images,
            amenityLabels = amenities,
            roommateNames = roommates,
            latitude = lat,
            longitude = lng,
            status = post.status
        )
    }
}