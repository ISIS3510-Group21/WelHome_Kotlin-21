package com.team21.myapplication.ui.detailView

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.team21.myapplication.data.repository.HousingPostRepository
import com.team21.myapplication.domain.usecase.GetHousingPostByIdUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.team21.myapplication.domain.mapper.DetailHousingUiMapper
import com.team21.myapplication.ui.detailView.state.DetailHousingUiState

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

    fun load(housingId: String) {
        Log.d("DetailHousing", "VM.load() -> $housingId")
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
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "Error al cargar: ${e.message ?: "desconocido"}"
                )
            }
        }
    }
}
