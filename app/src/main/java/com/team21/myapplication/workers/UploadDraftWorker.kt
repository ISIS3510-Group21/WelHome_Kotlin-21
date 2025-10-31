package com.team21.myapplication.workers

import android.Manifest
import android.R
import android.content.Context
import android.net.Uri
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.Timestamp
import com.team21.myapplication.data.local.AppDatabase
import com.team21.myapplication.data.local.entity.DraftPostEntity
import com.team21.myapplication.data.local.files.DraftFileStore
import com.team21.myapplication.data.model.Ammenities
import com.team21.myapplication.data.model.HousingPost
import com.team21.myapplication.data.model.Location
import com.team21.myapplication.data.repository.AuthRepository
import com.team21.myapplication.data.repository.HousingPostRepository
import com.team21.myapplication.data.repository.OwnerUserRepository
import com.team21.myapplication.data.local.entity.MyPostEntity
import com.team21.myapplication.data.model.BasicHousingPost
import java.io.File

class UploadDraftWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    companion object {
        const val KEY_DRAFT_ID = "draftId"
        const val NOTIF_CHANNEL = "uploads"
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override suspend fun doWork(): Result {
        val draftId = inputData.getString(KEY_DRAFT_ID) ?: return Result.failure()

        val db = AppDatabase.getDatabase(applicationContext)
        val dao = db.draftPostDao()
        val draft: DraftPostEntity = dao.getDraftById(draftId) ?: return Result.failure()

        val images = dao.getImagesFor(draftId)
        if (images.isEmpty()) return Result.failure()

        val repo = HousingPostRepository()
        val auth = AuthRepository()
        val ownerRepo = OwnerUserRepository()
        val myPostsDao = AppDatabase.getDatabase(applicationContext).myPostsDao()
        val ownerId = auth.getCurrentUserId() ?: draft.ownerId

        // Reconstruir modelo HousingPost
        val post = HousingPost(
            id = "",
            address = draft.address,
            closureDate = null,
            creationDate = Timestamp.now(),
            description = draft.description,
            host = ownerId,
            location = Location(4.6097, -74.0817),  // TODO: obtener real
            price = draft.price,
            rating = 5.0,
            status = "Available",
            statusChange = Timestamp.now(),
            title = draft.title,
            updatedAt = Timestamp.now(),
            thumbnail = ""
        )

        // Amenities: convertir ids CSV → lista “dummy” con id y name vacío
        val amenities = draft.amenitiesIdsCsv
            .split(',')
            .filter { it.isNotBlank() }
            .map { Ammenities(id = it.trim(), name = "", iconPath = "") }

        // URIs desde rutas locales
        val uris = images.map { Uri.fromFile(File(it.localPath)) }

        val res = repo.createHousingPost(
            housingPost = post,
            selectedAmenities = amenities,
            imageUris = uris,
            selectedTagId = draft.selectedTagId
        )

        return if (res.isSuccess) {

            // 1) Tomar postId y miniatura desde el repo (tu repo ya los retorna)
            val created = res.getOrNull()
            val postId = created?.postId ?: return Result.retry()
            val mainUrl = created.mainPhotoUrl ?: ""

            // 2) Crear el PREVIEW del owner: OwnerUser/<ownerId>/HousingPost/<postId>
            val preview = BasicHousingPost(
                id = postId,
                housing = postId,
                title = draft.title,
                photoPath = mainUrl,
                price = draft.price,
                isDraft = false
            )
            ownerRepo.addOwnerHousingPost(ownerId, postId, preview)

            // 3) Upsert inmediato en cache local (Room) para que MyPosts lo vea al instante
            myPostsDao.upsert(
                MyPostEntity(
                    id = postId,
                    ownerId = ownerId,
                    title = draft.title,
                    thumbnailUrl = mainUrl,
                    price = draft.price,
                    updatedAt = System.currentTimeMillis()
                )
            )

            // 4) Limpiar borrador y archivos locales
            dao.deleteImagesFor(draftId)
            dao.deleteDraft(draftId)
            DraftFileStore.deleteDraftFolder(applicationContext, draftId)
            // 5) Notificación de éxito
            notifySuccess(draft.title)
            Result.success()
        } else {
            Result.retry() // WorkManager reintenta cuando haya red
        }
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    private fun notifySuccess(title: String) {
        val nm = NotificationManagerCompat.from(applicationContext)
        val n = NotificationCompat.Builder(applicationContext, NOTIF_CHANNEL)
            .setSmallIcon(R.drawable.stat_sys_upload_done)
            .setContentTitle("Post uploaded")
            .setContentText("“$title” was successfully uploaded!")
            .setAutoCancel(true)
            .build()
        nm.notify((System.currentTimeMillis()%1_000_000).toInt(), n)
    }
}
