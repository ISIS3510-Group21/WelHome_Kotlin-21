package com.team21.myapplication.data.model

import com.google.firebase.Timestamp

/**
 * Modelo enriquecido de Booking para la vista del Owner
 * Incluye información del visitante
 */
data class OwnerScheduledVisit(
    val bookingId: String,
    val date: Timestamp,
    val timeRange: String,      // "4:00 PM - 5:00 PM"
    val propertyName: String,   // housingTitle
    val visitorName: String,    // nombre del estudiante
    val propertyImageUrl: String, // thumbnail
    val status: String,         // state
    val timestamp: Long,         // para ordenar y filtrar
    val isAvailable: Boolean = false,
    val visitorPhotoUrl: String? = null,
    val isPendingDraft: Boolean = false // indica que es un slot local pendiente de subir
)