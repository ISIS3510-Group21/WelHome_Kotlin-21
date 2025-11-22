package com.team21.myapplication.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

object ImageCompressor {

    // Comprimir manteniendo orientación correcta
    suspend fun compressImage(
        context: Context,
        uri: Uri,
        maxWidth: Int = 1920,
        maxHeight: Int = 1440,
        quality: Int = 85
    ): Uri = withContext(Dispatchers.IO) {

        val inputStream = context.contentResolver.openInputStream(uri)
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        BitmapFactory.decodeStream(inputStream, null, options)
        inputStream?.close()

        // Calcular factor de escala
        val scaleFactor = calculateInSampleSize(options, maxWidth, maxHeight)

        // Decodificar con escala
        val inputStream2 = context.contentResolver.openInputStream(uri)
        val scaledOptions = BitmapFactory.Options().apply {
            inSampleSize = scaleFactor
        }
        var bitmap = BitmapFactory.decodeStream(inputStream2, null, scaledOptions)
        inputStream2?.close()

        // Corregir rotación EXIF si es necesario
        bitmap = rotateImageIfRequired(context, bitmap!!, uri)

        // Guardar comprimido
        val compressedFile = File(context.cacheDir, "compressed_${System.currentTimeMillis()}.jpg")
        FileOutputStream(compressedFile).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, out)
        }

        Uri.fromFile(compressedFile)
    }

    private fun calculateInSampleSize(
        options: BitmapFactory.Options,
        reqWidth: Int,
        reqHeight: Int
    ): Int {
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2

            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    private fun rotateImageIfRequired(context: Context, bitmap: Bitmap, uri: Uri): Bitmap {
        val input = context.contentResolver.openInputStream(uri) ?: return bitmap

        val exif = ExifInterface(input)
        val orientation = exif.getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_NORMAL
        )

        return when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> rotateImage(bitmap, 90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> rotateImage(bitmap, 180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> rotateImage(bitmap, 270f)
            else -> bitmap
        }
    }

    private fun rotateImage(bitmap: Bitmap, degrees: Float): Bitmap {
        val matrix = Matrix().apply { postRotate(degrees) }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }
}