package com.team21.myapplication.ui.components.navbar

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.team21.myapplication.ui.ownerMainView.OwnerMainScreen

@Composable
fun OwnerNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = OwnerDest.Home.route,
        modifier = modifier
    ) {
        composable(OwnerDest.Home.route)    { OwnerMainScreen() }
        composable(OwnerDest.MyPosts.route) { Text("My posts") }   // TODO: tu pantalla de posts
        composable(OwnerDest.Forum.route)   { Text("Forum") }      // puedes reusar la de filtros si quieres
        composable(OwnerDest.Visits.route)  { Text("Visits") }
        composable(OwnerDest.Profile.route) { Text("Profile") }    // o tu ProfileRoute
    }
}


