package com.team21.myapplication.ui.bookVisit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.team21.myapplication.ui.theme.AppTheme

class BookVisitActivity : ComponentActivity() {

    companion object {
        const val EXTRA_HOUSING_ID = "housing_id"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val housingId = intent?.getStringExtra(EXTRA_HOUSING_ID).orEmpty()

        setContent {
            AppTheme {
                BookVisitRoute(
                    housingId = housingId,
                    onBack = { finish() }
                )
            }
        }
    }
}
