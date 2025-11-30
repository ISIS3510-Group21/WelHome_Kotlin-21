package com.team21.myapplication.ui.profileView

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.team21.myapplication.R
import com.team21.myapplication.ui.components.buttons.BlueButton
import com.team21.myapplication.ui.components.icons.ProfilePicture
import com.team21.myapplication.ui.components.text.BlackText
import com.team21.myapplication.ui.components.text.BlueText
import com.team21.myapplication.ui.components.text.GrayText
import androidx.compose.ui.unit.Dp
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.ui.platform.LocalContext
import com.team21.myapplication.utils.NetworkMonitor
import com.team21.myapplication.utils.PendingProfileSync
import com.team21.myapplication.data.model.StudentUser

@Composable
fun ProfileView(
    onLogout: () -> Unit = {},
    onEditProfile: () -> Unit = {},
    onPublishPending: (StudentUser) -> Unit = {},
    name: String = "",
    email: String = "",
    country: String = "",
    phoneNumber: String = "",
    contentTopPadding: Dp = 0.dp
) {
    val context = LocalContext.current
    val networkMonitor = NetworkMonitor.get(context)
    val isOnline = networkMonitor.isOnline.collectAsState().value

    // Al recuperar conexión, intentar publicar cambios pendientes
    LaunchedEffect(isOnline) {
        if (isOnline) {
            PendingProfileSync.load(context)?.let { pending ->
                onPublishPending(pending)
                PendingProfileSync.clear(context)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()) // Enable scrolling for long content
            .padding(top = contentTopPadding)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Banner informativo si hay datos sin publicar
        if (PendingProfileSync.hasPending(context)) {
            Surface(
                tonalElevation = 2.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                Text(
                    text = "There are profile changes pending to be published.",
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Spacer(modifier = Modifier.width(32.dp))
            BlackText(
                text = "Your profile",
                size = 30.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        ProfilePicture(
            painter = painterResource(R.drawable.profile_picture_owner),
            modifier = Modifier.size(150.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        BlackText(
            text = name,
            size = 24.sp,
            fontWeight = FontWeight.Bold
        )

        GrayText(
            text = email,
            size = 14.sp,
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Section Title: "Basic Information"
        BlueText(
            text = "Personal Information",
            size = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Name
        BlackText(
            text = "Name",
            size = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(4.dp))
        BlackText(
            text = name,
            size = 16.sp,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Email
        BlackText(
            text = "Email",
            size = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(4.dp))
        BlackText(
            text = email,
            size = 16.sp,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        // country
        BlackText(
            text = "Country",
            size = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(4.dp))
        BlackText(
            text = country,
            size = 16.sp,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // phone number
        BlackText(
            text = "Phone number",
            size = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(4.dp))
        BlackText(
            text = phoneNumber,
            size = 16.sp,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(32.dp)) // Spacer before the button

        BlueButton(
            text = "Edit Profile",
            onClick = onEditProfile
        )

        Spacer(modifier = Modifier.height(16.dp))

        BlueButton(
            text = "Log out",
            onClick = onLogout
        )

    }
}

// Preview
@Preview(showBackground = true, widthDp = 360)
@Composable
fun ProfileLayoutPreview() {
    ProfileView()
}
