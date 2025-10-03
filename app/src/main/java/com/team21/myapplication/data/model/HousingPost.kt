package com.team21.myapplication.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.PropertyName

// Nombres de colecciones en Firebase
object CollectionNames {
    const val HOUSING_POST = "HousingPost"
    const val PICTURES = "Pictures"
    const val TAG = "Tag"
    const val AMMENITIES = "Ammenities"
    const val ROOMATE_PROFILE = "RoomateProfile"
}

// Documento HousingPost (sin subcolecciones)
data class HousingPost(
    var id: String = "",
    var creationDate: Timestamp? = null,
    var updatedAt: Timestamp? = null,
    var closureDate: Timestamp? = null,
    var address: String = "",
    var price: Double = 0.0,
    var rating: Double = 0.0,
    var title: String = "",
    var description: String = "",
    var host: String = "",
    //val reviews: String = "",
    //val bookingDates: String = "",
    var location: GeoPoint? = null,
    var status: String = "",
    var statusChange: Timestamp? = null
)

// Subcolecciones
data class Picture(
    var id: String = "",
    var name: String = "",
    var PhotoPath: String = ""
)

data class RoomateProfile(
    var id: String = "",
    var name: String = "",
    var StudentUserID: String = "",
    var roomieTags: List<String> = emptyList()
)

// Modelo compuesto con todo
data class HousingPostFull(
    var post: HousingPost = HousingPost(),
    @PropertyName("Pictures")
    var pictures: List<Picture> = emptyList(),
    @PropertyName("Tag")
    var tag: List<TagHousingPost> = emptyList(),
    @PropertyName("Ammenities")
    var ammenities: List<Ammenities> = emptyList(),
    @PropertyName("RoomateProfile")
    var roomateProfile: List<RoomateProfile> = emptyList()
)
