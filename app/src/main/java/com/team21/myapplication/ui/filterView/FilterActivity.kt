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
import com.team21.myapplication.ui.components.navbar.AppNavBar
import com.team21.myapplication.ui.components.navbar.AppDest
import com.team21.myapplication.ui.main.MainActivity
class FilterActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AppTheme {
                Scaffold(
                    bottomBar = {
                        AppNavBar(
                            currentRoute = AppDest.Home.route, // o null si no quieres resaltar
                            onNavigate = { route ->
                                startActivity(
                                    Intent(this, MainActivity::class.java)
                                        .putExtra(MainActivity.EXTRA_START_DEST, route)
                                )
                                // opcional: evita apilar esta Activity
                                finish()
                            }
                        )
                    }
                ) { innerPadding ->
                    FilterRoute(
                        onNavigateToResults = { items ->
                            FilterResultsCache.items = items
                            startActivity(Intent(this, FilterResultsActivity::class.java))
                        },
                        onOpenDetail = { id ->
                            startActivity(
                                Intent(this, DetailHousingActivity::class.java)
                                    .putExtra(DetailHousingActivity.EXTRA_HOUSING_ID, id)
                            )
                        },
                        onMapSearch = {
                            // si tienes Map como tab en Main, puedes mandar a Main también
                            startActivity(
                                Intent(this, MainActivity::class.java)
                                    .putExtra(MainActivity.EXTRA_START_DEST, "mapSearch")
                            )
                            finish()
                        }
                    )
                }
            }
        }
    }
}
