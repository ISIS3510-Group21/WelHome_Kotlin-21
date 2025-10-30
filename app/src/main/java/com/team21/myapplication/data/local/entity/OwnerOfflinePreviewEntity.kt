package com.team21.myapplication.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.team21.myapplication.data.model.HousingPreview

@Entity(tableName = "owner_offline_previews")
data class OwnerOfflinePreviewEntity(
    @PrimaryKey val id: String,
    val title: String,
    val price: Double,
    val rating: Float,
    val reviewsCount: Float,
    val photoPath: String?,
    val housing: String?,
    val savedAt: Long = System.currentTimeMillis()
)

fun HousingPreview.toOwnerOfflineEntity(): OwnerOfflinePreviewEntity =
    OwnerOfflinePreviewEntity(
        id = this.id,
        title = this.title,
        price = this.price,
        rating = this.rating,
        reviewsCount = this.reviewsCount,
        photoPath = this.photoPath,
        housing = this.housing
    )

fun OwnerOfflinePreviewEntity.toHousingPreview(): HousingPreview =
    HousingPreview(
        id = this.id,
        title = this.title,
        price = this.price,
        rating = this.rating,
        reviewsCount = this.reviewsCount,
        photoPath =  this.photoPath
            ?: "https://www.deutschland.de/sites/default/files/styles/image_carousel_mobile/public/media/image/deutschland-wohnt.jpg",
        housing = this.housing
    )
