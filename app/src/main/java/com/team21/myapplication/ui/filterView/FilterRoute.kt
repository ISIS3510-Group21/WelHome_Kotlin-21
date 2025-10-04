package com.team21.myapplication.ui.filterView

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.team21.myapplication.data.repository.FilterMode
import com.team21.myapplication.ui.filterView.state.PreviewCardUi
import kotlinx.coroutines.flow.collectLatest

/**
 * ROUTE:
 * - Crea/obtiene el ViewModel.
 * - OBSERVER: observa state y effects (one-shot).
 * - Entrega state + callbacks a la View.
 */
@Composable
fun FilterRoute(
    onNavigateToResults: (items: List<PreviewCardUi>) -> Unit,
    onOpenDetail: (String) -> Unit
) {
    val vm: FilterViewModel = viewModel()
    val state = vm.state.collectAsStateWithLifecycle().value

    // Carga inicial
    LaunchedEffect(Unit) { vm.load() }

    // Efectos: navegación a resultados
    LaunchedEffect(Unit) {
        vm.effects.collectLatest { effect ->
            when (effect) {
                is FilterEffect.ShowResults -> onNavigateToResults(effect.items)
            }
        }
    }

    // Llama a la VIEW (debe existir en el MISMO paquete y con esta firma)
    FilterView(
        state = state,
        onToggleTag = vm::toggleTag,
        onSearch = { vm.search(FilterMode.AND) }, // AND por defecto (intersección)
        onOpenDetail = onOpenDetail
    )
}
