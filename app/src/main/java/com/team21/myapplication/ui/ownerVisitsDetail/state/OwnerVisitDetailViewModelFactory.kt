package com.team21.myapplication.ui.ownerVisitsDetail.state

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.team21.myapplication.ui.ownerVisitsDetail.OwnerVisitDetailViewModel

class OwnerVisitDetailViewModelFactory(
    private val application: Application
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(OwnerVisitDetailViewModel::class.java)) {
            return OwnerVisitDetailViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}