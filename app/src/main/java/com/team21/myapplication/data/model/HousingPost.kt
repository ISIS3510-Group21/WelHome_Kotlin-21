package com.team21.myapplication.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName

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
    val thumbnail: String = "https://img.freepik.com/free-photo/beautiful-interior-shot-modern-house-with-white-relaxing-walls-furniture-technology_181624-3828.jpg?semt=ais_hybrid&w=740&q=80",

    val host: String = "",
    val reviews: String = "",
    val bookingDates: String = "",

    @PropertyName("Pictures")
    val pictures: List<Picture> = emptyList(),
    @PropertyName("Tag")
    val tag: List<TagHousingPost> = emptyList(),
    @PropertyName("Ammenities")
    val ammenities: List<Ammenities> = emptyList(),
    @PropertyName("RoomateProfile")
    val roomateProfile: RoomateProfile = RoomateProfile()

)

data class Location (
    val lat: Double = 0.0,
    val lng: Double = 0.0,
)

data class RoomateProfile(
    val id: String = "",
    val name: String = "",
    @PropertyName("StudentUserID")
    val studentUserID: String = ""
)

data class Picture(
    @PropertyName("PhotoPath")
    val photoPath: String = "",
    val name: String = ""
)