package com.team21.myapplication.ui.createAccountView

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.google.firebase.messaging.FirebaseMessaging
import com.team21.myapplication.ui.main.MainActivity
import com.team21.myapplication.ui.theme.AppTheme
import com.team21.myapplication.ui.ownerMainView.OwnerMainActivity
import com.team21.myapplication.utils.App

private const val EXTRA_BYPASS_AUTH_GUARD = "EXTRA_BYPASS_AUTH_GUARD"
class WelcomeActivity : ComponentActivity() {

    private val signUpViewModel: SignUpViewModel by viewModels()

    private val requestNotifPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* noop: si lo niegan, solo no mostramos notifs */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 🔹 Detecta si viene de una notificación (bypass) o si el usuario ya está autenticado
        val bypass = intent?.getBooleanExtra(EXTRA_BYPASS_AUTH_GUARD, false) == true
        val currentUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser

        if (bypass && currentUser != null) {
            // El usuario ya estaba autenticado y venía desde una notificación:
            // saltamos directo al MainActivity (o OwnerMainActivity)
            val target = com.team21.myapplication.ui.main.MainActivity::class.java
            startActivity(
                Intent(this, target).apply {
                    putExtra(EXTRA_BYPASS_AUTH_GUARD, true)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
            )
            finish()
            return
        }

        // 🔹 Si está logueado pero no venía desde notificación, también salta al Main
        if (currentUser != null) {
            val target = com.team21.myapplication.ui.main.MainActivity::class.java
            startActivity(
                Intent(this, target).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
            )
            finish()
            return
        }

        // 🔹 Si no está logueado, seguimos con el flujo normal de login
        FirebaseMessaging.getInstance().subscribeToTopic("trending_filters")
        FirebaseMessaging.getInstance().subscribeToTopic("all")

        val networkMonitor = (application as App).networkMonitor
        val welcomeViewModel: WelcomeViewModel by viewModels {
            WelcomeViewModel.WelcomeViewModelFactory((application as App).networkMonitor)
        }

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
                                viewModel = welcomeViewModel,
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