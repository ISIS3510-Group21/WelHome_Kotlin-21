package com.team21.myapplication.domain.mapper

import com.team21.myapplication.data.model.HousingPreview
import com.team21.myapplication.data.model.HousingTag
import com.team21.myapplication.ui.filterView.state.PreviewCardUi
import com.team21.myapplication.ui.filterView.state.TagChipUi

object FilterUiMapper {

    // Canon y sinónimos para resolver nombres reales en BD
    private val FEATURED_CANONICAL = listOf("House", "Room", "Cabins", "Apartment")
    private val SYNONYMS = mapOf(
        "House" to setOf("House", "Houses"),
        "Room" to setOf("Room", "Rooms"),
        "Cabins" to setOf("Cabins", "Cabin"),
        "Apartment" to setOf("Apartment", "Apartments")
    )

    fun toFeaturedAndOthers(
        tags: List<HousingTag>,
        selected: Set<String>
    ): Pair<List<TagChipUi>, List<TagChipUi>> {
        val byLower = tags.groupBy { it.name.lowercase() }

        // Resuelve cada canonical buscando el primer sinónimo disponible en la BD
        val featuredResolved = FEATURED_CANONICAL.mapNotNull { canonical ->
            val options = SYNONYMS[canonical] ?: setOf(canonical)
            val match = options.firstNotNullOfOrNull { opt ->
                byLower[opt.lowercase()]?.firstOrNull()
            }
            match?.let { TagChipUi(id = it.id, label = it.name, selected = it.id in selected) }
        }

        val featuredIds = featuredResolved.map { it.id }.toSet()

        val others = tags
            .filter { it.id !in featuredIds }
            .sortedBy { it.name.lowercase() }
            .map { TagChipUi(id = it.id, label = it.name, selected = it.id in selected) }

        return featuredResolved to others
    }

    fun toPreviewUi(previews: List<HousingPreview>): List<PreviewCardUi> {
        return previews.mapNotNull { p ->
            val housingId = p.housing?.substringAfterLast('/') ?: return@mapNotNull null
            PreviewCardUi(
                housingId = housingId,
                title = p.title,
                pricePerMonthLabel = if (p.price > 0) "$${p.price}/month" else "",
                rating = p.rating.toDouble(),
                reviewsCount = p.reviewsCount.toInt(),
                photoUrl = p.photoPath
            )
        }
    }
}