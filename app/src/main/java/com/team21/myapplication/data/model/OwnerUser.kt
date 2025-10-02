package com.team21.myapplication.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentReference

data class OwnerUser (
    val id: String = "",
    val name: String = "",
    val creationDate: Timestamp = Timestamp.now(),
    val email: String = "",
    val phoneNumber: String = "",
    val photoPath: String = "",
    val gender: String = "",
    val password: String = "", // just local validation
    val nationality: String = "",
    val language: String = "",
    val birthDate: String = "",
    val rating: Float = 0.0f,
    val housingPost: List<DocumentReference> = emptyList()
)