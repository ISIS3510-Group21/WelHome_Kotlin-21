package com.team21.myapplication.ui.createforumpost

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.team21.myapplication.data.model.ForumPost
import com.team21.myapplication.data.model.ThreadForum
import com.team21.myapplication.data.repository.ForumRepository
import com.team21.myapplication.data.repository.StudentUserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CreateForumPostViewModel(
    private val forumRepository: ForumRepository,
    private val studentUserRepository: StudentUserRepository
) : ViewModel() {

    private val _threads = MutableStateFlow<List<ThreadForum>>(emptyList())
    val threads: StateFlow<List<ThreadForum>> = _threads.asStateFlow()

    private val _postCreationState = MutableStateFlow<PostCreationState>(PostCreationState.Idle)
    val postCreationState: StateFlow<PostCreationState> = _postCreationState.asStateFlow()

    init {
        loadThreads()
    }

    private fun loadThreads() {
        viewModelScope.launch {
            _threads.value = forumRepository.getThreads()
        }
    }

    fun createPost(
        threadId: String?,
        newThreadTitle: String?,
        newThreadDescription: String?,
        postContent: String
    ) {
        viewModelScope.launch {
            _postCreationState.value = PostCreationState.Loading
            try {
                val auth = FirebaseAuth.getInstance().currentUser?.uid
                val userProfile = studentUserRepository.getStudentUser(auth)
                val forumPost = ForumPost(
                    content = postContent,
                    user = auth ?: "",
                    userName = userProfile?.name ?: "Anonymous",
                    userPhoto = userProfile?.photoPath ?: ""
                )

                if (threadId == null && !newThreadTitle.isNullOrBlank() && !newThreadDescription.isNullOrBlank()) {
                    // Create new thread and post
                    Log.d("CreateForumPostViewModel", "Creating new thread and post")
                    Log.d("CreateForumPostViewModel", "forumPost: $forumPost")
                    forumRepository.createThread(newThreadTitle, newThreadDescription, forumPost)
                } else if (threadId != null) {
                    // Create post in existing thread
                    forumRepository.createPost(threadId, forumPost)
                }
                _postCreationState.value = PostCreationState.Success
            } catch (e: Exception) {
                _postCreationState.value = PostCreationState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun resetState() {
        _postCreationState.value = PostCreationState.Idle
    }
}

sealed class PostCreationState {
    object Idle : PostCreationState()
    object Loading : PostCreationState()
    object Success : PostCreationState()
    data class Error(val message: String) : PostCreationState()
}
