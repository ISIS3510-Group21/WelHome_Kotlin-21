package com.team21.myapplication.ui.components.navbar

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import androidx.compose.material3.Text
import androidx.compose.ui.platform.LocalContext
import com.team21.myapplication.ui.filterView.FilterRoute
import com.team21.myapplication.ui.filterView.results.FilterResultsCache
import com.team21.myapplication.ui.filterView.results.FilterResultsRoute
import com.team21.myapplication.ui.mapsearch.MapSearchActivity

object DetailRoutes {
    const val DETAIL_PATTERN = "detail/{housingId}"
    fun detail(id: String) = "detail/$id"
}

@Composable
fun AppNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val ctx = LocalContext.current
    NavHost(
        navController = navController,
        startDestination = AppDest.Home.route,
        modifier = modifier
    ) {

        // HOME
        composable(AppDest.Home.route) {
            com.team21.myapplication.ui.main.MainScreen(
                onOpenFilters = { navController.navigate("filters") },
                onOpenDetail  = { id -> navController.navigate(DetailRoutes.detail(id)) },
                onNavigateToDetail = { id -> navController.navigate(DetailRoutes.detail(id)) }
            )
        }

        // PANTALLA DE FILTROS
        composable("filters") {
            FilterRoute(
                onNavigateToResults = { items ->
                    FilterResultsCache.items = items
                    navController.navigate("filterResults")
                },
                onOpenDetail = { housingId ->
                    navController.navigate(DetailRoutes.detail(housingId))
                },
                onMapSearch = {
                    ctx.startActivity(Intent(ctx, MapSearchActivity::class.java))
                }
            )
        }

        // DESTINO con Deep Link (para notificaciones)
        composable(
            route = "filterResults?tags={tags}",
            arguments = listOf(
                navArgument("tags") {
                    type = NavType.StringType
                    defaultValue = ""
                    nullable = true
                }
            ),
            deepLinks = listOf(
                navDeepLink { uriPattern = "welhome://filterResults?tags={tags}" }
            )
        ) { backStackEntry ->
            val tagsCsv = backStackEntry.arguments?.getString("tags").orEmpty()
            // TODO: si tu FilterResultsRoute soporta este parámetro, pásalo.
            // Por ahora tiramos de la cache poblada desde FilterRoute.
            FilterResultsRoute(
                onOpenDetail = { id -> navController.navigate(DetailRoutes.detail(id)) },
                onNavigateBottomBar = { /* no-op aquí */ }
            )
        }

        // RESULTADOS (cuando vienes desde la pantalla de filtros)
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

        // OTROS (placeholders)
        composable(AppDest.Saved.route)   { Text("Saved") }
        composable(AppDest.Forum.route)   {
            // Como pediste: Forum abre Filtros
            FilterRoute(
                onNavigateToResults = { items ->
                    FilterResultsCache.items = items
                    navController.navigate("filterResults")
                },
                onOpenDetail = { housingId ->
                    navController.navigate(DetailRoutes.detail(housingId))
                },
                onMapSearch = {
                    ctx.startActivity(Intent(ctx, MapSearchActivity::class.java))
                }
            )
        }
        composable(AppDest.Visits.route)  { Text("Visits") }
        composable(AppDest.Profile.route) { Text("Profile") }

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
