package com.team21.myapplication.ui.forum;

import com.team21.myapplication.data.model.ThreadForum;

import kotlin.collections.List;

data class ForumState (
    val threads: List<ThreadForum> = emptyList(),
    val selectedThread: ThreadForum? = null,
    val isLoading: Boolean = true,
    val error: String? = null
)
