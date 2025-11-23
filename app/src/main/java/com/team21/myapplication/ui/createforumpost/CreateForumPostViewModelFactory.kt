package com.team21.myapplication.ui.createforumpost

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.team21.myapplication.data.repository.ForumRepository
import com.team21.myapplication.data.repository.StudentUserRepository

class CreateForumPostViewModelFactory(
    private val forumRepository: ForumRepository,
    private val studentUserRepository: StudentUserRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CreateForumPostViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CreateForumPostViewModel(forumRepository, studentUserRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
