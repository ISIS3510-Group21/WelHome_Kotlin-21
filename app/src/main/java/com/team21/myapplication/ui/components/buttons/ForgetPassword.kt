package com.team21.myapplication.ui.components.buttons

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

Composable
private fun ForgotPasswordScreen(
    onSend: (email: String, report: (success: Boolean, message: String) -> Unit) -> Unit,
    onClose: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var touched by remember { mutableStateOf(false) }
    val emailValid = remember(email) { android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() }
    val showEmailError = touched && email.isNotBlank() && !emailValid

    var loading by remember { mutableStateOf(false) }
    var infoMessage by remember { mutableStateOf<String?>(null) } // mensaje neutro tras intento
    var errorMessage by remember { mutableStateOf<String?>(null) } // para validaciones locales

    val primary = MaterialTheme.colorScheme.primary

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .padding(top = 32.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Forgot your password?", style = MaterialTheme.typography.headlineSmall, color = primary)
        Spacer(Modifier.height(12.dp))
        Text(
            "Enter the email associated with your account and we’ll send you a reset link.",
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(Modifier.height(16.dp))

        // Campo de email con validación visual (rojo) y helper
        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
                if (!touched && it.isNotEmpty()) touched = true
                errorMessage = null
                infoMessage = null
            },
            label = { Text("Email", color = primary) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Done
            ),
            isError = showEmailError,
            supportingText = {
                if (showEmailError) {
                    Text(
                        "Please enter a valid email.",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        if (errorMessage != null) {
            Spacer(Modifier.height(8.dp))
            Text(errorMessage!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
        }
        if (infoMessage != null) {
            Spacer(Modifier.height(8.dp))
            Text(infoMessage!!, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodyMedium)
        }

        Spacer(Modifier.height(16.dp))

        // Botón "Send reset email" (Blue CTA). Deshabilitado si loading.
        Button(
            onClick = {
                // Aquí valido localmente antes de contactar Firebase.
                touched = true
                if (email.isBlank()) {
                    errorMessage = "Please fill out the email."
                    infoMessage = null
                    return@Button
                }
                if (!emailValid) {
                    errorMessage = "Please enter a valid email."
                    infoMessage = null
                    return@Button
                }
                errorMessage = null
                loading = true

                // Aquí hago tal cosa: disparo el envío y recibo un resultado neutro.
                onSend(email.trim()) { _, message ->
                    loading = false
                    infoMessage = message
                    // Opcional: toast corto
                    // Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                }
            },
            enabled = !loading,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = primary)
        ) {
            if (loading) {
                CircularProgressIndicator(
                    strokeWidth = 2.dp,
                    modifier = Modifier
                        .size(18.dp)
                        .padding(end = 8.dp)
                )
            }
            Text(if (loading) "Sending..." else "Send reset email")
        }

        Spacer(Modifier.height(12.dp))

        // Botón secundario para volver/cerrar
        OutlinedButton(
            onClick = { onClose() },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Back") }
    }
}