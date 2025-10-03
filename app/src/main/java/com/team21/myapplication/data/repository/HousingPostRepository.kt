package com.team21.myapplication.data.repository

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.team21.myapplication.data.model.*
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.tasks.await

class HousingPostRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val col = db.collection(CollectionNames.HOUSING_POST)

    /** Convierte 'location' si viene como GeoPoint o como Map {lat,lng}/{latitude,longitude}. */
    private fun coerceGeoPoint(raw: Any?): GeoPoint? = when (raw) {
        is GeoPoint -> raw
        is Map<*, *> -> {
            val lat = (raw["lat"] ?: raw["latitude"]) as? Number
            val lng = (raw["lng"] ?: raw["longitude"]) as? Number
            if (lat != null && lng != null) GeoPoint(lat.toDouble(), lng.toDouble()) else null
        }
        else -> null
    }

    /** Mapea el documento base SIN usar toObject, para evitar el crash por location. */
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
            location      = coerceGeoPoint(data["location"]),
            status        = (data["status"]  as? String).orEmpty(),
            statusChange  = data["statusChange"]  as? Timestamp
        )
    }

    /** Lista simple (sin subcolecciones). */
    suspend fun getHousingPosts(): List<HousingPost> {
        val snapshot = col.get().await()
        return snapshot.documents.mapNotNull { d -> mapHousingPostBase(d) }
    }

    /** Detalle completo con subcolecciones. */
    suspend fun getHousingPostById(housingId: String): HousingPostFull? = coroutineScope {
        val docRef = col.document(housingId)
        val snap = docRef.get().await()

        val base = mapHousingPostBase(snap) ?: return@coroutineScope null

        // Subcolecciones en paralelo (puedes dejar toObject aquí)
        val picturesDef = async {
            docRef.collection(CollectionNames.PICTURES).get().await().documents.mapNotNull { d ->
                d.toObject(Picture::class.java)?.apply { id = d.id }
            }
        }
        val tagsDef = async {
            docRef.collection(CollectionNames.TAG).get().await().documents.mapNotNull { d ->
                d.toObject(TagHousingPost::class.java)?.copy(id = d.id)
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
}
