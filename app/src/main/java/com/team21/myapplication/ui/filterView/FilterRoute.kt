package com.team21.myapplication.ui.filterView

import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.team21.myapplication.data.repository.FilterMode
import com.team21.myapplication.ui.filterView.state.PreviewCardUi
import kotlinx.coroutines.flow.collectLatest

// NEW
import androidx.compose.foundation.layout.Column
import androidx.compose.ui.Modifier
import com.team21.myapplication.ui.components.banners.ConnectivityBanner
import com.team21.myapplication.ui.components.banners.BannerPosition
import com.team21.myapplication.utils.NetworkMonitor
import androidx.compose.ui.platform.LocalContext

@Composable
fun FilterRoute(
    onNavigateToResults: (items: List<PreviewCardUi>) -> Unit,
    onOpenDetail: (String) -> Unit,
    onMapSearch: () -> Unit
) {
    val vm: FilterViewModel = viewModel()
    val state = vm.state.collectAsStateWithLifecycle().value

    // [EVENTUAL CONNECTIVITY] Observe connectivity changes
    val ctx = LocalContext.current
    val isOnline = remember { NetworkMonitor.get(ctx).isOnline }.collectAsStateWithLifecycle().value

    // Initial load
    LaunchedEffect(Unit) { vm.load() }

    // Effects: navigate to results
    LaunchedEffect(Unit) {
        vm.effects.collectLatest { effect ->
            when (effect) {
                is FilterEffect.ShowResults -> onNavigateToResults(effect.items)
            }
        }
    }

    // VIEW + Banner
    Column {
        // [EVENTUAL CONNECTIVITY] Banner visible when offline
        ConnectivityBanner(
            visible = !isOnline,
            position = BannerPosition.Top
        )
        FilterView(
            state = state,
            onToggleTag = vm::toggleTag,
            onSearch = { vm.search(FilterMode.AND) },
            onOpenDetail = onOpenDetail,
            onMapSearch = onMapSearch
        )
    }
}
