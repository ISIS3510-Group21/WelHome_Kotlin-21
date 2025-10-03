package com.team21.myapplication.utils
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.team21.myapplication.analytics.AnalyticsHelper
import com.team21.myapplication.ui.main.MainViewModel

class AppViewModelFactory(
    private val analyticsHelper: AnalyticsHelper
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {

            modelClass.isAssignableFrom(MainViewModel::class.java) -> {
                MainViewModel(analyticsHelper) as T
            }


            // modelClass.isAssignableFrom(ExampleViewModel::class.java) -> {
            //     ExampleViewModel(analyticsHelper) as T
            // }


            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}