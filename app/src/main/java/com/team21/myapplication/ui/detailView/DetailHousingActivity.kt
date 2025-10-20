package com.team21.myapplication.ui.detailView

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.team21.myapplication.ui.theme.AppTheme

class DetailHousingActivity : ComponentActivity() {

    companion object {
        const val EXTRA_HOUSING_ID = "housing_id"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val housingId = intent?.getStringExtra(EXTRA_HOUSING_ID) ?: ""

        setContent {
            AppTheme {
                // Aquí uso tu Route actual del detalle y paso finish() en el back
                DetailHousingRoute(
                    housingId = housingId,
                    onBack = { finish() }
                )
            }
        }
    }
}
