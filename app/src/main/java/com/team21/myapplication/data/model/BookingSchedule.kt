package com.team21.myapplication.data.model

import com.google.firebase.Timestamp

data class BookingSchedule (
    val id: String = "",
    val availableDates: Int = 0,
    val updatedAt: Timestamp = Timestamp.now(),
    val housing: String = "",
    val bookingDate: List<BookingDate> = emptyList()
)

data class BookingDate(
    val id: String = "",
    val date: Timestamp = Timestamp.now(),
    val availableSlots: Int = 0,
    val bookingSlot: List<BookingSlot> = emptyList()
)

data class BookingSlot(
    val id: String = "",
    val time: Timestamp = Timestamp.now(),
    val duration: Float = 1f,
    val availableUsers: Int = 0,
    val description: String = ""
)