package com.team21.myapplication.ui.saved

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.team21.myapplication.ui.detailView.DetailHousingActivity
import com.team21.myapplication.ui.filterView.FilterActivity

class SavedPostsActivity : ComponentActivity() {
    companion object {
        const val EXTRA_FROM_NAV = "EXTRA_FROM_NAV"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            SavedPostsRoute(
                onOpenDetail = { id ->
                    startActivity(
                        Intent(this, DetailHousingActivity::class.java)
                            .putExtra(DetailHousingActivity.EXTRA_HOUSING_ID, id)
                    )
                },
                onOpenFilters = {
                    startActivity(Intent(this, FilterActivity::class.java))
                }
            )
        }
    }
}
