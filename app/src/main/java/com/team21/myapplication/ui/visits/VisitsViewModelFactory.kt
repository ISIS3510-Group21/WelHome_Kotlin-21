package com.team21.myapplication.ui.visits

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class VisitsViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(VisitsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return VisitsViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
