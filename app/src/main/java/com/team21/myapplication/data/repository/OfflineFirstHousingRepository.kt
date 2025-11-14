package com.team21.myapplication.data.repository

import android.util.Log
import com.team21.myapplication.data.local.dao.HousingDao
import com.team21.myapplication.data.model.HousingPreview
import com.team21.myapplication.data.local.entity.toHousingPreview
import com.team21.myapplication.data.local.entity.toRecommendedEntity
import com.team21.myapplication.data.local.entity.toRecentlySeenEntity
import com.team21.myapplication.utils.NetworkMonitor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class OfflineFirstHousingRepository(
    private val housingDao: HousingDao,
    private val studentUserProfileRepository: StudentUserProfileRepository,
    private val authRepository: AuthRepository,
    private val networkMonitor: NetworkMonitor
) {

    fun getRecommendedHousings(): Flow<List<HousingPreview>> {
        // 💡 Este Flow trabaja en hilos de I/O internamente (Room lo maneja así automáticamente)
        return housingDao.getRecommendedHousings().map { entities ->
            entities.map { it.toHousingPreview() }
        }
    }

    fun getRecentlySeenHousings(): Flow<List<HousingPreview>> {
        return housingDao.getRecentlySeenHousings().map { entities ->
            entities.map { it.toHousingPreview() }
        }
    }

    suspend fun refreshHousings() = withContext(Dispatchers.IO) {
        // 💡 Estrategia 1: Corrutina con Dispatcher explícito (I/O)
        // Todo este bloque se ejecuta fuera del hilo principal.
        if (!networkMonitor.isOnline.value) {
            Log.d("OfflineRepo", "Offline. Skipping refresh.")
            return@withContext
        }

        try {
            val uid = authRepository.getCurrentUserId() ?: return@withContext
            Log.d("OfflineRepo", "Refreshing housings for user: $uid")

            // 💡 Estrategia 2: Corrutina anidada (separar lógica de red y almacenamiento)
            val profile = withContext(Dispatchers.Default) {
                studentUserProfileRepository.getStudentUserProfile(uid)
            }

            profile?.let {
                val recommended = it.recommendedHousingPosts.map { it.toRecommendedEntity() }
                val recentlySeen = it.visitedHousingPosts.map { it.toRecentlySeenEntity() }

                // 💡 Estrategia 3: Mantener operaciones de BD en I/O
                withContext(Dispatchers.IO) {
                    if (recommended.isNotEmpty() || recentlySeen.isNotEmpty()) {
                        housingDao.deleteRecommendedHousings()
                        housingDao.deleteRecentlySeenHousings()
                        housingDao.insertHousings(recommended + recentlySeen)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("OfflineFirstHousingRepository", "Error refreshing housings, cache preserved.", e)
        }
    }
}