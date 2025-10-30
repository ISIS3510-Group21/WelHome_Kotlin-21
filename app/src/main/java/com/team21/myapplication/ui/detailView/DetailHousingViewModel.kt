package com.team21.myapplication.ui.detailView

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.messaging.FirebaseMessaging
import com.team21.myapplication.analytics.AnalyticsHelper
import com.team21.myapplication.cache.LruCacheProvider
import com.team21.myapplication.cache.RecentDetailCache
import com.team21.myapplication.data.local.DetailLocalDataSource
import com.team21.myapplication.data.local.RoomDetailLocalDataSource
import com.team21.myapplication.data.repository.AuthRepository
import com.team21.myapplication.data.repository.HousingPostRepository
import com.team21.myapplication.data.repository.StudentUserRepository
import com.team21.myapplication.domain.mapper.DetailHousingUiMapper
import com.team21.myapplication.domain.usecase.GetHousingPostByIdUseCase
import com.team21.myapplication.ui.detailView.state.DetailHousingUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

/**
 * DetailHousingViewModel
 *
 * Primary constructor ONLY receives Application so the default AndroidViewModelFactory can instantiate it.
 *
 * Patterns used and tagged inline:
 * - [EVENTUAL CONNECTIVITY] [CACHE-FIRST]  memory (MRU/LRU) -> Room snapshot -> network
 * - [LOCAL STORAGE]  DetailLocalDataSource (Room) persists/reads the UI snapshot
 * - [CACHING]  LruCacheProvider (1/8 memory) + non-singleton MRU (passed from Route)
 * - [MULTITHREADING]  IO for DB/network; Main for UI updates (viewModelScope)
 */
class DetailHousingViewModel(app: Application) : AndroidViewModel(app) {

    // --- Repos & Use cases (build inside for factory-friendliness) ---
    private val appCtx = app.applicationContext
    private val housingRepo = HousingPostRepository()
    private val getHousingPostById = GetHousingPostByIdUseCase(housingRepo)

    // [LOCAL STORAGE] Room-backed local data source
    private val local: DetailLocalDataSource = RoomDetailLocalDataSource(appCtx)

    // [CACHING] Per-VM LRU (1/8 of process memory by default)
    private val lru = LruCacheProvider<String, DetailHousingUiState>()

    // --- UI state ---
    private val _state = MutableStateFlow(DetailHousingUiState())
    val state: StateFlow<DetailHousingUiState> = _state

    private var currentHousingId: String? = null
    private var cachedTags: List<String> = emptyList()

    /**
     * Load the detail using cache-first → network strategy.
     *
     * @param housingId      the post to load
     * @param isOnline       current connectivity flag
     * @param recentCache    [CACHING] non-singleton MRU cache owned by the Route
     */
    fun load(housingId: String, isOnline: Boolean, recentCache: RecentDetailCache) {
        currentHousingId = housingId
        _state.value = _state.value.copy(isLoading = true, error = null)

        viewModelScope.launch {
            // ----------------- MEMORY: MRU (fast path) -----------------
            // [EVENTUAL CONNECTIVITY] [CACHE-FIRST] [CACHING]
            recentCache.get(housingId)?.let { mruUi ->
                _state.value = mruUi.copy(isLoading = false)
            } ?: run {
                // ----------------- MEMORY: LRU (fall back) -----------------
                // [EVENTUAL CONNECTIVITY] [CACHE-FIRST] [CACHING]
                lru.get("detail:$housingId")?.let { lruUi ->
                    _state.value = lruUi.copy(isLoading = false)
                    // keep MRU in sync
                    recentCache.put(housingId, _state.value)
                }
            }

            // ----------------- ROOM snapshot -----------------
            // [EVENTUAL CONNECTIVITY] [CACHE-FIRST] [LOCAL STORAGE] [MULTITHREADING]
            if (_state.value.title.isBlank()) {
                val snapshot = withContext(Dispatchers.IO) { local.getSnapshot(housingId) }
                if (snapshot != null) {
                    _state.value = snapshot.copy(isLoading = false)
                    // update caches
                    recentCache.put(housingId, _state.value)
                    lru.put("detail:$housingId", _state.value)
                }
            }

            // ----------------- NETWORK fetch (if online) -----------------
            if (isOnline) {
                // [MULTITHREADING] Network/DB on IO; UI on Main.
                runCatching {
                    val domain = withContext(Dispatchers.IO) { getHousingPostById(housingId) }
                    if (domain == null) error("Housing not found")

                    val ui = DetailHousingUiMapper.toUiState(domain)
                    _state.value = ui.copy(isLoading = false)

                    // For analytics later
                    cachedTags = try {
                        withContext(Dispatchers.IO) { housingRepo.getTagsForPostId(housingId) }.map { it.name }
                    } catch (_: Exception) { emptyList() }

                    // Update caches
                    // [CACHING]
                    recentCache.put(housingId, _state.value)
                    lru.put("detail:$housingId", _state.value)

                    // Persist snapshot for offline reuse
                    // [LOCAL STORAGE] [MULTITHREADING]
                    withContext(Dispatchers.IO) { local.saveSnapshot(housingId, _state.value) }
                }.onFailure { e ->
                    // If nothing has been shown yet, surface an error or placeholder when offline
                    if (_state.value.title.isBlank()) {
                        _state.value = _state.value.copy(isLoading = false, error = "Error loading: ${e.message ?: "unknown"}")
                    }
                }
            } else {
                // ----------------- OFFLINE fallback: placeholder -----------------
                // If caches/Room had nothing, render a generic placeholder (non-blocking)
                if (_state.value.title.isBlank()) {
                    // Your View already handles blanks; make it explicit
                    _state.value = DetailHousingUiState(
                        isLoading = false,
                        error = null,
                        title = "",
                        rating = 0.0,
                        pricePerMonthLabel = "",
                        address = "",
                        ownerName = "",
                        imagesFromServer = emptyList(),
                        amenityLabels = emptyList(),
                        roommateNames = emptyList(),
                        roommateCount = 0,
                        reviewsCount = 0,
                        latitude = null,
                        longitude = null,
                        status = "offline"
                    )
                }
            }
        }
    }

