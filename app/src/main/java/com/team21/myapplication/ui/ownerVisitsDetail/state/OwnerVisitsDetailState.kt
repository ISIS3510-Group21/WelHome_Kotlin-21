package com.team21.myapplication.ui.ownerVisitsDetail.state

import com.team21.myapplication.ui.ownerVisitsDetail.VisitStatus

data class OwnerVisitDetailState(
    val isLoading: Boolean = true,
    val error: String? = null,

    // Datos de la visita
    val propertyImageUrl: String = "",
    val visitStatus: VisitStatus = VisitStatus.AVAILABLE,
    val visitDate: String = "",
    val visitTime: String = "",
    val visitorName: String = "",
    val visitorNationality: String = "",
    val visitorPhotoUrl: String? = null,
    val visitorFeedback: String? = null,
    val visitorRating: Int? = null,
    val ownerComment: String = "",
    val isSavingComment: Boolean = false,
    val commentSaveMessage: String? = null,   // texto para snackbar (éxito / error)
    val commentSaveError: Boolean = false,     // para saber si el mensaje es de error
    val isEditingOwnerComment: Boolean = false,
    val ownerCommentDraft: String = "",

    // Metadata
    val bookingId: String = "",
    val isAvailable: Boolean = false
)