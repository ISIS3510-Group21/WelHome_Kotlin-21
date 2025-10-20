package com.team21.myapplication.ui.filterView.results

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Scaffold
import androidx.navigation.compose.rememberNavController
import com.team21.myapplication.ui.components.navbar.AppNavBar
import com.team21.myapplication.ui.detailView.DetailHousingActivity
import com.team21.myapplication.ui.theme.AppTheme

class FilterResultsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AppTheme {
                val navController = rememberNavController()
                Scaffold(
                    bottomBar = { AppNavBar(navController) }
                ) { inner ->
                    FilterResultsRoute(
                        onOpenDetail = { housingId ->
                            startActivity(
                                Intent(this, DetailHousingActivity::class.java)
                                    .putExtra(DetailHousingActivity.EXTRA_HOUSING_ID, housingId)
                            ) },
                        onNavigateBottomBar = { /* navegar por bottom bar si aplica */ }
                    )
                }
            }
        }
    }
}