    // ---------------- Analytics (unchanged, runs on IO) ----------------
    fun onDetailVisibleFor(
        durationMs: Long,
        analytics: AnalyticsHelper,
        authRepo: AuthRepository,
        studentRepo: StudentUserRepository,
        housingRepo: HousingPostRepository,
        firestore: FirebaseFirestore,
        messaging: FirebaseMessaging
    ) {
        val housingId = currentHousingId ?: return
        val title = state.value.title.ifBlank { "Unknown" }

        viewModelScope.launch {
            val uid = authRepo.getCurrentUserId() ?: return@launch

            val nationality = try {
                studentRepo.getStudentUser(uid)?.nationality ?: "Unknown"
            } catch (_: Exception) { "Unknown" }

            val tags = cachedTags.ifEmpty { listOf("Unknown") }

            analytics.logHousingDetailViewTime(
                postId = housingId,
                postTitle = title,
                tags = tags,
                durationMs = durationMs,
                userNationality = nationality
            )

            withContext(NonCancellable + Dispatchers.IO) {
                runCatching {
                    incrementUserTagCounters(firestore, uid, tags, durationMs)
                    updateUserPreferredTopic(firestore, messaging, uid, analytics)
                }
            }
        }
    }

    private suspend fun incrementUserTagCounters(
        firestore: FirebaseFirestore,
        uid: String,
        tags: List<String>,
        durationMs: Long
    ) {
        val doc = firestore.collection("UserTagStats").document(uid)
        val updates = tags.associate { it to FieldValue.increment(durationMs.toDouble()) }
        doc.set(updates, SetOptions.merge()).await()
    }

    private suspend fun updateUserPreferredTopic(
        firestore: FirebaseFirestore,
        messaging: FirebaseMessaging,
        uid: String,
        analytics: AnalyticsHelper
    ) {
        val snap = firestore.collection("UserTagStats").document(uid).get().await()
        val data = snap.data ?: return
        val top = data.entries
            .filter { it.key !in setOf("_topic","_top") && it.value is Number }
            .maxByOrNull { (it.value as Number).toDouble() }
            ?.key ?: return

        firestore.collection("UserTagStats").document(uid)
            .set(mapOf("_top" to top), SetOptions.merge()).await()

        val topic = "pref_tag_" + top.lowercase().replace(" ", "_").replace(Regex("[^a-z0-9_]+"), "")
        messaging.subscribeToTopic(topic)

        firestore.collection("UserTagStats").document(uid)
            .set(mapOf("_topic" to topic), SetOptions.merge()).await()

        analytics.setUserPreferredTag(topic)
    }
}
