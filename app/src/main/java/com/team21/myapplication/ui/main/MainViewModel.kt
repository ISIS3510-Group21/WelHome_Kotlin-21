package com.team21.myapplication.ui.main

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.team21.myapplication.analytics.AnalyticsHelper
import com.team21.myapplication.data.local.AppDatabase
import com.team21.myapplication.data.repository.AuthRepository
import com.team21.myapplication.data.repository.HousingPostRepository
import com.team21.myapplication.data.repository.OfflineFirstHousingRepository
import com.team21.myapplication.data.repository.StudentUserProfileRepository
import com.team21.myapplication.data.repository.StudentUserRepository
import com.team21.myapplication.utils.NetworkMonitor
import com.team21.myapplication.utils.getNetworkType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainViewModel(
    private val applicationContext: Context,
    networkMonitor: NetworkMonitor,
    private val housingRepository: OfflineFirstHousingRepository
) : ViewModel() {

    private val _homeState = MutableStateFlow(HomeState())
    val homeState: StateFlow<HomeState> = _homeState

    private val analyticsHelper: AnalyticsHelper = AnalyticsHelper(applicationContext)
    private val repositoryStudentUser = StudentUserRepository()
    private val housingPostRepo = HousingPostRepository()
    private val authRepo = AuthRepository()

    val isOnline: StateFlow<Boolean> =
        networkMonitor.isOnline.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = networkMonitor.isOnline.value
        )

    init {
        // 💡 Estrategia 3: Corrutina en Main que consume datos de un flujo (que trabaja en I/O)
        // Aquí se combinan dos hilos: Main (UI) y I/O (Room)
        viewModelScope.launch {
            housingRepository.getRecommendedHousings().collect { housings ->
                _homeState.value = _homeState.value.copy(recommendedHousings = housings)
            }
        }

        viewModelScope.launch {
            housingRepository.getRecentlySeenHousings().collect { housings ->
                _homeState.value = _homeState.value.copy(recentlySeenHousings = housings)
            }
        }
    }

    fun getHousingPosts() {
        viewModelScope.launch(Dispatchers.IO) {

            // 🔹 Marca el inicio para medir tiempo de carga (como ya hacías)
            val start = System.currentTimeMillis()
            analyticsHelper.logHomeOpen()

            // Verifica si ya hay datos locales para decidir si mostrar "loading"
            val hasLocalData =
                _homeState.value.recommendedHousings.isNotEmpty() ||
                        _homeState.value.recentlySeenHousings.isNotEmpty()

            // Solo muestra la pantalla de carga si no hay datos locales
            if (!hasLocalData) {
                withContext(Dispatchers.Main) {
                    _homeState.value = _homeState.value.copy(isLoading = true)
                }
            }

            // Refresca desde red (si hay conexión) — se ejecuta en hilo IO
            housingRepository.refreshHousings()

            // Marca el final y registra evento de analítica
            val end = System.currentTimeMillis()
            val networkType = getNetworkType(applicationContext)

            withContext(Dispatchers.Main) {
                // Oculta el loading solo al final
                _homeState.value = _homeState.value.copy(isLoading = false)

                // Envía el tiempo de carga a Analytics
                analyticsHelper.logHomeLoadingTime(
                    end - start,
                    Build.MODEL,
                    networkType
                )
            }
        }
    }

    fun logHousingPostClick(postId: String?, postTitle: String, price: Double) {
        // 💡 Estrategia 2: Múltiples corrutinas anidadas (una dentro de otra)
        // Se usa para separar trabajo de red, BD y registro analítico sin bloquear el hilo principal.
        viewModelScope.launch {
            val safePostId = postId ?: run {
                Log.w("MainViewModel", "postId es null, no se registra el click")
                return@launch
            }

            var message = "Post $safePostId clickeado con éxito"

            try {
                val auth = authRepo.getCurrentUserId() ?: return@launch

                // 🔹 Corrutina interna en I/O: acceder a BD/local
                val studentUser = withContext(Dispatchers.IO) {
                    repositoryStudentUser.getStudentUser(auth)
                }

                val userNationality = studentUser?.nationality ?: "Unknown"

                // 🔹 Corrutina interna en Default (cálculo/lógica intermedia)
                withContext(Dispatchers.Default) {
                    studentUser?.let {
                        analyticsHelper.setUserNationality(it.nationality)
                        analyticsHelper.setUserType(true)
                        analyticsHelper.setUserLanguage(it.language)
                    }
                }

                // 🔹 Corrutina en I/O: buscar post y tags en BD local
                val tags = withContext(Dispatchers.IO) {
                    val post = housingPostRepo.getHousingPosts().firstOrNull { it.id == safePostId }
                    housingPostRepo.getTagsForPostId(safePostId)
                }

                // 🔹 Corrutina en Main: registrar eventos analíticos (ligeros, pueden ir en Main)
                withContext(Dispatchers.Main) {
                    if (tags.isEmpty()) {
                        analyticsHelper.logHousingPostClick(
                            postId = safePostId,
                            postTitle = postTitle,
                            housingCategory = "Unknown",
                            price = price,
                            userNationality = userNationality
                        )
                    } else {
                        tags.forEach { tag ->
                            analyticsHelper.logHousingPostClick(
                                postId = safePostId,
                                postTitle = postTitle,
                                housingCategory = tag.name,
                                price = price,
                                userNationality = userNationality
                            )
                        }
                    }
                }

            } catch (e: Exception) {
                Log.e("MainViewModel", "Error logging post click: ${e.message}")
                message = "ERROR al registrar click $postId: ${e.message}"
            } finally {
                Log.d("MainViewModel", message)
            }
        }
    }

    class Factory(
        private val context: Context,
        private val networkMonitor: NetworkMonitor
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val db = AppDatabase.getDatabase(context)
            val housingDao = db.housingDao()
            val offlineFirstHousingRepository = OfflineFirstHousingRepository(
                housingDao = housingDao,
                studentUserProfileRepository = StudentUserProfileRepository(),
                authRepository = AuthRepository(),
                networkMonitor = networkMonitor
            )
            return MainViewModel(context, networkMonitor, offlineFirstHousingRepository) as T
        }
    }
}
