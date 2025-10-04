package com.team21.myapplication.ui.components.navbar

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.compose.material3.Text
import com.team21.myapplication.ui.filterView.FilterRoute
import com.team21.myapplication.ui.filterView.results.FilterResultsCache
import com.team21.myapplication.ui.filterView.results.FilterResultsRoute

object DetailRoutes {
    const val DETAIL_PATTERN = "detail/{housingId}"
    fun detail(id: String) = "detail/$id"
}

@Composable
fun AppNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = AppDest.Home.route,
        modifier = modifier
    ) {

        // HOME
        composable(AppDest.Home.route) {
            com.team21.myapplication.ui.main.MainScreen(
                onOpenFilters = { navController.navigate("filters") },
                // Ambas reciben el mismo callback para navegar al detalle
                onOpenDetail = { id -> navController.navigate(DetailRoutes.detail(id)) },
                onNavigateToDetail = { id -> navController.navigate(DetailRoutes.detail(id)) }
            )
        }

        // FILTROS (pantalla para escoger filtros)
        composable("filters") {
            FilterRoute(
                onNavigateToResults = { items ->
                    // OJO: usa SIEMPRE el mismo nombre de ruta que declaras abajo ("filterResults")
                    FilterResultsCache.items = items
                    navController.navigate("filterResults")
                },
                onOpenDetail = { housingId ->
                    navController.navigate(DetailRoutes.detail(housingId))
                }
            )
        }

        // SAVED (placeholder)
        composable(AppDest.Saved.route) { Text("Saved") }

        // FORUM -> también abre filtros (como pediste)
        composable(AppDest.Forum.route) {
            FilterRoute(
                onNavigateToResults = { items ->
                    FilterResultsCache.items = items
                    navController.navigate("filterResults")
                },
                onOpenDetail = { housingId ->
                    navController.navigate(DetailRoutes.detail(housingId))
                }
            )
        }

        // VISITS / PROFILE (placeholders)
        composable(AppDest.Visits.route) { Text("Visits") }
        composable(AppDest.Profile.route) { Text("Profile") }

        // RESULTADOS DE FILTROS
        composable("filterResults") {
            FilterResultsRoute(
                onOpenDetail = { housingId ->
                    navController.navigate(DetailRoutes.detail(housingId))
                },
                onNavigateBottomBar = { route ->
                    if (route != navController.currentDestination?.route) {
                        navController.navigate(route) {
                            launchSingleTop = true
                            restoreState = true
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                        }
                    }
                }
            )
        }

        // DETALLE
        composable(
            route = DetailRoutes.DETAIL_PATTERN,
            arguments = listOf(navArgument("housingId") { type = NavType.StringType })
        ) { backStackEntry ->
            val housingId = backStackEntry.arguments?.getString("housingId") ?: return@composable
            com.team21.myapplication.ui.detailView.DetailHousingRoute(
                housingId = housingId,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
