package com.team21.myapplication.ui.forum

import android.content.Intent
import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.SignalWifiOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.team21.myapplication.data.model.ForumPost
import com.team21.myapplication.data.model.ThreadForum
import com.team21.myapplication.ui.components.banners.BannerPosition
import com.team21.myapplication.ui.components.banners.ConnectivityBanner
import com.team21.myapplication.ui.components.buttons.BlueButton
import com.team21.myapplication.ui.components.cards.CommentCard
import com.team21.myapplication.ui.components.cards.ExpandableCard
import com.team21.myapplication.ui.createforumpost.CreateForumPostActivity
import com.team21.myapplication.ui.theme.AppTheme

@Composable
fun ForumScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    forumViewModel: ForumViewModel = viewModel()
) {
    val state by forumViewModel.state.collectAsState()
    val isOnline by forumViewModel.isOnline.collectAsState()

    val view = LocalView.current
    val context = LocalContext.current

    val statusBarColor = if (!isOnline) {
        androidx.compose.ui.graphics.Color.Black
    } else {
        MaterialTheme.colorScheme.background
    }

    SideEffect {
        val window = (view.context as android.app.Activity).window
        // Variable de fondo
        window.statusBarColor = statusBarColor.toArgb()

        // La lógica para decidir el color de los íconos
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars =
            if (!isOnline) {
                false // Íconos blancos para fondo negro
            } else {
                statusBarColor.luminance() > 0.5f // Decide según la luminancia del fondo
            }
    }

    ForumContent(
        modifier = modifier,
        threads = state.threads,
        isLoading = state.isLoading,
        isOnline = isOnline,
        onThreadClick = {
            Log.d("ForumScreen", "Thread clicked: $it")
            forumViewModel.onThreadClicked(it)
        },
        selectedThreadId = state.selectedThread?.id,
        onCreatePostClick = {
            context.startActivity(Intent(context, CreateForumPostActivity::class.java))
        }
    )
}


@Composable
fun ForumContent(
    modifier: Modifier = Modifier,
    threads: List<ThreadForum>,
    isLoading: Boolean = false,
    isOnline: Boolean = true,
    selectedThreadId: String? = null,
    onThreadClick: (ThreadForum) -> Unit = {},
    onCreatePostClick: () -> Unit = {}
) {
    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            ConnectivityBanner(
                visible = !isOnline,
                position = BannerPosition.Top,
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
            )

            Spacer(modifier = Modifier.height(24.dp))
            Column(Modifier.padding(horizontal = 16.dp)) {
                Text(
                    text = "Forum",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 16.dp, bottom = 10.dp)
                )

                Text(
                    text = "Topics",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF757575)
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (isLoading && threads.isEmpty()) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                } else {
                    LazyColumn(contentPadding = PaddingValues(bottom = 80.dp)) {
                        items(threads) { thread ->
                            ExpandableCard(
                                title = thread.title,
                                icon = getIconForTopic(thread.title),
                                initiallyExpanded = thread.id == selectedThreadId,
                                onExpand = { onThreadClick(thread) }
                            ) {
                                Column {
                                    if (thread.forumPost.isEmpty() && thread.id == selectedThreadId) {
                                        if(isOnline) {
                                            CircularProgressIndicator(
                                                modifier = Modifier
                                                    .padding(16.dp)
                                                    .align(Alignment.CenterHorizontally)
                                            )
                                        } else {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(16.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                    Icon(
                                                        imageVector = Icons.Outlined.SignalWifiOff,
                                                        contentDescription = "No internet connection."
                                                    )
                                                    Spacer(modifier = Modifier.height(8.dp))
                                                    Text(
                                                        text = "No internet connection.",
                                                        textAlign = TextAlign.Center
                                                    )
                                                }
                                            }
                                        }
                                    } else {
                                        thread.forumPost.forEach { post ->
                                            CommentCard(
                                                imageUrl = post.userPhoto.takeIf { it.isNotBlank() },
                                                name = post.userName,
                                                country = "Canada",
                                                comment = post.content,
                                                rating = 4.95f
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        BlueButton(
            text = "Create Post",
            onClick = onCreatePostClick,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 16.dp, vertical = 16.dp)
        )
    }
}
fun getIconForTopic(topicTitle: String): ImageVector {
    return when (topicTitle) {
        "General Questions" -> Icons.AutoMirrored.Outlined.HelpOutline
        "Food" -> Icons.Outlined.Restaurant
        "Mobility" -> Icons.Outlined.DirectionsCar
        else -> Icons.AutoMirrored.Outlined.HelpOutline
    }
}

@Preview(showBackground = true)
@Composable
fun ForumScreenPreview() {
    val samplePosts = listOf(
        ForumPost(
            id = "1",
            content = "Lorem ipsum dolor sit amet, consectetur adipiscing elit...",
            positiveVotes = 495,
            negativeVotes = 5,
            userName = "Jhon Doe",
            userPhoto = ""
        ),
        ForumPost(
            id = "2",
            content = "Ut enim ad minim veniam, quis nostrud exercitation ullamco...",
            positiveVotes = 495,
            negativeVotes = 5,
            userName = "Jane Doe",
            userPhoto = ""
        )
    )

    val sampleThreads = listOf(
        ThreadForum(id = "1", title = "General Questions", forumPost = emptyList()),
        ThreadForum(id = "2", title = "Food", forumPost = emptyList()),
        ThreadForum(id = "3", title = "Mobility", forumPost = samplePosts)
    )

    AppTheme {
        ForumContent(
            threads = sampleThreads,
            selectedThreadId = "3" // para que "Mobility" aparezca expandido
        )
    }
}
