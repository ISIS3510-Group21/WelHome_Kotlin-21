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
import java.util.UUID
import com.google.firebase.storage.FirebaseStorage
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
     * Crea un nuevo post de vivienda en Firestore
     * ACTUALIZADO: Ahora sube imágenes a Firebase Storage
     *
     * @param title Título del post
     * @param description Descripción de la vivienda
     * @param price Precio del arriendo
     * @param address Dirección de la vivienda
     * @param imageUris Lista de URIs de las imágenes a subir
     * @return Result con el HousingPost creado o un error
     */
    suspend fun createHousingPost(
        title: String,
        description: String,
        price: Double,
        address: String,
        imageUris: List<Uri>,
        selectedTagId: String? = null
    ): Result<HousingPost> {
        return try {
            // 1. Generar un ID único para el documento
            val postId = housingPostsCollection.document().id

            // 2. SUBIR IMÁGENES A FIREBASE STORAGE
            println("Subiendo ${imageUris.size} imágenes...")
            val uploadedPictures = uploadImages(postId, imageUris)

            if (imageUris.isEmpty()) {
                return Result.failure(Exception("Debes adjuntar al menos una imagen"))
            }
            if (uploadedPictures.isEmpty()) {
                return Result.failure(Exception("Falló la subida de imágenes. Revisa permisos/reglas de Storage y logs."))
            }

            println("${uploadedPictures.size} imágenes subidas exitosamente")

            // 3. Obtener el usuario actual
            val currentUserId = "temporary_user_${System.currentTimeMillis()}" //TODO: obtain real id

            // 4. La primera imagen subida será el thumbnail
            val thumbnailUrl = uploadedPictures.firstOrNull()?.photoPath
                ?: "https://img.freepik.com/free-photo/beautiful-interior-shot-modern-house-with-white-relaxing-walls-furniture-technology_181624-3828.jpg?semt=ais_hybrid&w=740&q=80"

            // 5. Crear el objeto HousingPost
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
                    lat = 4.6097, // Coordenadas por defecto (Bogotá)
                    lng = -74.0817
                ),
                thumbnail = thumbnailUrl, // Usar la primera imagen subida
                host = currentUserId,
                reviews = "",
                bookingDates = "",
                pictures = uploadedPictures, // IMÁGENES REALES subidas
                tag = if (selectedTagId != null) {
                    listOf(
                        TagHousingPost(
                            id = selectedTagId,
                            name = "", // El nombre no es necesario si solo guardas la referencia
                            housingTag = db.collection("HousingTag").document(selectedTagId)
                        )
                    )
                } else {
                    emptyList()
                },
                ammenities = emptyList(),
                roomateProfile = RoomateProfile(
                    id = "",
                    name = "No roommates yet",
                    studentUserID = ""
                )
            )

            // 6. Guardar el documento en Firestore
            housingPostsCollection
                .document(postId)
                .set(housingPost)
                .await()

            println("Post creado exitosamente con ID: $postId")

            // 7. Retornar éxito
            Result.success(housingPost)

        } catch (e: Exception) {
            println("Error al crear post: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }

    /**
     * Sube múltiples imágenes a Firebase Storage
     *
     * ESTRUCTURA EN STORAGE:
     * housing_posts/
     *   {postId}/
     *     image_0_uuid.jpg
     *     image_1_uuid.jpg
     *     ...
     *
     * @param postId ID del post (para organizar las imágenes)
     * @param imageUris Lista de URIs de las imágenes
     * @return Lista de objetos Picture con las URLs de descarga
     */
    private suspend fun uploadImages(postId: String, imageUris: List<Uri>): List<Picture> {
        val uploadedPictures = mutableListOf<Picture>()

        imageUris.forEachIndexed { index, uri ->
            try {
                println("Subiendo imagen ${index + 1}/${imageUris.size}...")

                // 1. Crear nombre único para la imagen
                val imageName = "image_${index}_${UUID.randomUUID()}.jpg"

                // 2. Crear referencia en Storage
                val storageRef = storage.reference
                    .child("housing_posts")
                    .child(postId)
                    .child(imageName)

                // 3. Subir la imagen
                val uploadTask = storageRef.putFile(uri).await()

                // 4. Obtener URL de descarga
                val downloadUrl = storageRef.downloadUrl.await().toString()

                // 5. Crear objeto Picture
                val picture = Picture(
                    photoPath = downloadUrl,
                    name = imageName
                )

                uploadedPictures.add(picture)
                println("Imagen ${index + 1} subida: $imageName")

            } catch (e: Exception) {
                println("Error al subir imagen ${index + 1}: ${e.message}")
                // Continuar con las demás imágenes aunque una falle
            }
        }

        return uploadedPictures
    }


}