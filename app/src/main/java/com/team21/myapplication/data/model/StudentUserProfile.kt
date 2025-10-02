package com.team21.myapplication.data.model

data class StudentUserProfile (
    val id: String = "",
    val userId: String = "",
    val usedTags: List<UsedTags> = emptyList(),
    val visitedHousingPosts: List<HousingPreview> = emptyList(),
    val recommendedHousingPosts: List<HousingPreview> = emptyList()
)

data class UsedTags (
    val id: String = "",
    val housing: String = ""
)