package com.team21.myapplication.data.model

data class HousingTag (
    val id: String = "",
    val name: String = "",
    val iconPath: String = "",
    val housingPreview: List<HousingPreview> = emptyList()
)