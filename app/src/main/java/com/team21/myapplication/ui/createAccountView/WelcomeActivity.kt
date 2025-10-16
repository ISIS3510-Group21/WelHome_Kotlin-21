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
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.team21.myapplication.ui.main.MainActivity
import com.team21.myapplication.ui.theme.AppTheme
import com.team21.myapplication.ui.ownerMainView.OwnerMainActivity

class WelcomeActivity : ComponentActivity() {

    private val viewModel: WelcomeViewModel by viewModels()
    private val signUpViewModel: SignUpViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()

                    NavHost(navController = navController, startDestination = "signin") {
                        composable("signin") {
                            WelcomeLayout(
                                viewModel = viewModel,
                                onSignInSuccess = { isOwner ->
                                    val target = if (isOwner) OwnerMainActivity::class.java else MainActivity::class.java
                                    // Navigate to the principal screen, eliminates back to auth
                                    startActivity(
                                        Intent(this@WelcomeActivity, target).apply{
                                            putExtra("login_success", true)
                                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                        }
                                    )
                                },
                                onNavigateToSignUp = {
                                    // Navigate to SignUpActivity
                                    navController.navigate("signup")
                                }
                            )
                        }
                        composable("signup") {
                            SignUpLayout(
                                viewModel = signUpViewModel, // SignUpViewModel
                                onSignUpSuccess = {
                                    // Volver a SignIn para que el usuario inicie sesión
                                    navController.popBackStack() // vuelve a "signin"
                                    Toast.makeText(this@WelcomeActivity, "Account created!", Toast.LENGTH_LONG).show()
                                },
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }

                    }

                }
            }
        }
    }
}