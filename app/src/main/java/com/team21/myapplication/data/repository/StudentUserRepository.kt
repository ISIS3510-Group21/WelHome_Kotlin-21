package com.team21.myapplication.data.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.team21.myapplication.data.model.StudentUser
import kotlinx.coroutines.tasks.await

class StudentUserRepository {

    private val db = FirebaseFirestore.getInstance()
    private val studentUserCollection = db.collection("StudentUser")

    suspend fun getStudentUser(userId: String?): StudentUser? {
        if (userId == null) {
            Log.e("StudentUserRepo", "userId es null")
            return null
        }

        Log.d("StudentUserRepo", "Intentando obtener usuario: $userId")

        return try {
            val documentSnapshot = studentUserCollection
                .document(userId)
                .get()
                .await()

            Log.d("StudentUserRepo", "Documento existe: ${documentSnapshot.exists()}")
            Log.d("StudentUserRepo", "Datos del documento: ${documentSnapshot.data}")

            if (documentSnapshot.exists()) {
                documentSnapshot.toObject(StudentUser::class.java)
            } else {
                Log.e("StudentUserRepo", "El documento con ID $userId NO existe")
                null
            }
        } catch (e: Exception) {
            Log.e("StudentUserRepo", "Error obteniendo StudentUser con ID $userId: ${e.message}", e)
            null
        }
    }

}