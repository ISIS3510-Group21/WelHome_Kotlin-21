package com.team21.myapplication.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.team21.myapplication.data.model.Booking
import kotlinx.coroutines.tasks.await
import com.google.firebase.Timestamp
import java.time.*
import java.time.format.DateTimeFormatter
import java.util.*
import com.google.firebase.firestore.FieldValue
import android.util.Log

class BookingRepository {
    private val db = FirebaseFirestore.getInstance()
    private val col = db.collection("Booking")
    private val auth = FirebaseAuth.getInstance()

    suspend fun getUserBookings(): List<Booking> {
        val userId = auth.currentUser?.uid ?: return emptyList()

        val snapshot = col.whereEqualTo("user", userId).get().await()
        return snapshot.documents.mapNotNull { d -> d.toObject(Booking::class.java) }
    }


    /**
     * Crea un Booking y elimina el BookingSlot correspondiente en una transacción.
     *
     * @param housingId id de la vivienda
     * @param housingTitle título de la vivienda
     * @param thumbnail url thumbnail de la vivienda
     * @param selectedDateMillis millis que vienen del DatePicker (00:00 UTC del día seleccionado)
     * @param selectedHourString hora seleccionada formateada "H:mm" (ej. "7:00")
     * @return id del documento Booking creado
     */
    suspend fun createBookingAndConsumeSlot(
        housingId: String,
        housingTitle: String,
        thumbnail: String,
        selectedDateMillis: Long,
        selectedHourString: String
    ): String {
        val userId = auth.currentUser?.uid
            ?: throw IllegalStateException("User not logged in")

        // Zona oficial de la app
        val zone = ZoneId.of("America/Bogota")

        // 1) Reconstruir el LocalDate del DatePicker (ojo: viene en 00:00 UTC)
        val selectedLocalDate = Instant.ofEpochMilli(selectedDateMillis)
            .atZone(ZoneOffset.UTC).toLocalDate()

        // 2) Parsear la hora "H:mm"
        val parsedTime = LocalTime.parse(
            selectedHourString,
            DateTimeFormatter.ofPattern("H:mm")
        )

        // 3) Combinar fecha (día en Bogotá) + hora en Bogotá → Instant real
        val selectedInstant = ZonedDateTime.of(selectedLocalDate, parsedTime, zone).toInstant()
        val selectedTs = Timestamp(Date.from(selectedInstant))

        // 4) Localizar el Schedule → BookingDate → BookingSlot a eliminar
        val db = FirebaseFirestore.getInstance()
        val scheduleCol = db.collection("BookingSchedule")

        // a) buscar doc de schedule por housing (soporta housing guardado como string o ref o path)
        suspend fun findScheduleDocId(housingId: String): String? {
            scheduleCol.whereEqualTo("housing", housingId).limit(1).get().await().let { q ->
                if (!q.isEmpty) return q.documents.first().id
            }
            scheduleCol.whereEqualTo("housing", db.document("HousingPost/$housingId")).limit(1).get().await().let { q ->
                if (!q.isEmpty) return q.documents.first().id
            }
            scheduleCol.whereEqualTo("housing", "HousingPost/$housingId").limit(1).get().await().let { q ->
                if (!q.isEmpty) return q.documents.first().id
            }
            return null
        }

        val scheduleId = findScheduleDocId(housingId)
            ?: throw IllegalStateException("Schedule not found for housingId=$housingId")

        // b) encontrar el BookingDate cuyo 'date' (Timestamp) sea el mismo día (en Bogotá)
        val bookingDateSnap = scheduleCol.document(scheduleId)
            .collection("BookingDate")
            .get().await()
            .documents
            .firstOrNull { dateDoc ->
                val dateTs = dateDoc.get("date") as? Timestamp ?: return@firstOrNull false
                val localDate = dateTs.toDate().toInstant().atZone(zone).toLocalDate()
                localDate == selectedLocalDate
            } ?: throw IllegalStateException("BookingDate not found for $selectedLocalDate")

        // c) dentro de ese día, buscar el BookingSlot cuya 'time' (Timestamp) sea la hora elegida (en Bogotá)
        val slotDoc = bookingDateSnap.reference.collection("BookingSlot")
            .get().await()
            .documents
            .firstOrNull { slot ->
                val timeTs = slot.get("time") as? Timestamp ?: return@firstOrNull false
                val lt = timeTs.toDate().toInstant().atZone(zone).toLocalTime()
                // Log más explícito para depurar

                Log.e("BookingRepository", "Found slot raw: $lt, parsedTime: $parsedTime")

                // Compara solo hora y minuto (ignora segundos y milisegundos)
                lt.hour == parsedTime.hour && lt.minute == parsedTime.minute
            } ?: throw IllegalStateException("BookingSlot not found for $selectedLocalDate $parsedTime")

        // 5) Preparar la escritura del Booking y la eliminación del Slot
        val bookingDoc = col.document() // generamos id
        val slotString = selectedInstant.atZone(zone).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
        val payload = mapOf(
            "id" to bookingDoc.id,
            "confirmedVisit" to true,
            "date" to selectedTs,                 // Timestamp del instante exacto (día+hora en Bogotá)
            "housing" to housingId,
            "housingTitle" to housingTitle,
            "thumbnail" to thumbnail,
            "slot" to slotString,                 // "2025-12-24 13:00"
            "state" to "Scheduled",
            "user" to userId,
            "userComment" to "",
            "ownerComment" to "",
            "rating" to 0.0
        )

        // 6) Transacción: crear booking + eliminar slot + DECREMENTAR availableSlots
        val bookingDateRef = bookingDateSnap.reference
        db.runTransaction { tr ->
            // 6.1) Verifica cupos y decrementa
            val dateSnap = tr.get(bookingDateRef)
            val currentAvail = (dateSnap.getLong("availableSlots") ?: 0L)
            if (currentAvail <= 0L) {
                throw IllegalStateException("No available slots remaining for this date")
            }
            // Evita condiciones de carrera: primero validas, luego decrementas
            tr.update(bookingDateRef, "availableSlots", FieldValue.increment(-1))

            // 6.2) Crea el booking
            tr.set(bookingDoc, payload)

            // 6.3) Elimina el slot reservado
            tr.delete(slotDoc.reference)

            null
        }.await()

        return bookingDoc.id
    }

