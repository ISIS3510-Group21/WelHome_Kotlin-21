package com.team21.myapplication.data.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.team21.myapplication.data.model.StudentUserProfile
import kotlinx.coroutines.tasks.await

class StudentUserProfileRepository {
    private val db = FirebaseFirestore.getInstance()
    private val studentUserCollection = db.collection("StudentUserProfile")
    //Se usa un id provisional para probar
    private val auth = "FquwUQnVlm4Lx1ej380N"
    //private val auth = FirebaseAuth.getInstance().currentUser?.uid

    suspend fun getStudentUserProfile(): StudentUserProfile? {
        val userRef = db.document("StudentUser/$auth")
        val querySnapshot = studentUserCollection
            .whereEqualTo("userId", userRef)
            .get()
            .await()
        return querySnapshot
            .documents
            .firstOrNull()?.toObject(StudentUserProfile::class.java)

    }
}