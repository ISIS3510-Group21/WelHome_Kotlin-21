package com.team21.myapplication.data.storage

object Providers {
    /** Cambia a FirebaseStorageUploader() - Cloudinary */
    var storageUploader: StorageUploader = CloudinaryStorageUploader()
}