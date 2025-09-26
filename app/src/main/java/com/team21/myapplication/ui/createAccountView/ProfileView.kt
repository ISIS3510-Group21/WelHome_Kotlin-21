package com.team21.myapplication.ui.createAccountView

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
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
import com.team21.myapplication.ui.components.buttons.CustomRadioButton
import com.team21.myapplication.ui.components.buttons.CustomToggleButton
import com.team21.myapplication.ui.components.icons.AppIcons
import com.team21.myapplication.ui.components.navbar.StepProgressBar
import com.team21.myapplication.ui.components.text.BlackText
import com.team21.myapplication.ui.components.text.BlueText
import com.team21.myapplication.ui.theme.BlueCallToAction
import com.team21.myapplication.ui.theme.GrayIcon
import com.team21.myapplication.ui.theme.LavanderLight
import com.team21.myapplication.ui.theme.WhiteBackground


@Composable
fun ProfileLayout() {
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
            currentStep = 3 // "Profile" is the current step
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Section Title: "Profile"
        BlueText(
            text = "Profile",
            size = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Input Field Label: Type of user
        BlueText(
            text = "Type of user",
            size = 16.sp,
            modifier = Modifier.fillMaxWidth(),
            fontWeight = FontWeight.Bold,
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Radio Buttons for "Type of user"
        Column(modifier = Modifier.fillMaxWidth()) {
            CustomRadioButton(
                text = "Host (Offering accomodation)",
                selected = false,
                onClick = { }
            )
            CustomRadioButton(
                text = "Student (Looking for accomodation)",
                selected = true,
                onClick = { }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Input Field Label: City of accommodation
        BlueText(
            text = "City of accommodation",
            size = 16.sp,
            modifier = Modifier.fillMaxWidth(),
            fontWeight = FontWeight.Bold,
        )
        Spacer(modifier = Modifier.height(8.dp))
        // Custom Dropdown Menu for City
        val cities = listOf("Bogotá", "Medellín", "Cali", "Barranquilla")
        var selectedCity by remember { mutableStateOf("Select") }
        CustomDropdownMenu(
            placeholderText = selectedCity,
            items = cities,
            onItemSelected = { city -> selectedCity = city },
            textColor = GrayIcon,
            backgroundColor = LavanderLight,
            menuBackgroundColor = WhiteBackground
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Input Field Label: Profile photo (optional)
        BlueText(
            text = "Profile photo (optional)",
            size = 16.sp,
            modifier = Modifier.fillMaxWidth(),
            fontWeight = FontWeight.Bold,
        )
        Spacer(modifier = Modifier.height(8.dp))
        // Upload Profile Photo Button
        BlueButton(
            text = "Upload profile photo",
            onClick = { /* TODO: Handle photo upload */ },
            modifier = Modifier.fillMaxWidth(), // Fill width for this button
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Input Field Label: Genre (optional)
        BlueText(
            text = "Genre (optional)",
            size = 16.sp,
            modifier = Modifier.fillMaxWidth(),
            fontWeight = FontWeight.Bold,
        )
        Spacer(modifier = Modifier.height(8.dp))
        // Custom Dropdown Menu for Genre
        val genres = listOf("Male", "Female", "Non-binary", "Prefer not to say")
        var selectedGenre by remember { mutableStateOf("Select") }
        CustomDropdownMenu(
            placeholderText = selectedGenre,
            items = genres,
            onItemSelected = { genre -> selectedGenre = genre },
            textColor = GrayIcon,
            backgroundColor = LavanderLight,
            menuBackgroundColor = WhiteBackground
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Toggle Switches for preferences
        Column(
            modifier = Modifier.fillMaxWidth(),
        ) {
            var checkedState1 by remember { mutableStateOf(false) }
            var checkedState2 by remember { mutableStateOf(false) }
            var checkedState3 by remember { mutableStateOf(false) }

            CustomToggleButton(
                text = "Accept terms and conditions",
                checked = checkedState1,
                onCheckedChange = { checkedState1 = it }
            )
            CustomToggleButton(
                text = "Accept privacy policy",
                checked = checkedState2,
                onCheckedChange = { checkedState2 = it }
            )
            CustomToggleButton(
                text = "Allow notifications emails",
                checked = checkedState3,
                onCheckedChange = { checkedState3 = it }
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Bottom Navigation Buttons (Previous, Submit)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(32.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BlackButton(
                text = "Previous",
                onClick = { /* TODO: Handle previous action */ },
                modifier = Modifier.weight(1f)
            )
            BlueButton(
                text = "Submit",
                onClick = { /* TODO: Handle submit action */ },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
fun ProfileLayoutPreview() {
    ProfileLayout()
}