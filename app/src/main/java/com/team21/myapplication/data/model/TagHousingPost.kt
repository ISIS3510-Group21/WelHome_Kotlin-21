package com.team21.myapplication.data.model

import com.google.firebase.firestore.DocumentReference

data class TagHousingPost (
    val id: String = "",
    val name: String = "",
    val housingTag: String? = null
)