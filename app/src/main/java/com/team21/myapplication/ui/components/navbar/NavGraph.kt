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
import android.app.Activity
import com.team21.myapplication.ui.filterView.FilterActivity
import androidx.compose.ui.platform.LocalContext
import com.team21.myapplication.data.repository.AuthRepository
import com.team21.myapplication.ui.createAccountView.WelcomeActivity
import com.team21.myapplication.ui.mapsearch.MapSearchView
import com.team21.myapplication.ui.profileView.ProfileRoute
import com.team21.myapplication.ui.detailView.DetailHousingActivity

object DetailRoutes {
    const val DETAIL_PATTERN = "detail/{housingId}"
    fun detail(id: String) = "detail/$id"
}


@Composable
fun AppNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val mapSearchRoute = "mapSearch"
    val context = LocalContext.current
    NavHost(
        navController = navController,
        startDestination = AppDest.Home.route,
        modifier = modifier
    ) {

        // HOME

        composable(AppDest.Home.route) {
            com.team21.myapplication.ui.main.MainScreen(
                onOpenFilters = {
                    context.startActivity(Intent(context, FilterActivity::class.java))
                },
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
                    context.startActivity(
                        Intent(context, DetailHousingActivity::class.java)
                            .putExtra(DetailHousingActivity.EXTRA_HOUSING_ID, housingId)
                    )
                },
                onMapSearch = {
                    navController.navigate(mapSearchRoute)
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
                    context.startActivity(
                        Intent(context, DetailHousingActivity::class.java)
                            .putExtra(DetailHousingActivity.EXTRA_HOUSING_ID, housingId)
                    )
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

        // MAPA
        composable(mapSearchRoute) {
            MapSearchView(
                navController = navController,
                onNavigateToDetail = { id ->
                    navController.navigate(DetailRoutes.detail(id)) }
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
                    navController.navigate(mapSearchRoute)
                }
            )
        }
        composable(AppDest.Visits.route)  { Text("Visits") }
        composable(AppDest.Profile.route) {
            val ctx = LocalContext.current
            ProfileRoute(
                onLogout =
                {
                    // cerrar sesion
                    AuthRepository().signOut()

                    // devolver a la pantalla de welcome y limpiar backstack
                    val intent = Intent(ctx, WelcomeActivity::class.java).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    }
                    ctx.startActivity(intent)

                    // asegurar cierre de MainActivity actual
                    (ctx as? Activity)?.finish()
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
