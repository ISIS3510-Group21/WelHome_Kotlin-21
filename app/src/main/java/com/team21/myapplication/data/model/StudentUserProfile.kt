package com.team21.myapplication.data.model

import com.google.firebase.firestore.DocumentReference

data class StudentUserProfile (
    val id: String = "",
    val userId: DocumentReference? = null,
    val usedTags: List<DocumentReference> = emptyList(),
    val visitedHousingPosts: List<HousingPreview> = emptyList()
)