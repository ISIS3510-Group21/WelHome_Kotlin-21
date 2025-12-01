package com.team21.myapplication.ui.components.cards

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.team21.myapplication.ui.components.icons.AppIcons
import com.team21.myapplication.ui.components.text.BlackText
import com.team21.myapplication.ui.components.text.GrayText
import com.team21.myapplication.ui.theme.AppTheme
import com.team21.myapplication.ui.theme.BlueSecondary
import com.team21.myapplication.ui.theme.GrayIcon
import kotlin.collections.forEach
import androidx.compose.ui.draw.clip

data class PropertyOptionUi(
    val id: String,
    val title: String,
    val subtitle: String,
    val thumbnailUrl: String
)

/**
 * Tarjeta “desplegable” para seleccionar una propiedad.
 */
@Composable
fun PropertySelectorCard(
    selectedTitle: String,
    selectedSubtitle: String,
    selectedThumbnail: String,
    options: List<PropertyOptionUi>,
    onOptionSelected: (PropertyOptionUi) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true },
            shape = RoundedCornerShape(16.dp),
            tonalElevation = 2.dp,
            shadowElevation = 2.dp,
            color = MaterialTheme.colorScheme.surface
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Thumbnail de la propiedad
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(
                            color = BlueSecondary,
                            shape = RoundedCornerShape(12.dp)
                        )
                ) {
                    if (selectedThumbnail.isNotBlank()) {
                        AsyncImage(
                            model = selectedThumbnail,
                            contentDescription = "Property thumbnail",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.matchParentSize().clip(RoundedCornerShape(12.dp))
                        )
                    }
                }

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    val titleToShow =
                        if (selectedTitle.isNotBlank()) selectedTitle else "Select a property"
                    val subtitleToShow =
                        if (selectedSubtitle.isNotBlank()) selectedSubtitle else "Tap to choose one"

                    BlackText(
                        text = titleToShow,
                        size = 16.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    GrayText(
                        text = subtitleToShow,
                        size = 14.sp
                    )
                }

                // Flecha “desplegable”: reutilizamos GoBack rotado 90°
                Icon(
                    imageVector = AppIcons.GoBack,
                    contentDescription = "Expand properties",
                    tint = GrayIcon,
                    modifier = Modifier
                        .size(20.dp)
                        .rotate(270f)  // apunta hacia abajo
                )
            }
        }

        // Lista desplegable de propiedades (simple lista bajo la tarjeta)
        if (expanded && options.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(vertical = 4.dp)
            ) {
                options.forEach { option ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onOptionSelected(option)
                                expanded = false
                            }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(
                                    color = BlueSecondary,
                                    shape = RoundedCornerShape(10.dp)
                                )
                        ) {
                            if (option.thumbnailUrl.isNotBlank()) {
                                AsyncImage(
                                    model = option.thumbnailUrl,
                                    contentDescription = option.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.matchParentSize().clip(RoundedCornerShape(12.dp))
                                )
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            BlackText(text = option.title, size = 14.sp)
                            if (option.subtitle.isNotBlank()) {
                                Spacer(modifier = Modifier.height(2.dp))
                                GrayText(text = option.subtitle, size = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFCFCFC)
@Composable
fun PropertySelectorCard_Preview_Selected() {
    AppTheme {
        val options = listOf(
            PropertyOptionUi(
                id = "1",
                title = "Portal de los Rosales",
                subtitle = "Calle 61 #4-74",
                thumbnailUrl = "" //url real
            ),
            PropertyOptionUi(
                id = "2",
                title = "Chapinero Alto Loft",
                subtitle = "Cra 4 #58-12",
                thumbnailUrl = ""
            )
        )

        PropertySelectorCard(
            selectedTitle = options[0].title,
            selectedSubtitle = options[0].subtitle,
            selectedThumbnail = "",
            options = options,
            onOptionSelected = {}
        )
    }
}