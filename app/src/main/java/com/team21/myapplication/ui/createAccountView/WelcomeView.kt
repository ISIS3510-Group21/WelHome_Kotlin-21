package com.team21.myapplication.ui.createAccountView

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.team21.myapplication.ui.components.buttons.BlueButton
import com.team21.myapplication.ui.components.buttons.GrayButton
import com.team21.myapplication.ui.components.icons.AppIcons
import com.team21.myapplication.ui.components.inputs.PlaceholderTextField
import com.team21.myapplication.ui.components.text.BlackText
import com.team21.myapplication.ui.components.text.BlueText
import com.team21.myapplication.ui.theme.BlueCallToAction
import com.team21.myapplication.ui.theme.GrayIcon
import com.team21.myapplication.R
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource

@Composable
fun WelcomeLayout() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()) // Enable scrolling for long content
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(36.dp))

        Image(
            painter = painterResource(R.drawable.app_logo),
            contentDescription = "App Logo",
            modifier = Modifier.size(250.dp),
        )

        // Input Field: Name
        BlueText(
            text = "Email",
            size = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        PlaceholderTextField(
            placeholderText = "youremail@example.com",
            value ="",
            onValueChange = {},
        )
        Spacer(modifier = Modifier.height(32.dp))

        // Input Field: Password
        BlueText(
            text = "Password",
            size = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        PlaceholderTextField(
            placeholderText = "**********",
            value ="",
            onValueChange = {},
            trailingIcon = {
                Icon(
                    imageVector = AppIcons.PasswordEye,
                    contentDescription = "Info",
                    tint = GrayIcon
                )
            }
        )
        Spacer(modifier = Modifier.height(8.dp))
        BlueText(text = "Forgot password?")
        Spacer(modifier = Modifier.height(32.dp))
        // Blue Button: "LogIn"
        BlueButton(text = "LogIn", onClick = { /* TODO: Handle next action */ })

        Spacer(modifier = Modifier.height(40.dp))
        BlackText(text="Not a member?")

        Spacer(modifier = Modifier.height(8.dp))
        GrayButton(text = "Register now", onClick = { /* TODO: Handle next action */ })

    }
}

// Preview
@Preview(showBackground = true, widthDp = 360)
@Composable
fun WelcomeLayoutPreview() {
    WelcomeLayout()
}