package com.team21.myapplication.ui.ownerVisitsDetail

import android.util.Log
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.team21.myapplication.ui.components.banners.BannerPosition
import com.team21.myapplication.ui.components.banners.ConnectivityBanner
import com.team21.myapplication.ui.components.buttons.BlueButton
import com.team21.myapplication.ui.components.icons.AppIcons
import com.team21.myapplication.ui.components.inputs.PlaceholderTextField
import com.team21.myapplication.ui.components.text.BlackText
import com.team21.myapplication.ui.components.text.BlueText
import com.team21.myapplication.ui.components.text.GrayText
import com.team21.myapplication.ui.theme.AppTheme
import com.team21.myapplication.ui.theme.*

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
    ownerCommentDraft: String = "",
    isEditingOwnerComment: Boolean = false,
    onCommentChange: (String) -> Unit = {},
    onSaveComment: () -> Unit = {},
    onStartEditOwnerComment: () -> Unit = {},
    onCancelEditOwnerComment: () -> Unit = {},
    onMessageClick: () -> Unit = {},
    onBackClick: () -> Unit = {},
    snackbarHostState: SnackbarHostState,
    isOnline: Boolean = true,
    modifier: Modifier = Modifier
) {

    var showSnackbar by remember { mutableStateOf(false) }

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
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState
            ) { snackbarData ->
                Snackbar(
                    snackbarData = snackbarData,
                    contentColor = WhiteBackground
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = AppIcons.GoBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                BlackText(
                    text = "Visit Details",
                    size = 22.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

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

            // ¿Tenemos ya info real del visitante?
            val hasVisitorInfo = !visitorName.isNullOrBlank() || !visitorPhotoUrl.isNullOrBlank()
            Log.d("OwnerVisitDetailView", "visitorPhotoUrl = $visitorPhotoUrl")

            // Visitor Information Section
            if (!hasVisitorInfo && visitStatus == VisitStatus.AVAILABLE) {
                //  Solo mostramos el texto “dummy” cuando:
                //  - la visita está AVAILABLE
                //  - Y aún NO tenemos info real del visitante
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
                        text = "Once this appointment has been confirmed with someone, you will be able to see the visitor's information here.",
                        size = 14.sp,
                        fontWeight = FontWeight.Normal
                    )
                }
            } else {
                // Si ya tenemos visitorName o visitorPhotoUrl,
                // SIEMPRE mostramos la tarjeta con la foto
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
                            val context = LocalContext.current
                            // Avatar del visitante (foto real o placeholder)
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.secondaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                // 1. Dibujamos SIEMPRE el ícono placeholder de fondo
                                Icon(
                                    imageVector = AppIcons.Profile,
                                    contentDescription = "Visitor avatar placeholder",
                                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.size(28.dp)
                                )

                                // 2. Encima, si hay URL, intentamos cargar la foto real
                                if (!visitorPhotoUrl.isNullOrBlank()) {
                                    AsyncImage(
                                        model = ImageRequest.Builder(context)
                                            .data(visitorPhotoUrl)
                                            .crossfade(true)
                                            .build(),
                                        contentDescription = "Visitor photo",
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(CircleShape),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            }

                            Column {
                                BlackText(
                                    text = if (visitorName.isNotBlank()) visitorName else "Visitor",
                                    size = 16.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                GrayText(
                                    text = visitorNationality.ifBlank { "Roommate" },
                                    size = 14.sp,
                                    fontWeight = FontWeight.Normal
                                )
                            }
                        }

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
                    size = 20.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(12.dp))

                when (visitStatus) {
                    VisitStatus.COMPLETED -> {
                        // Mostrar rating y feedback del visitante
                        if (visitorRating != null) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Estrellas
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    repeat(5) { index ->
                                        Icon(
                                            imageVector = if (index < visitorRating)
                                                AppIcons.StarFilled
                                            else
                                                AppIcons.Star,
                                            contentDescription = "Star ${index + 1}",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(30.dp)
                                        )
                                    }
                                }

                                // Número
                                BlueText(
                                    text = "${visitorRating}/5",
                                    size = 16.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                        } else {
                            // Caso: visita completada pero sin rating
                            GrayText(
                                text = "This visit has not been rated yet",
                                size = 14.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        if (!visitorFeedback.isNullOrBlank()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        Color(BlueSecondary.value),
                                        RoundedCornerShape(12.dp)
                                    )
                                    .padding(12.dp)
                            ) {
                                BlackText(
                                    text = visitorFeedback,
                                    size = 16.sp
                                )
                            }

                            if (visitorFeedback.length > 100) {
                                Spacer(modifier = Modifier.height(4.dp))
                                BlueText(
                                    text = "More",
                                    size = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                    underline = true
                                )
                            }
                        } else {
                            // Visita completada pero sin comentarios
                            GrayText(
                                text = "There are no comments for this visit yet",
                                size = 16.sp
                            )
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
                                    size = 16.sp
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
                    size = 20.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(8.dp))

                GrayText(
                    text = if (visitStatus == VisitStatus.AVAILABLE) {
                        "You can add comments here after the visit is completed"
                    } else {
                        "Your private comments about this visit will only be visible to you"
                    },
                    size = 16.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                val canEdit = (visitStatus == VisitStatus.COMPLETED || visitStatus == VisitStatus.MISSED)

                if (!canEdit) {
                    // No se puede editar aún: dejamos el TextField morado deshabilitado
                    PlaceholderTextField(
                        placeholderText = "e.g Student seemed very interested, asked about the lease terms...",
                        value = ownerComment,
                        onValueChange = { /* no-op, está deshabilitado */ },
                        height = 120.dp,
                        maxChars = 500,
                        maxCharsMessage = "Maximum 500 characters",
                        enabled = false
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    BlueButton(
                        text = "Save Comment",
                        onClick = {
                            // Mostrar snackbar explicando por qué no se puede
                            showSnackbar = true
                        },
                        enabled = false
                    )
                } else {
                    // La visita está COMPLETED o MISSED: se permite comentar / editar

                    if (ownerComment.isBlank() && !isEditingOwnerComment) {
                        // Caso 1: no hay comentario aún → TextField morado y "Save Comment"
                        PlaceholderTextField(
                            placeholderText = "e.g Student seemed very interested, asked about the lease terms...",
                            value = ownerCommentDraft,
                            onValueChange = { onCommentChange(it) },
                            height = 120.dp,
                            maxChars = 500,
                            maxCharsMessage = "Maximum 500 characters",
                            enabled = true
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        BlueButton(
                            text = "Save Comment",
                            onClick = { onSaveComment() },
                            enabled = true
                        )
                    } else if (!isEditingOwnerComment) {
                        // Caso 2: ya hay comentario y NO estamos editando → BlackText + botón "Edit"
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Color(BlueSecondary.value),
                                    RoundedCornerShape(12.dp)
                                )
                                .padding(12.dp)
                        ) {
                            BlackText(
                                text = ownerComment,
                                size = 16.sp,
                                fontWeight = FontWeight.Normal
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        TextButton(onClick = onStartEditOwnerComment) {
                            BlueText(
                                text = "Edit comment",
                                size = 16.sp,
                                fontWeight = FontWeight.Medium,
                            )
                        }
                    } else {
                        // Caso 3: ya hay comentario y ESTAMOS editando → TextField + Update + Cancel
                        PlaceholderTextField(
                            placeholderText = "e.g Student seemed very interested, asked about the lease terms...",
                            value = ownerCommentDraft,
                            onValueChange = { onCommentChange(it) },
                            height = 120.dp,
                            maxChars = 500,
                            maxCharsMessage = "Maximum 500 characters",
                            enabled = true
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Botón "Update Comment"
                        BlueButton(
                            text = "Update Comment",
                            onClick = { onSaveComment() },
                            enabled = true
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Acción "Cancel"
                        TextButton(onClick = onCancelEditOwnerComment) {
                            BlackText(
                                text = "Cancel",
                                size = 16.sp,
                                fontWeight = FontWeight.Medium,
                            )
                        }
                    }
                }
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
        val snackbarHostState = remember { SnackbarHostState() }
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
            ownerComment = "e.g Student seemed very interested, asked about the lease terms...",
            isEditingOwnerComment = false,
            snackbarHostState = snackbarHostState
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFCFCFC)
@Composable
fun OwnerVisitDetailView_Missed_Preview() {
    AppTheme {
        val snackbarHostState = remember { SnackbarHostState() }
        OwnerVisitDetailView(
            propertyImageUrl = "https://images.unsplash.com/photo-1502672260266-1c1ef2d93688?w=400",
            visitStatus = VisitStatus.MISSED,
            visitDate = "October 26, 2025",
            visitTime = "4:00 PM",
            visitorName = "Vova Parkhomchuk",
            visitorNationality = "USA",
            visitorPhotoUrl = "https://i.pravatar.cc/150?img=12",
            snackbarHostState = snackbarHostState
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFCFCFC)
@Composable
fun OwnerVisitDetailView_Confirmed_Preview() {
    val snackbarHostState = remember { SnackbarHostState() }
    AppTheme {
        OwnerVisitDetailView(
            propertyImageUrl = "https://images.unsplash.com/photo-1502672260266-1c1ef2d93688?w=400",
            visitStatus = VisitStatus.CONFIRMED,
            visitDate = "October 26, 2025",
            visitTime = "4:00 PM",
            visitorName = "Vova Parkhomchuk",
            visitorNationality = "Colombia",
            visitorPhotoUrl = "https://i.pravatar.cc/150?img=12",
            snackbarHostState = snackbarHostState
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFCFCFC)
@Composable
fun OwnerVisitDetailView_Available_Preview() {
    AppTheme {
        val snackbarHostState = remember { SnackbarHostState() }
        OwnerVisitDetailView(
            propertyImageUrl = "https://images.unsplash.com/photo-1502672260266-1c1ef2d93688?w=400",
            visitStatus = VisitStatus.AVAILABLE,
            visitDate = "October 26, 2025",
            visitTime = "4:00 PM",
            visitorName = "",
            visitorNationality = "",
            visitorPhotoUrl = null,
            snackbarHostState = snackbarHostState
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFCFCFC)
@Composable
fun OwnerVisitDetailView_Scheduled_Preview() {
    AppTheme {
        val snackbarHostState = remember { SnackbarHostState() }
        OwnerVisitDetailView(
            propertyImageUrl = "https://images.unsplash.com/photo-1502672260266-1c1ef2d93688?w=400",
            visitStatus = VisitStatus.SCHEDULED,
            visitDate = "October 26, 2025",
            visitTime = "4:00 PM",
            visitorName = "Vova Parkhomchuk",
            visitorNationality = "Ukraine",
            visitorPhotoUrl = "https://i.pravatar.cc/150?img=12",
            snackbarHostState = snackbarHostState
        )
    }
}