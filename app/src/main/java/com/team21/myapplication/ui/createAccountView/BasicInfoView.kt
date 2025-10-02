package com.team21.myapplication.ui.createAccountView

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.team21.myapplication.ui.components.buttons.BlueButton
import com.team21.myapplication.ui.components.icons.AppIcons
import com.team21.myapplication.ui.components.inputs.PlaceholderTextField
import com.team21.myapplication.ui.components.navbar.StepProgressBar
import com.team21.myapplication.ui.components.text.BlackText
import com.team21.myapplication.ui.components.text.BlueText
import com.team21.myapplication.ui.theme.*

@Composable
fun CreateAccountLayout() {
    var text1 by remember { mutableStateOf("") }
    var text2 by remember { mutableStateOf("") }
    var text3 by remember { mutableStateOf("") }
    var text4 by remember { mutableStateOf("") }
    var text5 by remember { mutableStateOf("") }
    var text6 by remember { mutableStateOf("") }
    var text7 by remember { mutableStateOf("") }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()) // Enable scrolling for long content
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Bar (Back icon and "Create account" title)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            IconButton(onClick = {/* TODO: action */ }) {
                Icon(
                    imageVector = AppIcons.GoBack,
                    contentDescription = "Back",
                    modifier = Modifier.size(24.dp),
                    tint = BlueCallToAction
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            BlackText(
                text = "Create account",
                size = 24.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Step Progress Bar
        StepProgressBar(
            stepLabels = listOf("Basic Info", "Verification", "Profile"),
            currentStep = 1
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Section Title: "Basic Information"
        BlueText(
            text = "Basic Information",
            size = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Input Field: Name
        BlueText(
            text = "Name",
            size = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        PlaceholderTextField(
            placeholderText = "Jhoan Doe",
            value = text1,
            onValueChange = { text1 = it }
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Input Field: Email
        BlueText(
            text = "Email",
            size = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        PlaceholderTextField(
            placeholderText = "youremail@example.com",
            value = text2,
            onValueChange = { text2 = it }
        )
        Spacer(modifier = Modifier.height(16.dp))

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
            value = text3,
            onValueChange = { text3 = it },
            trailingIcon = {
                Icon(
                    imageVector = AppIcons.PasswordEye,
                    contentDescription = "Info",
                    tint = GrayIcon
                )
            }
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Input Field: Confirm password
        BlueText(
            text = "Confirm password",
            size = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        PlaceholderTextField(
            placeholderText = "**********",
            value = text4,
            onValueChange = { text4 = it },
            trailingIcon = {
                Icon(
                    imageVector = AppIcons.PasswordEye,
                    contentDescription = "Info",
                    tint = GrayIcon
                )
            }
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Section: Birth date (Day, Month, Year dropdowns)
        BlueText(
            text = "Birth date",
            size = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Placeholder for Day dropdown
            PlaceholderTextField(
                placeholderText = "Day",
                value = text5,
                onValueChange = { text5 = it },
                modifier = Modifier
                    .height(50.dp)
                    .weight(1f)
            )

            // Placeholder for Month dropdown
            PlaceholderTextField(
                placeholderText = "Month",
                value = text6,
                onValueChange = { text6 = it },
                modifier = Modifier
                    .height(50.dp)
                    .weight(1f)
            )
            // Placeholder for Year dropdown
            PlaceholderTextField(
                placeholderText = "Year",
                value = text7,
                onValueChange = { text7 = it },
                modifier = Modifier
                    .height(50.dp)
                    .weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(32.dp)) // Spacer before the button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Box(){
                Spacer(modifier = Modifier.width(180.dp))
            }
            // Blue Button: "Next"
            BlueButton(text = "Next", onClick = { /* TODO: Handle next action */ })
        }
    }
}

// Preview
@Preview(showBackground = true, widthDp = 360)
@Composable
fun CreateAccountLayoutPreview() {
    CreateAccountLayout()
}