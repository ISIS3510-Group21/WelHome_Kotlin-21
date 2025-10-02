package com.team21.myapplication.ui.createAccountView

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.team21.myapplication.ui.main.MainActivity
import com.team21.myapplication.ui.theme.AppTheme

class WelcomeActivity : ComponentActivity() {

    private val viewModel: WelcomeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    WelcomeLayout(
                        viewModel = viewModel,
                        onSignInSuccess = {
                            // Navigate to the principal screen
                            val intent = Intent(this, MainActivity::class.java)
                            startActivity(intent)
                            finish()
                        },
                        onNavigateToSignUp = {
                            // Navigate to SignUpActivity
                            val intent = Intent(this, SignUpActivity::class.java)
                            startActivity(intent)
                        }
                    )
                }
            }
        }
    }
}