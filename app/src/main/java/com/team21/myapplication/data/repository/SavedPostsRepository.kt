package com.team21.myapplication.data.repository

import android.util.Log
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.team21.myapplication.ui.filterView.state.PreviewCardUi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

/**
 * SavedPostsRepository
 * - Lee StudentUser/{id}/SavedHousing (subcollection) y resuelve HousingPost/{id}
 * - Mapea "a mano" solo los campos que necesita el preview, evitando `toObject()`
 *   para no chocar con tipos heterogéneos (e.g., reviews como DocumentReference).
 *
 * Estrategias:
 * [EVENTUAL CONNECTIVITY] Online source
 * [MULTITHREADING] Resolución en paralelo con async(Dispatchers.IO)
 */
class SavedPostsRepository(
    private val studentRepo: StudentUserRepository = StudentUserRepository(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val users = db.collection("StudentUser")
    private val housing = db.collection("HousingPost")

    private val TAG = "SavedRepo"

    // Mutaciones delegadas al StudentUserRepository (subcollection)
    suspend fun addSaved(userId: String, housingId: String) =
        studentRepo.addSavedHousing(userId, housingId)

    suspend fun removeSaved(userId: String, housingId: String) =
        studentRepo.removeSavedHousing(userId, housingId)

    /**
     * Obtiene los previews desde StudentUser/{id}/SavedHousing (subcollection),
     * y resuelve cada HousingPost con lectura segura de campos.
     */
    suspend fun getSavedPreviews(userId: String): List<PreviewCardUi> = coroutineScope {
        Log.d(TAG, "getSavedPreviews(userId=$userId) ▶")

        // 1) Leer subcolección de guardados
        val savedDocs = withContext(Dispatchers.IO) {
            users.document(userId).collection("SavedHousing").get().await().documents
        }.also { docs ->
            Log.d(TAG, "SavedHousing docs=${docs.size}")
            docs.forEachIndexed { i, d ->
                val refAny = d.get("ref")
                val refType = refAny?.let { it::class.java.simpleName } ?: "null"
                Log.d(TAG, "  [$i] docId=${d.id} data=${d.data} (refType=$refType)")
            }
        }

        if (savedDocs.isEmpty()) return@coroutineScope emptyList()

        // 2) Extraer IDs (preferimos el campo "id"; si no, del "ref")
        val ids: List<String> = savedDocs.mapNotNull { d ->
            d.getString("id") ?: (d.get("ref") as? DocumentReference)?.id
        }.distinct()

        Log.d(TAG, "extracted ids=$ids")
        if (ids.isEmpty()) return@coroutineScope emptyList()

        // 3) Resolver cada HousingPost en paralelo con lectura segura
        val jobs = ids.map { id ->
            async(Dispatchers.IO) {
                try {
                    val snap = housing.document(id).get().await()
                    if (!snap.exists()) {
                        Log.w(TAG, "HousingPost '$id' not found")
                        return@async null
                    }

                    // ---- Lectura segura de campos para el preview ----
                    val title = snap.getString("title") ?: ""
                    val rating = (snap.getDouble("rating") ?: 0.0)

                    // 'reviews' puede ser DocumentReference, número o string según tu esquema.
                    // Para el preview usamos "reviewsCount": primero intenta número, luego string->int.
                    val reviewsCount: Int = when (val rv = snap.get("reviews")) {
                        is Number -> rv.toInt()
                        is String -> rv.toFloatOrNull()?.toInt() ?: 0
                        is DocumentReference -> 0 // es una ref a subcolección, no contamos aquí
                        else -> 0
                    }

                    val price = snap.getDouble("price") ?: 0.0
                    val pricePerMonthLabel = priceLabel(price)

                    // Imagen: intenta 'thumbnail' y si no, pictures[0].PhotoPath
                    val thumb: String? = snap.getString("thumbnail")
                    val pictures = snap.get("pictures") as? List<*>
                    val firstPhotoPath: String? =
                        (pictures?.firstOrNull() as? Map<*, *>)?.get("PhotoPath") as? String

                    // >>> Asegura String no nulo (elige el orden de preferencia)
                    val photoUrl: String = when {
                        !thumb.isNullOrBlank() -> thumb
                        !firstPhotoPath.isNullOrBlank() -> firstPhotoPath
                        else -> "" // o un placeholder absoluto si tu UI lo requiere
                    }

                    Log.d(
                        TAG,
                        "Resolved preview id=$id title='$title' rating=$rating reviewsCount=$reviewsCount photoUrl=$photoUrl"
                    )

                    PreviewCardUi(
                        housingId = id,
                        title = title,
                        rating = rating,
                        reviewsCount = reviewsCount,
                        pricePerMonthLabel = pricePerMonthLabel,
                        photoUrl = photoUrl
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "Resolving '$id' failed: ${e.message}", e)
                    null
                }
            }
        }

        val previews = jobs.mapNotNull { it.await() }
        Log.d(TAG, "built previews size=${previews.size}, ids=${previews.map { it.housingId }}")
        previews
    }

    private fun priceLabel(price: Double): String {
        if (price <= 0.0) return ""
        val intPart = price.toLong()
        return "$$intPart /month"
    }
}
