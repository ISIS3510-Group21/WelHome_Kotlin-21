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

class MainActivityViewModel: ViewModel() {
    private val repositoryStudentUserProfile = StudentUserProfileRepository()
    private val repositoryHousingTag = HousingTagRepository()

    private val _recommendedHousingPosts = MutableStateFlow<List<HousingPreview>>(emptyList())
    val recommendedHousingPosts: StateFlow<List<HousingPreview>> = _recommendedHousingPosts
    private val _recentlySeenHousingPosts = MutableStateFlow<List<HousingPreview>>(emptyList())
    val recentlySeenHousingPosts: StateFlow<List<HousingPreview>> = _recentlySeenHousingPosts
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        getRecommendedHousingPosts()
    }

    private fun getRecommendedHousingPosts() {
        viewModelScope.launch {
            _isLoading.value = true

            val userProfile = repositoryStudentUserProfile.getStudentUserProfile()
            val usedTags = userProfile?.usedTags
            _recentlySeenHousingPosts.value = userProfile?.visitedHousingPosts ?: emptyList()

            val housingTags = repositoryHousingTag.getHousingTags(usedTags ?: emptyList())
            Log.d("MainActivityViewModel", "Housing tags: $housingTags")

            val allHousingPosts = housingTags.flatMap { it.housingPreview }
            val uniqueHousingPosts = allHousingPosts.distinctBy { it.housing }
            Log.d("MainActivityViewModel", "Unique housing posts: $uniqueHousingPosts")

            val scoredHousingPosts = uniqueHousingPosts.map { housingPreview ->
                val countTags = housingTags.count { housingTag ->
                    housingTag.housingPreview.any { it.id == housingPreview.id }
                }
                val score = countTags * 2 + housingPreview.rating
                housingPreview to score
            }

            val recommendedPosts = scoredHousingPosts
                .sortedByDescending { it.second }
                .map { it.first }
                .take(10)

            _recommendedHousingPosts.value = recommendedPosts
            _isLoading.value = false
        }
    }
}