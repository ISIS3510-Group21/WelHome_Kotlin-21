package com.team21.myapplication.ui.components.cards

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.material3.Text
import com.team21.myapplication.ui.theme.AppTheme
import com.team21.myapplication.ui.theme.GrayIcon
import com.team21.myapplication.ui.theme.WhiteBackground
import com.team21.myapplication.ui.theme.LocalDSTypography
import androidx.compose.foundation.ExperimentalFoundationApi

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ImageCarouselCard(
    images: List<String>,
    modifier: Modifier = Modifier,
    height: Dp = 220.dp,
    showCounter: Boolean = true,
    counterFormatter: (index: Int, total: Int) -> String = { i, n -> "${i + 1}/$n" },
    onImageClick: ((index: Int) -> Unit)? = null
) {
    val total = images.size

    if (total == 0) return

    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { total }
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
        ) { page ->
            val url = images[page]
            AsyncImage(
                model = url,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(enabled = onImageClick != null) {
                        onImageClick?.invoke(page)
                    }
            )
        }

        if (showCounter) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(12.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(GrayIcon)
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = counterFormatter(pagerState.currentPage, total),
                    style = LocalDSTypography.current.IconText,
                    color = WhiteBackground,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun ImageCarouselCard_Preview() {
    AppTheme {
        ImageCarouselCard(
            images = listOf(
                "https://picsum.photos/800/500?1",
                "https://picsum.photos/800/500?2",
                "https://picsum.photos/800/500?3",
                "https://picsum.photos/800/500?4"
            ),
            onImageClick = {}
        )
    }
}
