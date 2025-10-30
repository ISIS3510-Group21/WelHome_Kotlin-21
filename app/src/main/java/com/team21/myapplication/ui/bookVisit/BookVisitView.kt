package com.team21.myapplication.ui.bookVisit

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.compose.material3.DatePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.sp
import com.team21.myapplication.ui.components.buttons.BlueButton
import com.team21.myapplication.ui.components.icons.AppIcons
import com.team21.myapplication.ui.components.text.BlackText
import com.team21.myapplication.ui.theme.BlueCallToAction
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.setSelectedDate
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.text.font.FontWeight
import com.team21.myapplication.ui.bookVisit.state.BookVisitUiState
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.background
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties



@Composable
fun BookVisitView(
    state: BookVisitUiState,
    onBack: () -> Unit,
    onSelectDate: (Long?) -> Unit,
    onSelectHour: (String) -> Unit,
    onConfirm: () -> Unit,
    onSuccess: () -> Unit
) {
    val dateState = rememberDatePickerState(
        initialSelectedDateMillis = state.selectedDateMillis
    )

    // Mantener sincronizado el DatePicker con el VM:
    LaunchedEffect(dateState.selectedDateMillis) {
        onSelectDate(dateState.selectedDateMillis)
    }


    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {

        Box(
            modifier = Modifier.fillMaxSize()
        ) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
            ) {
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = AppIcons.GoBack,
                            contentDescription = "Back",
                            modifier = Modifier.size(24.dp),
                            tint = BlueCallToAction
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    BlackText(
                        text = "Book your appointment",
                        size = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
                BlackText(text = "Select a date")
                Spacer(modifier = Modifier.height(8.dp))

                DatePicker(
                    state = dateState,
                    showModeToggle = false,
                    title = null,
                    headline = null,
                    colors = DatePickerDefaults.colors(
                        containerColor = MaterialTheme.colorScheme.background,
                        titleContentColor = MaterialTheme.colorScheme.onSurface,
                        headlineContentColor = MaterialTheme.colorScheme.onSurface,
                        weekdayContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        dayContentColor = MaterialTheme.colorScheme.onSurface,
                        selectedDayContainerColor = MaterialTheme.colorScheme.primary,
                        selectedDayContentColor = MaterialTheme.colorScheme.onPrimary,
                        todayDateBorderColor = MaterialTheme.colorScheme.primary,
                        navigationContentColor = MaterialTheme.colorScheme.onSurface

                    )
                )

                Spacer(modifier = Modifier.height(8.dp))
                BlackText(text = "Select an hour")
                Spacer(modifier = Modifier.height(8.dp))

                if (state.selectedDateMillis != null && state.availableHours.isEmpty()) {
                    BlackText(text = "No availability for this date")
                    Spacer(modifier = Modifier.height(8.dp))
                }

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    state = rememberLazyGridState(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                ) {
                    items(state.availableHours) { h ->
                        BlueButton(
                            text = if (h == state.selectedHour) "✓ $h" else h,
                            onClick = { onSelectHour(h) },
                            enabled = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                BlueButton(
                    text = "Confirm date",
                    enabled = state.selectedDateMillis != null && state.selectedHour != null && !state.isConfirming,
                    onClick = onConfirm
                )

                Spacer(modifier = Modifier.height(12.dp))

            }

            state.successMessage?.let {
                Spacer(modifier = Modifier.height(8.dp))
                BlackText(text = it)
            }

            state.error?.let {
                Spacer(modifier = Modifier.height(8.dp))
                BlackText(text = it)
            }

            if (state.isConfirming) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.35f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        modifier = Modifier
                            .background(
                                color = MaterialTheme.colorScheme.surface,
                                shape = MaterialTheme.shapes.medium
                            )
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        androidx.compose.material3.CircularProgressIndicator()
                        Text("Scheduling your visit…", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

        }

        state.successMessage?.let { msg ->
            AlertDialog(
                onDismissRequest = { /* obligatorio tocar el botón OK */ },
                confirmButton = {
                    BlueButton(text = "OK", onClick = onSuccess) // <- callback que nos pasas desde Route
                },
                title = { Text("Success") },
                text = { Text(msg) }
            )
        }

    }
}

@Composable
fun BookVisitRoute(
    housingId: String,
    onBack: () -> Unit = {}
) {
    val vm: BookVisitViewModel = viewModel()
    val uiState by vm.state.collectAsState()

    LaunchedEffect(housingId) { vm.load(housingId) }

    BookVisitView(
        state = uiState,
        onBack = onBack,
        onSelectDate = vm::onDateSelected,
        onSelectHour = vm::onHourSelected,
        onConfirm = vm::onConfirm,
        onSuccess = vm::onSuccessAcknowledged
    )
}



