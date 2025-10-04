package com.team21.myapplication.data.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.team21.myapplication.data.model.HousingPreview
import com.team21.myapplication.data.model.StudentUserProfile
import kotlinx.coroutines.tasks.await

class StudentUserProfileRepository {
    private val db = FirebaseFirestore.getInstance()
    private val studentUserCollection = db.collection("StudentUserProfile")
    //Se usa un id provisional para probar
    private val auth = "tcGfBwE5JbhMFbrnfAzHyXolT0t1"
    //private val auth = FirebaseAuth.getInstance().currentUser?.uid

    suspend fun getStudentUserProfile(): StudentUserProfile? {
        val querySnapshot = studentUserCollection
            .whereEqualTo("userId", auth)
            .get()
            .await()
        val doc = querySnapshot.documents.firstOrNull()
        val userProfile = doc?.toObject(StudentUserProfile::class.java)

        val visitedHousingPosts = doc
            ?.reference
            ?.collection("VisitedHousingPosts")
            ?.get()
            ?.await()
            ?.toObjects(HousingPreview::class.java)

        val recommendedHousingPosts = doc
            ?.reference
            ?.collection("RecommendedHousingPosts")
            ?.get()
            ?.await()
            ?.toObjects(HousingPreview::class.java)

        return userProfile?.copy(
            visitedHousingPosts = visitedHousingPosts ?: emptyList(),
            recommendedHousingPosts = recommendedHousingPosts ?: emptyList()
        )
    }
}