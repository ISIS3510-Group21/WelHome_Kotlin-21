package com.team21.myapplication.data.storage

import android.net.Uri

data class UploadResult(
    val url: String, // URL alta resolución
    val thumbnailUrl: String, // URL miniatura
    val suggestedName: String
)

interface StorageUploader {
    /**
     * Sube un archivo y devuelve URL pública + un nombre sugerido para guardarlo en BD.
     * @param desiredName nombre deseado (el proveedor puede o no respetarlo)
     */
    suspend fun upload(uri: Uri, folder: String, desiredName: String? = null): UploadResult
}