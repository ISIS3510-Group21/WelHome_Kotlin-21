package com.team21.myapplication.ui.saved

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.team21.myapplication.R
import com.team21.myapplication.ui.components.cards.HousingInfoCard
import com.team21.myapplication.ui.saved.state.SavedPostsUiState

/**
 * Pure list View for Saved Posts (analogous to FilterResultsView).
 *
 * Notes:
 * - Keeps rendering dumb and stateless; state + side effects live in the Route/ViewModel.
 * - Respects your existing card component (HousingInfoCard).
 */
@Composable
fun SavedPostsView(
    uiState: SavedPostsUiState,
    onOpenDetail: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    when {
        uiState.isLoading && uiState.items.isEmpty() -> {
            CircularProgressIndicator()
        }
        uiState.error != null && uiState.items.isEmpty() -> {
            Text(
                text = uiState.error ?: "Error",
                color = MaterialTheme.colorScheme.error
            )
        }
        else -> {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = modifier
            ) {
                items(uiState.items, key = { it.housingId }) { item ->
                    HousingInfoCard(
                        title = item.title,
                        rating = item.rating.toDouble(),
                        reviewsCount = item.reviewsCount,
                        pricePerMonthLabel = item.pricePerMonthLabel,
                        imageRes = R.drawable.sample_house,   // offline fallback
                        imageUrl = item.photoUrl,              // online/cached URL if present
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { onOpenDetail(item.housingId) }
                    )
                }
            }
        }
    }
}
