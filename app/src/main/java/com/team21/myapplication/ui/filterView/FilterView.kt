package com.team21.myapplication.ui.filterView

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.team21.myapplication.R
import com.team21.myapplication.ui.components.buttons.BlueButton
import com.team21.myapplication.ui.components.buttons.GrayButton
import com.team21.myapplication.ui.components.buttons.GrayButtonWithIcon
import com.team21.myapplication.ui.components.cards.HousingInfoCard
import com.team21.myapplication.ui.components.carousel.HorizontalCarousel
import com.team21.myapplication.ui.components.icons.AppIcons
import com.team21.myapplication.ui.components.inputs.SearchBar
import com.team21.myapplication.ui.filterView.state.FilterUiState
import com.team21.myapplication.ui.filterView.state.PreviewCardUi
import com.team21.myapplication.ui.filterView.state.TagChipUi
import com.team21.myapplication.ui.theme.AppTextStyles
import com.team21.myapplication.ui.theme.BlackText
import com.team21.myapplication.ui.theme.WhiteBackground
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues

/**
 * VIEW (State-driven):
 * - Recibe 'state' y callbacks del ViewModel vía Route.
 * - No incluye bottomBar (para evitar duplicados).
 */
@Composable
fun FilterView(
    state: FilterUiState,
    onToggleTag: (String) -> Unit,
    onSearch: () -> Unit,
    onOpenDetail: (String) -> Unit = {},
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
                SearchBar(
                    query = "",
                    onQueryChange = { },
                    placeholder = if (state.selectedCount > 0) "Search (${state.selectedCount})" else "Search",
                    asButton = true,          // ← render tipo botón
                    onClick = onSearch,       // ← al click, ejecutar búsqueda
                    enabled = true
                )
            }
        }
    ) { inner ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(inner)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Título
            Text(
                text = "Filter by",
                style = AppTextStyles.TitleView,
                color = BlackText
            )

            Spacer(Modifier.height(20.dp))

            // 4 destacados (House, Room, Cabins, Apartment)
            fun iconFor(label: String) = when (label.lowercase()) {
                "house", "houses" -> AppIcons.Houses
                "room", "rooms" -> AppIcons.Rooms
                "cabins", "cabin" -> AppIcons.Cabins
                "apartment", "apartments" -> AppIcons.Apartments
                else -> AppIcons.Houses
            }

            val featured = state.featuredTags.take(4)

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val a = featured.getOrNull(0)
                    GrayButtonWithIcon(
                        text = a?.label ?: "Houses",
                        imageVector = iconFor(a?.label ?: "Houses"),
                        onClick = { a?.let { onToggleTag(it.id) } },
                        selected = a?.selected == true,
                        modifier = Modifier.weight(1f)
                    )
                    val b = featured.getOrNull(1)
                    GrayButtonWithIcon(
                        text = b?.label ?: "Rooms",
                        imageVector = iconFor(b?.label ?: "Rooms"),
                        onClick = { b?.let { onToggleTag(it.id) } },
                        selected = b?.selected == true,
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
                        onClick = { c?.let { onToggleTag(it.id) } },
                        selected = c?.selected == true,
                        modifier = Modifier.weight(1f)
                    )
                    val d = featured.getOrNull(3)
                    GrayButtonWithIcon(
                        text = d?.label ?: "Apartments",
                        imageVector = iconFor(d?.label ?: "Apartments"),
                        onClick = { d?.let { onToggleTag(it.id) } },
                        selected = d?.selected == true,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            // Chips de otros tags
            HorizontalCarousel(
                items = state.otherTags,
                contentPadding = PaddingValues(horizontal = 0.dp),
                horizontalSpacing = 8.dp,
                snapToItems = false
            ) { chip: TagChipUi ->
                GrayButton(
                    text = chip.label,
                    onClick = { onToggleTag(chip.id) },
                    selected = chip.selected
                )
            }

            Spacer(Modifier.height(18.dp))

            // Resultados (si existen) como carrusel clicable a detail
            val results: List<PreviewCardUi> = state.lastResults
            if (results.isNotEmpty()) {
                HorizontalCarousel(
                    items = results,
                    contentPadding = PaddingValues(horizontal = 0.dp),
                    horizontalSpacing = 12.dp,
                    snapToItems = true
                ) { item ->
                    HousingInfoCard(
                        title = item.title,
                        rating = item.rating,
                        reviewsCount = item.reviewsCount,
                        pricePerMonthLabel = item.pricePerMonthLabel,
                        imageUrl = item.photoUrl,
                        onClick = { onOpenDetail(item.housingId) }
                    )
                }
            } else {
                // Placeholder de ejemplo (estático) y clicable al detail si quisieras
                data class HouseUi(
                    val title: String,
                    val rating: Double,
                    val reviews: Int,
                    val price: String
                )
                HorizontalCarousel(
                    items = listOf(
                        HouseUi("Living 72", 4.95, 22, "$700'000 /month"),
                        HouseUi("City U", 4.95, 22, "$700'000 /month"),
                        HouseUi("Modern Loft", 4.90, 18, "$680'000 /month")
                    ),
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
                        onClick = { /* opcional: onOpenDetail("someId") */ }
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            // Botón Search
            BlueButton(
                text = if (state.canSearch) "Search" else "Search (select filters)",
                onClick = onSearch,
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.CenterHorizontally)
            )

            Spacer(Modifier.height(20.dp))
        }
    }
}
