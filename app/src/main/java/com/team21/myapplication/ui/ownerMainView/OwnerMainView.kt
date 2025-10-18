package com.team21.myapplication.ui.ownerMainView

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import com.team21.myapplication.R
import com.team21.myapplication.ui.components.cards.HousingInfoCard
import com.team21.myapplication.ui.components.inputs.SearchBar
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.Scaffold
import com.team21.myapplication.ui.main.LoadingScreen  // reutiliza el loading existente

@Composable
fun OwnerMainScreenLayout() {
    // Principal component as Column
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()) // allows scrolling
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ){

            SearchBar(
                query = "",
                onQueryChange = {},
                placeholder = "Search",
                asButton = true,
                onClick = {}
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Box(
            contentAlignment = Alignment.Center,
        ) {
            HousingInfoCard(
                title = "Portal de los Rosales",
                rating = 4.95,
                reviewsCount = 22,
                pricePerMonthLabel = "$700’000 /month",
                imageRes = R.drawable.sample_house,
                modifier = Modifier
                    .widthIn(max = 560.dp)
                    .fillMaxWidth()
            )
        }
        Spacer(modifier = Modifier.height(16.dp))

        Box(
            contentAlignment = Alignment.Center,
        ) {
            HousingInfoCard(
                title = "Portal de los Rosales",
                rating = 4.95,
                reviewsCount = 22,
                pricePerMonthLabel = "$700’000 /month",
                imageRes = R.drawable.sample_house,
                modifier = Modifier
                    .widthIn(max = 560.dp)
                    .fillMaxWidth()
            )
        }
        Spacer(modifier = Modifier.height(16.dp))


    }
}

// Preview of the component
@Preview(showBackground = true)
@Composable
fun OwnerMainScreenLayoutScreenLayoutPreview() {
    OwnerMainScreenLayout()
}

@Composable
fun OwnerMainScreen() {
    val appContext = LocalContext.current.applicationContext
    val viewModel: OwnerMainViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return OwnerMainViewModel(appContext) as T
            }
        }
    )
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { viewModel.loadOwnerHome() }

    Scaffold(

    ) { inner ->
        if (state.isLoading) {
            LoadingScreen(modifier = Modifier.padding(inner))
        } else {
            Column(modifier = Modifier.padding(inner)) {
                OwnerMainScreenLayout()
            }
        }
    }
}
