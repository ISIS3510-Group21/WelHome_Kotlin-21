package com.team21.myapplication.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentReference

data class StudentUser(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val photoPath: String = "",
    val gender: String = "",
    val password: String= "",
    val nationality: String= "",
    val language: String= "",
    val birthDate: Timestamp = Timestamp.now(),
    val university: String= "",
    val roomieTags: List<RoomieTag> = emptyList(),
    val savedBookings: List<DocumentReference> = emptyList(),
    val savedHousing: List<DocumentReference> = emptyList()
)