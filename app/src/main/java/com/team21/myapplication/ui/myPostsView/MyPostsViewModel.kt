package com.team21.myapplication.ui.myPostsView

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.team21.myapplication.data.repository.OwnerUserRepository
import com.team21.myapplication.data.repository.AuthRepository
import com.team21.myapplication.ui.myPostsView.state.MyPostsState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MyPostsViewModel(
    private val ownerRepo: OwnerUserRepository = OwnerUserRepository(),
    private val authRepo: AuthRepository = AuthRepository()
) : ViewModel(){
    private val _state = MutableStateFlow(MyPostsState())
    val state: StateFlow<MyPostsState> = _state

    fun loadMyPosts() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)

            val uid = authRepo.getCurrentUserId()
            ///val uid = "OwnerUser1" example

            if (uid == null) {
                _state.value = _state.value.copy(isLoading = false, error = "User not logged in")
                return@launch
            }

            val res = ownerRepo.getOwnerHousingPosts(uid)
            _state.value = res.fold(
                onSuccess = { list -> MyPostsState(isLoading = false, posts = list) },
                onFailure = { e ->
                    MyPostsState(
                        isLoading = false,
                        error = e.message ?: "Error loading posts"
                    )
                }
            )
        }
    }
}