package com.team21.myapplication.data.repository

import android.util.Log
import com.team21.myapplication.data.local.dao.HousingDao
import com.team21.myapplication.data.model.HousingPreview
import com.team21.myapplication.data.local.entity.toHousingPreview
import com.team21.myapplication.data.local.entity.toRecommendedEntity
import com.team21.myapplication.data.local.entity.toRecentlySeenEntity
import com.team21.myapplication.utils.NetworkMonitor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class OfflineFirstHousingRepository(
    private val housingDao: HousingDao,
    private val studentUserProfileRepository: StudentUserProfileRepository,
    private val authRepository: AuthRepository,
    private val networkMonitor: NetworkMonitor
) {

    fun getRecommendedHousings(): Flow<List<HousingPreview>> {
        return housingDao.getRecommendedHousings().map { entities ->
            entities.map { it.toHousingPreview() }
        }
    }

    fun getRecentlySeenHousings(): Flow<List<HousingPreview>> {
        return housingDao.getRecentlySeenHousings().map { entities ->
            entities.map { it.toHousingPreview() }
        }
    }

    suspend fun refreshHousings() {
        if (!networkMonitor.isOnline.value) {
            Log.d("OfflineRepo", "Offline. Skipping refresh.")
            return // Si no hay conexión, no hacemos nada.
        }

        try {
            val uid = authRepository.getCurrentUserId() ?: return
            Log.d("OfflineRepo", "Refreshing housings for user: $uid")
            val userProfile = studentUserProfileRepository.getStudentUserProfile(uid)

            // Solo procedemos si el perfil no es nulo
            userProfile?.let { profile ->
                Log.d("OfflineRepo", "Profile fetched. Recommended: ${profile.recommendedHousingPosts.size}, Visited: ${profile.visitedHousingPosts.size}")

                val recommended = profile.recommendedHousingPosts.map { it.toRecommendedEntity() }
                val recentlySeen = profile.visitedHousingPosts.map { it.toRecentlySeenEntity() }

                // Solo borramos la caché si hemos recibido contenido nuevo para reemplazarla
                if (recommended.isNotEmpty() || recentlySeen.isNotEmpty()) {
                    Log.d("OfflineRepo", "New data found. Clearing old cache and inserting new data.")
                    housingDao.deleteRecommendedHousings()
                    housingDao.deleteRecentlySeenHousings()
                    housingDao.insertHousings(recommended + recentlySeen)
                } else {
                    Log.d("OfflineRepo", "Network data is empty. Keeping existing cache.")
                    // Opcional: podrías querer borrar la caché si las listas vacías son intencionales.
                    // En ese caso, se podría mover el borrado fuera de este if.
                    // Pero para el caso de "la caché se vacía inesperadamente", esta lógica es más segura.
                }
            }
        } catch (e: Exception) {
            Log.e("OfflineFirstHousingRepository", "Error refreshing housings, cache will be preserved.", e)
            // Si hay un error de red, no tocamos la caché, preservando los datos antiguos.
        }
    }
}
