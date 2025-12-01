package com.team21.myapplication.ui.postBookingSchedule.state

import android.app.Application
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.team21.myapplication.ui.postBookingSchedule.PostBookingScheduleViewModel
import com.team21.myapplication.ui.postBookingSchedule.PostBookingScheduleViewModelFactory
import com.team21.myapplication.ui.postBookingSchedule.PostBookingView

@Composable
fun PostBookingScheduleRoute(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val application = context.applicationContext as Application
    val factory = PostBookingScheduleViewModelFactory(application)
    val viewModel: PostBookingScheduleViewModel = viewModel(factory = factory)

    val state by viewModel.state.collectAsState()


    PostBookingView(
        state = state,
        onBack = onBack,
        onSelectProperty = { viewModel.onPropertySelected(it) },
        onSelectDate = { millis -> viewModel.onDateSelected(millis) },
        onToggleHour = { hour -> viewModel.onToggleHour(hour) },
        onSave = { viewModel.saveSchedule() },
        onResultAcknowledged = { isError ->
            if (!isError) {
                // Éxito → cerramos Activity (vuelve a lista de visitas)
                onBack()
            }
            // Limpiamos el mensaje en el estado para que se esconda el modal
            viewModel.consumeSnackbarMessage()
        }
    )
}
