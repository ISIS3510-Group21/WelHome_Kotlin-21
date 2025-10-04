package com.team21.myapplication.ui.filterView.results

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.team21.myapplication.R
import com.team21.myapplication.ui.components.cards.HousingInfoCard
import com.team21.myapplication.ui.components.navbar.AppNavBar
import com.team21.myapplication.ui.filterView.state.PreviewCardUi
import com.team21.myapplication.ui.theme.AppTextStyles
import com.team21.myapplication.ui.theme.BlackText
import com.team21.myapplication.ui.theme.WhiteBackground

@Composable
fun FilterResultsView(
    items: List<PreviewCardUi>,
    onOpenDetail: (String) -> Unit,
    onNavigateBottomBar: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        containerColor = WhiteBackground,
    ) { inner ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(inner),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "Results (${items.size})",
                    style = AppTextStyles.TitleView,
                    color = BlackText
                )
                Spacer(Modifier.height(8.dp))
            }

            items(items, key = { it.housingId }) { item ->
                HousingInfoCard(
                    title = item.title,
                    rating = item.rating,
                    reviewsCount = item.reviewsCount,
                    pricePerMonthLabel = item.pricePerMonthLabel,
                    imageRes = R.drawable.sample_house,
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { onOpenDetail(item.housingId) }
                )
            }
        }
    }
}