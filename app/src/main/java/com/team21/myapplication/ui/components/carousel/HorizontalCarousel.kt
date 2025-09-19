package com.team21.myapplication.ui.components.carousel

import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.gestures.ScrollableDefaults
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.team21.myapplication.ui.theme.AppTheme

@Composable
fun <T> HorizontalCarousel(
    items: List<T>,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp),
    horizontalSpacing: Dp = 12.dp,
    snapToItems: Boolean = true,
    itemContent: @Composable (T) -> Unit
) {
    val listState = rememberLazyListState()

    val flingBehavior: FlingBehavior =
        if (snapToItems) rememberSnapFlingBehavior(listState)
        else ScrollableDefaults.flingBehavior()

    LazyRow(
        state = listState,
        modifier = modifier.fillMaxWidth(),
        contentPadding = contentPadding,
        horizontalArrangement = Arrangement.spacedBy(horizontalSpacing),
        flingBehavior = flingBehavior
    ) {
        items(items) { item ->
            itemContent(item)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewHorizontalCarousel() {
    AppTheme {
        val sample = listOf("House 1", "House 2", "House 3", "House 4")
        HorizontalCarousel(
            items = sample,
            snapToItems = true
        ) { text ->
            Card(
                modifier = Modifier.fillMaxWidth(0.7f)
            ) {
                Text(text = text, modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp))
            }
        }
    }
}
