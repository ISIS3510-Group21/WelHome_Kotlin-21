package com.team21.myapplication.ui.main

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.team21.myapplication.analytics.AnalyticsHelper
import com.team21.myapplication.data.repository.StudentUserProfileRepository
import com.team21.myapplication.utils.getNetworkType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MainViewModel(
    private val analyticsHelper: AnalyticsHelper
): ViewModel() {
    private val repositoryStudentUserProfile = StudentUserProfileRepository()

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
}