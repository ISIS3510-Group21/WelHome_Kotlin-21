package com.team21.myapplication.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import com.team21.myapplication.data.model.BasicHousingPost

class OwnerUserRepository {
    private val firestore = FirebaseFirestore.getInstance()

    suspend fun getOwnerHousingPosts(ownerId: String): Result<List<BasicHousingPost>> {
        return try {
            val snap = firestore
                .collection("OwnerUser")
                .document(ownerId)
                .collection("HousingPost")
                .get()
                .await()

            val posts = snap.documents.map { doc ->
                BasicHousingPost(
                    id = (doc.getString("id") ?: doc.id),
                    title = doc.getString("title") ?: "",
                    photoPath = doc.getString("photoPath") ?: "",
                    price = when (val p = doc.get("price")) {
                        is Number -> p.toDouble()
                        is String -> p.toDoubleOrNull() ?: 0.0
                        else -> 0.0
                    },
                    housing = doc.getString("housing") ?: ""
                )
            }

            Result.success(posts)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Guarda/actualiza el post en /OwnerUser/{ownerId}/HousingPost/{postId}
    suspend fun addOwnerHousingPost(
        ownerId: String,
        postId: String,
        post: BasicHousingPost
    ): Result<Unit> {
        return try {
            val data = hashMapOf(
                "id" to postId, // same ID as post
                "title" to post.title,
                "photoPath" to post.photoPath,
                "price" to post.price,
                "housing" to post.housing,
                //"ownerId" to ownerId
            )
            firestore
                .collection("OwnerUser")
                .document(ownerId)
                .collection("HousingPost") // si no existe, Firestore la crea
                .document(postId)
                .set(data)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Obtiene solo los IDs de las propiedades del owner
     */
    suspend fun getOwnerHousingIds(ownerId: String): Result<List<String>> {
        return try {
            val snap = firestore
                .collection("OwnerUser")
                .document(ownerId)
                .collection("HousingPost")
                .get()
                .await()

            val ids = snap.documents.mapNotNull { doc ->
                doc.getString("id") ?: doc.id
            }

            Result.success(ids)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}