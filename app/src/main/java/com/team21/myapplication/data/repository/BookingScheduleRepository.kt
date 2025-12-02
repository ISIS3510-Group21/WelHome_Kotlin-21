package com.team21.myapplication.data.repository

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.time.*
import java.time.format.DateTimeFormatter
import android.util.ArrayMap
import java.time.Instant
import com.google.firebase.firestore.FieldValue

class BookingScheduleRepository {

    private val db = FirebaseFirestore.getInstance()
    private val scheduleCol = db.collection("BookingSchedule")
    companion object {
        // cache en memoria por housingId — compartida entre instancias
        private val availabilityCache = ArrayMap<String, Pair<Map<LocalDate, List<LocalTime>>, Instant>>()

        // Cache separado para el flujo del OWNER (postear BookingSchedule + offline)
        private val ownerAvailabilityCache =
            ArrayMap<String, Pair<Map<LocalDate, List<LocalTime>>, Instant>>()
    }

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

        val result = mutableMapOf<LocalDate, MutableList<LocalTime>>()

        val zone = ZoneId.of("America/Bogota")

        for (dateDoc in datesSnap.documents) {
            val dateTs = dateDoc.get("date") as? Timestamp ?: continue
            val localDate = dateTs.toDate().toInstant().atZone(zone).toLocalDate()

            val slotsSnap = dateDoc.reference.collection("BookingSlot").get().await()
            for (slotDoc in slotsSnap.documents) {
                val availableUsersAny = slotDoc.get("availableUsers") ?: slotDoc.get("avilableUsers")
                val availableUsers = (availableUsersAny as? Number)?.toInt() ?: 0
                if (availableUsers <= 0) continue

                val timeTs = slotDoc.get("time") as? Timestamp ?: continue
                val localTime = timeTs.toDate().toInstant().atZone(zone).toLocalTime().withSecond(0).withNano(0)
                result.getOrPut(localDate) { mutableListOf() }.add(localTime)
            }
        }
        return result.mapValues { (_, times) -> times.sorted() }
    }

    /** Formatea hora a "H:mm" (7:00, 8:00, …). */
    fun formatHour(t: LocalTime): String = t.format(DateTimeFormatter.ofPattern("H:mm"))

    /**
     * Devuelve disponibilidad usando cache si se esta offline.
     *
     * @param housingId id de la vivienda
     * @param isOnline  si hay internet ahora
     * @return Pair(map, updatedAt) donde updatedAt es el Instant de última actualización OK
     */
    suspend fun getAvailabilitySmart(housingId: String, isOnline: Boolean): Pair<Map<LocalDate, List<LocalTime>>, Instant?> {
        val zone = ZoneId.of("America/Bogota")

        if (!isOnline) {
            // OFFLINE -> devuelve cache si hay
            val cached = availabilityCache[housingId]
            return if (cached != null) cached else (emptyMap<LocalDate, List<LocalTime>>() to null)
        }

        // Online -> perdir a Firestore y actualizar cache
        val fresh = getAvailabilityByDay(housingId)
        val now = Instant.now()
        // guardar en cache
        availabilityCache[housingId] = fresh to now
        return fresh to now
    }

    /** Para leer la fecha/hora de cache sin pedir a red. */
    fun getCachedStamp(housingId: String): Instant? = availabilityCache[housingId]?.second

    /**
     * Obtiene todos los slots disponibles para una lista de housing IDs del owner.
     * Retorna una lista de tuplas (housingId, LocalDate, LocalTime, housingTitle, thumbnail)
     */
    suspend fun getAvailableSlotsByHousingIds(
        housingIds: List<String>,
        housingDetailsMap: Map<String, Pair<String, String>> // housingId -> (title, thumbnail)
    ): List<AvailableSlotInfo> {
        if (housingIds.isEmpty()) return emptyList()

        val zone = ZoneId.of("America/Bogota")
        val results = mutableListOf<AvailableSlotInfo>()

        try {
            for (housingId in housingIds) {
                val scheduleId = findScheduleDocId(housingId) ?: continue

                val datesSnap = scheduleCol.document(scheduleId)
                    .collection("BookingDate")
                    .get().await()

                for (dateDoc in datesSnap.documents) {
                    val dateTs = dateDoc.get("date") as? Timestamp ?: continue
                    val localDate = dateTs.toDate().toInstant().atZone(zone).toLocalDate()

                    val slotsSnap = dateDoc.reference.collection("BookingSlot").get().await()
                    for (slotDoc in slotsSnap.documents) {
                        val availableUsersAny = slotDoc.get("availableUsers") ?: slotDoc.get("avilableUsers")
                        val availableUsers = (availableUsersAny as? Number)?.toInt() ?: 0
                        if (availableUsers <= 0) continue

                        val timeTs = slotDoc.get("time") as? Timestamp ?: continue
                        val localTime = timeTs.toDate().toInstant().atZone(zone).toLocalTime()

                        val (title, thumbnail) = housingDetailsMap[housingId] ?: ("" to "")

                        results.add(
                            AvailableSlotInfo(
                                housingId = housingId,
                                date = localDate,
                                time = localTime,
                                housingTitle = title,
                                thumbnail = thumbnail,
                                timestamp = timeTs
                            )
                        )
                    }
                }
            }
        } catch (e: Exception) {
            // Log error pero no fallar completamente
        }

        return results
    }

    // Data class auxiliar
    data class AvailableSlotInfo(
        val housingId: String,
        val date: LocalDate,
        val time: LocalTime,
        val housingTitle: String,
        val thumbnail: String,
        val timestamp: Timestamp
    )

    /**
     * Agrega nuevos slots disponibles para un housingId y una fecha dada.
     *
     * - Crea el documento en BookingSchedule si no existe.
     * - Crea el BookingDate correspondiente si no existe.
     * - Crea documentos en BookingSlot para cada hora.
     * - availableUsers = 1, duration = 1, description = "".
     * - Timestamps en zona America/Bogota (UTC-5) con segundos y nanos en 0.
     */
    suspend fun addAvailableSlots(
        housingId: String,
        date: LocalDate,
        times: List<LocalTime>
    ): Result<Unit> {
        if (times.isEmpty()) return Result.success(Unit)

        return try {
            val zone = ZoneId.of("America/Bogota")

            // 1) Obtener o crear documento padre en BookingSchedule
            val existingScheduleId = findScheduleDocId(housingId)
            val scheduleRef = if (existingScheduleId != null) {
                scheduleCol.document(existingScheduleId)
            } else {
                val newRef = scheduleCol.document()
                val nowTs = Timestamp.now()
                val payload = mapOf(
                    "id" to newRef.id,
                    "housing" to housingId,          // String simple
                    "availableDates" to 0,           // se incrementa BookingDate
                    "updatedAt" to nowTs
                )
                newRef.set(payload).await()
                newRef
            }

            // 2) Buscar o crear BookingDate para la fecha dada (ID determinístico por día)
            val datesCol = scheduleRef.collection("BookingDate")

            // Usamos el propio LocalDate como ID: "2025-12-02"
            val dateKey = date.toString()
            val dateDocRef = datesCol.document(dateKey)

            // Consultar si ya existe
            val dateSnap = dateDocRef.get().await()

            if (!dateSnap.exists()) {
                val dateStart = date.atStartOfDay(zone)
                val dateTs = Timestamp(dateStart.toEpochSecond(), 0)

                val datePayload = mapOf(
                    "id" to dateDocRef.id,
                    "date" to dateTs,
                    "availableSlots" to 0
                )
                dateDocRef.set(datePayload).await()

                // aumentar availableDates en el root solo la primera vez
                scheduleRef.update(
                    mapOf(
                        "availableDates" to FieldValue.increment(1),
                        "updatedAt" to Timestamp.now()
                    )
                ).await()
            }


            // 3) Crear BookingSlot para cada hora (evitando duplicados)
            val slotsCol = dateDocRef!!.collection("BookingSlot")
            val existingSlotsSnap = slotsCol.get().await()
            val existingTimes: Set<LocalTime> = existingSlotsSnap.documents.mapNotNull { doc ->
                val timeTs = doc.get("time") as? Timestamp ?: return@mapNotNull null
                timeTs.toDate().toInstant().atZone(zone).toLocalTime().withSecond(0).withNano(0)
            }.toSet()

            // set que iremos actualizando para evitar duplicados dentro de la misma llamada
            val seenTimes = existingTimes.toMutableSet()

            var newSlots = 0

            for (time in times) {
                // normalizamos la hora de entrada también
                val normalized = time.withSecond(0).withNano(0)

                // si ya existe en Firestore o dentro de la propia lista 'times', lo saltamos
                if (!seenTimes.add(normalized)) {
                    // add() devuelve false si ya estaba en el set
                    continue
                }

                val slotId = normalized.format(java.time.format.DateTimeFormatter.ofPattern("HHmm"))
                val slotRef = slotsCol.document(slotId)

                val zoned = date.atTime(time).atZone(zone).withSecond(0).withNano(0)
                val timeTs = Timestamp(zoned.toEpochSecond(), 0)

                val slotPayload = mapOf(
                    "id" to slotRef.id,
                    "time" to timeTs,
                    "duration" to 1,          // siempre 1
                    "availableUsers" to 1,    // siempre 1
                    "description" to ""       // vacío
                )

                slotRef.set(slotPayload).await()
                newSlots++
            }

            // 4) Actualizar contadores si realmente se crearon slots
            if (newSlots > 0) {
                dateDocRef.update(
                    mapOf(
                        "availableSlots" to FieldValue.increment(newSlots.toLong())
                    )
                ).await()
                scheduleRef.update("updatedAt", Timestamp.now()).await()
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun addTimesToCacheOffline(
        housingId: String,
        date: LocalDate,
        times: List<LocalTime>
    ) {
        if (times.isEmpty()) return

        val current = ownerAvailabilityCache[housingId]
        val oldMap = current?.first ?: emptyMap()

        val existingForDay = oldMap[date] ?: emptyList()
        val mergedForDay = (existingForDay + times).distinct().sorted()

        val newMap = oldMap.toMutableMap().apply {
            this[date] = mergedForDay
        }.toMap()

        val stamp = current?.second ?: Instant.now()
        ownerAvailabilityCache[housingId] = newMap to stamp
    }


    suspend fun getAvailabilitySlots(
        housingId: String,
        isOnline: Boolean
    ): Pair<Map<LocalDate, List<LocalTime>>, Instant?> {
        val cached = ownerAvailabilityCache[housingId]

        if (!isOnline) {
            // OFFLINE -> lo que haya en caché
            return cached ?: (emptyMap<LocalDate, List<LocalTime>>() to null)
        }

        // ONLINE -> siempre pedimos a Firestore
        val remote = getAvailabilityByDay(housingId)
        val now = Instant.now()

        if (cached == null) {
            ownerAvailabilityCache[housingId] = remote to now
            return remote to now
        }

        val merged = mutableMapOf<LocalDate, MutableList<LocalTime>>()

        // 1) remoto como base
        for ((date, times) in remote) {
            merged.getOrPut(date) { mutableListOf() }.addAll(times)
        }

        // 2) cache del owner encima (incluye slots offline todavía no subidos)
        for ((date, times) in cached.first) {
            merged.getOrPut(date) { mutableListOf() }.addAll(times)
        }

        val mergedFinal: Map<LocalDate, List<LocalTime>> =
            merged.mapValues { (_, list) -> list.distinct().sorted() }

        ownerAvailabilityCache[housingId] = mergedFinal to now
        return mergedFinal to now
    }

}
