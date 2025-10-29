package com.team21.myapplication.data.repository

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.time.*
import java.time.format.DateTimeFormatter

class BookingScheduleRepository {

    private val db = FirebaseFirestore.getInstance()
    private val scheduleCol = db.collection("BookingSchedule")

    /** Busca el schedule de un housingId. Tolera housing como String, DocumentReference o path String. */
    private suspend fun findScheduleDocId(housingId: String): String? {
        // 1) housing guardado como String simple
        scheduleCol.whereEqualTo("housing", housingId).limit(1).get().await().let { q ->
            if (!q.isEmpty) return q.documents.first().id
        }
        // 2) housing guardado como DocumentReference
        scheduleCol.whereEqualTo("housing", db.document("HousingPost/$housingId")).limit(1).get().await().let { q ->
            if (!q.isEmpty) return q.documents.first().id
        }
        // 3) housing guardado como path String
        scheduleCol.whereEqualTo("housing", "HousingPost/$housingId").limit(1).get().await().let { q ->
            if (!q.isEmpty) return q.documents.first().id
        }
        return null
    }

    /**
     * Devuelve LocalDate -> List<LocalTime> con slots que tengan cupo (>0).
     */
    suspend fun getAvailabilityByDay(housingId: String): Map<LocalDate, List<LocalTime>> {
        val scheduleId = findScheduleDocId(housingId) ?: return emptyMap()
        val datesSnap = scheduleCol.document(scheduleId)
            .collection("BookingDate")
            .get().await()

        val zone = ZoneId.systemDefault()
        val result = mutableMapOf<LocalDate, MutableList<LocalTime>>()

        // UTC para agrupar por día (coherente con DatePicker)
        val dayZone = ZoneOffset.UTC
        // Zona local para mostrar la hora al usuario
        val timeZone = ZoneId.systemDefault()

        for (dateDoc in datesSnap.documents) {
            val dateTs = dateDoc.get("date") as? Timestamp ?: continue
            val localDate = dateTs.toDate().toInstant().atZone(dayZone).toLocalDate()

            val slotsSnap = dateDoc.reference.collection("BookingSlot").get().await()
            for (slotDoc in slotsSnap.documents) {
                val availableUsersAny = slotDoc.get("availableUsers") ?: slotDoc.get("avilableUsers")
                val availableUsers = (availableUsersAny as? Number)?.toInt() ?: 0
                if (availableUsers <= 0) continue

                val timeTs = slotDoc.get("time") as? Timestamp ?: continue
                val localTime = timeTs.toDate().toInstant().atZone(timeZone).toLocalTime()
                result.getOrPut(localDate) { mutableListOf() }.add(localTime)
            }
        }
        return result.mapValues { (_, times) -> times.sorted() }
    }

    /** Formatea hora a "H:mm" (7:00, 8:00, …). */
    fun formatHour(t: LocalTime): String = t.format(DateTimeFormatter.ofPattern("H:mm"))
}
