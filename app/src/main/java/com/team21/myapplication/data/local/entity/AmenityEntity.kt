package com.team21.myapplication.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "amenities")
data class AmenityEntity(
    @PrimaryKey val id: String,
    val name: String,
    val iconPath: String = "" // opcional
)
