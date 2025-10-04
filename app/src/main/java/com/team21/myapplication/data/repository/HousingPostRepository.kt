package com.team21.myapplication.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.team21.myapplication.data.model.HousingPost
import kotlinx.coroutines.tasks.await
import com.google.firebase.Timestamp
import com.team21.myapplication.data.model.Location
import com.team21.myapplication.data.model.Picture
import com.team21.myapplication.data.model.RoomateProfile
import kotlin.collections.emptyList
import android.net.Uri
import android.util.Log
import java.util.UUID
import com.google.firebase.storage.FirebaseStorage
import com.team21.myapplication.data.model.Ammenities
import com.team21.myapplication.data.model.TagHousingPost

class HousingPostRepository {
    private val db = FirebaseFirestore.getInstance()
    private val housingPostsCollection = db.collection("HousingPost")

    private val auth = FirebaseAuth.getInstance().currentUser?.uid
    private val storage = FirebaseStorage.getInstance()

    suspend fun getHousingPosts(): List<HousingPost> {
        val querySnapshot = housingPostsCollection.get().await()
        return querySnapshot.documents.mapNotNull { document ->
            document.toObject(HousingPost::class.java)
        }
    }

    /**
     * Creates a new housing post in Firestore
     * UPDATED: Now uploads images to Firebase Storage
     *
     * @param title Post title
     * @param description Housing description
     * @param price Rental price
     * @param address Housing address
     * @param imageUris List of URIs of the images to upload
     * @return Result with the created HousingPost or an error
     */
    suspend fun createHousingPost(
        title: String,
        description: String,
        price: Double,
        address: String,
        imageUris: List<Uri>,
        selectedTagId: String? = null,
        selectedAmenities: List<Ammenities> = emptyList()
    ): Result<HousingPost> {
        return try {
            // 1. Generate a unique ID for the document
            val postId = housingPostsCollection.document().id

            // 2. UPLOAD IMAGES TO FIREBASE STORAGE
            println("Uploading ${imageUris.size} images...")
            val uploadedPictures = uploadImages(postId, imageUris)

            if (imageUris.isEmpty()) {
                return Result.failure(Exception("You must attach at least one image"))
            }
            if (uploadedPictures.isEmpty()) {
                return Result.failure(Exception("Image upload failed. Check Storage permissions/rules and logs."))
            }

            println("${uploadedPictures.size} images uploaded successfully")

            // 3. Get the current user
            val currentUserId = "temporary_user_${System.currentTimeMillis()}" //TODO: obtain real id

            // 4. The first uploaded image will be the thumbnail
            val thumbnailUrl = uploadedPictures.firstOrNull()?.photoPath
                ?: "https://img.freepik.com/free-photo/beautiful-interior-shot-modern-house-with-white-relaxing-walls-furniture-technology_181624-3828.jpg?semt=ais_hybrid&w=740&q=80"

            // 5. Create the HousingPost object
            val housingPost = HousingPost(
                id = postId,
                creationDate = Timestamp.now(),
                updateAt = Timestamp.now(),
                closureDate = Timestamp.now(),
                address = address,
                price = price,
                rating = 0f,
                title = title,
                description = description,
                location = Location(
                    lat = 4.6097, // Default coordinates (Bogotá)
                    lng = -74.0817
                ),
                thumbnail = thumbnailUrl, // Use the first uploaded image
                host = currentUserId,
                reviews = "",
                bookingDates = "",
                pictures = uploadedPictures, // REAL uploaded images
                tag = if (selectedTagId != null) {
                    // Generate unique UUID for this TagHousingPost
                    val tagUuid = "tag_${System.currentTimeMillis()}"

                    // Get the tag name according to the ID
                    val tagName = when(selectedTagId) {
                        "HousingTag1" -> "House"
                        "HousingTag2" -> "Apartment"
                        "HousingTag3" -> "Cabin"
                        "HousingTag11" -> "Residence"
                        else -> "Unknown"
                    }
                    listOf(
                        TagHousingPost(
                            id = tagUuid,
                            name = tagName,
                            housingTag = db.collection("HousingTag").document(selectedTagId)
                        )
                    )
                } else {
                    emptyList()
                },

                ammenities = selectedAmenities.map { amenity ->
                    Ammenities(
                        id = amenity.id,
                        name = amenity.name,
                        iconPath = amenity.iconPath
                    )
                },

                roomateProfile = RoomateProfile(
                    id = "",
                    name = "No roommates yet",
                    studentUserID = ""
                )
            )

            // 6. Save the document in Firestore
            housingPostsCollection
                .document(postId)
                .set(housingPost)
                .await()

            // 7. Update HousingTag with the HousingPreview
            if (selectedTagId != null) {
                updateHousingTagWithPreview(
                    housingTagId = selectedTagId,
                    postId = postId,
                    title = title,
                    price = price,
                    rating = 0f,
                    thumbnailUrl = thumbnailUrl
                )
            }

            println("Post created successfully with ID: $postId")

            // 8. Return success
            Result.success(housingPost)

        } catch (e: Exception) {
            println("Error creating post: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }

    /**
     * Uploads multiple images to Firebase Storage
     *
     * STRUCTURE IN STORAGE:
     * housing_posts/
     * {postId}/
     * image_0_uuid.jpg
     * image_1_uuid.jpg
     * ...
     *
     * @param postId Post ID (to organize the images)
     * @param imageUris List of image URIs
     * @return List of Picture objects with download URLs
     */
    private suspend fun uploadImages(postId: String, imageUris: List<Uri>): List<Picture> {
        val uploadedPictures = mutableListOf<Picture>()

        imageUris.forEachIndexed { index, uri ->
            try {
                println("Uploading image ${index + 1}/${imageUris.size}...")

                // 1. Create unique name for the image
                val imageName = "image_${index}_${UUID.randomUUID()}.jpg"

                // 2. Create Storage reference
                val storageRef = storage.reference
                    .child("housing_posts")
                    .child(postId)
                    .child(imageName)

                // 3. Upload the image
                val uploadTask = storageRef.putFile(uri).await()

                // 4. Get download URL
                val downloadUrl = storageRef.downloadUrl.await().toString()

                // 5. Create Picture object
                val picture = Picture(
                    photoPath = downloadUrl,
                    name = imageName
                )

                uploadedPictures.add(picture)
                println("Image ${index + 1} uploaded: $imageName")

            } catch (e: Exception) {
                println("Error uploading image ${index + 1}: ${e.message}")
                // Continue with the remaining images even if one fails
            }
        }

        return uploadedPictures
    }

    /**
     * Updates or creates a HousingTag by adding a HousingPreview
     * If the tag doesn't exist, it creates it. If it exists, it adds the preview to the list
     */
    private suspend fun updateHousingTagWithPreview(
        housingTagId: String,
        postId: String,
        title: String,
        price: Double,
        rating: Float,
        thumbnailUrl: String
    ) {
        try {
            val housingTagRef = db.collection("HousingTag").document(housingTagId)
            val housingTagDoc = housingTagRef.get().await()

            // Create the HousingPreview
            val housingPreview = mapOf(
                "id" to postId,
                "title" to title,
                "price" to price,
                "rating" to rating,
                "photoPath" to thumbnailUrl,
                "housing" to db.collection("HousingPost").document(postId),
                "reviewsCount" to 0
            )

            if (housingTagDoc.exists()) {
                // The tag exists, add the preview to the list
                println("HousingTag $housingTagId already exists, adding preview...")

                val currentPreviews = housingTagDoc.get("housingPreview") as? List<*> ?: emptyList<Any>()
                val updatedPreviews = currentPreviews.toMutableList().apply {
                    add(housingPreview)
                }

                housingTagRef.update("housingPreview", updatedPreviews).await()
                println("Preview added to existing HousingTag")

            } else {
                // The tag doesn't exist, create it
                println("Creating new HousingTag $housingTagId...")

                val tagName = when(housingTagId) {
                    "HousingTag1" -> "House"
                    "HousingTag2" -> "Apartment"
                    "HousingTag3" -> "Cabin"
                    "HousingTag11" -> "Residence"
                    else -> "Unknown"
                }

                val newHousingTag = hashMapOf(
                    "id" to housingTagId,
                    "name" to tagName,
                    "iconPath" to "/storage/icons/housingtags/${tagName.lowercase()}.png",
                    "housingPreview" to listOf(housingPreview)
                )

                housingTagRef.set(newHousingTag).await()
                println("New HousingTag created with preview")
            }

        } catch (e: Exception) {
            println("Error updating HousingTag: ${e.message}")
            e.printStackTrace()
            // Do not throw an exception so the post is created even if this fails
        }
    }

    /*
    Gets the tags of a housing post given the id of the housing post
 */
    suspend fun getTagsForHousingPost(housingPostId: String): List<TagHousingPost> {
        return try {
            Log.d("TagRepo", "Obteniendo tags para el post: $housingPostId")
            val tagsSnapshot = db.collection("HousingPost")
                .document(housingPostId)
                .collection("Tag")
                .get()
                .await()
            Log.d(
                "TagRepo",
                "Documentos encontrados en subcolección: ${tagsSnapshot.documents.size}"
            )

            val tags = tagsSnapshot.documents.mapNotNull { doc ->
                Log.d("TagRepo", "Documento Tag ID: ${doc.id}, datos: ${doc.data}")
                // Crear el objeto manualmente con el ID del documento
                val tag = doc.toObject(TagHousingPost::class.java)
                if (tag != null) {
                    TagHousingPost(
                        id = doc.id,  // Usar el ID del documento
                        name = tag.name,
                        housingTag = null
                    )
                } else {
                    null
                }
            }
            Log.d("TagRepo", "Tags deserializados: ${tags.size} - Nombres: ${tags.map { it.name }}")
            tags
        } catch (e: Exception) {
            Log.e("TagRepo", "Error obteniendo tags para el post $housingPostId: ${e.message}", e)
            emptyList()
        }
    }

}