package com.team21.myapplication.data.repository

import android.net.Uri
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.storage.FirebaseStorage
import com.team21.myapplication.data.model.*
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.tasks.await
import java.util.UUID

class HousingPostRepository {
    private val db = FirebaseFirestore.getInstance()
    private val col = db.collection(CollectionNames.HOUSING_POST)
    private val housingPostsCollection = col
    private val auth = FirebaseAuth.getInstance().currentUser?.uid
    private val storage = FirebaseStorage.getInstance()

    // ------------------------------
    // Helpers de coerción / parseo
    // ------------------------------

    /** Convierte GeoPoint o Map {lat,lng}/{latitude,longitude} → Location */
    private fun coerceLocation(raw: Any?): Location? = when (raw) {
        is GeoPoint -> Location(lat = raw.latitude, lng = raw.longitude)
        is Map<*, *> -> {
            val lat = (raw["lat"] ?: raw["latitude"]) as? Number
            val lng = (raw["lng"] ?: raw["longitude"]) as? Number
            if (lat != null && lng != null) Location(lat.toDouble(), lng.toDouble()) else null
        }
        else -> null
    }

    /** Convierte DocumentReference o String → String (path o id) */
    private fun coerceDocPath(raw: Any?): String? = when (raw) {
        is DocumentReference -> raw.path       // p.ej. "HousingTag/HousingTag1"
        is String -> raw                       // ya es path o id
        else -> null
    }

    // ------------------------------
    // Parsers de listas embebidas
    // ------------------------------

    private fun parsePicturesList(raw: Any?): List<Picture> {
        val items = raw as? List<*> ?: return emptyList()
        return items.mapNotNull { it as? Map<*, *> }.map {
            Picture(
                id = (it["id"] as? String).orEmpty(),
                name = (it["name"] as? String).orEmpty(),
                PhotoPath = (it["PhotoPath"] as? String).orEmpty()
            )
        }
    }

    /** TagHousingPost con housingTag como String (id o path) */
    private fun parseTagList(raw: Any?): List<TagHousingPost> {
        val items = raw as? List<*> ?: return emptyList()
        return items.mapNotNull { it as? Map<*, *> }.map {
            TagHousingPost(
                id = (it["id"] as? String).orEmpty(),
                name = (it["name"] as? String).orEmpty(),
                housingTag = coerceDocPath(it["housingTag"]) // ← String
            )
        }
    }

    private fun parseAmmenitiesList(raw: Any?): List<Ammenities> {
        val items = raw as? List<*> ?: return emptyList()
        return items.mapNotNull { it as? Map<*, *> }.map {
            Ammenities(
                id = (it["id"] as? String).orEmpty(),
                name = (it["name"] as? String).orEmpty(),
                iconPath = (it["iconPath"] as? String).orEmpty()
            )
        }
    }

    private fun parseRoomiesList(raw: Any?): List<RoomateProfile> {
        val items = raw as? List<*> ?: return emptyList()
        return items.mapNotNull { it as? Map<*, *> }.map {
            RoomateProfile(
                id = (it["id"] as? String).orEmpty(),
                name = (it["name"] as? String).orEmpty(),
                StudentUserID = (it["StudentUserID"] as? String).orEmpty(),
                roomieTags = (it["roomieTags"] as? List<*>)?.mapNotNull { s -> s as? String } ?: emptyList()
            )
        }
    }

    // ------------------------------
    // Mapping de documento base
    // ------------------------------

