// MY POSTS VIEW MODEL
package com.team21.myapplication.ui.myPostsView

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.team21.myapplication.data.local.SecureSessionManager
import com.team21.myapplication.data.local.dao.DraftPostDao
import com.team21.myapplication.data.local.dao.MyPostsDao
import com.team21.myapplication.data.model.BasicHousingPost
import com.team21.myapplication.data.repository.OwnerUserRepository
import com.team21.myapplication.data.repository.AuthRepository
import com.team21.myapplication.ui.myPostsView.state.MyPostsState
import com.team21.myapplication.utils.NetworkMonitor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MyPostsViewModel(
    private val ownerRepo: OwnerUserRepository,
    private val authRepo: AuthRepository,
    private val session: SecureSessionManager,
    private val dao: MyPostsDao,
    private val net: NetworkMonitor,
    private val draftDao: DraftPostDao
) : ViewModel() {
    private val _state = MutableStateFlow(MyPostsState())
    val state: StateFlow<MyPostsState> = _state

    private var observeJob: kotlinx.coroutines.Job? = null
    private var observedOwnerId: String? = null

    fun loadMyPosts() {
        viewModelScope.launch { // Inicia corrutina ligada al ciclo de vida del ViewModel
            // ejecutado en Main (Dispatcher.Main)
            _state.value = _state.value.copy(isLoading = true, error = null)


            // Resolver usuario
            val uidOnline = authRepo.getCurrentUserId()
            val uidLocal = session.getSession()?.userId
            val ownerId = uidOnline ?: uidLocal ?: run {
                _state.value = _state.value.copy(isLoading = false, error = "User not logged in"); return@launch
            }

            // Observa publicados (Room cache existente)
            val postedFlow = dao.observeByOwner(ownerId).map { entities ->
                entities.map {
                    BasicHousingPost(
                        id = it.id,
                        title = it.title,
                        photoPath = it.thumbnailUrl,
                        price = it.price,
                        housing = it.id,
                        isDraft = false
                    )
                }
            }

            // Observa borradores + resuelve foto principal local
            val draftsFlow = draftDao.observeAllDrafts().map { drafts ->
                drafts.map { d ->
                    // obtener la ruta del main
                    val imgs = draftDao.getImagesFor(d.id)
                    val mainPath = imgs.firstOrNull { it.isMain }?.localPath ?: imgs.firstOrNull()?.localPath.orEmpty()

                    BasicHousingPost(
                        id = d.id,
                        title = d.title,
                        photoPath = mainPath,     // MyPostsView ya agrega "file://"
                        price = d.price,
                        housing = "",             // no existe en draft
                        isDraft = true
                    )
                }
            }

            // Combinar ambos para la UI
            observeJob?.cancel()
            observeJob = combine(postedFlow, draftsFlow) { posted, drafts ->
                // borradores arriba (más recientes primero)
                val draftsSorted = drafts.sortedByDescending { it.id }
                draftsSorted + posted
            }.collectIn(viewModelScope) { merged ->
                _state.value = _state.value.copy(posts = merged, isLoading = false)
            }

            // Refresh remoto si hay internet
            if (net.isOnline.value) {
                try {
                    // pedir los posts al backend con dispatcher de IO para no saturar el hilo principal
                    val list = withContext(Dispatchers.IO) { ownerRepo.getOwnerHousingPosts(ownerId).getOrThrow() }
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
                    // persistir en Room -> usar dispatcher IO para no saturar el hilo principal
                    withContext(Dispatchers.IO) { dao.upsertAll(entities) }
                } catch (e: Throwable) {
                    _state.value = _state.value.copy(error = e.message)
                }
            }
        }
    }

    // helper pequeño
    private fun <T> kotlinx.coroutines.flow.Flow<T>.collectIn(
        scope: CoroutineScope,
        block: suspend (T) -> Unit
    ) = scope.launch { collect { block(it) } }




    class MyPostsViewModelFactory(
        private val ownerRepo: OwnerUserRepository,
        private val authRepo: AuthRepository,
        private val session: SecureSessionManager,
        private val dao: MyPostsDao,
        private val net: NetworkMonitor,
        private val draftDao: DraftPostDao
    ) : androidx.lifecycle.ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(MyPostsViewModel::class.java))
            @Suppress("UNCHECKED_CAST")
            return MyPostsViewModel(ownerRepo, authRepo, session, dao, net, draftDao) as T
        }
    }
}
