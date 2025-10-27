package com.team21.myapplication.ui.components.navbar

import android.app.Activity
import android.content.Intent
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.team21.myapplication.data.local.SecureSessionManager
import com.team21.myapplication.data.repository.AuthRepository
import com.team21.myapplication.ui.createAccountView.WelcomeActivity
import com.team21.myapplication.ui.ownerMainView.OwnerMainScreen
import com.team21.myapplication.ui.profileView.ProfileRoute
import com.team21.myapplication.ui.myPostsView.MyPostsScreen

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
        composable(OwnerDest.MyPosts.route) { MyPostsScreen() }
        composable(OwnerDest.Forum.route)   { Text("Forum") }
        composable(OwnerDest.Visits.route)  { Text("Visits") }
        composable(OwnerDest.Profile.route) {
            val ctx = LocalContext.current
            val activity = ctx as Activity
            val auth = remember { AuthRepository() }
            // instancia app-context del session manager
            val session = remember { SecureSessionManager(ctx.applicationContext) }

            ProfileRoute(
                onLogout = {
                    // 1) Borrar sesión local siempre -incluye snapshot de perfil-
                    session.clearSession()

                    // 2) Cerrar sesión
                    auth.signOut()

                    // 3) Ir a Welcome limpiando el back stack
                    activity.startActivity(
                        Intent(activity, WelcomeActivity::class.java).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        }
                    )

                    // 4) Cerrar esta Activity por si acaso
                    activity.finish()
                }
            )
        }
    }
}


