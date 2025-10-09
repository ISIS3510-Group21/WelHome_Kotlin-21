package com.team21.myapplication.ui.main

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.team21.myapplication.data.repository.StudentUserProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.team21.myapplication.analytics.AnalyticsHelper
import com.team21.myapplication.utils.getNetworkType
import android.os.Build
import com.google.firebase.auth.FirebaseAuth
import com.team21.myapplication.data.model.TagHousingPost
import com.team21.myapplication.data.repository.AuthRepository
import com.team21.myapplication.data.repository.HousingPostRepository
import com.team21.myapplication.data.repository.StudentUserRepository


class MainViewModel(
    private val applicationContext: Context
): ViewModel() {
    private val repositoryStudentUserProfile = StudentUserProfileRepository()

    private val _homeState = MutableStateFlow(HomeState())
    val homeState: StateFlow<HomeState> = _homeState

    private val  analyticsHelper: AnalyticsHelper = AnalyticsHelper(applicationContext)
    private val repositoryStudentUser = StudentUserRepository()
    private val housingPostRepo = HousingPostRepository()
    private val authRepo = AuthRepository()
     fun getHousingPosts() {
         val start = System.currentTimeMillis()
         analyticsHelper.logHomeOpen()
         viewModelScope.launch {
             _homeState.value = _homeState.value.copy(isLoading = true)

             val uid = authRepo.getCurrentUserId()
             if (uid == null) {
                 // No hay sesión
                 _homeState.value = _homeState.value.copy(isLoading = false)
                 return@launch
             }
             // Guarda el uid en el estado (útil para otras consultas)
             _homeState.value = _homeState.value.copy(currentUserId = uid)

             val userProfile = repositoryStudentUserProfile.getStudentUserProfile(uid)

             Log.d("UserProfile", userProfile.toString())
             _homeState.value = _homeState.value.copy(
                 recentlySeenHousings = userProfile?.visitedHousingPosts ?: emptyList(),
                 recommendedHousings = userProfile?.recommendedHousingPosts ?: emptyList(),
                 isLoading = false
             )
         }

        val networkType = getNetworkType(applicationContext)
        val end = System.currentTimeMillis()
        analyticsHelper.logHomeLoadingTime(
            end - start,
            Build.MODEL,
            networkType)
    }

    fun logHousingPostClick(postId: String?, postTitle: String, price: Double) {
        viewModelScope.launch {
            val safePostId = postId ?: run {
                Log.w("MainViewModel", "postId es null, no se registra el click")
                return@launch
            }
            var message = "Post $safePostId clickeado con éxito" //TODO: remove
            try {
                // Obtain current user
                //val auth = FirebaseAuth.getInstance().currentUser?.uid
                //val auth = "StudentUser99" //TODO:prueba

                val auth = authRepo.getCurrentUserId() ?: run {
                    Log.w("MainViewModel", "No current user; skip logging")
                    return@launch
                }

                Log.d("MainViewModel", "Buscando usuario con ID: $auth")
                val studentUser = repositoryStudentUser.getStudentUser(auth)

                if (studentUser == null) {
                    message += " (ADVERTENCIA: Usuario no encontrado en DB)" //TODO: remove
                }

                val userNationality = studentUser?.nationality ?: "Unknown"

                // set user properties for analytics
                studentUser?.let {
                    analyticsHelper.setUserNationality(it.nationality)
                    analyticsHelper.setUserType(true) // true for student
                    analyticsHelper.setUserLanguage(it.language)
                }

                // Obtain post for the tag
                val post = housingPostRepo.getHousingPosts()
                    .firstOrNull { it.id == safePostId }
                Log.e("post", post.toString())

                if (post == null) {
                    message += " (ADVERTENCIA: Post no encontrado localmente)"//TODO: remove
                }

                //obtain tags
                val tags: List<TagHousingPost> = housingPostRepo.getTagsForPostId(safePostId)

                // Registers an event for each track found
                tags.forEach { tag ->
                    val housingCategory = tag.name
                    Log.d("MainViewModel", "Registrando click para categoría: $housingCategory")

                    analyticsHelper.logHousingPostClick(
                        postId = safePostId,
                        postTitle = postTitle,
                        housingCategory = housingCategory,
                        price = price,
                        userNationality = userNationality
                    )
                }

                // Si no hay tags, registra como "Unknown"
                if (tags.isEmpty()) {
                    analyticsHelper.logHousingPostClick(
                        postId = safePostId,
                        postTitle = postTitle,
                        housingCategory = "Unknown",
                        price = price,
                        userNationality = userNationality
                    )
                }

                if (studentUser == null || post == null) {
                    message += " (ADVERTENCIA: Datos incompletos)"
                }

            } catch (e: Exception) {
                Log.e("MainViewModel", "Error logging post click: ${e.message}")
                message = "ERROR al registrar click $postId: ${e.message}" //TODO: remove
            }
            finally{//TODO: remove
                Log.d("MainViewModel", message)
            }
        }
    }
}