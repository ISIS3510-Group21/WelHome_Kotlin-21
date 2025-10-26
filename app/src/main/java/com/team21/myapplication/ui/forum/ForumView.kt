package com.team21.myapplication.ui.forum

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.team21.myapplication.data.model.ForumPost
import com.team21.myapplication.data.model.ThreadForum
import com.team21.myapplication.ui.components.cards.CommentCard
import com.team21.myapplication.ui.components.cards.ExpandableCard
import com.team21.myapplication.ui.theme.AppTheme

@Composable
fun ForumScreen(
    threads: List<ThreadForum>
) {
    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
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

            LazyColumn(
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(threads) { thread ->
                    ExpandableCard(
                        title = thread.title,
                        icon = getIconForTopic(thread.title),
                        initiallyExpanded = thread.title == "Mobility" // To match the screenshot
                    ) {
                        Column {
                            thread.forumPost.forEach { post ->
                                CommentCard(
                                    imageUrl = post.userPhoto.takeIf { it.isNotBlank() },
                                    name = post.userName,
                                    country = "Canada", // Hardcoded as in the image
                                    comment = post.content,
                                    rating = 4.95f // Hardcoded as in the image
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

fun getIconForTopic(topicTitle: String): ImageVector {
    return when (topicTitle) {
        "General Questions" -> Icons.Outlined.HelpOutline
        "Food" -> Icons.Outlined.Restaurant
        "Mobility" -> Icons.Outlined.DirectionsCar
        else -> Icons.Outlined.HelpOutline
    }
}

@Preview(showBackground = true)
@Composable
fun ForumScreenPreview() {
    val samplePosts = listOf(
        ForumPost(
            id = "1",
            content = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco",
            positiveVotes = 495,
            negativeVotes = 5,
            userName = "Jhon Doe",
            userPhoto = ""
        ),
        ForumPost(
            id = "2",
            content = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud",
            positiveVotes = 495,
            negativeVotes = 5,
            userName = "Jhon Doe",
            userPhoto = ""
        )
    )

    val sampleThreads = listOf(
        ThreadForum(id = "1", title = "General Questions", forumPost = emptyList()),
        ThreadForum(id = "2", title = "Food", forumPost = emptyList()),
        ThreadForum(id = "3", title = "Mobility", forumPost = samplePosts)
    )

    AppTheme {
        ForumScreen(threads = sampleThreads)
    }
}
