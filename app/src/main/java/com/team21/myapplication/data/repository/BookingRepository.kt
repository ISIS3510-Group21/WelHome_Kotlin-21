package com.team21.myapplication.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.team21.myapplication.data.model.Booking
import kotlinx.coroutines.tasks.await

class BookingRepository {
    private val db = FirebaseFirestore.getInstance()
    private val col = db.collection("Booking")
    private val auth = FirebaseAuth.getInstance()

    suspend fun getUserBookings(): List<Booking> {
        val userId = auth.currentUser?.uid ?: return emptyList()

        val snapshot = col.whereEqualTo("user", userId).get().await()
        return snapshot.documents.mapNotNull { d -> d.toObject(Booking::class.java) }
    }
}