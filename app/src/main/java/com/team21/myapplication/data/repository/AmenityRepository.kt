package com.team21.myapplication.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.team21.myapplication.data.model.Ammenities
import kotlinx.coroutines.tasks.await
import android.content.Context
import com.team21.myapplication.data.local.AppDatabase
import com.team21.myapplication.data.local.entity.AmenityEntity


class AmenityRepository {

    private val db = FirebaseFirestore.getInstance()
    private val amenitiesCollection = db.collection("Amenities")
    private val auth = FirebaseAuth.getInstance().currentUser?.uid

    suspend fun getAmenities(): List<Ammenities> {
        val querySnapshot = amenitiesCollection.get().await()
        return querySnapshot.documents.mapNotNull { document ->
            document.toObject(Ammenities::class.java)
        }
    }

    private fun AmenityEntity.toModel() =
       Ammenities(id = id, name = name, iconPath = iconPath)

    private fun com.team21.myapplication.data.model.Ammenities.toEntity() =
        AmenityEntity(id = id, name = name, iconPath = iconPath ?: "")

    suspend fun cacheAmenities(context: Context, list: List<com.team21.myapplication.data.model.Ammenities>) {
        val db = AppDatabase.getDatabase(context)
        db.amenityDao().upsertAll(list.map { it.toEntity() })
    }

    suspend fun getAmenitiesLocal(context: Context): List<com.team21.myapplication.data.model.Ammenities> {
        val db = AppDatabase.getDatabase(context)
        return db.amenityDao().getAll().map { it.toModel() }
    }


}