package com.team21.myapplication.ui.createforumpost

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import com.team21.myapplication.data.repository.ForumRepository
import com.team21.myapplication.data.repository.StudentUserRepository
import com.team21.myapplication.ui.theme.AppTheme

class CreateForumPostActivity : ComponentActivity() {
    private lateinit var viewModel: CreateForumPostViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize repositories
        val forumRepository = ForumRepository(applicationContext)
        val studentUserRepository = StudentUserRepository()

        // Initialize ViewModel
        val viewModelFactory = CreateForumPostViewModelFactory(forumRepository, studentUserRepository)
        viewModel = ViewModelProvider(this, viewModelFactory)[CreateForumPostViewModel::class.java]

        setContent {
            AppTheme {
                Scaffold { innerPadding ->
                    CreateForumPostScreenLayout(
                        viewModel = viewModel,
                        onNavigateBack = { finish() },
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}
