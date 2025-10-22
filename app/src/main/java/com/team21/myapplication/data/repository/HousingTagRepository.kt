package com.team21.myapplication.data.repository

import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.team21.myapplication.data.model.HousingPreview
import com.team21.myapplication.data.model.HousingTag
import kotlinx.coroutines.tasks.await

class HousingTagRepository {
    private val db = FirebaseFirestore.getInstance()
    private val tagCol = db.collection("HousingTag")

    /**
     * Carga una lista de HousingTag a partir de referencias.
     * NOTA: Mapeo manual (no toObject) para tolerar esquemas viejos con DocumentReference.
     * housingPreview se deja vacío (los previews se cargan por subcolección cuando se necesiten).
     */
    suspend fun getHousingTags(listRefs: List<DocumentReference>): List<HousingTag> {
        val out = mutableListOf<HousingTag>()
        for (ref in listRefs) {
            val snap = ref.get().await()
            val data = snap.data ?: continue
            out += HousingTag(
                id = snap.id,
                name = (data["name"] as? String).orEmpty(),
                iconPath = (data["iconPath"] as? String).orEmpty(),
                housingPreview = emptyList() // se cargan bajo demanda
            )
        }
        return out
    }

    /**
     * Normaliza el valor de 'housing' (id o path o DocumentReference) a un String id.
     * - Si es DocumentReference -> usa ref.id
     * - Si es String con path "HousingPost/<id>" -> devuelve "<id>"
     * - Si es String con solo id -> lo deja igual
     */
    private fun coerceHousingId(raw: Any?): String? = when (raw) {
        is DocumentReference -> raw.id
        is String -> raw.substringAfterLast('/')
        else -> null
    }

    /** Obtiene todos los tags (sin previews). */
    suspend fun getAllTags(): List<HousingTag> {
        val snaps = tagCol.get().await()
        return snaps.documents.mapNotNull { d ->
            val data = d.data ?: return@mapNotNull null
            HousingTag(
                id = d.id,
                name = (data["name"] as? String).orEmpty(),
                iconPath = (data["iconPath"] as? String).orEmpty(),
                housingPreview = emptyList() // se cargan bajo demanda
            )
        }
    }

    /**
     * Carga un tag y sus previews desde la SUBCOLECCIÓN "HousingPreview".
     * (No usa el array embebido para evitar incompatibilidades de tipo).
     */
    suspend fun getTagWithPreviews(tagId: String): HousingTag? {
        val doc = tagCol.document(tagId).get().await()
        val data = doc.data ?: return null

        val previewsSnap = tagCol.document(tagId)
            .collection("HousingPreview")
            .get()
            .await()

        val previews = previewsSnap.documents.mapNotNull { p ->
            val pd = p.data ?: return@mapNotNull null
            HousingPreview(
                id = p.id,
                price = (pd["price"] as? Number)?.toDouble() ?: 0.0,
                rating = (pd["rating"] as? Number)?.toFloat() ?: 0f,
                reviewsCount = (pd["reviewsCount"] as? Number)?.toFloat() ?: 0f,
                title = (pd["title"] as? String).orEmpty(),
                photoPath = (pd["photoPath"] as? String).orEmpty(),
                housing = coerceHousingId(pd["housing"]) // ← ahora String? (id)
            )
        }

        return HousingTag(
            id = doc.id,
            name = (data["name"] as? String).orEmpty(),
            iconPath = (data["iconPath"] as? String).orEmpty(),
            housingPreview = previews
        )
    }

    /** Lee SOLO los previews de un tag (desde la subcolección). */
    suspend fun getPreviewsForTag(tagId: String): List<HousingPreview> {
        val snaps = tagCol.document(tagId).collection("HousingPreview").get().await()
        return snaps.documents.mapNotNull { d ->
            val data = d.data ?: return@mapNotNull null
            HousingPreview(
                id = d.id,
                price = (data["price"] as? Number)?.toDouble() ?: 0.0,
                rating = (data["rating"] as? Number)?.toFloat() ?: 0f,
                reviewsCount = (data["reviewsCount"] as? Number)?.toFloat() ?: 0f,
                title = (data["title"] as? String).orEmpty(),
                photoPath = (data["photoPath"] as? String).orEmpty(),
                housing = coerceHousingId(data["housing"]) // ← ahora String? (id)
            )
        }
    }

    suspend fun getTagNameById(tagId: String): String? {
        val doc = tagCol.document(tagId).get().await()
        return doc.getString("name")
    }

    /**
     * Busca previews por múltiples tags.
     *  - OR: unión de resultados.
     *  - AND: intersección por housingId (String).
     */
    suspend fun getPreviewsForTags(
        tagIds: List<String>,
        mode: FilterMode
    ): List<HousingPreview> {
        if (tagIds.isEmpty()) return emptyList()

        // Cargar previews por tag (secuencial simple; paraleliza si lo necesitas)
        val perTag: Map<String, List<HousingPreview>> = tagIds.associateWith { id ->
            getPreviewsForTag(id)
        }

        // Índice por key = housingId (String) para deduplicar
        fun keyOf(p: HousingPreview): String? = p.housing?.takeIf { it.isNotBlank() }

        val union = linkedMapOf<String, HousingPreview>()
        perTag.values.flatten().forEach { p ->
            keyOf(p)?.let { hid -> if (!union.containsKey(hid)) union[hid] = p }
        }

        return when (mode) {
            FilterMode.OR -> union.values.toList()
            FilterMode.AND -> {
                val sets: List<Set<String>> = perTag.values.map { list ->
                    list.mapNotNull { keyOf(it) }.toSet()
                }
                if (sets.isEmpty()) return emptyList()
                val intersection = sets.reduce { acc, s -> acc intersect s }
                intersection.mapNotNull { hid -> union[hid] }
            }
        }
    }
}

/** Modo de combinación de filtros. */
enum class FilterMode { AND, OR }
