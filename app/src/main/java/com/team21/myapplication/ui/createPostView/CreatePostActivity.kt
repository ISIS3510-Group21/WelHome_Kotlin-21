package com.team21.myapplication.ui.createPostView

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.team21.myapplication.ui.theme.AppTheme
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import kotlinx.coroutines.launch

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
            // Apply application's theme
            AppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    NavHost(navController = navController, startDestination = "createPost"){
                        composable("createPost"){
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
                                },
                                onOpenAmenities  = {
                                    navController.navigate("amenities")
                                }
                            )
                        }
                        composable("amenities"){
                            val postUiState = viewModel.uiState.collectAsState().value
                            AddAmenitiesLayout(
                                initialAmenities = postUiState.selectedAmenities, //precargar seleccionados
                                onSaveToPost = { selected ->
                                    viewModel.updateSelectedAmenities(selected)
                                    navController.popBackStack()
                                },
                                onBack = { navController.popBackStack() },
                            )
                        }
                    }


                }
            }
        }

        lifecycleScope.launch {
            val db = com.team21.myapplication.data.local.AppDatabase.getDatabase(this@CreatePostActivity)
            val pending = db.draftPostDao().getAllDraftsOnce()
            pending.forEach { draft ->
                com.team21.myapplication.workers.enqueueUploadDraft(this@CreatePostActivity, draft.id)
            }
        }

    }
}