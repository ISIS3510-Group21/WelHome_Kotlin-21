package com.team21.myapplication.ui.detailView

import android.util.Log
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.messaging.FirebaseMessaging
import com.team21.myapplication.analytics.AnalyticsHelper
import com.team21.myapplication.data.repository.AuthRepository
import com.team21.myapplication.data.repository.HousingPostRepository
import com.team21.myapplication.data.repository.StudentUserRepository
import com.team21.myapplication.domain.usecase.GetHousingPostByIdUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.team21.myapplication.domain.mapper.DetailHousingUiMapper
import com.team21.myapplication.ui.detailView.state.DetailHousingUiState
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers

/**
 * MVVM (ViewModel):
 * - Orquesta caso de uso + mapeo a UiState.
 * - Expone STATE inmutable vía StateFlow (la View OBSERVA este flujo).
 */
class DetailHousingViewModel(
    // Default para evitar factory por ahora (puedes cambiar a Hilt/factory luego)
    private val getHousingPostById: GetHousingPostByIdUseCase =
        GetHousingPostByIdUseCase(HousingPostRepository())
) : ViewModel() {

    private val _state = MutableStateFlow(DetailHousingUiState())
    val state: StateFlow<DetailHousingUiState> = _state

    private var currentHousingId: String? = null
    private var cachedTags: List<String> = emptyList()
    val housingRepo = HousingPostRepository()

    fun load(housingId: String) {
        currentHousingId = housingId
        _state.value = _state.value.copy(isLoading = true, error = null)

        viewModelScope.launch {
            try {
                val full = getHousingPostById(housingId)
                if (full == null) {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = "Housing no encontrado"
                    )
                } else {
                    _state.value = DetailHousingUiMapper.toUiState(full)
                    cachedTags = try {
                        housingRepo.getTagsForPostId(housingId).map { it.name }
                    } catch (e: Exception) {
                        emptyList()
                    }
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "Error al cargar: ${e.message ?: "desconocido"}"
                )
            }
        }
    }

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
            //val uid = authRepo.getCurrentUserId()
            val uid = authRepo.getCurrentUserId() ?: return@launch

            val student = studentRepo.getStudentUser(uid)

            val nationality = try {
                if (uid != null) studentRepo.getStudentUser(uid)?.nationality ?: "Unknown" else "Unknown"
            } catch (e: Exception) {
                Log.w("DetailHousing", "No se pudo leer student/nationality: ${e.message}")
                "Unknown"
            }

            val tags = cachedTags

            // 1) Analytics: evento por tag
            analytics.logHousingDetailViewTime(
                postId = housingId,
                postTitle = title,
                tags = if (tags.isEmpty()) listOf("Unknown") else tags,
                durationMs = durationMs,
                userNationality = nationality
            )

            // Ejecuta ambas cosas en un bloque NO cancelable y en IO
            withContext(NonCancellable + Dispatchers.IO) {

                try {
                    // 1) Acumular duración por tag en Firestore
                    incrementUserTagCounters(
                        firestore = firestore,
                        uid = uid,
                        tags = if (tags.isEmpty()) listOf("Unknown") else tags,
                        durationMs = durationMs
                    )
                } catch (e: Exception) {
                    Log.e("PrefTopic", "Error incrementando contadores: ${e.message}")
                }

                try {
                    // 2) Calcular top y suscribirse al topic; además escribe _top y _topic
                    updateUserPreferredTopic(
                        firestore = firestore,
                        messaging = messaging,
                        uid = uid,
                        analytics = analytics
                    )
                } catch (e: Exception) {
                    Log.e("PrefTopic", "Error en updateUserPreferredTopic: ${e.message}")
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
        if (!snap.exists()){
            return
        }
        val data = snap.data ?:  run {
            Log.w("PrefTopic", "Doc vacío en UserTagStats/$uid")
            return
        }

        val top = data.entries
            .filter { it.key !in setOf("_topic","_top") && it.value is Number }
            .maxByOrNull { (it.value as Number).toDouble() }
            ?.key

        if (top == null) {
            Log.w("PrefTopic", "No hay campos numéricos para calcular top en $uid")
            return
        }

        firestore.collection("UserTagStats").document(uid)
            .set(mapOf("_top" to top), SetOptions.merge()).await()

        val topic = "pref_tag_" + top.lowercase().replace(" ", "_").replace(Regex("[^a-z0-9_]+"), "")

        messaging.subscribeToTopic(topic).addOnCompleteListener {
            if (!it.isSuccessful) {
                Log.w("PrefTopic", "Fallo al suscribir a $topic: ${it.exception?.message}")
            }
        }

        firestore.collection("UserTagStats").document(uid)
            .set(mapOf("_topic" to topic), SetOptions.merge()).await()

        analytics.setUserPreferredTag(topic)
    }
}