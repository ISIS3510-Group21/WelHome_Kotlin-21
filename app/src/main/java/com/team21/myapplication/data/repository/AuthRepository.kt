package com.team21.myapplication.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.team21.myapplication.data.model.OwnerUser
import com.team21.myapplication.data.model.StudentUser
import kotlinx.coroutines.tasks.await

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
        birthDate: String,
        isStudent: Boolean
    ): Result<String> {
        return try {
            // 1. Create user in Firebase Auth
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val userId = authResult.user?.uid
                ?: return Result.failure(Exception("The user ID could not be obtained."))

            // 2. Save data in Firestore based on user type
            if (isStudent) {
                val studentUser = StudentUser(
                    id = userId,
                    name = name,
                    email = email,
                    phoneNumber = phoneNumber,
                    photoPath = "", // Empty by default
                    gender = gender,
                    password = "", //Never save password
                    nationality = nationality,
                    language = language,
                    birthDate = birthDate,
                    university = "Traingle Insitute", //by default
                    roomieTags = emptyList(),
                    savedBookings = emptyList(),
                    savedHousing = emptyList()
                )

                firestore.collection("StudentUser")
                    .document(userId)
                    .set(studentUser)
                    .await()
            } else {
                val ownerUser = OwnerUser(
                    id = userId,
                    name = name,
                    email = email,
                    phoneNumber = phoneNumber,
                    photoPath = "", // empty by default
                    gender = gender,
                    password = "", // Never save password
                    nationality = nationality,
                    language = language,
                    birthDate = birthDate,
                    rating = 0.0f // Initial rating
                )

                firestore.collection("OwnerUser")
                    .document(userId)
                    .set(ownerUser)
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
}