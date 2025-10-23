package com.team21.myapplication.ui.filterView.results

import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.team21.myapplication.data.repository.HousingTagRepository
import com.team21.myapplication.domain.usecase.SearchPreviewsByTagsUseCase
import com.team21.myapplication.domain.mapper.FilterUiMapper
import com.team21.myapplication.ui.filterView.state.PreviewCardUi
import kotlinx.coroutines.launch

@Composable
fun FilterResultsFromTagNamesRoute(
    tagNamesCsv: String,
    onOpenDetail: (String) -> Unit
) {
    val tagRepo = remember { HousingTagRepository() }
    val searchUseCase = remember { SearchPreviewsByTagsUseCase(tagRepo) }
    var items by remember { mutableStateOf<List<PreviewCardUi>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }

    val scope = rememberCoroutineScope()

    LaunchedEffect(tagNamesCsv) {
        scope.launch {
            loading = true
            // 1) Traigo todos los tags y mapeo nombres -> IDs (case-insensitive, trimmed)
            val allTags = tagRepo.getAllTags()
            val wanted = tagNamesCsv.split(",").map { it.trim().lowercase() }.filter { it.isNotEmpty() }
            val selectedIds = allTags.filter { it.name.trim().lowercase() in wanted }.map { it.id }

            // 2) Si nada matchea, no fallamos: no hay filtros (o podrías decidir un default)
            val previews = if (selectedIds.isNotEmpty()) {
                searchUseCase(selectedIds, com.team21.myapplication.data.repository.FilterMode.AND)
            } else {
                emptyList()
            }
            items = FilterUiMapper.toPreviewUi(previews)
            loading = false
        }
    }

    FilterResultsView(
        items = items,
        onOpenDetail = onOpenDetail,
        onNavigateBottomBar = {}
    )
}
