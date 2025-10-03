package com.team21.myapplication.ui.filterView

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.team21.myapplication.data.repository.FilterMode
import kotlinx.coroutines.flow.collectLatest

/**
 * ROUTE:
 * - Crea/obtiene el ViewModel.
 * - OBSERVER: observa state y effects.
 * - Entrega state + callbacks a la View.
 */
@Composable
fun FilterRoute(
    onNavigateToResults: (items: List<com.team21.myapplication.ui.filterView.state.PreviewCardUi>) -> Unit,
    onOpenDetail: (housingId: String) -> Unit
) {
    val vm: FilterViewModel = viewModel()
    val state by vm.state.collectAsState()

    // Carga inicial
    LaunchedEffect(Unit) { vm.load() }

    // Efectos → navegación a resultados
    LaunchedEffect(Unit) {
        vm.effects.collectLatest { effect ->
            when (effect) {
                is FilterEffect.ShowResults -> onNavigateToResults(effect.items)
            }
        }
    }

    FilterView(
        state = state,
        onToggleTag = vm::toggleTag,
        onSearch = { vm.search(FilterMode.AND) },
        onMapSearch = { /* TODO map search */ },
        onOpenDetail = onOpenDetail
    )
}
