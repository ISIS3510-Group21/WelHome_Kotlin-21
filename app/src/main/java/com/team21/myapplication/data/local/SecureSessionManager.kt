package com.team21.myapplication.data.local

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.io.IOException
import java.security.GeneralSecurityException

//DTO local
data class BasicProfileLocal(
    val name: String?,
    val nationality: String?,
    val phoneNumber: String?
)

class SecureSessionManager(context: Context) {

    private val masterKey: MasterKey
    private val sharedPreferences: SharedPreferences

    init {
        masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        sharedPreferences = try {
            EncryptedSharedPreferences.create(
                context,
                "secure_session_prefs",
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            if (e is GeneralSecurityException || e is IOException) {
                Log.e("SecureSessionManager", "Error reading encrypted preferences, deleting and re-creating.", e)
                context.deleteSharedPreferences("secure_session_prefs")
                EncryptedSharedPreferences.create(
                    context,
                    "secure_session_prefs",
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
                )
            } else {
                throw e
            }
        }
    }

    companion object {
        private const val KEY_USER_ID = "user_id"
        private const val KEY_EMAIL = "email"
        private const val KEY_IS_OWNER = "is_owner"
        private const val KEY_SESSION_TIMESTAMP = "session_timestamp"
        private const val SESSION_VALIDITY_DAYS = 30L

        private const val KEY_PROFILE_NAME = "profile_name"
        private const val KEY_PROFILE_NATIONALITY = "profile_nationality"
        private const val KEY_PROFILE_PHONE = "profile_phone"

        private const val KEY_OFFLINE_USER_ID = "offline_user_id"
        private const val KEY_OFFLINE_EMAIL = "offline_email"
        private const val KEY_OFFLINE_IS_OWNER = "offline_is_owner"

        private const val KEY_OFFLINE_PASSWORD = "offline_password"   // cifrado por EncryptedSharedPreferences


    }

    // Guardar sesión después del login exitoso
    fun saveSession(userId: String, email: String, isOwner: Boolean) {
        sharedPreferences.edit().apply {
            putString(KEY_USER_ID, userId)
            putString(KEY_EMAIL, email)
            putBoolean(KEY_IS_OWNER, isOwner)
            putLong(KEY_SESSION_TIMESTAMP, System.currentTimeMillis())
            apply()
        }
        Log.d("SecureSession", "Session saved for user: $email")
    }

    // Obtener sesión guardada (null si no existe o expiró)
    fun getSession(): SessionData? {
        val userId = sharedPreferences.getString(KEY_USER_ID, null) ?: return null
        val email = sharedPreferences.getString(KEY_EMAIL, null) ?: return null
        val isOwner = sharedPreferences.getBoolean(KEY_IS_OWNER, false)
        val timestamp = sharedPreferences.getLong(KEY_SESSION_TIMESTAMP, 0L)

        // Validar expiración (30 días)
        val currentTime = System.currentTimeMillis()
        val daysPassed = (currentTime - timestamp) / (1000 * 60 * 60 * 24)

        if (daysPassed > SESSION_VALIDITY_DAYS) {
            Log.d("SecureSession", "Session expired (${daysPassed} days old)")
            clearSession()
            return null
        }

        Log.d("SecureSession", "Valid session found for: $email")
        return SessionData(userId, email, isOwner)
    }

    // Verificar si hay sesión válida
    fun hasValidSession(): Boolean = getSession() != null

    // Limpiar sesión (logout)
    fun clearSession() {
        // Solo se borra las claves de sesion activa
        // se deja identidad offline y snapshot de perfil
        sharedPreferences.edit()
            .remove(KEY_USER_ID)
            .remove(KEY_EMAIL)
            .remove(KEY_IS_OWNER)
            .remove(KEY_SESSION_TIMESTAMP)
            .apply()
        Log.d("SecureSession", "Session cleared (offline identity preserved)")
    }

    fun saveBasicProfile(profile: BasicProfileLocal) {
        sharedPreferences.edit()
            .putString(KEY_PROFILE_NAME, profile.name)
            .putString(KEY_PROFILE_NATIONALITY, profile.nationality)
            .putString(KEY_PROFILE_PHONE, profile.phoneNumber)
            .apply()
    }

    fun getBasicProfileOrNull(): BasicProfileLocal? {
        val name = sharedPreferences.getString(KEY_PROFILE_NAME, null)
        val nationality = sharedPreferences.getString(KEY_PROFILE_NATIONALITY, null)
        val phone = sharedPreferences.getString(KEY_PROFILE_PHONE, null)
        return if (name != null || nationality != null || phone != null) {
            BasicProfileLocal(name, nationality, phone)
        } else null
    }

    fun saveOfflineIdentity(userId: String, email: String, isOwner: Boolean) {
        sharedPreferences.edit()
            .putString(KEY_OFFLINE_USER_ID, userId)
            .putString(KEY_OFFLINE_EMAIL, email)
            .putBoolean(KEY_OFFLINE_IS_OWNER, isOwner)
            .apply()
    }

    data class OfflineIdentity(val userId: String, val email: String, val isOwner: Boolean)

    fun getOfflineIdentityOrNull(): OfflineIdentity? {
        val uid = sharedPreferences.getString(KEY_OFFLINE_USER_ID, null)
        val email = sharedPreferences.getString(KEY_OFFLINE_EMAIL, null)
        val owner = sharedPreferences.getBoolean(KEY_OFFLINE_IS_OWNER, false)
        return if (uid != null && email != null) OfflineIdentity(uid, email, owner) else null
    }

    fun saveOfflinePassword(password: String) {
        sharedPreferences.edit().putString(KEY_OFFLINE_PASSWORD, password).apply()
    }

    fun getOfflinePasswordOrNull(): String? =
        sharedPreferences.getString(KEY_OFFLINE_PASSWORD, null)

    fun verifyOfflineEmailAndPassword(email: String, password: String): Boolean {
        val id = getOfflineIdentityOrNull()
        val stored = getOfflinePasswordOrNull()
        return id?.email?.equals(email, ignoreCase = true) == true && stored == password
    }


}

data class SessionData(
    val userId: String,
    val email: String,
    val isOwner: Boolean
)
