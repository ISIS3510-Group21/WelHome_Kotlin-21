package com.team21.myapplication.ui.mapsearch

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.team21.myapplication.R // Asegúrate de tener una imagen de mapa de ejemplo en tus recursos
import com.team21.myapplication.ui.components.cards.HousingCardListItem
import com.team21.myapplication.ui.components.inputs.SearchBar
import com.team21.myapplication.ui.components.navbar.AppNavBar
import com.team21.myapplication.ui.theme.AppTheme
import com.team21.myapplication.ui.theme.LocalDSTypography

class MapSearchActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                val navController = rememberNavController()
                MapSearchView(navController = navController)
            }
        }
    }
}

@Composable
fun MapSearchView(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }

    // Example data for housing items
    val sampleHousingItems = remember {
        listOf(
            HousingItemData("Portal de los Rosales", 4.95, "$700'000 /month", R.drawable.sample_house),
            HousingItemData("Living 71", 4.95, "$700'000 /month", R.drawable.sample_house),
            HousingItemData("Apartamento en Calendaria", 4.95, "$700'000 /month", R.drawable.sample_house)
        )
    }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Map Search",
                    style = LocalDSTypography.current.TitleView,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(16.dp))
                SearchBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    placeholder = "Search",
                    onSearch = {
                        // Search logic
                    }
                )
            }
        },
        bottomBar = {
            AppNavBar(navController = navController)
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.example_map),
                    contentDescription = "Map Placeholder",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(16.dp))


            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                items(sampleHousingItems) { housingItem ->
                    HousingCardListItem(
                        imageRes = housingItem.imageRes,
                        title = housingItem.title,
                        rating = housingItem.rating,
                        price = housingItem.price,
                        modifier = Modifier.padding(horizontal = 2.dp)
                    )
                }
            }
        }
    }
}

data class HousingItemData(
    val title: String,
    val rating: Double,
    val price: String,
    val imageRes: Int? = null,
    val imageUrl: String? = null
)

@Preview(showBackground = true)
@Composable
fun MapSearchViewPreview() {
    AppTheme {
        val navController = rememberNavController()
        MapSearchView(navController = navController)
    }
}
