package com.team21.myapplication.ui.components.navbar

import android.content.Intent
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.team21.myapplication.ui.createAccountView.WelcomeActivity
import com.team21.myapplication.ui.ownerMainView.OwnerMainScreen
import com.team21.myapplication.ui.profileView.ProfileRoute

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
        composable(OwnerDest.MyPosts.route) { Text("My posts") }
        composable(OwnerDest.Forum.route)   { Text("Forum") }
        composable(OwnerDest.Visits.route)  { Text("Visits") }
        composable(OwnerDest.Profile.route) {
            val ctx = LocalContext.current
            ProfileRoute(
                onLogout = {
                    ctx.startActivity(
                        Intent(ctx, WelcomeActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        }
                    )
                }
            )
        }    // o tu ProfileRoute
    }
}


