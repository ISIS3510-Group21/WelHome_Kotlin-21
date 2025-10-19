package com.team21.myapplication.ui.forgot

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth

class ForgotPasswordActivity : ComponentActivity() {

    private val auth by lazy { FirebaseAuth.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            // Aquí uso tu Theme global
            // WelHomeTheme {
            Surface(color = MaterialTheme.colorScheme.background) {
                ForgotPasswordScreen(
                    onSend = { email, report ->
                        // Aquí hago el envío del correo con Firebase Auth (flujo estándar).
                        auth.sendPasswordResetEmail(email)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    report(true, "If an account exists for this address, you’ll receive an email shortly.")
                                } else {
                                    // Mensaje neutro para no filtrar existencia del email
                                    report(false, "If an account exists for this address, you’ll receive an email shortly.")
                                }
                            }
                    },
                    onClose = {
                        // Aquí cierro la Activity al finalizar (si quieres).
                        finish()
                    }
                )
            }
            // }
        }
    }
}

@Composable
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