    /** Mapea el documento base SIN toObject para tolerar location y tipos mixtos. */
    private fun mapHousingPostBase(snap: DocumentSnapshot): HousingPost? {
        val data = snap.data ?: return null
        return HousingPost(
            id = snap.id,
            creationDate  = data["creationDate"]  as? Timestamp,
            updatedAt     = data["updatedAt"]     as? Timestamp,
            closureDate   = data["closureDate"]   as? Timestamp,
            address       = (data["address"] as? String).orEmpty(),
            price         = (data["price"]   as? Number)?.toDouble() ?: 0.0,
            rating        = (data["rating"]  as? Number)?.toDouble() ?: 0.0,
            title         = (data["title"]   as? String).orEmpty(),
            description   = (data["description"] as? String).orEmpty(),
            host          = (data["host"]    as? String).orEmpty(),
            location      = coerceLocation(data["location"]),
            status        = (data["status"]  as? String).orEmpty(),
            statusChange  = data["statusChange"]  as? Timestamp,
            thumbnail     = (data["thumbnail"] as? String).orEmpty(),
            reviews       = (data["reviews"] as? String).orEmpty(),
            bookingDates  = (data["bookingDates"] as? String).orEmpty(),

            // Listas embebidas si existen (compatibilidad hacia adelante)
            pictures      = parsePicturesList(data["pictures"]),
            tag           = parseTagList(data["tag"]),                 // ← housingTag como String
            ammenities    = parseAmmenitiesList(data["ammenities"]),
            roomateProfile= parseRoomiesList(data["roomateProfile"])
        )
    }

    // ------------------------------
    // Lecturas
    // ------------------------------

    /** Lista simple (usa solo root; no subcolecciones) */
    suspend fun getHousingPosts(): List<HousingPost> {
        val snapshot = col.get().await()
        return snapshot.documents.mapNotNull { d -> mapHousingPostBase(d) }
    }

    /**
     * Detalle completo.
     * - Si el doc ya trae listas embebidas → las usamos.
     * - Si no las trae → fallback a subcolecciones (datos antiguos).
     */
    suspend fun getHousingPostById(housingId: String): HousingPostFull? = coroutineScope {
        val docRef = col.document(housingId)
        val snap = docRef.get().await()
        val base = mapHousingPostBase(snap) ?: return@coroutineScope null

        val hasEmbedded =
            base.pictures.isNotEmpty() ||
                    base.tag.isNotEmpty() ||
                    base.ammenities.isNotEmpty() ||
                    base.roomateProfile.isNotEmpty()

        if (hasEmbedded) {
            return@coroutineScope HousingPostFull(
                post = base,
                pictures = base.pictures,
                tag = base.tag,
                ammenities = base.ammenities,
                roomateProfile = base.roomateProfile
            )
        }

        // Fallback: subcolecciones (compatibilidad hacia atrás)
        val picturesDef = async {
            docRef.collection(CollectionNames.PICTURES).get().await().documents.mapNotNull { d ->
                d.toObject(Picture::class.java)?.apply { id = d.id }
            }
        }
        val tagsDef = async {
            docRef.collection(CollectionNames.TAG).get().await().documents.mapNotNull { d ->
                val m = d.data ?: return@mapNotNull null
                TagHousingPost(
                    id = d.id,
                    name = (m["name"] as? String).orEmpty(),
                    housingTag = coerceDocPath(m["housingTag"]) // ← String (id o path)
                )
            }
        }
        val ammsDef = async {
            docRef.collection(CollectionNames.AMMENITIES).get().await().documents.mapNotNull { d ->
                d.toObject(Ammenities::class.java)?.copy(id = d.id)
            }
        }
        val roomiesDef = async {
            docRef.collection(CollectionNames.ROOMATE_PROFILE).get().await().documents.mapNotNull { d ->
                d.toObject(RoomateProfile::class.java)?.copy(id = d.id)
            }
        }

        HousingPostFull(
            post = base,
            pictures = picturesDef.await(),
            tag = tagsDef.await(),
            ammenities = ammsDef.await(),
            roomateProfile = roomiesDef.await()
        )
    }

    // ------------------------------
    // Create “todo en uno” (root)
    // ------------------------------

