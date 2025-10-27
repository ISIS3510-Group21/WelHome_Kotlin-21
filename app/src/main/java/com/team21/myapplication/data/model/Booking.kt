package com.team21.myapplication.data.model

data class Booking (
    val id: String = "",
    val housing: String = "",
    val slot: String = "",
    val confirmedVisit: Boolean = false,
    val user: String = "",
    val userComment: String = "",
    val ownerComment: String = ""
)