package com.team21.myapplication.ui.visits

import android.app.Application
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RateVisitView(navController: NavController, visitId: String?) {
    var rating by remember { mutableStateOf(0f) }
    var comment by remember { mutableStateOf("") }
    val context = LocalContext.current

    // Correctly instantiate the AndroidViewModel
    val rateVisitViewModel: RateVisitViewModel = viewModel(
        factory = ViewModelProvider.AndroidViewModelFactory.getInstance(context.applicationContext as Application)
    )

    val uiState by rateVisitViewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is RateVisitUiState.Success -> {
                // Navigate back when the rating is successfully submitted online
                navController.popBackStack()
            }
            is RateVisitUiState.Error -> {
                scope.launch {
                    snackbarHostState.showSnackbar(state.message ?: "An unknown error occurred")
                }
                // Reset state to allow further interactions
                rateVisitViewModel.resetState()
            }
            is RateVisitUiState.Offline -> {
                // Show a confirmation and navigate back immediately
                scope.launch {
                    snackbarHostState.showSnackbar("No internet. Rating will sync later.")
                }
                navController.popBackStack()
            }
            else -> {}
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Rate your Visit") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            val isLoading = uiState is RateVisitUiState.Loading

            Text("Visit ID: $visitId")
            Spacer(modifier = Modifier.height(16.dp))
            Text("Please rate your visit from 1 to 5")
            Slider(
                value = rating,
                onValueChange = { newValue -> rating = newValue },
                valueRange = 0f..5f,
                steps = 4,
                enabled = !isLoading
            )
            Text(String.format("%.1f", rating))
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = comment,
                onValueChange = { newComment -> comment = newComment },
                label = { Text("Leave a comment") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            )
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = {
                    if (visitId != null) {
                        rateVisitViewModel.submitRating(visitId, rating, comment)
                    }
                },
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Submit")
                }
            }
        }
    }
}