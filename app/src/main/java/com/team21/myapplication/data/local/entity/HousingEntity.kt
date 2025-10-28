package com.team21.myapplication.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.team21.myapplication.data.model.HousingPreview

@Entity(tableName = "housings")
data class HousingEntity(
    @PrimaryKey
    val id: String,
    val price: Double,
    val rating: Float,
    val reviewsCount: Float,
    val title: String,
    val photoPath: String?,
    val housing: String?,
    var isRecommended: Boolean = false,
    var isRecentlySeen: Boolean = false
)

fun HousingPreview.toRecommendedEntity(): HousingEntity {
    return HousingEntity(
        id = this.id,
        price = this.price,
        rating = this.rating,
        reviewsCount = this.reviewsCount,
        title = this.title,
        photoPath = this.photoPath,
        housing = this.housing,
        isRecommended = true,
        isRecentlySeen = false
    )
}

fun HousingPreview.toRecentlySeenEntity(): HousingEntity {
    return HousingEntity(
        id = this.id,
        price = this.price,
        rating = this.rating,
        reviewsCount = this.reviewsCount,
        title = this.title,
        photoPath = this.photoPath,
        housing = this.housing,
        isRecommended = false,
        isRecentlySeen = true
    )
}

fun HousingEntity.toHousingPreview(): HousingPreview {
    return HousingPreview(
        id = this.id,
        price = this.price,
        rating = this.rating,
        reviewsCount = this.reviewsCount,
        title = this.title,
        photoPath = this.photoPath
            ?: "https://www.deutschland.de/sites/default/files/styles/image_carousel_mobile/public/media/image/deutschland-wohnt.jpg",
        housing = this.housing
    )
}