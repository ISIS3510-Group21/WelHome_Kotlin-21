package com.team21.myapplication.notification

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.TaskStackBuilder
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.team21.myapplication.R
import com.team21.myapplication.ui.main.MainActivity
import java.net.URLEncoder

private const val EXTRA_BYPASS_AUTH_GUARD = "EXTRA_BYPASS_AUTH_GUARD"

class MyMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(message: RemoteMessage) {
        // Título/cuerpo: prioriza notification; si no viene, prueba data

        val route = message.data["route"]
        val housingId = message.data["housingId"]
        val tagsCsv = message.data["tags"].orEmpty()

        val title = message.notification?.title
            ?: message.data["title"]
            ?: "Trending ahora"
        val body = message.notification?.body
            ?: message.data["body"]
            ?: "Toca para ver resultados"

        if (route == "detail" && !housingId.isNullOrBlank()) {
            showDetailNotification(this, title, body, housingId) 
            return
        }
        if (route == "filterResults" && tagsCsv.isNotBlank()) {
            showTrendingDirectToResults(this, title, body, tagsCsv)
            return
        }

        showTrending(this, title, body, tagsCsv)
    }
    @SuppressLint("MissingPermission")
    private fun showDetailNotification(
        ctx: Context,
        title: String,
        body: String,
        housingId: String
    ) {
        ensureChannel(ctx)
        val intent = Intent(ctx, com.team21.myapplication.ui.detailView.DetailHousingActivity::class.java).apply {
            putExtra(com.team21.myapplication.ui.detailView.DetailHousingActivity.EXTRA_HOUSING_ID, housingId)
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pi = PendingIntent.getActivity(
            ctx,
            housingId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(ctx, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) return

        val smallIcon = runCatching { R.drawable.ic_notification }.getOrDefault(R.mipmap.ic_launcher)

        val notif = NotificationCompat.Builder(ctx, CHANNEL_ID)
            .setSmallIcon(smallIcon)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setAutoCancel(true)
            .setContentIntent(pi)
            .build()

        try {
            NotificationManagerCompat.from(ctx).notify(housingId.hashCode(), notif)
        } catch (_: SecurityException) { /* no crashea si falta permiso */ }

    }

    private fun showTrending(ctx: Context, title: String, body: String, tagsCsv: String) {
        ensureChannel(ctx)

        // Deep link: welhome://filterresults?tags=<csv>
        val encoded = URLEncoder.encode(tagsCsv, "UTF-8")
        val uri = Uri.parse("welhome://filterresults?tags=$encoded")

        val intent = Intent(Intent.ACTION_VIEW, uri, ctx, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pi = PendingIntent.getActivity(
            ctx,
            2001,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Android 13+: si no hay permiso, salimos silenciosamente
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(ctx, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) return

        val smallIcon = try {
            // usa tu ícono si existe; si no, fallback al launcher
            R.drawable.ic_notification
        } catch (_: Exception) {
            R.mipmap.ic_launcher
        }

        val notif = NotificationCompat.Builder(ctx, CHANNEL_ID)
            .setSmallIcon(smallIcon)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setAutoCancel(true)
            .setContentIntent(pi)
            .build()

        NotificationManagerCompat.from(ctx)
            .notify((System.currentTimeMillis() % Int.MAX_VALUE).toInt(), notif)
    }

    private fun ensureChannel(ctx: Context) {
        if (Build.VERSION.SDK_INT >= 26) {
            val m = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val ch = NotificationChannel(
                CHANNEL_ID,
                "Trending Filters",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifica filtros populares"
                enableLights(true)
                lightColor = Color.BLUE
            }
            m.createNotificationChannel(ch)
        }
    }

    @SuppressLint("MissingPermission")
    private fun showTrendingDirectToResults(
        ctx: Context,
        title: String,
        body: String,
        tagsCsv: String
    ) {
        ensureChannel(ctx)

        val mainIntent = Intent(ctx, com.team21.myapplication.ui.main.MainActivity::class.java).apply {
            putExtra(EXTRA_BYPASS_AUTH_GUARD, true)
        }

        val resultIntent = Intent(
            ctx,
            com.team21.myapplication.ui.filterView.results.FilterResultsActivity::class.java
        ).apply {
            putExtra(com.team21.myapplication.ui.filterView.results.FilterResultsActivity.EXTRA_TAGS_CSV, tagsCsv)
            putExtra(EXTRA_BYPASS_AUTH_GUARD, true)
        }

        val stackBuilder = TaskStackBuilder.create(ctx).apply {
            addNextIntent(mainIntent)
            addNextIntent(resultIntent)
        }

        val pending = stackBuilder.getPendingIntent(
            3001,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(ctx, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) return

        val smallIcon = runCatching { R.drawable.ic_notification }.getOrDefault(R.mipmap.ic_launcher)

        val notif = NotificationCompat.Builder(ctx, CHANNEL_ID)
            .setSmallIcon(smallIcon)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setAutoCancel(true)
            .setContentIntent(pending)
            .build()

        try {
            NotificationManagerCompat.from(ctx)
                .notify((System.currentTimeMillis() % Int.MAX_VALUE).toInt(), notif)
        } catch (_: SecurityException) { }
    }



    companion object {
        private const val CHANNEL_ID = "trending_filters"
    }
}
