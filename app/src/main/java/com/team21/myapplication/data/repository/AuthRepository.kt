package com.team21.myapplication.data.repository

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.team21.myapplication.data.model.OwnerUser
import com.team21.myapplication.data.model.StudentUser
import kotlinx.coroutines.tasks.await
import com.google.firebase.firestore.SetOptions

class AuthRepository {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    /**
     * Registers a new user with emial and password
     * Saves data in corresponding collection
     */
    suspend fun registerUser(
        email: String,
        password: String,
        name: String,
        phoneNumber: String,
        gender: String,
        nationality: String,
        language: String,
        birthDate: Timestamp,
        isStudent: Boolean
    ): Result<String> {
        return try {
            // 1. Create user in Firebase Auth
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val userId = authResult.user?.uid
                ?: return Result.failure(Exception("The user ID could not be obtained."))

            // 2. Save data in Firestore based on user type
            if (isStudent) {
                val studentPayload = mapOf(
                    "id" to userId,
                    "name" to name,
                    "email" to email,
                    "phoneNumber" to phoneNumber,
                    "photoPath" to "",                 // default
                    "gender" to gender,
                    "password" to "",
                    "nationality" to nationality,
                    "language" to language,
                    "birthDate" to birthDate,
                    "university" to "Triangle Institute" // default
                )

                firestore.collection("StudentUser")
                    .document(userId)
                    .set(studentPayload, SetOptions.merge())
                    .await()
            } else {
                val ownerPayload = mapOf(
                    "id" to userId,
                    "name" to name,
                    "email" to email,
                    "phoneNumber" to phoneNumber,
                    "photoPath" to "",      // default
                    "gender" to gender,
                    "password" to "",
                    "nationality" to nationality,
                    "language" to language,
                    "birthDate" to birthDate,
                    "rating" to 5
                )

                firestore.collection("OwnerUser")
                    .document(userId)
                    .set(ownerPayload, SetOptions.merge())
                    .await()
            }

            println("registered user: $userId (${if (isStudent) "Student" else "Owner"})")
            Result.success(userId)

        } catch (e: Exception) {
            println("Error registering user: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Log in with email and password
     */
    suspend fun signIn(email: String, password: String): Result<String> {
        return try {
            val authResult = auth.signInWithEmailAndPassword(email, password).await()
            val userId = authResult.user?.uid
                ?: return Result.failure(Exception("Error logging in"))

            Result.success(userId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Log out the current user
     */
    fun signOut() {
        auth.signOut()
    }

    /**
     * Get the ID of the current user
     */
    fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }

    // Modelo simple para la UI del perfil
    data class BasicProfile(
        val name: String = "",
        val email: String = "",
        val nationality: String = "",
        val phoneNumber: String = "",
    )

    // Obtiene el perfil básico desde StudentUser o OwnerUser (el que exista)
    suspend fun fetchBasicProfile(userId: String): Result<BasicProfile> {
        return try {
            // 1) Intentar StudentUser
            val studentSnap = firestore.collection("StudentUser").document(userId).get().await()
            if (studentSnap.exists()) {
                val name = studentSnap.getString("name").orEmpty()
                val email = studentSnap.getString("email").orEmpty()
                val nationality = studentSnap.getString("nationality").orEmpty()
                val phone = studentSnap.getString("phoneNumber").orEmpty()
                return Result.success(BasicProfile(name, email, nationality, phone))
            }

            // 2) Intentar OwnerUser
            val ownerSnap = firestore.collection("OwnerUser").document(userId).get().await()
            if (ownerSnap.exists()) {
                val name = ownerSnap.getString("name").orEmpty()
                val email = ownerSnap.getString("email").orEmpty()
                val nationality = ownerSnap.getString("nationality").orEmpty()
                val phone = ownerSnap.getString("phoneNumber").orEmpty()
                return Result.success(BasicProfile(name, email, nationality, phone))
            }

            Result.failure(IllegalStateException("Profile not found for $userId"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun isOwner(userId: String): Boolean {
        return try {
            firestore.collection("OwnerUser").document(userId).get().await().exists()
        } catch (e: Exception) {
            false
        }
    }



}