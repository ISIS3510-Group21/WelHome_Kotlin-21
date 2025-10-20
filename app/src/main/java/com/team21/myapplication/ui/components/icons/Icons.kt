package com.team21.myapplication.ui.components.icons

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Message
import androidx.compose.material.icons.filled.ArrowBackIos
import androidx.compose.material.icons.filled.CropFree
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.team21.myapplication.ui.theme.AppTheme
import com.team21.myapplication.ui.theme.GrayIcon

object AppIcons {
    val Home: ImageVector = Icons.Outlined.Home
    val Saved: ImageVector = Icons.Outlined.BookmarkBorder
    val Forum: ImageVector = Icons.Outlined.Forum
    val Visits: ImageVector = Icons.Outlined.CalendarMonth
    val Profile: ImageVector = Icons.Outlined.Person

    val Notification: ImageVector = Icons.Outlined.Notifications
    val Search: ImageVector = Icons.Outlined.Search
    val Houses: ImageVector = Icons.Outlined.HolidayVillage
    val Cabins: ImageVector = Icons.Outlined.Cottage
    val Rooms: ImageVector = Icons.Outlined.MeetingRoom
    val Apartments: ImageVector = Icons.Outlined.Apartment
    val StarFilled: ImageVector = Icons.Filled.Star
    val Star: ImageVector = Icons.Outlined.StarBorder
    val Phone: ImageVector = Icons.Outlined.Phone
    val Message: ImageVector = Icons.Outlined.ChatBubbleOutline
    val ArrowDropDown: ImageVector = Icons.Outlined.KeyboardArrowDown
    val PasswordEye: ImageVector = Icons.Outlined.Visibility
    val PasswordEyeOff: ImageVector = Icons.Outlined.VisibilityOff
    val Add: ImageVector = Icons.Outlined.Add
    val CameraAlt: ImageVector = Icons.Outlined.CameraAlt
    val Queue: ImageVector = Icons.Outlined.Queue
    val ProfilePicture: ImageVector = Icons.Outlined.Person

    val GoBack: ImageVector = Icons.Default.ArrowBackIos

    val HeartSaved: ImageVector = Icons.Default.FavoriteBorder

    val Bath: ImageVector = Icons.Outlined.Bathtub

    val SquareMeters: ImageVector = Icons.Default.CropFree

    val People: ImageVector = Icons.Outlined.Groups

    val Close: ImageVector = Icons.Outlined.Close
}

@Composable
fun IconTile(label: String, icon: ImageVector, iconColor: Color = MaterialTheme.colorScheme.onSurfaceVariant) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(80.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = iconColor,
            modifier = Modifier
                .size(28.dp)
                .padding(4.dp)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            maxLines = 1
        )
    }
}

@Preview(showBackground = true)
@Composable
fun Preview_AppIcons_All() {
    AppTheme {
        val items = listOf(
            "Home" to AppIcons.Home,
            "Saved" to AppIcons.Saved,
            "Forum" to AppIcons.Forum,
            "Visits" to AppIcons.Visits,
            "Profile" to AppIcons.Profile,
            "Notification" to AppIcons.Notification,
            "Search" to AppIcons.Search,
            "Houses" to AppIcons.Houses,
            "Cabins" to AppIcons.Cabins,
            "Rooms" to AppIcons.Rooms,
            "Apartments" to AppIcons.Apartments,
            "StarFilled" to AppIcons.StarFilled,
            "Star" to AppIcons.Star,
            "Phone" to AppIcons.Phone,
            "Message" to AppIcons.Message,
            "ArrowDropDown" to AppIcons.ArrowDropDown,
            "PasswordEye" to AppIcons.PasswordEye,
            "PasswordEyeOff" to AppIcons.PasswordEyeOff,
            "Add" to AppIcons.Add,
            "Queue" to AppIcons.Queue,
            "Profile" to AppIcons.ProfilePicture,
            "HeartSaved" to AppIcons.HeartSaved,
            "GoBack" to AppIcons.GoBack,
            "SquareMeters" to AppIcons.SquareMeters,
            "People" to AppIcons.People,
            "Bath" to AppIcons.Bath,


        )

        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            items.chunked(5).forEach { row ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    row.forEach { (label, icon) ->
                        IconTile(label, icon)
                    }
                }
            }
        }
    }
}
