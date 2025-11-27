package com.team21.myapplication.ui.ownerVisitsDetail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CommentsDisabled
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.team21.myapplication.ui.components.buttons.BlueButton
import com.team21.myapplication.ui.components.icons.AppIcons
import com.team21.myapplication.ui.components.inputs.PlaceholderTextField
import com.team21.myapplication.ui.components.text.BlackText
import com.team21.myapplication.ui.components.text.BlueText
import com.team21.myapplication.ui.components.text.GrayText
import com.team21.myapplication.ui.theme.AppTheme
import com.team21.myapplication.ui.theme.BlueSecondary
import com.team21.myapplication.ui.theme.GrayIcon

// Estados posibles de una visita
enum class VisitStatus {
    AVAILABLE,   // Time slot disponible (gris con ícono de calendario)
    SCHEDULED,   // Agendada (verde con check)
    CONFIRMED,   // Confirmada (verde con check)
    COMPLETED,   // Completada (azul con check)
    MISSED       // Perdida (rojo con X)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OwnerVisitDetailView(
    propertyImageUrl: String,
    visitStatus: VisitStatus,
    visitDate: String,
    visitTime: String,
    visitorName: String,
    visitorNationality: String = "Roomate",
    visitorPhotoUrl: String? = null,
    visitorFeedback: String? = null,
    visitorRating: Int? = null,
    ownerComment: String = "",
    onCommentChange: (String) -> Unit = {},
    onSaveComment: () -> Unit = {},
    onMessageClick: () -> Unit = {},
    onBackClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var comment by remember { mutableStateOf(ownerComment) }
    var showSnackbar by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(showSnackbar) {
        if (showSnackbar) {
            snackbarHostState.showSnackbar(
                message = "You can only add comments after the visit is completed",
                duration = SnackbarDuration.Short
            )
            showSnackbar = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    BlackText(
                        text = "Visit Details",
                        size = 22.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = AppIcons.GoBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // Imagen de la propiedad
            AsyncImage(
                model = propertyImageUrl,
                contentDescription = "Property image",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(16.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Status Badge
            StatusBadge(status = visitStatus)

            Spacer(modifier = Modifier.height(16.dp))

            // Fecha y hora
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Fecha
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = AppIcons.Visits,
                        contentDescription = "Date",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    BlackText(
                        text = visitDate,
                        size = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Hora
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Schedule,
                        contentDescription = "Time",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    BlackText(
                        text = visitTime,
                        size = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Divider
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp),
                color = MaterialTheme.colorScheme.outlineVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Visitor Information Section
            if (visitStatus == VisitStatus.AVAILABLE) {
                // Estado AVAILABLE: muestra mensaje informativo
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    BlackText(
                        text = "Visitor Information",
                        size = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    GrayText(
                        text = "Once this appointment has been confirmed by someone, you will be able to see the visitor's information here.",
                        size = 14.sp,
                        fontWeight = FontWeight.Normal
                    )
                }
            } else {
                // Otros estados: muestra información del visitante
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    BlackText(
                        text = "Visitor Information",
                        size = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Avatar del visitante (foto real o placeholder)
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.secondaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                if (!visitorPhotoUrl.isNullOrBlank()) {
                                    // Mostrar foto real del visitante
                                    AsyncImage(
                                        model = visitorPhotoUrl,
                                        contentDescription = "Visitor photo",
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(CircleShape),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    // Mostrar ícono placeholder si no hay foto
                                    Icon(
                                        imageVector = AppIcons.Profile,
                                        contentDescription = "Visitor avatar",
                                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            }

                            Column {
                                BlackText(
                                    text = visitorName,
                                    size = 16.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                GrayText(
                                    text = visitorNationality,
                                    size = 13.sp
                                )
                            }
                        }

                        // Message Button
                        Button(
                            onClick = onMessageClick,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp)
                        ) {
                            Text(
                                text = "Message",
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Feedback Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                BlackText(
                    text = "Feedback",
                    size = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(12.dp))

                when (visitStatus) {
                    VisitStatus.COMPLETED -> {
                        // Mostrar rating y feedback del visitante
                        if (visitorRating != null) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                repeat(5) { index ->
                                    Icon(
                                        imageVector = if (index < visitorRating)
                                            AppIcons.StarFilled
                                        else
                                            AppIcons.Star,
                                        contentDescription = "Star ${index + 1}",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        if (!visitorFeedback.isNullOrBlank()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        BlueSecondary,
                                        RoundedCornerShape(12.dp)
                                    )
                                    .padding(16.dp)
                            ) {
                                GrayText(
                                    text = visitorFeedback,
                                    size = 14.sp
                                )
                            }

                            if (visitorFeedback.length > 100) {
                                Spacer(modifier = Modifier.height(4.dp))
                                BlueText(
                                    text = "More",
                                    size = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    VisitStatus.MISSED -> {
                        // Mensaje cuando se perdió la visita
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Color(BlueSecondary.value),
                                    RoundedCornerShape(12.dp)
                                )
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.CommentsDisabled,
                                    contentDescription = "No feedback",
                                    tint = GrayIcon,
                                    modifier = Modifier.size(40.dp)
                                )
                                GrayText(
                                    text = "No feedback was provided as the visit was missed",
                                    size = 14.sp
                                )
                            }
                        }
                    }

                    VisitStatus.CONFIRMED, VisitStatus.SCHEDULED, VisitStatus.AVAILABLE -> {
                        // Mensaje cuando aún no hay feedback
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Color(BlueSecondary.value),
                                    RoundedCornerShape(12.dp)
                                )
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.CommentsDisabled,
                                    contentDescription = "No feedback",
                                    tint = GrayIcon,
                                    modifier = Modifier.size(40.dp)
                                )
                                GrayText(
                                    text = "Feedback from visitor will appear here after the visit is completed",
                                    size = 14.sp
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Add Your Comment Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                BlackText(
                    text = "Add Your Comment",
                    size = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(8.dp))

                GrayText(
                    text = if (visitStatus == VisitStatus.AVAILABLE) {
                        "You can add comments here after the visit is completed"
                    } else {
                        "Your private comments about this visit will only be visible to you"
                    },
                    size = 13.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Comment TextField
                PlaceholderTextField(
                    placeholderText = "e.g Student seemed very interested, asked about the lease terms...",
                    value = comment,
                    onValueChange = {
                        // Solo permitir cambios si la visita ya pasó
                        if (visitStatus == VisitStatus.COMPLETED || visitStatus == VisitStatus.MISSED) {
                            comment = it
                            onCommentChange(it)
                        }
                    },
                    height = 120.dp,
                    maxChars = 500,
                    maxCharsMessage = "Maximum 500 characters",
                    // Deshabilitar si no es COMPLETED o MISSED
                    enabled = (visitStatus == VisitStatus.COMPLETED || visitStatus == VisitStatus.MISSED)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Save Comment Button
                BlueButton(
                    text = "Save Comment",
                    onClick = {
                        if (visitStatus == VisitStatus.COMPLETED || visitStatus == VisitStatus.MISSED) {
                            onSaveComment()
                        } else {
                            showSnackbar = true
                        }
                    },
                    enabled = (visitStatus == VisitStatus.COMPLETED || visitStatus == VisitStatus.MISSED)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun StatusBadge(status: VisitStatus) {
    val (backgroundColor, iconColor, icon, title, subtitle) = when (status) {
        VisitStatus.COMPLETED -> Tuple5(
            Color(0xFFE0E7FF),
            MaterialTheme.colorScheme.primary,
            Icons.Filled.Check,
            "Visit Completed",
            null
        )
        VisitStatus.MISSED -> Tuple5(
            Color(0xFFFFE4E6),
            Color(0xFFEF4444),
            Icons.Filled.Close,
            "Visit Missed",
            "The visitor did not attend"
        )
        VisitStatus.CONFIRMED -> Tuple5(
            Color(0xFFD1FAE5),
            Color(0xFF10B981),
            Icons.Filled.Check,
            "Visit Confirmed",
            "This visit is confirmed"
        )
        VisitStatus.SCHEDULED -> Tuple5(
            Color(0xFFD1FAE5),
            Color(0xFF10B981),
            Icons.Filled.Check,
            "Visit scheduled",
            "Awaiting scheduled visit time"
        )
        VisitStatus.AVAILABLE -> Tuple5(
            Color(0xFFF3F4F6),
            Color(0xFF6B7280),
            Icons.Outlined.CalendarMonth,
            "Time Slot Available",
            "Time slot is available for booking"
        )
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .background(backgroundColor, RoundedCornerShape(12.dp))
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = "Status icon",
            tint = iconColor,
            modifier = Modifier.size(28.dp)
        )

        Column {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = iconColor
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    fontSize = 14.sp,
                    color = Color(0xFF6B7280)
                )
            }
        }
    }
}

// Helper data class para retornar múltiples valores
private data class Tuple5<A, B, C, D, E>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D,
    val fifth: E?
)

// ============ PREVIEWS ============

@Preview(showBackground = true, backgroundColor = 0xFFFCFCFC)
@Composable
fun OwnerVisitDetailView_Completed_Preview() {
    AppTheme {
        OwnerVisitDetailView(
            propertyImageUrl = "https://images.unsplash.com/photo-1502672260266-1c1ef2d93688?w=400",
            visitStatus = VisitStatus.COMPLETED,
            visitDate = "October 26, 2025",
            visitTime = "4:00 PM",
            visitorName = "Vova Parkhomchuk",
            visitorNationality = "Mexico",
            visitorPhotoUrl = "https://i.pravatar.cc/150?img=12",
            visitorFeedback = "Description text about something on this page that can be long or short, it can be a single line or a long and explaining information...",
            visitorRating = 4,
            ownerComment = "e.g Student seemed very interested, asked about the lease terms..."
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFCFCFC)
@Composable
fun OwnerVisitDetailView_Missed_Preview() {
    AppTheme {
        OwnerVisitDetailView(
            propertyImageUrl = "https://images.unsplash.com/photo-1502672260266-1c1ef2d93688?w=400",
            visitStatus = VisitStatus.MISSED,
            visitDate = "October 26, 2025",
            visitTime = "4:00 PM",
            visitorName = "Vova Parkhomchuk",
            visitorNationality = "USA",
            visitorPhotoUrl = "https://i.pravatar.cc/150?img=12"
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFCFCFC)
@Composable
fun OwnerVisitDetailView_Confirmed_Preview() {
    AppTheme {
        OwnerVisitDetailView(
            propertyImageUrl = "https://images.unsplash.com/photo-1502672260266-1c1ef2d93688?w=400",
            visitStatus = VisitStatus.CONFIRMED,
            visitDate = "October 26, 2025",
            visitTime = "4:00 PM",
            visitorName = "Vova Parkhomchuk",
            visitorNationality = "Colombia",
            visitorPhotoUrl = "https://i.pravatar.cc/150?img=12"
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFCFCFC)
@Composable
fun OwnerVisitDetailView_Available_Preview() {
    AppTheme {
        OwnerVisitDetailView(
            propertyImageUrl = "https://images.unsplash.com/photo-1502672260266-1c1ef2d93688?w=400",
            visitStatus = VisitStatus.AVAILABLE,
            visitDate = "October 26, 2025",
            visitTime = "4:00 PM",
            visitorName = "",
            visitorNationality = "",
            visitorPhotoUrl = null
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFCFCFC)
@Composable
fun OwnerVisitDetailView_Scheduled_Preview() {
    AppTheme {
        OwnerVisitDetailView(
            propertyImageUrl = "https://images.unsplash.com/photo-1502672260266-1c1ef2d93688?w=400",
            visitStatus = VisitStatus.SCHEDULED,
            visitDate = "October 26, 2025",
            visitTime = "4:00 PM",
            visitorName = "Vova Parkhomchuk",
            visitorNationality = "Ukraine",
            visitorPhotoUrl = "https://i.pravatar.cc/150?img=12"
        )
    }
}