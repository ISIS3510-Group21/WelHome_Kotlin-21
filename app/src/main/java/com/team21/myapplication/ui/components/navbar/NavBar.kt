package com.team21.myapplication.ui.components.navbar

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.team21.myapplication.ui.components.icons.AppIcons
import com.team21.myapplication.ui.theme.BlueCallToAction
import com.team21.myapplication.ui.theme.GrayIcon
import com.team21.myapplication.ui.theme.LocalDSTypography
import com.team21.myapplication.ui.theme.WhiteBackground
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState

sealed class AppDest(val route: String, val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    data object Home    : AppDest("home",    "Home",    AppIcons.Home)
    data object Saved   : AppDest("saved",   "Saved",   AppIcons.Saved)
    data object Forum   : AppDest("forum",   "Forum",   AppIcons.Forum)
    data object Visits  : AppDest("visits",  "Visits",  AppIcons.Visits)
    data object Profile : AppDest("profile", "Profile", AppIcons.Profile)

    companion object {
        val All = listOf(Home, Saved, Forum, Visits, Profile)
        fun byRoute(route: String?): AppDest = All.firstOrNull { it.route == route } ?: Home
    }
}

@Composable
fun AppNavBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier,
    showDivider: Boolean = true
) {
    val labelStyle = LocalDSTypography.current
        .NavBarDescription.copy(fontWeight = FontWeight.SemiBold)

    if (showDivider) {
        Divider(color = GrayIcon, thickness = 1.dp, modifier = Modifier.fillMaxWidth())
    }

    NavigationBar(containerColor = WhiteBackground, tonalElevation = 0.dp, modifier = modifier) {
        val selectedRoute = AppDest.byRoute(currentRoute).route
        AppDest.All.forEach { dest ->
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
                        style = labelStyle,
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
fun AppNavBar(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    AppNavBar(
        currentRoute = currentRoute,
        onNavigate = { route ->
            if (route != currentRoute) {
                navController.navigate(route) {
                    launchSingleTop = true
                    restoreState = true
                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                }
            }
        }
    )
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, name = "AppNavBar Preview")
@Composable
private fun PreviewAppNavBar() {
    com.team21.myapplication.ui.theme.AppTheme {
        AppNavBar(
            currentRoute = "home",
            onNavigate = { /* noop */ }
        )
    }
}
