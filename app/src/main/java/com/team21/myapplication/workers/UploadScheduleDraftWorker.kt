package com.team21.myapplication.workers

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.team21.myapplication.R
import com.team21.myapplication.data.local.AppDatabase
import com.team21.myapplication.data.repository.BookingScheduleRepository
import com.team21.myapplication.sync.ScheduleUpdateBus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

class UploadScheduleDraftWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    companion object {
        const val KEY_DRAFT_ID = "scheduleDraftId"
    }

    private val zone: ZoneId = ZoneId.of("America/Bogota")

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val draftId = inputData.getString(KEY_DRAFT_ID) ?: return@withContext Result.failure()

        val db = AppDatabase.getDatabase(applicationContext)
        val draftDao = db.scheduleDraftDao()
        val draft = draftDao.getById(draftId) ?: return@withContext Result.failure()

        val repo = BookingScheduleRepository()

        // 1) Fecha objetivo (solo día) del draft principal
        val targetZoned = Instant.ofEpochMilli(draft.timestamp).atZone(zone)
        val targetDate: LocalDate = targetZoned.toLocalDate()

        // 2) Buscar TODOS los drafts que sean de la misma vivienda y mismo día
        val allDrafts = draftDao.getAll()
        val sameGroupDrafts = allDrafts.filter { entity ->
            entity.housingId == draft.housingId &&
                    Instant.ofEpochMilli(entity.timestamp).atZone(zone).toLocalDate() == targetDate
        }

        // 3) Construir lista de horas para todos los drafts del grupo
        val times: List<LocalTime> = sameGroupDrafts.map { entity ->
            Instant.ofEpochMilli(entity.timestamp)
                .atZone(zone)
                .toLocalTime()
                .withSecond(0)
                .withNano(0)
        }

        if (times.isEmpty()) {
            return@withContext Result.success()
        }

        // 4) Una sola llamada para TODOS los slots del día
        val result = repo.addAvailableSlots(
            housingId = draft.housingId,
            date = targetDate,
            times = times.distinct()
        )

        if (result.isSuccess) {
            // 5) borrar TODOS los drafts de ese grupo
            sameGroupDrafts.forEach { d ->
                draftDao.deleteById(d.id)
            }

            // 6) actualizar cache en memoria
            repo.addTimesToCacheOffline(
                housingId = draft.housingId,
                date = targetDate,
                times = times
            )

            // (Punto 3, notificación
            showUploadNotification(times.size)

            // 6) notificar a la app que el schedule cambió
           ScheduleUpdateBus.notifyUpdated()

            Result.success()
        } else {
            Result.retry()
        }
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    private fun UploadScheduleDraftWorker.showUploadNotification(slotCount: Int) {
        val context = applicationContext
        val channelId = "schedule_upload_channel"

        // Crear canal si es Android 8+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelName = "Visit schedule uploads"
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE)
                    as NotificationManager
            manager.createNotificationChannel(channel)
        }

        val title = "Visit schedules updated"
        val text = if (slotCount == 1) {
            "1 time slot was uploaded successfully."
        } else {
            "$slotCount time slots were uploaded successfully."
        }

        val notification = NotificationCompat.Builder(context, channelId)
            // Usa un ícono que ya tengas en tu app. Si no, puedes probar con ic_launcher_foreground
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(text)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context)
            .notify(1002, notification)
    }

}
