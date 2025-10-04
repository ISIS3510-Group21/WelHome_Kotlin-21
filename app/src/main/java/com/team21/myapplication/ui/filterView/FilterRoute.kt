package com.team21.myapplication.ui.filterView

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.team21.myapplication.data.repository.FilterMode
import com.team21.myapplication.ui.filterView.state.PreviewCardUi
import kotlinx.coroutines.flow.collectLatest

// Firebase Analytics (los que ya te reconoce)
import com.google.firebase.analytics.analytics
import com.google.firebase.analytics.logEvent
import com.google.firebase.Firebase

/**
 * ROUTE:
 * - Crea/obtiene el ViewModel.
 * - OBSERVER: observa state y effects.
 * - Entrega state + callbacks a la View.
 */
@Composable
fun FilterRoute(
    onNavigateToResults: (items: List<PreviewCardUi>) -> Unit = {}
) {
    val vm: FilterViewModel = viewModel()
    val state by vm.state.collectAsState()

    // Carga inicial
    LaunchedEffect(Unit) {
        vm.load()
    }

    // Screen view para analytics
    LaunchedEffect(Unit) {
        Firebase.analytics.logEvent("screen_view_filters") {
            param("source", "FilterRoute")
        }
    }

    // Efectos: navegación a resultados
    LaunchedEffect(Unit) {
        vm.effects.collectLatest { effect ->
            when (effect) {
                is FilterEffect.ShowResults -> {
                    // Evento por navegación a resultados (opcional)
                    Firebase.analytics.logEvent("navigate_filter_results") {
                        param("results_count", effect.items.size.toLong())
                    }
                    onNavigateToResults(effect.items)
                }
            }
        }
    }

    FilterView(
        state = state,
        onToggleTag = vm::toggleTag,
        onSearch = { vm.search(FilterMode.AND) } // usa AND (intersección). Cambia a OR si quieres.
    )
}
