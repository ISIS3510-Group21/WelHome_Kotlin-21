package com.team21.myapplication.ui.ownerVisits.state

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.team21.myapplication.ui.ownerVisits.OwnerVisitsViewModel

class OwnerVisitsViewModelFactory(
    private val application: Application
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(OwnerVisitsViewModel::class.java)) {
            return OwnerVisitsViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}