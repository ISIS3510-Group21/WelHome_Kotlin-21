package com.team21.myapplication.ui.components.navbar

import com.team21.myapplication.ui.components.icons.AppIcons

sealed class OwnerDest(
    val route: String,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    data object Home     : OwnerDest("owner_home",    "Home",     AppIcons.Home)
    data object MyPosts  : OwnerDest("owner_myposts", "My posts", AppIcons.Saved /* o tu ícono de posts */)
    data object Forum    : OwnerDest("owner_forum",   "Forum",    AppIcons.Forum)
    data object Visits   : OwnerDest("owner_visits",  "Visits",   AppIcons.Visits)
    data object Profile  : OwnerDest("owner_profile", "Profile",  AppIcons.Profile)

    companion object {
        val All = listOf(Home, MyPosts, Forum, Visits, Profile)
        fun byRoute(route: String?): OwnerDest = All.firstOrNull { it.route == route } ?: Home
    }
}
