package com.team21.myapplication.ui.updateprofile

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.team21.myapplication.data.repository.StudentUserRepository
import com.team21.myapplication.ui.theme.AppTheme

class UpdateProfileActivity : ComponentActivity() {

    private val studentUserRepository by lazy { StudentUserRepository() }
    private val viewModel: UpdateProfileViewModel by viewModels {
        UpdateProfileViewModelFactory(studentUserRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                val user by viewModel.user.collectAsState()

                user?.let {
                    Log.d("UpdateProfileActivity", "User: $it")
                    UpdateProfileScreen(
                        user = it,
                        onSave = {
                            viewModel.updateUser(it) { success ->
                                if (success) {
                                    Toast.makeText(this, "Profile updated", Toast.LENGTH_SHORT).show()
                                    finish()
                                } else {
                                    Toast.makeText(this, "Failed to update profile", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        onBack = { finish() }
                    )
                }
            }
        }
    }
}
