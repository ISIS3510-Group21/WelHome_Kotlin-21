package com.team21.myapplication.ui.ownerVisitsDetail

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.team21.myapplication.ui.theme.AppTheme
import android.content.Context
import android.content.Intent
import com.team21.myapplication.data.model.OwnerScheduledVisit
import com.team21.myapplication.ui.ownerVisitsDetail.state.OwnerVisitDetailRoute
import java.text.SimpleDateFormat
import java.util.Locale


class OwnerVisitDetailActivity : ComponentActivity() {

    companion object {
        const val EXTRA_BOOKING_ID = "booking_id"
        const val EXTRA_IS_AVAILABLE = "is_available"
        const val EXTRA_PROPERTY_IMAGE_URL = "property_image_url"
        const val EXTRA_PROPERTY_NAME = "property_name"
        const val EXTRA_VISIT_DATE = "visit_date"
        const val EXTRA_VISIT_TIME = "visit_time"

        fun newIntent(context: Context, visit: OwnerScheduledVisit): Intent {
            val formattedDate = SimpleDateFormat(
                "MMMM dd, yyyy",
                Locale.ENGLISH
            ).format(visit.date.toDate())

            return Intent(context, OwnerVisitDetailActivity::class.java).apply {
                putExtra(EXTRA_BOOKING_ID, visit.bookingId)
                putExtra(EXTRA_IS_AVAILABLE, visit.isAvailable)
                putExtra(EXTRA_PROPERTY_IMAGE_URL, visit.propertyImageUrl)
                putExtra(EXTRA_PROPERTY_NAME, visit.propertyName)
                putExtra(EXTRA_VISIT_DATE, formattedDate)
                putExtra(EXTRA_VISIT_TIME, visit.timeRange)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Extraer los datos del Intent
        val bookingId = intent.getStringExtra(EXTRA_BOOKING_ID) ?: ""
        val isAvailable = intent.getBooleanExtra(EXTRA_IS_AVAILABLE, false)
        val propertyImageUrl = intent.getStringExtra(EXTRA_PROPERTY_IMAGE_URL) ?: ""
        val propertyName = intent.getStringExtra(EXTRA_PROPERTY_NAME) ?: ""
        val visitDate = intent.getStringExtra(EXTRA_VISIT_DATE) ?: ""
        val visitTime = intent.getStringExtra(EXTRA_VISIT_TIME) ?: ""

        setContent {
            AppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    OwnerVisitDetailRoute(
                        bookingId = bookingId,
                        isAvailable = isAvailable,
                        propertyImageUrl = propertyImageUrl,
                        propertyName = propertyName,
                        visitDate = visitDate,
                        visitTime = visitTime,
                        onBack = { finish() }
                    )
                }
            }
        }
    }
}

