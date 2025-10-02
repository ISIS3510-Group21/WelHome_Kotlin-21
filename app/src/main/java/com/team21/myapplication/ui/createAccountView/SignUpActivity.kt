package com.team21.myapplication.ui.createAccountView

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.team21.myapplication.ui.theme.AppTheme

class SignUpActivity : ComponentActivity() {

    private val viewModel: SignUpViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    SignUpLayout(
                        viewModel = viewModel,
                        onSignUpSuccess = {
                            Toast.makeText(
                                this,
                                "Account created successfully!",
                                Toast.LENGTH_LONG
                            ).show()
                            // Go back to WelcomeActivity
                            finish()
                        },
                        onNavigateBack = {
                            finish()
                        }
                    )
                }
            }
        }
    }
}