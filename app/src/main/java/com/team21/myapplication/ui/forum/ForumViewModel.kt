package com.team21.myapplication.ui.forum

import androidx.lifecycle.ViewModel
import com.team21.myapplication.data.model.ForumPost
import com.team21.myapplication.data.model.ThreadForum
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ForumViewModel : ViewModel() {

    private val _threads = MutableStateFlow<List<ThreadForum>>(emptyList())
    val threads: StateFlow<List<ThreadForum>> = _threads

    init {
        loadThreads()
    }

    private fun loadThreads() {
        val samplePosts = listOf(
            ForumPost(
                id = "1",
                content = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco",
                positiveVotes = 495,
                negativeVotes = 5,
                userName = "Jhon Doe",
                userPhoto = ""
            ),
            ForumPost(
                id = "2",
                content = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud",
                positiveVotes = 495,
                negativeVotes = 5,
                userName = "Jhon Doe",
                userPhoto = ""
            )
        )

        _threads.value = listOf(
            ThreadForum(id = "1", title = "General Questions", forumPost = emptyList()),
            ThreadForum(id = "2", title = "Food", forumPost = emptyList()),
            ThreadForum(id = "3", title = "Mobility", forumPost = samplePosts)
        )
    }
}
