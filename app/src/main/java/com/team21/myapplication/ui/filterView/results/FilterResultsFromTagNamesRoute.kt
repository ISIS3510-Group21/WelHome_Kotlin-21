package com.team21.myapplication.ui.filterView.results

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.team21.myapplication.data.repository.HousingTagRepository
import com.team21.myapplication.data.repository.FilterMode
import com.team21.myapplication.domain.usecase.SearchPreviewsByTagsUseCase
import com.team21.myapplication.domain.mapper.FilterUiMapper
import com.team21.myapplication.ui.filterView.state.PreviewCardUi
import com.team21.myapplication.local.ResultsPrefs
import com.team21.myapplication.data.repository.offline.ResultsOfflineRepository
import com.team21.myapplication.utils.NetworkMonitor
import com.team21.myapplication.utils.TokenUtil
import com.team21.myapplication.ui.components.banners.ConnectivityBanner
import com.team21.myapplication.ui.components.banners.BannerPosition
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * ResultsFromTagNamesRoute
 * - Mapea nombres -> ids, busca previews (AND).
 * - Persiste combo en Hive (y LRU) y guarda token en ResultsPrefs.
 * - Muestra banner de conectividad.
 * - [MULTITHREADING] Nested coroutines IO dentro de Main.
 */
@Composable
fun FilterResultsFromTagNamesRoute(
    tagNamesCsv: String,
    onOpenDetail: (String) -> Unit
) {
    val ctx = LocalContext.current
    val isOnline by remember { NetworkMonitor.get(ctx).isOnline }.collectAsStateWithLifecycle()

    val tagRepo = remember { HousingTagRepository() }
    val searchUseCase = remember { SearchPreviewsByTagsUseCase(tagRepo) }
    val prefs = remember { ResultsPrefs.get(ctx) }
    val offline = remember { ResultsOfflineRepository(ctx) }

    var items by remember { mutableStateOf<List<PreviewCardUi>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }

    val scope = rememberCoroutineScope()

    LaunchedEffect(tagNamesCsv, isOnline) {
        scope.launch {
            loading = true

            // [MULTITHREADING] Anidado: trabajo IO dentro de coroutine Main
            val selectedIds: List<String> = withContext(Dispatchers.IO) {
                val allTags = tagRepo.getAllTags() // network
                val wanted = tagNamesCsv.split(",").map { it.trim().lowercase() }.filter { it.isNotEmpty() }
                allTags.filter { it.name.trim().lowercase() in wanted }.map { it.id }
            }

            val previews = if (selectedIds.isNotEmpty()) {
                withContext(Dispatchers.IO) { // [MULTITHREADING] IO
                    searchUseCase(selectedIds, FilterMode.AND)
                }
            } else {
                emptyList()
            }

            val ui = FilterUiMapper.toPreviewUi(previews)
            items = ui
            loading = false

            // [EVENTUAL CONNECTIVITY] [CACHE-FIRST] persistencia de combo + token
            val token = TokenUtil.tokenFor(selectedIds, FilterMode.AND.name)

            // [MULTITHREADING] guardar en prefs + hive en IO
            scope.launch {
                withContext(Dispatchers.IO) {
                    prefs.saveLastResultsToken(token)     // [LOCAL STORAGE]
                    offline.saveCombo(token, ui)          // [LOCAL STORAGE] [CACHING]
                }
            }
        }
    }

    androidx.compose.foundation.layout.Column {
        ConnectivityBanner(visible = !isOnline, position = BannerPosition.Top)

        FilterResultsView(
            items = items,
            onOpenDetail = onOpenDetail,
            onNavigateBottomBar = {}
        )
    }
}
