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
    //private val storage = FirebaseStorage.getInstance()

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
     fun parseTagList(raw: Any?): List<TagHousingPost> {
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
     * CREATE: uploads a new housing post (with subollections
     * of 'Ammenities', 'Pictures' and 'Tag')
     */

    data class CreatePostResult(val postId: String, val mainPhotoUrl: String?)
    suspend fun createHousingPost(
        housingPost: HousingPost,          // model con id, title, address, etc.
        selectedAmenities: List<Ammenities>,
        imageUris: List<Uri>,
        selectedTagId: String? = null
    ): Result<CreatePostResult> {
        return try {
            // Si viene sin id, generamos uno antes de escribir
            val posts = db.collection(CollectionNames.HOUSING_POST)
            val docRef = if (housingPost.id.isBlank()) posts.document() else posts.document(
                housingPost.id
            )
            val finalId = docRef.id

            // subir imagenes
            if (imageUris.isEmpty()) {
                return Result.failure(Exception("You must attach at least one image"))
            }
            val uploadedPictures = uploadImages(finalId, imageUris)
            if (uploadedPictures.isEmpty()) {
                return Result.failure(Exception("Image upload failed."))
            }

            val thumbnailUrl = uploadedPictures.first().PhotoPath

            val batch = db.batch()

            // 1) Documento padre del post
            val postPayload = mapOf(
                "id" to finalId,
                "address" to housingPost.address,
                "closureDate" to housingPost.closureDate,
                "creationDate" to housingPost.creationDate,
                "description" to housingPost.description,
                "host" to housingPost.host,
                "location" to housingPost.location,
                "price" to housingPost.price,
                "rating" to housingPost.rating,
                "status" to housingPost.status,
                "statusChange" to housingPost.statusChange,
                "title" to housingPost.title,
                "updatedAt" to housingPost.updatedAt,
                "thumbnail" to thumbnailUrl,
            )
            batch.set(docRef, postPayload, com.google.firebase.firestore.SetOptions.merge())

            // 2) Subcolección Ammenities
            val sub = docRef.collection(CollectionNames.AMMENITIES)
            selectedAmenities.forEach { am ->
                val amDoc = sub.document(am.id)
                val amPayload = mapOf(
                    "id" to am.id,
                    "name" to am.name,
                    "iconPath" to am.iconPath
                )
                batch.set(amDoc, amPayload)
            }

            // 3) subcoleccion de imagenes
            val picsCol = docRef.collection(CollectionNames.PICTURES) // ya lo usas en lecturas
            uploadedPictures.forEach { pic ->
                val picId = pic.id.takeIf { it.isNotBlank() } ?: picsCol.document().id
                val picDoc = picsCol.document(picId)
                val picPayload = mapOf(
                    "id" to picId,
                    "name" to pic.name,
                    "PhotoPath" to pic.PhotoPath
                )
                batch.set(picDoc, picPayload) // adicionar
            }

            // 4) Subcolección Tag
            if (!selectedTagId.isNullOrBlank()) {
                // Lee el tag
                val masterSnap = db.collection("HousingTag").document(selectedTagId).get().await()
                val tagName = (masterSnap.get("name") as? String)
                    ?: when (selectedTagId) {          // fallback por si no existiera el doc (hardcode previo)
                        "HousingTag1" -> "House"
                        "HousingTag2" -> "Apartment"
                        "HousingTag3" -> "Cabin"
                        "HousingTag11" -> "Residence"
                        else -> "Unknown"
                    }

                val tagCol = docRef.collection(CollectionNames.TAG)
                val tagDoc = tagCol.document(selectedTagId)
                val tagId = tagDoc.id
                val tagPayload = mapOf(
                    "id" to "tag_$tagId", //id tag unico
                    "name" to tagName,
                    "housingTag" to selectedTagId // referencia al tag
                )
                batch.set(tagDoc, tagPayload)
            }
            // 5) Subcolección de PREVIEW en HousingTag/<idTag>/HousingPreview/<postId>
            if (!selectedTagId.isNullOrBlank()) {
                val previewCol = db
                    .collection("HousingTag")
                    .document(selectedTagId)
                    .collection("HousingPreview")

                // postId como id del preview
                val previewDoc = previewCol.document(finalId)
                val previewId = previewDoc.id

                val previewPayload = mapOf(
                    "id" to "Preview_$previewId",
                    "housing" to finalId,
                    "title" to housingPost.title,
                    "price" to housingPost.price,
                    "rating" to housingPost.rating,
                    "photoPath" to thumbnailUrl
                )

                batch.set(previewDoc, previewPayload) // adicionar preview
            }

            batch.commit().await()
            Result.success(CreatePostResult(postId = finalId, mainPhotoUrl = thumbnailUrl))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun uploadImages(postId: String, imageUris: List<Uri>): List<Picture> {
        val uploader = com.team21.myapplication.data.storage.Providers.storageUploader
        val folder = "housing_posts/$postId"

        val uploadedPictures = mutableListOf<Picture>()
        imageUris.forEachIndexed { index, uri ->
            try {
                val desired = "image_${index}_${UUID.randomUUID()}.jpg"
                val res = uploader.upload(uri = uri, folder = folder, desiredName = desired)

                uploadedPictures.add(
                    Picture(
                        PhotoPath =  res.url,
                        name = res.suggestedName
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

    suspend fun getTagsForPostId(postId: String): List<TagHousingPost> {
        return getHousingPostById(postId)?.tag ?: emptyList()
    }

    // obtiene los primeros N como HousingPreview (para "default list")
    suspend fun getFirstNPreviews(limit: Int = 20): List<HousingPreview> {
        val snapshot = col.limit(limit.toLong()).get().await()
        return snapshot.documents.mapNotNull { d ->
            val p = mapHousingPostBase(d)
            if (p == null) null else HousingPreview(
                id = p.id,
                price = p.price,
                rating = p.rating.toFloat(),
                reviewsCount = 0f,
                title = p.title.ifBlank { "Untitled" },
                photoPath = p.thumbnail,
                housing = p.address
            )
        }
    }

    // obtiene todos como HousingPreview (modo online)
    suspend fun getAllPreviews(): List<HousingPreview> {
        val snapshot = col.get().await()
        return snapshot.documents.mapNotNull { d ->
            val p = mapHousingPostBase(d)
            if (p == null) null else HousingPreview(
                id = p.id,
                price = p.price,
                rating = p.rating.toFloat(),
                reviewsCount = 0f,
                title = p.title.ifBlank { "Untitled" },
                photoPath = p.thumbnail,
                housing = p.address
            )
        }
    }


}
