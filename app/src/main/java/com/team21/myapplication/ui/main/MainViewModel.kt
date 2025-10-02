package com.team21.myapplication.ui.main

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.team21.myapplication.data.model.HousingPreview
import com.team21.myapplication.data.repository.HousingTagRepository
import com.team21.myapplication.data.repository.StudentUserProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MainViewModel: ViewModel() {
    private val repositoryStudentUserProfile = StudentUserProfileRepository()

    private val _homeState = MutableStateFlow(HomeState())
    val homeState: StateFlow<HomeState> = _homeState

    init {
        getRecommendedHousingPosts()
    }

    private fun getRecommendedHousingPosts() {
        viewModelScope.launch {
            _homeState.value = _homeState.value.copy(isLoading = true)
            val userProfile = repositoryStudentUserProfile.getStudentUserProfile()
            Log.d("UserProfile", userProfile.toString())
            _homeState.value = _homeState.value.copy(
                recentlySeenHousings = userProfile?.visitedHousingPosts ?: emptyList(),
                isLoading = false
            )
        }
    }
}