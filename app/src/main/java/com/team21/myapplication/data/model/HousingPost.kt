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
    val location: Location = Location(),

    val host: String = "",
    val reviews: String = "",
    val bookingDates: String = "",

    val pictures: List<String> = emptyList(),
    val tag: List<TagHousingPost> = emptyList(),
    val amenities: List<Ammenities> = emptyList(),
    val roomateProfile: RoomateProfile = RoomateProfile()

)

data class Location (
    val lat: Double = 0.0,
    val long: Double = 0.0,
)

data class RoomateProfile(
    val id: String = "",
    val name: String = "",
    val studentUserID: String = ""
)