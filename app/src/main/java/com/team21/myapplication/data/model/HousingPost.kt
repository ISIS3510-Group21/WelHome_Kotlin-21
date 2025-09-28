package com.team21.myapplication.data.model

import com.google.firebase.Timestamp

data class HousingPost(
    val id: String = "",
    val creationDate: Timestamp = Timestamp.now(),
    val updateAt: Timestamp = creationDate,
    val closureDate: Timestamp = creationDate,
    val address: String = "",
    val price: Double = 0.0,
    val rating: Float = 0f,
    val title: String = "No title",
    val description: String = "",
    val hostId: String = "",
    val reviews: String = "",
    val bookingDates: String = "",
    val pictures: List<String> = emptyList(),
    val tags: List<TagHousingPost> = emptyList(),
)