    /**
     * CREATE: respeta tu enfoque "todo en uno" (root con listas embebidas).
     * Sube imágenes a Storage y guarda thumbnail + listas.
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
            val postId = housingPostsCollection.document().id

            // Subir imágenes
            val uploadedPictures = uploadImages(postId, imageUris)
            if (imageUris.isEmpty()) return Result.failure(Exception("You must attach at least one image"))
            if (uploadedPictures.isEmpty()) return Result.failure(Exception("Image upload failed."))

            val currentUserId = auth ?: "temporary_user_${System.currentTimeMillis()}"
            val thumbnailUrl = uploadedPictures.firstOrNull()?.PhotoPath
                ?: "https://img.freepik.com/free-photo/beautiful-interior-shot-modern-house-with-white-relaxing-walls-furniture-technology_181624-3828.jpg?semt=ais_hybrid&w=740&q=80"

            // Tag embebido (si aplica) — GUARDA **String** (path o id), no DocumentReference
            val embeddedTags = if (selectedTagId != null) {
                val tagUuid = "tag_${System.currentTimeMillis()}"
                val tagName = when (selectedTagId) {
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
                        // Elige: guardar el PATH completo...
                        housingTag = db.collection("HousingTag").document(selectedTagId).path
                        // ...o solo el ID:
                        // housingTag = selectedTagId
                    )
                )
            } else emptyList()

            // Amenidades embebidas
            val embeddedAmm = selectedAmenities.map { a ->
                Ammenities(id = a.id, name = a.name, iconPath = a.iconPath)
            }

            // Roomies embebidos (placeholder)
            val embeddedRoomies = listOf(
                RoomateProfile(
                    id = "",
                    name = "No roommates yet",
                    StudentUserID = ""
                )
            )

            val housingPost = HousingPost(
                id = postId,
                creationDate = Timestamp.now(),
                updatedAt = Timestamp.now(),
                closureDate = Timestamp.now(),
                address = address,
                price = price,
                rating = 0.0,
                title = title,
                description = description,
                location = Location( // guardamos como mapa {lat,lng}
                    lat = 4.6097,  // Bogotá por defecto
                    lng = -74.0817
                ),
                thumbnail = thumbnailUrl,
                host = currentUserId,
                reviews = "",
                bookingDates = "",

                pictures = uploadedPictures,
                tag = embeddedTags,           // ← housingTag String
                ammenities = embeddedAmm,
                roomateProfile = embeddedRoomies
            )

            housingPostsCollection.document(postId).set(housingPost).await()

            // Actualizar HousingTag con preview (best effort)
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

            Result.success(housingPost)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    private suspend fun uploadImages(postId: String, imageUris: List<Uri>): List<Picture> {
        val uploadedPictures = mutableListOf<Picture>()
        imageUris.forEachIndexed { index, uri ->
            try {
                val imageName = "image_${index}_${UUID.randomUUID()}.jpg"
                val storageRef = storage.reference
                    .child("housing_posts")
                    .child(postId)
                    .child(imageName)

                storageRef.putFile(uri).await()
                val downloadUrl = storageRef.downloadUrl.await().toString()

                uploadedPictures.add(
                    Picture(
                        PhotoPath = downloadUrl,
                        name = imageName
                    )
                )
            } catch (_: Exception) {
                // continúa con las demás imágenes
            }
        }
        return uploadedPictures
    }

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

            val housingPreview = mapOf(
                "id" to postId,
                "title" to title,
                "price" to price,
                "rating" to rating,
                "photoPath" to thumbnailUrl,
                "housing" to db.collection("HousingPost").document(postId), // ref en preview
                "reviewsCount" to 0
            )

            if (housingTagDoc.exists()) {
                val currentPreviews = housingTagDoc.get("housingPreview") as? List<*> ?: emptyList<Any>()
                val updatedPreviews = currentPreviews.toMutableList().apply { add(housingPreview) }
                housingTagRef.update("housingPreview", updatedPreviews).await()
            } else {
                val tagName = when (housingTagId) {
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
            }
        } catch (_: Exception) {
            // best-effort: no rompemos el create si esto falla
        }
    }
}
