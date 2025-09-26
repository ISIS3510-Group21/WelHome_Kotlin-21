package com.team21.myapplication.data.model

import com.google.firebase.firestore.DocumentReference

data class StudentUser(
    val id: String,
    val name: String,
    val email: String,
    val phoneNumber: String,
    val photoPath: String,
    val gender: String,
    val password: String,
    val nationality: String,
    val language: String,
    val birthDate: String,
    val university: String,
    val roomieTags: List<RoomieTag>,
    val savedBookings: List<DocumentReference>,
    val savedHousing: List<DocumentReference>
)