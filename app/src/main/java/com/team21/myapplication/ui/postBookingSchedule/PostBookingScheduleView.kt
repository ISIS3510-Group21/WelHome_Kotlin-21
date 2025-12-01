package com.team21.myapplication.ui.postBookingSchedule

import androidx.compose.foundation.layout.*
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.team21.myapplication.ui.components.buttons.BlueButton
import com.team21.myapplication.ui.components.cards.PropertyOptionUi
import com.team21.myapplication.ui.components.icons.AppIcons
import com.team21.myapplication.ui.components.text.BlackText
import com.team21.myapplication.ui.components.text.GrayText
import com.team21.myapplication.ui.postBookingSchedule.state.PostBookingScheduleUiState
import com.team21.myapplication.ui.components.cards.PropertySelectorCard
import com.team21.myapplication.ui.theme.AppTheme
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material3.Scaffold
import androidx.compose.foundation.background
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.AlertDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource


@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PostBookingView(
    state: PostBookingScheduleUiState,
    onBack: () -> Unit,
    onSelectProperty: (String) -> Unit,
    onSelectDate: (Long?) -> Unit,
    onToggleHour: (String) -> Unit,
    onSave: () -> Unit,
    onResultAcknowledged: (Boolean) -> Unit
) {
    val dateState = rememberDatePickerState(
        initialSelectedDateMillis = state.selectedDateMillis
    )

    // Mantener sincronizado DatePicker -> ViewModel
    LaunchedEffect(dateState.selectedDateMillis) {
        if (dateState.selectedDateMillis != state.selectedDateMillis) {
            onSelectDate(dateState.selectedDateMillis)
        }
    }

    Scaffold{ paddingValues ->

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            val scrollState = rememberScrollState()

            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                ) {
                    // Top bar
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
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        BlackText(
                            text = "Add visit schedule",
                            size = 20.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // 1) Selector de propiedad
                    BlackText(text = "Select a Property", size = 18.sp)
                    Spacer(modifier = Modifier.height(8.dp))

                    PropertySelectorCard(
                        selectedTitle = state.selectedPropertyTitle,
                        selectedSubtitle = state.selectedPropertySubtitle,
                        selectedThumbnail = state.selectedPropertyThumbnail,
                        options = state.properties,
                        onOptionSelected = { option ->
                            onSelectProperty(option.id)
                        }
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // 2) Selector de día (calendario)
                    BlackText(text = "Select a day", size = 18.sp)
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

                    Spacer(modifier = Modifier.height(4.dp))

                    // 3) Horas disponibles
                    BlackText(text = "Set available times", size = 18.sp)
                    Spacer(modifier = Modifier.height(8.dp))

                    if (state.availableHours.isEmpty()) {
                        GrayText(
                            text = "No time slots configured for this day yet.",
                            size = 13.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    } else {
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(
                                12.dp,
                                Alignment.CenterHorizontally
                            ),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            state.availableHours.forEach { hour ->
                                val isSelected = state.selectedHours.contains(hour)

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(0.45f)
                                ) {
                                    BlueButton(
                                        text = hour,
                                        onClick = { onToggleHour(hour) },
                                        modifier = Modifier.alpha(if (isSelected) 1f else 0.4f)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // 4) CTA para guardar slots
                    BlueButton(
                        text = "Save schedule",
                        onClick = onSave,
                        enabled = state.selectedPropertyId != null &&
                                state.selectedDateMillis != null &&
                                state.selectedHours.isNotEmpty()
                    )
                }

                // Overlay de carga mientras se hace el POST
                if (state.isSaving) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.2f))
                            .clickable(
                                enabled = true,
                                indication = null, // sin ripple
                                interactionSource = remember { MutableInteractionSource() }
                            ) {
                                // No hacemos nada
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }

    // Modal de resultado (éxito / error)
    if (state.snackbarMessage != null) {
        AlertDialog(
            onDismissRequest = {
                // Evitamos cerrar sin que el usuario pulse el botón
            },
            title = {
                BlackText(
                    text = if (state.snackbarError) "Something went wrong" else "Schedule saved",
                    size = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
            },
            text = {
                GrayText(
                    text = state.snackbarMessage,
                    size = 14.sp
                )
            },
            confirmButton = {
                // BlueButton pequeño de confirmación
                BlueButton(
                    text = "OK",
                    onClick = { onResultAcknowledged(state.snackbarError) },
                    // botón más compacto
                    modifier = Modifier
                        .fillMaxWidth()
                )
            }
        )
    }
}


@Preview(showBackground = true, backgroundColor = 0xFFFCFCFC)
@Composable
fun PostBookingView_Preview() {
    AppTheme {
        val sampleProperties = listOf(
            PropertyOptionUi(
                id = "1",
                title = "Portal de los Rosales",
                subtitle = "Calle 61 #4-74",
                thumbnailUrl = ""
            ),
            PropertyOptionUi(
                id = "2",
                title = "Chapinero Alto Loft",
                subtitle = "Cra 4 #58-12",
                thumbnailUrl = ""
            )
        )

        val sampleState = PostBookingScheduleUiState(
            properties = sampleProperties,
            selectedPropertyId = "1",
            selectedPropertyTitle = sampleProperties[0].title,
            selectedPropertySubtitle = sampleProperties[0].subtitle,
            selectedPropertyThumbnail = "",
            selectedDateMillis = 1735689600000L, // 1 Jan 2025 aprox
            availableHours = listOf("7:00", "9:00", "10:00", "11:00", "13:00", "14:00"),
            selectedHours = setOf("7:00", "11:00")
        )

        PostBookingView(
            state = sampleState,
            onBack = {},
            onSelectProperty = {},
            onSelectDate = {},
            onToggleHour = {},
            onSave = {},
            onResultAcknowledged = {}
        )
    }
}





