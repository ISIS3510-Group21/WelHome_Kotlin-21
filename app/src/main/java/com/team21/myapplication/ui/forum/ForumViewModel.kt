
package com.team21.myapplication.ui.forum

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.team21.myapplication.analytics.AnalyticsHelper
import com.team21.myapplication.data.model.ThreadForum
import com.team21.myapplication.data.repository.ForumRepository
import com.team21.myapplication.utils.App
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ForumViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: ForumRepository = ForumRepository(application)
    private val analyticsHelper: AnalyticsHelper = AnalyticsHelper(application)
    private val _state = MutableStateFlow(ForumState())
    val state: StateFlow<ForumState> = _state

    // Expose network state
    private val networkMonitor = (application as App).networkMonitor
    val isOnline: StateFlow<Boolean> = networkMonitor.isOnline

    init {
        loadThreads()

        // Observe network changes to trigger sync
        viewModelScope.launch {
            networkMonitor.isOnline.collectLatest { isConnected ->
                if (isConnected) {
                    Log.d("ForumViewModel", "Network connection restored. Refreshing data...")
                    refreshData()
                }
            }
        }
    }

    private fun loadThreads() {
        _state.value = _state.value.copy(isLoading = true)
        viewModelScope.launch {
            val threads = repository.getThreads()
            Log.d("ForumViewModel", "Loaded threads: $threads")
            _state.value = _state.value.copy(threads = threads, isLoading = false)
        }

    }

    fun onThreadClicked(thread: ThreadForum) {
        analyticsHelper.logForumThreadClick(thread.id, thread.title)
        selectThread(thread)
    }

    fun selectThread(thread: ThreadForum) {
        _state.value = _state.value.copy(selectedThread = thread)
        Log.d("ForumViewModel", "Selected thread: $thread")
        loadThreadPosts()
    }

    private fun loadThreadPosts() {
        viewModelScope.launch {
            val selectedThread = _state.value.selectedThread ?: return@launch
            // This check was removed to force re-loading from repository
            if (selectedThread.forumPost.isNotEmpty() && _state.value.threads.find { it.id == selectedThread.id }?.forumPost?.isNotEmpty() == true) {
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

    fun refreshData() {
        viewModelScope.launch {
            loadThreads() // This will trigger sync in the repository
            if (_state.value.selectedThread != null) {
                loadThreadPosts() // Refresh posts for the selected thread
            }
        }
    }
}

