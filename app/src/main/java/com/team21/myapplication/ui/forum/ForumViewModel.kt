package com.team21.myapplication.ui.forum

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.team21.myapplication.data.model.ThreadForum
import com.team21.myapplication.data.repository.ThreadForumRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ForumViewModel : ViewModel() {
    private val repository: ThreadForumRepository = ThreadForumRepository()
    private val _state = MutableStateFlow(ForumState())
    val state: StateFlow<ForumState> = _state

    init {
        loadThreads()
    }

    private fun loadThreads() {
        _state.value = _state.value.copy(isLoading = true)
        viewModelScope.launch {
            val threads = repository.getThreads()
            Log.d("ForumViewModel", "Loaded threads: $threads")
            _state.value = _state.value.copy(threads = threads, isLoading = false)
        }

    }

    fun selectThread(thread: ThreadForum) {
        _state.value = _state.value.copy(selectedThread = thread)
        Log.d("ForumViewModel", "Selected thread: $thread")
        loadThreadPosts()
    }

    private fun loadThreadPosts() {
        viewModelScope.launch {
            val selectedThread = _state.value.selectedThread ?: return@launch
            if (selectedThread.forumPost.isNotEmpty()) {
                return@launch
            }
            val posts = repository.getThreadForumPosts(selectedThread.id)


            val updatedThread = selectedThread.copy(forumPost = posts)

            val updatedThreads = _state.value.threads.map { thread ->
                if (thread.id == selectedThread.id) updatedThread else thread
            }
            _state.value = _state.value.copy(
                threads = updatedThreads,
                selectedThread = updatedThread
            )

        }


    }
}
