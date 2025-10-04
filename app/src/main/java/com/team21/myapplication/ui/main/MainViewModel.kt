package com.team21.myapplication.ui.main

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.team21.myapplication.analytics.AnalyticsHelper
import com.team21.myapplication.data.repository.HousingPostRepository
import com.team21.myapplication.data.repository.StudentUserProfileRepository
import com.team21.myapplication.data.repository.StudentUserRepository
import com.team21.myapplication.utils.getNetworkType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

private const val TEST_USER_ID = "ezyAhP2L38UOeVPcdVGaFThXMfl1" //TODO: remove
class MainViewModel(
    private val analyticsHelper: AnalyticsHelper
): ViewModel() {
    private val repositoryStudentUserProfile = StudentUserProfileRepository()
    private val repositoryStudentUser = StudentUserRepository()
    private val housingPostRepo = HousingPostRepository()

    private val _homeState = MutableStateFlow(HomeState())
    val homeState: StateFlow<HomeState> = _homeState

    fun getHousingPosts(context: Context) {
        val start = System.currentTimeMillis()
        analyticsHelper.logHomeOpen()
        viewModelScope.launch {
            _homeState.value = _homeState.value.copy(isLoading = true)
            val userProfile = repositoryStudentUserProfile.getStudentUserProfile()

            _homeState.value = _homeState.value.copy(
                recentlySeenHousings = userProfile?.visitedHousingPosts ?: emptyList(),
                recommendedHousings = userProfile?.recommendedHousingPosts ?: emptyList(),
                isLoading = false
            )

            Log.d("MainViewModel", "User Profile: $userProfile")

            val networkType = getNetworkType(context)
            val end = System.currentTimeMillis()
            analyticsHelper.logHomeLoadingTime(
                end - start,
                Build.MODEL,
                networkType)

        }
    }

    fun logHousingPostClick(postId: String, postTitle: String, price: Double) {
        viewModelScope.launch {
            var message = "Post $postId clickeado con éxito" //TODO: remove
            try {
                // Obtain current user
                //val auth = FirebaseAuth.getInstance().currentUser?.uid
                val auth = "StudentUser99" //TODO:prueba
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
                    .firstOrNull { it.id == postId }
                Log.e("post", post.toString())

                if (post == null) {
                    message += " (ADVERTENCIA: Post no encontrado localmente)"//TODO: remove
                }

                //obtain tags
                val tags = housingPostRepo.getTagsForHousingPost(postId)

                // Registers an event for each track found
                tags.forEach { tag ->
                    val housingCategory = tag.name
                    Log.d("MainViewModel", "Registrando click para categoría: $housingCategory")

                    analyticsHelper.logHousingPostClick(
                        postId = postId,
                        postTitle = postTitle,
                        housingCategory = housingCategory,
                        price = price,
                        userNationality = userNationality
                    )
                }

                // Si no hay tags, registra como "Unknown"
                if (tags.isEmpty()) {
                    analyticsHelper.logHousingPostClick(
                        postId = postId,
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
                _homeState.value = _homeState.value.copy(snackbarMessage = message)
            }
        }
    }

    fun clearSnackbarMessage() {
        _homeState.value = _homeState.value.copy(snackbarMessage = null) //TODO: remove
    }
}