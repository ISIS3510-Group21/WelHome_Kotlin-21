package com.team21.myapplication.ui.createAccountView

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.team21.myapplication.ui.components.buttons.BlueButton
import com.team21.myapplication.ui.components.buttons.GrayButton
import com.team21.myapplication.ui.components.icons.AppIcons
import com.team21.myapplication.ui.components.inputs.PlaceholderTextField
import com.team21.myapplication.ui.components.text.BlackText
import com.team21.myapplication.ui.components.text.BlueText
import com.team21.myapplication.ui.theme.GrayIcon
import com.team21.myapplication.R
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.lifecycle.viewmodel.compose.viewModel
import com.team21.myapplication.ui.createAccountView.state.SignInOperationState

@Composable
fun WelcomeLayout(
    viewModel: WelcomeViewModel = viewModel(),
    onSignInSuccess: () -> Unit = {},
    onNavigateToSignUp: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }



    LaunchedEffect(uiState.operationState) {
        when (val state = uiState.operationState) {
            is SignInOperationState.Success -> {
                snackbarHostState.showSnackbar("Successful login!")
                onSignInSuccess()
            }
            is SignInOperationState.Error -> {
                snackbarHostState.showSnackbar(state.message)
                viewModel.resetState()
            }
            else -> {}
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()) // Enable scrolling for long content
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(36.dp))

            Image(
                painter = painterResource(R.drawable.app_logo),
                contentDescription = "App Logo",
                modifier = Modifier.size(250.dp),
            )

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
                onValueChange = {viewModel.updateEmail(it)},
            )
            Spacer(modifier = Modifier.height(32.dp))

            // Input Field: Password
            BlueText(
                text = "Password",
                size = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            var passwordVisible by remember { mutableStateOf(false) }
            PlaceholderTextField(
                placeholderText = "**********",
                value = uiState.password,
                onValueChange = {viewModel.updatePassword(it)},
                visualTransformation = if (passwordVisible) {
                    VisualTransformation.None
                } else {
                    PasswordVisualTransformation()
                },
                trailingIcon = {
                    Icon(
                        imageVector = if (passwordVisible) AppIcons.PasswordEyeOff else AppIcons.PasswordEye,
                        contentDescription = if (passwordVisible) "Hide password" else "Show password",
                        tint = GrayIcon,
                        modifier = Modifier.clickable { passwordVisible = !passwordVisible }
                    )
                }
            )
            Spacer(modifier = Modifier.height(8.dp))
            BlueText(text = "Forgot password?")
            Spacer(modifier = Modifier.height(32.dp))
            // Blue Button: "LogIn"
            BlueButton(
                text = if (uiState.operationState is SignInOperationState.Loading) "Loading..." else "LogIn",
                onClick = {viewModel.signIn() },
                enabled = uiState.operationState !is SignInOperationState.Loading
            )
            if (uiState.operationState is SignInOperationState.Loading) {
                Spacer(modifier = Modifier.height(16.dp))
                CircularProgressIndicator()
            }

            Spacer(modifier = Modifier.height(40.dp))
            BlackText(text = "Not a member?")

            Spacer(modifier = Modifier.height(8.dp))
            GrayButton(text = "Register now", onClick = onNavigateToSignUp)

        }
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        )
    }
}

// Preview
@Preview(showBackground = true, widthDp = 360)
@Composable
fun WelcomeLayoutPreview() {
    WelcomeLayout()
}