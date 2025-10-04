package com.team21.myapplication.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName

// Nombres de colecciones en Firebase
object CollectionNames {
    const val HOUSING_POST = "HousingPost"
    const val PICTURES = "Pictures"
    const val TAG = "Tag"
    const val AMMENITIES = "Ammenities"
    const val ROOMATE_PROFILE = "RoomateProfile"
}

// Documento HousingPost con campos embebidos
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
    var location: Location? = null,
    var status: String = "",
    var statusChange: Timestamp? = null,
    var thumbnail: String = "",
    var reviews: String = "",
    var bookingDates: String = "",
    var pictures: List<Picture> = emptyList(),
    var tag: List<TagHousingPost> = emptyList(),
    var ammenities: List<Ammenities> = emptyList(),
    var roomateProfile: List<RoomateProfile> = emptyList()
)

// Usaremos este POJO como mapa {lat,lng} en Firestore
data class Location(
    val lat: Double = 0.0,
    val lng: Double = 0.0,
)

// Subtcolecciones
data class Picture(
    var id: String = "",
    var name: String = "",
    @PropertyName("PhotoPath")
    var PhotoPath: String = ""
)

data class RoomateProfile(
    var id: String = "",
    var name: String = "",
    @PropertyName("StudentUserID")
    var StudentUserID: String = "",
    var roomieTags: List<String> = emptyList()
)

// Modelo compuesto para compatibilidad con subcolecciones en Firestore
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
