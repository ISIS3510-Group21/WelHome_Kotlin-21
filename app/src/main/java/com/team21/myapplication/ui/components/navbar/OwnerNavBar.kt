package com.team21.myapplication.ui.components.navbar

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.team21.myapplication.ui.theme.*

@Composable
fun OwnerNavBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit,
) {
    NavigationBar(containerColor = WhiteBackground, tonalElevation = 0.dp) {
        val selectedRoute = OwnerDest.byRoute(currentRoute).route
        OwnerDest.All.forEach { dest ->
            val selected = dest.route == selectedRoute
            NavigationBarItem(
                selected = selected,
                onClick = { onNavigate(dest.route) },
                icon = {
                    Icon(
                        imageVector = dest.icon,
                        contentDescription = dest.label,
                        tint = if (selected) BlueCallToAction else GrayIcon
                    )
                },
                label = {
                    Text(
                        text = dest.label,
                        style = LocalDSTypography.current.NavBarDescription,
                        color = if (selected) BlueCallToAction else GrayIcon
                    )
                },
                alwaysShowLabel = true,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = BlueCallToAction,
                    unselectedIconColor = GrayIcon,
                    selectedTextColor = BlueCallToAction,
                    unselectedTextColor = GrayIcon,
                    indicatorColor = WhiteBackground
                )
            )
        }
    }
}

@Composable
fun OwnerNavBar(navController: NavController) {
    val backStack by navController.currentBackStackEntryAsState()
    val current = backStack?.destination?.route
    OwnerNavBar(
        currentRoute = current,
        onNavigate = { route ->
            if (route != current) {
                navController.navigate(route) {
                    launchSingleTop = true
                    restoreState = true
                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                }
            }
        }
    )
}


