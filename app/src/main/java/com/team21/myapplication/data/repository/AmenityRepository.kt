package com.team21.myapplication.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.team21.myapplication.data.model.Ammenities
import kotlinx.coroutines.tasks.await

class AmenityRepository {

    private val db = FirebaseFirestore.getInstance()
    private val amenitiesCollection = db.collection("Amenities")
    private val auth = FirebaseAuth.getInstance().currentUser?.uid

    suspend fun getAmenities(): List<Ammenities> {
        val querySnapshot = amenitiesCollection.get().await()
        return querySnapshot.documents.mapNotNull { document ->
            document.toObject(Ammenities::class.java)
        }
    }

}