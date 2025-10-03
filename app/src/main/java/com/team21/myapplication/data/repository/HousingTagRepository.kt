package com.team21.myapplication.data.repository

import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.team21.myapplication.data.model.HousingTag
import com.team21.myapplication.data.model.HousingPreview
import kotlinx.coroutines.tasks.await

class HousingTagRepository {
    private val db = FirebaseFirestore.getInstance()
    private val housingTagsCollection = db.collection("HousingTag")
    private val tagCol = db.collection("HousingTag")

    suspend fun getHousingTags(listRefs: List<DocumentReference>): List<HousingTag> {
        val housingTags = mutableListOf<HousingTag>()
        for (ref in listRefs) {
            val documentSnapshot = ref.get().await()
            val housingTag = documentSnapshot.toObject(HousingTag::class.java)
            housingTag?.let {
                housingTags.add(it)
            }
        }
        return housingTags
    }

    private fun coerceDocRef(raw: Any?): DocumentReference? = when (raw) {
        is DocumentReference -> raw
        is String -> {
            // Si viene solo el ID del HousingPost, creamos el ref. Si viene path completo, lo usamos.
            if (raw.contains("/")) db.document(raw) else db.collection("HousingPost").document(raw)
        }
        else -> null
    }

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
                housing = coerceDocRef(pd["housing"])
            )
        }

        return HousingTag(
            id = doc.id,
            name = (data["name"] as? String).orEmpty(),
            iconPath = (data["iconPath"] as? String).orEmpty(),
            housingPreview = previews
        )
    }

    /** Lee SOLO los previews de un tag (útil para búsquedas). */
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
                housing = coerceDocRef(data["housing"])
            )
        }
    }

    /**
     * Busca previews por múltiples tags.
     * - OR: unión de resultados.
     * - AND: intersección por housingId.
     */
    suspend fun getPreviewsForTags(
        tagIds: List<String>,
        mode: FilterMode
    ): List<HousingPreview> {
        if (tagIds.isEmpty()) return emptyList()

        // Cargar previews por tag
        val perTag: Map<String, List<HousingPreview>> = tagIds.associateWith { id ->
            getPreviewsForTag(id)
        }

        // Indexar por housingId (DocumentReference.id) para deduplicar
        fun keyOf(p: HousingPreview): String? = p.housing?.id

        val union = linkedMapOf<String, HousingPreview>()
        perTag.values.flatten().forEach { p ->
            keyOf(p)?.let { hid -> if (!union.containsKey(hid)) union[hid] = p }
        }

        return when (mode) {
            FilterMode.OR -> union.values.toList()
            FilterMode.AND -> {
                // Intersección: housings presentes en TODOS los tags seleccionados
                val sets = perTag.values.map { list -> list.mapNotNull { keyOf(it) }.toSet() }
                val intersection = sets.reduce { acc, s -> acc intersect s }
                intersection.mapNotNull { hid -> union[hid] }
            }
        }
    }
}

enum class FilterMode { AND, OR }
