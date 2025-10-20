package com.team21.myapplication.ui.createAccountView

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.team21.myapplication.ui.components.buttons.BlueButton
import com.team21.myapplication.ui.components.buttons.CustomDropdownMenu
import com.team21.myapplication.ui.components.buttons.CustomRadioButton
import com.team21.myapplication.ui.components.icons.AppIcons
import com.team21.myapplication.ui.components.inputs.PlaceholderTextField
import androidx.lifecycle.viewmodel.compose.viewModel
import com.team21.myapplication.ui.components.text.BlackText
import com.team21.myapplication.ui.components.text.BlueText
import com.team21.myapplication.ui.theme.*
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.text.input.KeyboardType
import com.team21.myapplication.ui.createAccountView.state.OperationState

@Composable
fun SignUpLayout(
    viewModel: SignUpViewModel = viewModel(),
    onSignUpSuccess: () -> Unit = {},
    onNavigateBack: () -> Unit = {}
) {
    // Observe state
    val uiState by viewModel.uiState.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

// Handle states - Reacciona al operationState dentro de uiState
    LaunchedEffect(uiState.operationState) {
        when (val state = uiState.operationState) {
            is OperationState.Success -> {
                snackbarHostState.showSnackbar("Successful registration!")
                onSignUpSuccess()
            }
            is OperationState.Error -> {
                snackbarHostState.showSnackbar(state.message)
                viewModel.resetState()
            }
            else -> {}
        }
    }

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
            IconButton(onClick = onNavigateBack) {
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
            value = uiState.name,
            onValueChange = {viewModel.updateName(it) },
            maxChars = 50
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
            value = uiState.email,
            onValueChange = { viewModel.updateEmail(it) },
            maxChars = 50,
            modifier = Modifier.onFocusChanged { st -> viewModel.onEmailFocusChanged(st.hasFocus) }
        )
        if (uiState.emailError != null) {
            Text(
                text = uiState.emailError!!,
                color = MaterialTheme.colorScheme.error,
                fontSize = 12.sp
            )
        }
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
            value = uiState.password,
            onValueChange = { viewModel.updatePassword(it)},
            trailingIcon = {
                Icon(
                    imageVector = AppIcons.PasswordEye,
                    contentDescription = "Info",
                    tint = GrayIcon
                )
            },
            maxChars = 50
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
                value = uiState.birthDay,
                onValueChange = { viewModel.updateBirthDay(it) },
                modifier = Modifier
                    .height(50.dp)
                    .weight(1f),
                maxChars = 3,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            // Placeholder for Month dropdown
            PlaceholderTextField(
                placeholderText = "Month",
                value = uiState.birthMonth,
                onValueChange = { viewModel.updateBirthMonth(it) },
                modifier = Modifier
                    .height(50.dp)
                    .weight(1f),
                maxChars = 3,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            // Placeholder for Year dropdown
            PlaceholderTextField(
                placeholderText = "Year",
                value = uiState.birthYear,
                onValueChange = { viewModel.updateBirthYear(it) },
                modifier = Modifier
                    .height(50.dp)
                    .weight(1f),
                maxChars = 5,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

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
            //var numbersPrefix by remember { mutableStateOf("Select Prefix") }

            CustomDropdownMenu(
                placeholderText = uiState.phonePrefix,
                items = prefixes,
                modifier = Modifier
                    .width(90.dp) // Fixed width for the dropdown
                    .height(52.dp),
                onItemSelected = {  viewModel.updatePhonePrefix(it)}
            )

            // Placeholder for Phone Number Input Field
            PlaceholderTextField(
                placeholderText = "(999) 111-0000",
                value = uiState.phoneNumber,
                onValueChange = { viewModel.updatePhoneNumber(it)},
                maxChars = 15,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Section Title: "Basic Information"
        BlueText(
            text = "Profile",
            size = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Input Field Label: Gender
        BlueText(
            text = "Gender",
            size = 16.sp,
            modifier = Modifier.fillMaxWidth(),
            fontWeight = FontWeight.Bold,
        )
        Spacer(modifier = Modifier.height(8.dp))
        // Custom Dropdown Menu for Genre
        val genres = listOf("Male", "Female", "Non-binary", "Prefer not to say")
        //var selectedGenre by remember { mutableStateOf("Select") }
        CustomDropdownMenu(
            placeholderText = uiState.gender.ifEmpty { "Select" },
            items = genres,
            onItemSelected = { viewModel.updateGender(it) },
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Input Field Label: Nationality
        BlueText(
            text = "Nationality",
            size = 16.sp,
            modifier = Modifier.fillMaxWidth(),
            fontWeight = FontWeight.Bold,
        )
        Spacer(modifier = Modifier.height(8.dp))
        // Custom Dropdown Menu for Genre
        val nationalities = listOf("USA", "Canada", "Mexico", "Colombia")

        CustomDropdownMenu(
            placeholderText = uiState.nationality.ifEmpty { "Select" },
            items = nationalities,
            onItemSelected = { viewModel.updateNationality(it)  },
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Input Field Label: Preferred Language
        BlueText(
            text = "Preferred Language",
            size = 16.sp,
            modifier = Modifier.fillMaxWidth(),
            fontWeight = FontWeight.Bold,
        )
        Spacer(modifier = Modifier.height(8.dp))
        // Custom Dropdown Menu for Genre
        val languages = listOf("English", "Spanish")
        //var selectedLanguage by remember { mutableStateOf("Select") }

        CustomDropdownMenu(
            placeholderText = uiState.language.ifEmpty { "Select" },
            items = languages,
            onItemSelected = { viewModel.updateLanguage(it) },
        )

        Spacer(modifier = Modifier.height(32.dp))

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
                text = "Host (Offering accommodation)",
                selected = uiState.isHost,
                onClick = {viewModel.toggleUserType(false) }
            )
            CustomRadioButton(
                text = "Student (Looking for accommodation)",
                selected = uiState.isStudent,
                onClick = {viewModel.toggleUserType(true) }
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        BlueButton(
            text = if (uiState.operationState is OperationState.Loading) "Loading..." else "Start",
            onClick = {viewModel.signUp()},
            enabled = uiState.operationState !is OperationState.Loading,
            modifier = Modifier.fillMaxWidth(), // Fill width for this button
        )

        if (uiState.operationState is OperationState.Loading) {
            Spacer(modifier = Modifier.height(16.dp))
            CircularProgressIndicator()
        }
    }

    SnackbarHost(
        hostState = snackbarHostState,
        modifier = Modifier.padding(16.dp)
    )
}

// Preview
@Preview(showBackground = true, widthDp = 360)
@Composable
fun SignUpLayoutPreview() {
    SignUpLayout()
}