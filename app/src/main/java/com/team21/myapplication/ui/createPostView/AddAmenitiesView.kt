package com.team21.myapplication.ui.createPostView

import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.team21.myapplication.data.model.Ammenities
import com.team21.myapplication.ui.components.buttons.BlueButton
import com.team21.myapplication.ui.components.buttons.CustomRadioButton
import com.team21.myapplication.ui.components.text.BlackText
import com.team21.myapplication.ui.components.icons.AppIcons
import com.team21.myapplication.ui.theme.BlueCallToAction
import androidx.compose.ui.platform.LocalContext
import com.team21.myapplication.utils.NetworkMonitor


@Composable
fun AddAmenitiesLayout(
    viewModel: AmenitiesViewModel = viewModel(),
    initialAmenities: List<Ammenities> = emptyList(),
    onSaveToPost: (List<Ammenities>) -> Unit = {},
    onBack: () -> Unit = {}
) {

    val context = LocalContext.current
    val networkMonitor = remember { NetworkMonitor.get(context) }
    val isOnline by networkMonitor.isOnline.collectAsState()


    // Establish initial section
    // solo  una vez con las amenities que vienen del post
    LaunchedEffect(Unit) {
        if (initialAmenities.isNotEmpty()) {
            viewModel.setInitialSelection(initialAmenities)
        }
    }

    LaunchedEffect(isOnline) {
        // Carga siempre que entra a la pantalla; si vuelve la red, refresca remoto y cachea.
        viewModel.refreshAmenities(context, isOnline)
    }


    val amenitiesList by viewModel.amenitiesList.collectAsState()
    val selectedIds by viewModel.selectedAmenitiesIds.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            IconButton(onClick = {onBack()}) {
                Icon(
                    imageVector = AppIcons.GoBack,
                    contentDescription = "Back",
                    modifier = Modifier.size(24.dp),
                    tint = BlueCallToAction
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            BlackText(
                text = "Add amenities",
                size = 24.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            CircularProgressIndicator()
        } else {
            if (amenitiesList.isEmpty()) {
                BlackText(text = if (isOnline) "No amenities found." else "No cached amenities available offline.")
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(8.dp)
                    .fillMaxWidth()
            ) {
                amenitiesList.forEach { amenity ->
                    CustomRadioButton(
                        text = amenity.name,
                        selected = selectedIds.contains(amenity.id),
                        onClick = { viewModel.toggleAmenity(amenity.id) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        BlueButton(
            text = "Save (${selectedIds.size} selected)",
            onClick = {
                val selected = viewModel.getSelectedAmenities()
                onSaveToPost(selected)
            }
        )
    }
}