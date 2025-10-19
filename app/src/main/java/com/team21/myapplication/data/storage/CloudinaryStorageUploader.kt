package com.team21.myapplication.data.storage

import android.net.Uri
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class CloudinaryStorageUploader : StorageUploader {
    override suspend fun upload(uri: Uri, folder: String, desiredName: String?): UploadResult =
        suspendCancellableCoroutine { cont ->
            val req = MediaManager.get().upload(uri)
                .unsigned(CloudinaryConfig.UPLOAD_PRESET)
                .option("folder", folder)

            desiredName?.let { req.option("public_id", it.substringBeforeLast(".")) }

            req.callback(object : UploadCallback {
                override fun onStart(requestId: String?) {}
                override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {}
                override fun onSuccess(requestId: String?, resultData: MutableMap<Any?, Any?>?) {
                    val url = (resultData?.get("secure_url") as? String).orElse("")
                    val publicId = (resultData?.get("public_id") as? String).orElse("image_${System.currentTimeMillis()}")
                    val suggestedName = if (publicId.contains("/")) publicId.substringAfterLast("/") else publicId
                    cont.resume(UploadResult(url = url, suggestedName = "$suggestedName.jpg"))
                }
                override fun onError(requestId: String?, error: ErrorInfo?) {
                    cont.resumeWithException(IllegalStateException(error?.description ?: "Cloudinary error"))
                }
                override fun onReschedule(requestId: String?, error: ErrorInfo?) {
                    cont.resumeWithException(IllegalStateException(error?.description ?: "Cloudinary rescheduled"))
                }
            }).dispatch()
        }
}

private fun String?.orElse(fallback: String) = this ?: fallback