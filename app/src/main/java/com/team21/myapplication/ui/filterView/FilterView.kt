package com.team21.myapplication.ui.filterView

import android.annotation.SuppressLint
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import android.os.Bundle
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.team21.myapplication.R
import com.team21.myapplication.ui.components.carousel.HorizontalCarousel
import com.team21.myapplication.ui.components.icons.AppIcons
import com.team21.myapplication.ui.components.navbar.AppNavBar
import com.team21.myapplication.ui.components.inputs.SearchBar
import com.team21.myapplication.ui.components.buttons.GrayButtonWithIcon
import com.team21.myapplication.ui.components.buttons.GrayButton
import com.team21.myapplication.ui.components.buttons.BlueButton
import com.team21.myapplication.ui.components.cards.HousingInfoCard
import com.team21.myapplication.ui.theme.AppTextStyles
import com.team21.myapplication.ui.theme.AppTheme
import com.team21.myapplication.ui.theme.BlackText
import com.team21.myapplication.ui.theme.WhiteBackground
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import com.team21.myapplication.ui.filterView.state.FilterUiState
import com.team21.myapplication.ui.filterView.state.TagChipUi

// Activity (solo diseño local; en app real usa FilterRoute())
class FilterViewActivity : ComponentActivity() {
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { AppTheme { /* FilterRoute() */ } }
    }
}

/**
 * VIEW (State-driven):
 * - Selección de filtros + carousels.
 * - SearchBar y botón Search ejecutan onSearch().
 * - Carousel default: items clickeables → onOpenDetail(housingId).
 * - [ANALYTICS] Se loguea en el ViewModel (toggle/search), aquí no hace falta tocar nada.
 */
@Composable
fun FilterView(
    state: FilterUiState,
    onToggleTag: (String) -> Unit,
    onSearch: () -> Unit,
    onMapSearch: () -> Unit = {},
    onOpenDetail: (String) -> Unit,   // navegación al detail
    modifier: Modifier = Modifier
) {
    Scaffold(
        containerColor = WhiteBackground,
        topBar = {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                // [CAMBIO: SearchBar actúa como botón y dispara onSearch()]
                SearchBar(
                    query = "",
                    onQueryChange = {},
                    placeholder = if (state.selectedCount > 0) "Search (${state.selectedCount})" else "Search",
                    asButton = true,
                    onClick = onSearch,  // ← dispara búsqueda (el VM hace Analytics)
                    enabled = true
                )
            }
        },

        ) { inner ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(inner),
            contentPadding = PaddingValues(
                start = 16.dp, end = 16.dp, top = 12.dp, bottom = 96.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Título
            item {
                Text(
                    text = "Filter by",
                    style = AppTextStyles.TitleView,
                    color = BlackText
                )
            }

            // 4 destacados (House, Room, Cabins, Apartment)
            item {
                val featured = state.featuredTags.take(4)

                fun iconFor(label: String) = when (label.lowercase()) {
                    "house", "houses" -> AppIcons.Houses
                    "room", "rooms" -> AppIcons.Rooms
                    "cabins", "cabin" -> AppIcons.Cabins
                    "apartment", "apartments" -> AppIcons.Apartments
                    else -> AppIcons.Houses
                }

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val a = featured.getOrNull(0)
                        GrayButtonWithIcon(
                            text = a?.label ?: "Houses",
                            imageVector = iconFor(a?.label ?: "Houses"),
                            selected = a?.selected == true,
                            onClick = { a?.let { onToggleTag(it.id) } }, // [CAMBIO: toggle selección]
                            modifier = Modifier.weight(1f)
                        )
                        val b = featured.getOrNull(1)
                        GrayButtonWithIcon(
                            text = b?.label ?: "Rooms",
                            imageVector = iconFor(b?.label ?: "Rooms"),
                            selected = b?.selected == true,
                            onClick = { b?.let { onToggleTag(it.id) } }, // [CAMBIO: toggle selección]
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val c = featured.getOrNull(2)
                        GrayButtonWithIcon(
                            text = c?.label ?: "Cabins",
                            imageVector = iconFor(c?.label ?: "Cabins"),
                            selected = c?.selected == true,
                            onClick = { c?.let { onToggleTag(it.id) } }, // [CAMBIO: toggle selección]
                            modifier = Modifier.weight(1f)
                        )
                        val d = featured.getOrNull(3)
                        GrayButtonWithIcon(
                            text = d?.label ?: "Apartments",
                            imageVector = iconFor(d?.label ?: "Apartments"),
                            selected = d?.selected == true,
                            onClick = { d?.let { onToggleTag(it.id) } }, // [CAMBIO: toggle selección]
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Chips de otros tags (carousel) con selección
            item {
                HorizontalCarousel(
                    items = state.otherTags,
                    contentPadding = PaddingValues(horizontal = 0.dp),
                    horizontalSpacing = 8.dp,
                    snapToItems = false
                ) { chip: TagChipUi ->
                    GrayButton(
                        text = chip.label,
                        selected = chip.selected,
                        onClick = { onToggleTag(chip.id) } // [CAMBIO: toggle selección]
                    )
                }
            }

            // Carousel DEFAULT (estático) → cada card navega al detail
            item {
                val defaultItems = listOf(
                    DefaultHouseUi(id = "HousingPost10", title = "Living 72",  rating = 4.95, reviews = 22, price = "$700'000 /month"),
                    DefaultHouseUi(id = "HousingPost11", title = "City U",     rating = 4.95, reviews = 22, price = "$700'000 /month"),
                    DefaultHouseUi(id = "HousingPost12", title = "Modern Loft", rating = 4.90, reviews = 18, price = "$680'000 /month")
                )
                HorizontalCarousel(
                    items = defaultItems,
                    contentPadding = PaddingValues(horizontal = 0.dp),
                    horizontalSpacing = 12.dp,
                    snapToItems = true
                ) { house ->
                    HousingInfoCard(
                        title = house.title,
                        rating = house.rating,
                        reviewsCount = house.reviews,
                        pricePerMonthLabel = house.price,
                        imageRes = R.drawable.sample_house,
                        modifier = Modifier.fillMaxWidth(0.8f),
                        onClick = { onOpenDetail(house.id) }   // [CAMBIO: navega al detail]
                    )
                }
            }

            // Botón Search
            item {
                BlueButton(
                    text = if (state.canSearch) "Search" else "Search (select filters)",
                    onClick = onSearch, // [CAMBIO: dispara búsqueda]
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Botón Map Search (opcional)
            item {
                BlueButton(
                    text = "Map Search",
                    onClick = onMapSearch,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

private data class DefaultHouseUi(
    val id: String,
    val title: String,
    val rating: Double,
    val reviews: Int,
    val price: String
)

@Preview(showBackground = true, name = "FilterView")
@Composable
private fun PreviewFilterView() {
    AppTheme {
        FilterView(
            state = FilterUiState(
                isLoading = false,
                featuredTags = listOf(
                    TagChipUi("1","House", true),
                    TagChipUi("2","Room"),
                    TagChipUi("3","Cabins"),
                    TagChipUi("4","Apartment")
                ),
                otherTags = listOf(
                    TagChipUi("t1","Private Backyard", true),
                    TagChipUi("t2","Vape Free"),
                    TagChipUi("t3","Car Park")
                ),
                canSearch = true,
                selectedCount = 2
            ),
            onToggleTag = {},
            onSearch = {},
            onMapSearch = {},
            onOpenDetail = {}
        )
    }
}
