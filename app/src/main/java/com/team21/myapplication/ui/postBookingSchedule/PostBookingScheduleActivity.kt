package com.team21.myapplication.ui.postBookingSchedule

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.team21.myapplication.ui.postBookingSchedule.state.PostBookingScheduleRoute
import com.team21.myapplication.ui.theme.AppTheme

class PostBookingScheduleActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AppTheme {
                PostBookingScheduleRoute(
                    onBack = { finish() }
                )
            }
        }
    }

    companion object {
        fun newIntent(context: Context): Intent {
            return Intent(context, PostBookingScheduleActivity::class.java)
        }
    }
}