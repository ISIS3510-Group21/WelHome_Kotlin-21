package com.team21.myapplication.ui.ownerPostsDetail

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.team21.myapplication.ui.theme.AppTheme

class OwnerPostDetailActivity : ComponentActivity() {

    companion object {
        const val EXTRA_HOUSING_ID = "housing_id"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val housingId = intent?.getStringExtra(EXTRA_HOUSING_ID) ?: ""

        setContent {
            AppTheme {
                // VM usando el AndroidViewModelFactory por defecto
                val vm: OwnerPostDetailViewModel = viewModel()

                val uiState by vm.state.collectAsStateWithLifecycle()

                LaunchedEffect(housingId) {
                    vm.load(housingId)
                }

                when {
                    uiState.isLoading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                    uiState.error != null -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = uiState.error ?: "Error",
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                    else -> {
                        OwnerPostDetailView(
                            images = uiState.images,
                            title = uiState.title,
                            address = uiState.address,
                            rating = uiState.rating,
                            reviewsCount = uiState.reviewsCount,
                            pricePerMonthLabel = uiState.pricePerMonthLabel,
                            amenities = uiState.amenities,
                            roommatesPhotoUrls = uiState.roommatesPhotoUrls,
                            onBack = { finish() },
                            onManagePropertyClick = {
                                // TODO: navegación a pantalla de gestión de la propiedad
                            },
                            onManageRoommatesClick = {
                                // TODO: navegación a gestión de roommates
                            }
                        )
                    }
                }
            }
        }
    }
}
