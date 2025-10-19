package com.team21.myapplication.data.storage

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.UUID

class FirebaseStorageUploader : StorageUploader {
    private val storage = FirebaseStorage.getInstance()

    override suspend fun upload(uri: Uri, folder: String, desiredName: String?): UploadResult {
        val name = desiredName ?: "image_${UUID.randomUUID()}.jpg"
        val ref = storage.reference.child(folder).child(name)
        ref.putFile(uri).await()
        val url = ref.downloadUrl.await().toString()
        return UploadResult(url = url, suggestedName = name)
    }
}