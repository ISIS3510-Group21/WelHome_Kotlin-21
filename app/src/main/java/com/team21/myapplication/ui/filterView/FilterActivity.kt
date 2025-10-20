package com.team21.myapplication.ui.filterView

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.team21.myapplication.ui.components.navbar.AppNavBar
import com.team21.myapplication.ui.filterView.results.FilterResultsActivity
import com.team21.myapplication.ui.filterView.results.FilterResultsCache
import com.team21.myapplication.ui.filterView.state.PreviewCardUi
import com.team21.myapplication.ui.detailView.DetailHousingActivity
import com.team21.myapplication.ui.mapsearch.MapSearchActivity
import com.team21.myapplication.ui.theme.AppTheme

class FilterActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AppTheme {
                // Nota: usamos AppNavBar para mantener estética (como MainActivity).
                val navController = rememberNavController()

                Scaffold(
                    bottomBar = { AppNavBar(navController) } // misma navbar visual
                ) { innerPadding ->
                    // Aquí monto la Route del feature
                    FilterRoute(
                        onNavigateToResults = { items: List<PreviewCardUi> ->
                            // Aquí paso resultados vía cache simple y abro la Activity de resultados
                            FilterResultsCache.items = items
                            startActivity(Intent(this, FilterResultsActivity::class.java))
                        },
                        onOpenDetail = { housingId ->
                            startActivity(
                                Intent(this, DetailHousingActivity::class.java)
                                    .putExtra(DetailHousingActivity.EXTRA_HOUSING_ID, housingId)
                            )
                        },
                        onMapSearch = {
                            startActivity(Intent(this, MapSearchActivity::class.java))
                        }
                    )
                }
            }
        }
    }
}
