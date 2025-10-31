package com.team21.myapplication.data.local.files

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import java.io.File

object DraftFileStore {

    fun draftDir(context: Context, draftId: String): File =
        File(context.filesDir, "drafts/$draftId").apply { mkdirs() }

    /**
     * Copia la Uri a un archivo interno y devuelve la ruta absoluta.
     */
    fun copyIntoDraft(context: Context, draftId: String, uri: Uri, index: Int): String {
        val dir = draftDir(context, draftId)
        val out = File(dir, "img_${index}.jpg")
        context.contentResolver.openInputStream(uri).use { input ->
            out.outputStream().use { output -> input?.copyTo(output) }
        }
        return out.absolutePath
    }

    fun deleteDraftFolder(context: Context, draftId: String) {
        draftDir(context, draftId).deleteRecursively()
    }
}
