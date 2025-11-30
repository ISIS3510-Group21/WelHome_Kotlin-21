package com.team21.myapplication.data.repository

import android.util.Log
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.team21.myapplication.data.model.StudentUser
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.tasks.await

// TODO: remove logs - just for debugging

class StudentUserRepository {

    private val db = FirebaseFirestore.getInstance()
    private val studentUserCollection = db.collection("StudentUser")
    private val housingCollection = db.collection("HousingPost")
    private val auth = AuthRepository()

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
                documentSnapshot.toObject(StudentUser::class.java)?.copy(id = documentSnapshot.id)
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

    fun getCurrentUser(): Flow<StudentUser?> = channelFlow {
        val email = auth.getCurrentUserEmail()
        if (email == null) {
            send(null)
            close()
            return@channelFlow
        }

        val userId = findStudentIdByEmail(email)
        if (userId == null) {
            send(null)
            close()
            return@channelFlow
        }

        val listener = studentUserCollection.document(userId).addSnapshotListener { snapshot, error ->
            if (error != null) {
                return@addSnapshotListener
            }
            if (snapshot != null && snapshot.exists()) {
                val user = snapshot.toObject(StudentUser::class.java)?.copy(id = snapshot.id)
                trySend(user)
            } else {
                trySend(null)
            }
        }
        awaitClose { listener.remove() }
    }


    suspend fun updateUser(user: StudentUser): Boolean {
        if (user.id.isBlank()) {
            Log.e("StudentUserRepo", "User ID is blank. Cannot update user.")
            return false
        }
        return try {
            val data = mapOf(
                "name" to user.name,
                "phoneNumber" to user.phoneNumber,
                "gender" to user.gender,
                "nationality" to user.nationality,
                "language" to user.language,
                "university" to user.university,
                "birthDate" to user.birthDate
            )
            studentUserCollection.document(user.id).update(data).await()
            true
        } catch (e: Exception) {
            Log.e("StudentUserRepo", "Error updating user: ", e)
            false
        }
    }

    suspend fun getCurrentUserDocument(): StudentUser? = getStudentUser(findStudentIdByEmail(auth.getCurrentUserEmail()))

    /**
     * Obtiene el nombre y foto de un estudiante por su ID
     */
    suspend fun getStudentBasicInfo(studentId: String): Pair<String, String?> {
        return try {
            val doc = studentUserCollection.document(studentId).get().await()
            val name = doc.getString("name") ?: "Unknown User"
            val photoPath = doc.getString("photoPath")
            Pair(name, photoPath)
        } catch (e: Exception) {
            Log.e("StudentUserRepo", "Error obteniendo info: ${e.message}")
            Pair("Unknown User", null)
        }
    }

}
