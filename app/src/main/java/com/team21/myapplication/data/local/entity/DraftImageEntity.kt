package com.team21.myapplication.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "draft_images")
data class DraftImageEntity(
    @PrimaryKey(autoGenerate = true) val localId: Long = 0,
    val draftId: String, // FK al borrador
    val isMain: Boolean,
    val localPath: String // ruta absoluta
)
