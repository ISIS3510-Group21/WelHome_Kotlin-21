package com.team21.myapplication.ui.updateprofile

import android.app.DatePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.google.firebase.Timestamp
import com.team21.myapplication.data.model.StudentUser
import com.team21.myapplication.utils.NetworkMonitor
import com.team21.myapplication.utils.PendingProfileSync
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdateProfileScreen(
    user: StudentUser,
    onSave: (StudentUser) -> Unit,
    onBack: () -> Unit
) {
    var name by remember { mutableStateOf(user.name) }
    var phoneNumber by remember { mutableStateOf(user.phoneNumber) }
    var gender by remember { mutableStateOf(user.gender) }
    var nationality by remember { mutableStateOf(user.nationality) }
    var language by remember { mutableStateOf(user.language) }
    var university by remember { mutableStateOf(user.university) }
    var birthDate by remember { mutableStateOf(user.birthDate) }

    var nameError by remember { mutableStateOf<String?>(null) }
    var phoneNumberError by remember { mutableStateOf<String?>(null) }
    var genderError by remember { mutableStateOf<String?>(null) }
    var nationalityError by remember { mutableStateOf<String?>(null) }
    var languageError by remember { mutableStateOf<String?>(null) }
    var universityError by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Update Profile") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AsyncImage(
                model = user.photoPath.ifEmpty { null },
                contentDescription = "Profile picture",
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it
                    nameError = null
                },
                label = { Text("Name") },
                modifier = Modifier.fillMaxWidth(),
                isError = nameError != null,
                supportingText = { nameError?.let { Text(it) } }
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = phoneNumber,
                onValueChange = {
                    phoneNumber = it
                    phoneNumberError = null
                },
                label = { Text("Phone Number") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                isError = phoneNumberError != null,
                supportingText = { phoneNumberError?.let { Text(it) } }
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = gender,
                onValueChange = {
                    gender = it
                    genderError = null
                },
                label = { Text("Gender") },
                modifier = Modifier.fillMaxWidth(),
                isError = genderError != null,
                supportingText = { genderError?.let { Text(it) } }
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = nationality,
                onValueChange = {
                    nationality = it
                    nationalityError = null
                },
                label = { Text("Nationality") },
                modifier = Modifier.fillMaxWidth(),
                isError = nationalityError != null,
                supportingText = { nationalityError?.let { Text(it) } }
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = language,
                onValueChange = {
                    language = it
                    languageError = null
                },
                label = { Text("Language") },
                modifier = Modifier.fillMaxWidth(),
                isError = languageError != null,
                supportingText = { languageError?.let { Text(it) } }
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = university,
                onValueChange = {
                    university = it
                    universityError = null
                },
                label = { Text("University") },
                modifier = Modifier.fillMaxWidth(),
                isError = universityError != null,
                supportingText = { universityError?.let { Text(it) } }
            )
            Spacer(modifier = Modifier.height(8.dp))

            val context = LocalContext.current
            val networkMonitor = remember { NetworkMonitor.get(context) }
            val isOnline by networkMonitor.isOnline.collectAsState()

            // Efecto para sincronizar perfil pendiente al recuperar conectividad
            LaunchedEffect(isOnline) {
                if (isOnline) {
                    PendingProfileSync.load(context)?.let { pending ->
                        onSave(pending)
                        PendingProfileSync.clear(context)
                    }
                }
            }

// Formato reutilizable
            val dateFormatter = remember {
                SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            }

// Calendar inicial (solo se crea una vez)
            val calendar = remember {
                Calendar.getInstance().apply {
                    time = birthDate.toDate()
                }
            }

// Recordar el DatePickerDialog
            val datePickerDialog = remember {
                DatePickerDialog(
                    context,
                    { _, year, month, day ->
                        val newDate = Calendar.getInstance()
                        newDate.set(year, month, day)
                        birthDate = Timestamp(newDate.time)
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { datePickerDialog.show() }
            ) {
                OutlinedTextField(
                    value = dateFormatter.format(birthDate.toDate()),
                    onValueChange = { },
                    label = { Text("Birth Date") },
                    readOnly = true,              // no se deshabilita el campo
                    enabled = true,               // se mantiene la apariencia normal
                    trailingIcon = {
                        Icon(Icons.Default.DateRange, contentDescription = "Select Date")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                )
            }


            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    var isValid = true
                    if (name.isBlank()) {
                        nameError = "Name is required"
                        isValid = false
                    }
                    if (phoneNumber.isBlank()) {
                        phoneNumberError = "Phone number is required"
                        isValid = false
                    }
                    if (gender.isBlank()) {
                        genderError = "Gender is required"
                        isValid = false
                    }
                    if (nationality.isBlank()) {
                        nationalityError = "Nationality is required"
                        isValid = false
                    }
                    if (language.isBlank()) {
                        languageError = "Language is required"
                        isValid = false
                    }
                    if (university.isBlank()) {
                        universityError = "University is required"
                        isValid = false
                    }

                    if (isValid) {
                        val updatedUser = user.copy(
                            name = name,
                            phoneNumber = phoneNumber,
                            gender = gender,
                            nationality = nationality,
                            language = language,
                            university = university,
                            birthDate = birthDate
                        )
                        if (isOnline) {
                            onSave(updatedUser)
                        } else {
                            PendingProfileSync.save(context, updatedUser)
                            // Aquí se podría mostrar Snackbar/Toast: "Guardado localmente. Se sincronizará cuando haya conexión."
                            onBack()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun UpdateProfileScreenPreview() {
    UpdateProfileScreen(
        user = StudentUser(
            id = "1",
            name = "John Doe",
            email = "john.doe@example.com",
            phoneNumber = "1234567890",
            gender = "Male",
            nationality = "American",
            language = "English",
            university = "Example University",
            birthDate = Timestamp.now()
        ),
        onSave = {},
        onBack = {}
    )
}
