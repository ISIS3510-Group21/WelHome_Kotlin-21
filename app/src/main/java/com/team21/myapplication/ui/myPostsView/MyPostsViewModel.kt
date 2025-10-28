package com.team21.myapplication.ui.myPostsView

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.team21.myapplication.data.local.SecureSessionManager
import com.team21.myapplication.data.local.dao.MyPostsDao
import com.team21.myapplication.data.repository.OwnerUserRepository
import com.team21.myapplication.data.repository.AuthRepository
import com.team21.myapplication.ui.myPostsView.state.MyPostsState
import com.team21.myapplication.utils.NetworkMonitor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MyPostsViewModel(
    private val ownerRepo: OwnerUserRepository,
    private val authRepo: AuthRepository,
    private val session: SecureSessionManager,
    private val dao: MyPostsDao,
    private val net: NetworkMonitor
) : ViewModel(){
    private val _state = MutableStateFlow(MyPostsState())
    val state: StateFlow<MyPostsState> = _state

    private var observeJob: kotlinx.coroutines.Job? = null
    private var observedOwnerId: String? = null


    fun loadMyPosts() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)

            // 1) UID - Firebase o local
            val uidOnline = authRepo.getCurrentUserId()
            val uidLocal = session.getSession()?.userId
            val ownerId = uidOnline ?: uidLocal

            if (ownerId == null) {
                _state.value = _state.value.copy(isLoading = false, error = "User not logged in")
                return@launch
            }

            // 2) UI desde room - offline first
            if (observedOwnerId != ownerId) {
                observeJob?.cancel()
                observedOwnerId = ownerId
                observeJob = viewModelScope.launch {
                    dao.observeByOwner(ownerId).collect { entities ->
                        val list = entities.map {
                            com.team21.myapplication.data.model.BasicHousingPost(
                                id = it.id,
                                title = it.title,
                                photoPath = it.thumbnailUrl,
                                price = it.price,
                                housing = "" // TODO: ajustar
                            )
                        }
                        _state.value = _state.value.copy(posts = list)
                    }
                }
            }


            // refresh remoto si hay internet
            if (net.isOnline.value) {
                try {
                    val list = withContext(Dispatchers.IO) {
                        ownerRepo.getOwnerHousingPosts(ownerId).getOrThrow()
                    }
                    val entities = list.map {
                        com.team21.myapplication.data.local.entity.MyPostEntity(
                            id = it.id,
                            ownerId = ownerId,
                            title = it.title,
                            thumbnailUrl = it.photoPath,
                            price = it.price,
                            updatedAt = System.currentTimeMillis()
                        )
                    }
                    withContext(Dispatchers.IO) {
                        dao.upsertAll(entities)
                    }
                    _state.value = _state.value.copy(isLoading = false)
                } catch (e: Throwable) {
                    _state.value = _state.value.copy(isLoading = false, error = e.message)
                }
            } else {
                _state.value = _state.value.copy(isLoading = false)
            }
        }
    }
}

class MyPostsViewModelFactory(
    private val ownerRepo: OwnerUserRepository,
    private val authRepo: AuthRepository,
    private val session: SecureSessionManager,
    private val dao: MyPostsDao,
    private val net: NetworkMonitor
) : androidx.lifecycle.ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(MyPostsViewModel::class.java))
        @Suppress("UNCHECKED_CAST")
        return MyPostsViewModel(ownerRepo, authRepo, session, dao, net) as T
    }
}
