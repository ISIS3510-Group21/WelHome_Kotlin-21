package com.team21.myapplication.data.repository

import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.team21.myapplication.data.model.HousingTag
import kotlinx.coroutines.tasks.await

class HousingTagRepository {
    private val db = FirebaseFirestore.getInstance()
    private val housingTagsCollection = db.collection("HousingTag")

    suspend fun getHousingTags(listRefs: List<DocumentReference>): List<HousingTag> {
        val housingTags = mutableListOf<HousingTag>()
        for (ref in listRefs) {
            val documentSnapshot = ref.get().await()
            val housingTag = documentSnapshot.toObject(HousingTag::class.java)
            housingTag?.let {
                housingTags.add(it)
            }
        }
        return housingTags
    }
}