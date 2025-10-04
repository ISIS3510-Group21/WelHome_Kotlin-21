package com.team21.myapplication.ui.filterView.results

import androidx.compose.runtime.Composable
import com.team21.myapplication.ui.filterView.state.PreviewCardUi

@Composable
fun FilterResultsRoute(
    onOpenDetail: (String) -> Unit,
    onNavigateBottomBar: (String) -> Unit
) {
    val items: List<PreviewCardUi> = FilterResultsCache.items
    FilterResultsView(
        items = items,
        onOpenDetail = onOpenDetail,
        onNavigateBottomBar = onNavigateBottomBar
    )
}