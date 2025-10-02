package com.team21.myapplication.ui.createPostView

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.team21.myapplication.ui.theme.AppTheme

/**
 * Activity for creating a new housing post
 *
 * FUNCTION:
 * This Activity serves as a container for the Composable view
 * and handles navigation when the post is successfully created
 */
class CreatePostActivity : ComponentActivity() {

    // Get ViewModel instance using by viewModels()
    // This ensures the ViewModel survives configuration changes
    private val viewModel: CreatePostViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Configure content with Compose
        setContent {
            // Apply your application's theme
            AppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Render the post creation view
                    CreatePostScreenLayout(
                        viewModel = viewModel,
                        onPostCreated = {
                            // Callback when the post is successfully created
                            Toast.makeText(
                                this@CreatePostActivity,
                                "Post created successfully",
                                Toast.LENGTH_SHORT
                            ).show()

                            // Finish the Activity and return to the previous one
                            finish()
                        },
                        onNavigateBack = {
                            // Callback for the back button
                            finish()
                        }
                    )
                }
            }
        }
    }
}