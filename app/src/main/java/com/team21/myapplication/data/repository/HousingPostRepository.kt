package com.team21.myapplication.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.team21.myapplication.data.model.HousingPost
import kotlinx.coroutines.tasks.await

class HousingPostRepository {
    private val db = FirebaseFirestore.getInstance()
    private val housingPostsCollection = db.collection("HousingPost")

    private val auth = FirebaseAuth.getInstance().currentUser?.uid

    suspend fun getHousingPosts(): List<HousingPost> {
        val querySnapshot = housingPostsCollection.get().await()
        return querySnapshot.documents.mapNotNull { document ->
            document.toObject(HousingPost::class.java)
        }
    }
}