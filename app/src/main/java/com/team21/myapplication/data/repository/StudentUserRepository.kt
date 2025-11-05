package com.team21.myapplication.data.repository

import android.util.Log
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import com.team21.myapplication.data.model.StudentUser
import kotlinx.coroutines.tasks.await

// TODO: remove logs - just for debugging

class StudentUserRepository {

    private val db = FirebaseFirestore.getInstance()
    private val studentUserCollection = db.collection("StudentUser")
    private val housingCollection = db.collection("HousingPost")

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


    /**
     * Crea/actualiza el doc StudentUser/{userId}/SavedHousing/{housingId}
     * con campos { id: housingId, ref: /HousingPost/{housingId} }.
     */
    suspend fun addSavedHousing(userId: String, housingId: String) {
        val ref: DocumentReference = housingCollection.document(housingId)
        val savedCol = studentUserCollection.document(userId).collection("SavedHousing")
        val payload = mapOf(
            "id" to housingId,
            "ref" to ref
        )
        savedCol.document(housingId).set(payload).await()
    }

    suspend fun findStudentIdByEmail(email: String?): String? {
        // StudentUser tiene emails únicos
        val q = studentUserCollection.whereEqualTo("email", email).limit(1).get().await()
        val doc = q.documents.firstOrNull() ?: return null
        return doc.id // este es el "id verdadero" del StudentUser
    }

    /**
     * Elimina StudentUser/{userId}/SavedHousing/{housingId}.
     */
    suspend fun removeSavedHousing(userId: String, housingId: String) {
        val savedCol = studentUserCollection.document(userId).collection("SavedHousing")
        savedCol.document(housingId).delete().await()
    }

}