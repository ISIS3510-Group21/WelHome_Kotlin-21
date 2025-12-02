package com.team21.myapplication.ui.components.cards

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.team21.myapplication.ui.components.text.BlackText
import com.team21.myapplication.ui.components.text.BlueText
import com.team21.myapplication.ui.components.text.GrayText
import com.team21.myapplication.ui.theme.AppTheme
import com.team21.myapplication.ui.theme.Poppins

/**
 * Tarjeta que muestra información de una visita agendada a una propiedad.
 *
 * @param date Fecha de la visita (ej: "Mon, Oct 26")
 * @param timeRange Rango de hora de la visita (ej: "4:00 PM - 5:00 PM")
 * @param propertyName Nombre de la propiedad
 * @param visitorName Nombre del visitante
 * @param propertyImageUrl URL de la imagen de la propiedad
 * @param status Estado de la visita (ej: "Scheduled", "Completed", "Cancelled")
 * @param modifier Modificador para personalización adicional
 * @param onCardClick Callback cuando se presiona la tarjeta
 */
@Composable
fun ScheduledVisitCard(
    date: String,
    timeRange: String,
    propertyName: String,
    visitorName: String,
    propertyImageUrl: String,
    status: String = "Scheduled",
    isPending: Boolean = false,
    modifier: Modifier = Modifier,
    onCardClick: () -> Unit = {}
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        ),
        enabled = !isPending,
        onClick = onCardClick
    ) {
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .graphicsLayer {
                        if (isPending) {
                            alpha = 0.6f    // filtro grisáceo general
                        }
                    }
            ) {
                // Header: Fecha y Estado
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    // Fecha
                    Column {
                        BlackText(
                            text = date,
                            size = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        BlueText(
                            text = timeRange,
                            size = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Estado Badge
                    Box(
                        modifier = Modifier
                            .background(
                                color = when (status.lowercase()) {
                                    "completed" -> Color(0xFFE0E7FF)  // Azul claro
                                    "missed" -> Color(0xFFFFE4E6)     // Rojo claro
                                    "scheduled" -> Color(0xFFD1FAE5)  // Verde claro
                                    "available" -> Color(0xFFFFEDD5)  // amarillo claro
                                    "pending" -> Color(0xFFE5E7EB)
                                    else -> MaterialTheme.colorScheme.secondaryContainer
                                },
                                shape = RoundedCornerShape(20.dp)
                            )
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = status,
                            color = when (status.lowercase()) {
                                "completed" -> MaterialTheme.colorScheme.primary  // Azul
                                "missed" -> Color(0xFFEF4444) // Rojo
                                "scheduled" -> Color(0xFF10B981)  // Verde
                                "available" -> Color(0xFFF97316)// naranja
                                "pending" -> Color(0xFF4B5563) //gris
                                else -> MaterialTheme.colorScheme.primary
                            },
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            fontFamily = Poppins
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Divider
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Información de la propiedad y visitante
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Imagen de la propiedad
                    AsyncImage(
                        model = propertyImageUrl,
                        contentDescription = "Property image",
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )

                    // Información textual
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        BlackText(
                            text = propertyName,
                            size = 18.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        GrayText(
                            text = visitorName.ifBlank { "No visitor yet" },
                            size = 14.sp,
                            fontWeight = FontWeight.Normal
                        )
                    }
                }
            }

            // Relojito en la esquina superior derecha
            if (isPending) {
                Icon(
                    imageVector = Icons.Outlined.Schedule, // importa este ícono
                    contentDescription = "Pending upload",
                    tint = Color(0xFF4B5563),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(12.dp)
                )
            }
        }
    }
}

// Preview con datos de ejemplo
@Preview(showBackground = true, backgroundColor = 0xFFFCFCFC)
@Composable
fun ScheduledVisitCardPreview() {
    AppTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            ScheduledVisitCard(
                date = "Mon, Oct 26",
                timeRange = "4:00 PM - 5:00 PM",
                propertyName = "Portal de los Rosales",
                visitorName = "John Doe",
                propertyImageUrl = "https://images.unsplash.com/photo-1502672260266-1c1ef2d93688?w=400",
                status = "Scheduled"
            )

            ScheduledVisitCard(
                date = "Tue, Oct 27",
                timeRange = "2:00 PM - 3:00 PM",
                propertyName = "Apartamento Centro",
                visitorName = "Jane Smith",
                propertyImageUrl = "https://images.unsplash.com/photo-1522708323590-d24dbb6b0267?w=400",
                status = "Available"
            )

            ScheduledVisitCard(
                date = "Wed, Oct 28",
                timeRange = "10:00 AM - 11:00 AM",
                propertyName = "Residencia Universitaria Norte",
                visitorName = "Carlos González",
                propertyImageUrl = "https://images.unsplash.com/photo-1536376072261-38c75010e6c9?w=400",
                status = "Pending",
                isPending = true
            )
        }
    }
}

// Preview en tema oscuro
@Preview(showBackground = true, backgroundColor = 0xFF0E0F14)
@Composable
fun ScheduledVisitCardDarkPreview() {
    AppTheme {
        ScheduledVisitCard(
            date = "Mon, Oct 26",
            timeRange = "4:00 PM - 5:00 PM",
            propertyName = "Portal de los Rosales",
            visitorName = "John Doe",
            propertyImageUrl = "https://images.unsplash.com/photo-1502672260266-1c1ef2d93688?w=400",
            status = "Scheduled"
        )
    }
}