package com.team21.myapplication.ui.createAccountView

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
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
import com.team21.myapplication.ui.components.buttons.BlackButton
import com.team21.myapplication.ui.components.buttons.BlueButton
import com.team21.myapplication.ui.components.buttons.CustomDropdownMenu
import com.team21.myapplication.ui.components.icons.AppIcons
import com.team21.myapplication.ui.components.inputs.PlaceholderTextField
import com.team21.myapplication.ui.components.navbar.StepProgressBar
import com.team21.myapplication.ui.components.text.BlackText
import com.team21.myapplication.ui.components.text.BlueText
import com.team21.myapplication.ui.theme.BlueCallToAction

@Composable
fun VerificationLayout() {
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
            Icon(
                imageVector = AppIcons.GoBack,
                contentDescription = "Back",
                modifier = Modifier.size(24.dp),
                tint = BlueCallToAction
            )
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
            currentStep = 2
        )

       Spacer(modifier = Modifier.height(32.dp))

        // Section Title: "Verification"
        BlueText(
            text = "Verification",
            size = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Input Field Label: Phone Number
        BlueText(
            text = "Phone Number",
            size = 16.sp,
            modifier = Modifier.fillMaxWidth(),
            fontWeight = FontWeight.Bold,
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Phone Number Input (Country Code Dropdown + Phone Number Input Field)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Placeholder for Country Code Dropdown (e.g., +1)

            val prefixes = listOf("+57", "+60", "+14", "+85")
            var numbersPrefix by remember { mutableStateOf("Select Prefix") }

            CustomDropdownMenu(
                placeholderText = " + 1",
                items = prefixes,
                modifier = Modifier
                    .width(90.dp) // Fixed width for the dropdown
                    .height(52.dp),
                onItemSelected = { prefix -> numbersPrefix = prefix }
            )

            // Placeholder for Phone Number Input Field
            PlaceholderTextField(
                placeholderText = "(999) 111-0000"
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // "Send code" Button
        BlueButton(text = "Send code",
            modifier = Modifier
                .width(180.dp),
            onClick = { /* TODO: Handle send code action */ })

        Spacer(modifier = Modifier.height(24.dp))

        // Description Text for Code Input - Centered using a Box
        Box(
            modifier = Modifier.fillMaxWidth(), // Box takes full width
            contentAlignment = Alignment.Center // Center content (the BlackText) within the Box
        ) {
            BlackText(
                text = "Please enter the code we sent to your cell phone to continue. The code may take a few minutes to arrive.",
                // No modifier needed on BlackText itself for width if Box handles it
                size = 14.sp
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Code Input Fields (5 individual Boxes)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(6) {
                PlaceholderTextField(
                    placeholderText = "",
                    modifier = Modifier
                        .weight(1f) // Each box takes equal weight
                    .height(52.dp) // Fixed height for the code input box
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))


        // "Not working? Resend code" link - Centered using a Box
        Box(
            modifier = Modifier.fillMaxWidth(), // Box takes full width
            contentAlignment = Alignment.Center // Center content (the BlueText) within the Box
        ) {
            BlueText(
                text = "Not working? Resend code",
                size = 12.sp,
                // No modifier needed on BlueText itself for width if Box handles it
                fontWeight = FontWeight.Bold,
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // "Verify" Button
        BlueButton(text = "Verify", onClick = { /* TODO: Handle verify action */ })

        Spacer(modifier = Modifier.height(24.dp)) // Spacer before the bottom buttons

        // Bottom Navigation Buttons (Previous, Next)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(32.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Placeholder for "Previous" Button
            BlackButton(
                text = "Previous",
                onClick = { /* TODO: Handle next action */ },
                modifier = Modifier.weight(1f)
            )
            // Placeholder for "Next" Button
            BlueButton(
                text = "Next",
                onClick = { /* TODO: Handle next action */ },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

// Preview
@Preview(showBackground = true, widthDp = 360)
@Composable
fun VerificationLayoutPreview() {
    VerificationLayout()
}