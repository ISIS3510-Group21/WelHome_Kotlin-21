package com.team21.myapplication.data.model

import com.google.firebase.firestore.DocumentReference

data class HousingPreview (
    val id: String = "",
    val price: Double = 0.0,
    val rating: Float = 0f,
    val title: String = "",
    val photoPath: String = "",
    val housing: String = "",
    val reviewsCount: Int = 0
)