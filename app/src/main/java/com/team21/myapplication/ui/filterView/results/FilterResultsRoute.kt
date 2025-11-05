package com.team21.myapplication.ui.filterView.results

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.team21.myapplication.ui.filterView.state.PreviewCardUi
import com.team21.myapplication.local.ResultsPrefs
import com.team21.myapplication.data.repository.offline.ResultsOfflineRepository
import com.team21.myapplication.utils.NetworkMonitor
import com.team21.myapplication.ui.components.banners.ConnectivityBanner
import com.team21.myapplication.ui.components.banners.BannerPosition
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * ResultsRoute
 * [EVENTUAL CONNECTIVITY] [CACHE-FIRST] [LOCAL STORAGE] [CACHING] [MULTITHREADING]
 * - Banner de conectividad.
 * - Si el cache en memoria está vacío, intenta rehidratar por token (prefs -> Hive/LRU).
 * - Si offline y no existe la combinación, muestra 5 previews de fallback desde Hive.
 */
@Composable
fun FilterResultsRoute(
    onOpenDetail: (String) -> Unit,
    onNavigateBottomBar: (String) -> Unit ={}
) {
    val ctx = LocalContext.current
    val isOnline by remember { NetworkMonitor.get(ctx).isOnline }.collectAsStateWithLifecycle()

    // repos offline + prefs
    val offline = remember { ResultsOfflineRepository(ctx) }
    val prefs = remember { ResultsPrefs.get(ctx) }

    // estado local
    var items by remember { mutableStateOf<List<PreviewCardUi>>(FilterResultsCache.items) }
    var loading by remember { mutableStateOf(false) }

    // Rehidratación si no hay datos en memoria
    LaunchedEffect(isOnline) {
        if (items.isEmpty()) {
            loading = true
            val token = withContext(Dispatchers.IO) { prefs.readLastResultsToken() } // [LOCAL STORAGE] [MULTITHREADING]
            if (token != null) {
                val fromRepo = offline.loadCombo(token) // [EVENTUAL CONNECTIVITY] [CACHE-FIRST]
                if (fromRepo != null && fromRepo.isNotEmpty()) {
                    items = fromRepo
                } else if (!isOnline) {
                    // [EVENTUAL CONNECTIVITY] offline fallback (no combo disponible)
                    items = offline.fallbackFive()
                }
            } else if (!isOnline) {
                items = offline.fallbackFive()
            }
            loading = false
        }
    }

    androidx.compose.foundation.layout.Column {
        ConnectivityBanner(visible = !isOnline, position = BannerPosition.Top)

        FilterResultsView(
            items = items,
            onOpenDetail = onOpenDetail,
            onNavigateBottomBar = onNavigateBottomBar
        )
    }
}
