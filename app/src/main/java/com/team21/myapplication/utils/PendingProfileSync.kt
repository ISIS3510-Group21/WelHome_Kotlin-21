package com.team21.myapplication.utils

import android.content.Context
import com.google.firebase.Timestamp
import com.team21.myapplication.data.model.StudentUser
import org.json.JSONObject
import java.util.Date

/**
 * Utilidad sencilla para persistir una actualización de perfil cuando no hay conectividad
 * y reintentar el envío cuando se recupere la conexión.
 * Se usa SharedPreferences para evitar dependencias adicionales.
 */
object PendingProfileSync {
    private const val PREFS_NAME = "profile_sync_prefs"
    private const val KEY_PENDING = "pending_profile"

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun save(context: Context, user: StudentUser) {
        val json = JSONObject().apply {
            put("id", user.id)
            put("name", user.name)
            put("email", user.email)
            put("phoneNumber", user.phoneNumber)
            put("gender", user.gender)
            put("nationality", user.nationality)
            put("language", user.language)
            put("university", user.university)
            put("birthDateMillis", user.birthDate.toDate().time)
            put("photoPath", user.photoPath)
        }
        prefs(context).edit().putString(KEY_PENDING, json.toString()).apply()
    }

    fun load(context: Context): StudentUser? {
        val str = prefs(context).getString(KEY_PENDING, null) ?: return null
        return try {
            val obj = JSONObject(str)
            StudentUser(
                id = obj.optString("id"),
                name = obj.optString("name"),
                email = obj.optString("email"),
                phoneNumber = obj.optString("phoneNumber"),
                gender = obj.optString("gender"),
                nationality = obj.optString("nationality"),
                language = obj.optString("language"),
                university = obj.optString("university"),
                birthDate = Timestamp(Date(obj.optLong("birthDateMillis"))),
                photoPath = obj.optString("photoPath")
            )
        } catch (e: Exception) {
            // Si hay un error de parsing descartamos la entrada corrupta.
            clear(context)
            null
        }
    }

    fun hasPending(context: Context): Boolean = prefs(context).contains(KEY_PENDING)

    fun clear(context: Context) {
        prefs(context).edit().remove(KEY_PENDING).apply()
    }
}
