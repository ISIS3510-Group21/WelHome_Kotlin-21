package com.team21.myapplication.data.model

import com.google.firebase.Timestamp

data class Booking (
    val id: String = "",
    val housing: String = "",
    val housingTitle: String = "",
    val thumbnail: String = "https://www.nydailynews.com/wp-content/uploads/migration/2012/09/21/4YDFQ5XGGZKTZLJSZHGXLUON2A.jpg",
    val state: String = "",
    val date: Timestamp = Timestamp.now(),
    val slot: String = "",
    val confirmedVisit: Boolean = false,
    val user: String = "",
    val userComment: String = "",
    val ownerComment: String = "",
    val rating: Float = 0f
)
