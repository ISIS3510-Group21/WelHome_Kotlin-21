package com.team21.myapplication.ui.forum

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.team21.myapplication.ui.theme.AppTheme

class ForumActivity : ComponentActivity() {
    private val viewModel: ForumViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                ForumScreen()
            }
        }
    }
}