    suspend fun rateVisit(visitId: String, rating: Float, comment: String) {
        val visitRef = col.document(visitId)
        val updates = mapOf(
            "rating" to rating,
            "userComment" to comment
        )
        visitRef.update(updates).await()
    }

    /**
     * Obtiene todos los bookings asociados a una lista de housing IDs
     * Útil para que el owner vea todas las visitas a sus propiedades
     */
    suspend fun getBookingsByHousingIds(housingIds: List<String>): List<Booking> {
        if (housingIds.isEmpty()) return emptyList()

        return try {
            // Firestore tiene límite de 10 elementos en "in" queries
            // Si hay más de 10 propiedades, dividimos en chunks
            val bookings = mutableListOf<Booking>()

            housingIds.chunked(10).forEach { chunk ->
                val snapshot = col
                    .whereIn("housing", chunk)
                    .get()
                    .await()

                bookings.addAll(
                    snapshot.documents.mapNotNull { doc ->
                        doc.toObject(Booking::class.java)
                    }
                )
            }

            bookings
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Obtiene un booking específico por su ID
     */
    suspend fun getBookingById(bookingId: String): Booking? {
        return try {
            val doc = col.document(bookingId).get().await()
            doc.toObject(Booking::class.java)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun updateOwnerComment(bookingId: String, ownerComment: String) {
        try {
            col.document(bookingId)
                .update("ownerComment", ownerComment)
                .await()
        } catch (e: Exception) {
            // Puedes loguear si quieres
            Log.e("BookingRepository", "Error updating ownerComment", e)
            throw e
        }
    }

}