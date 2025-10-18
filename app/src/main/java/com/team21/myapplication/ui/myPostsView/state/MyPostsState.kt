package com.team21.myapplication.ui.myPostsView.state

import com.team21.myapplication.data.model.BasicHousingPost

data class MyPostsState(
    val isLoading: Boolean = false,
    val posts: List<BasicHousingPost> = emptyList(),
    val error: String? = null
)