package com.team21.myapplication.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.team21.myapplication.data.model.Ammenities
import com.team21.myapplication.ui.createPostView.state.SuggestedPrice
import kotlinx.coroutines.tasks.await
import kotlin.math.roundToInt

/**
 * Aquí hago tal cosa:
 * - Tomo comparables desde HousingTag/<tagId>/HousingPreview (vía HousingTagRepository)
 * - Calculo p25/mediana/p75 y devuelvo SuggestedPrice
 * - Si no hay datos suficientes por cualquier razón, devuelvo un DEFAULT de 9,500
 */
class PricingRepository(
    private val tagRepo: HousingTagRepository = HousingTagRepository()
) {
    suspend fun suggestPrice(
        selectedTagId: String,
        selectedAmenities: List<Ammenities>,
        maxComps: Int = 60
    ): Result<SuggestedPrice> = runCatching {
        // 1) Cargo previews por tag (fuente confiable en tu estructura)
        val previews = tagRepo.getPreviewsForTag(selectedTagId).take(maxComps)

        val prices = previews.mapNotNull { it.price.takeIf { p -> p > 0.0 } }.sorted()
        if (prices.isNotEmpty()) {
            val p25 = percentile(prices, 0.25)
            val p50 = percentile(prices, 0.50)
            val p75 = percentile(prices, 0.75)

            // Ajuste suave por amenities (opcional; amenities NO son requisito)
            val bump = 0.0 // puedes reintroducir un pequeño ajuste si quieres
            val suggested = (p50 * (1.0 + bump)).roundToInt().toDouble()

            SuggestedPrice(
                value = suggested,
                low = p25,
                high = p75,
                compsCount = prices.size,
                note = "Based on ${prices.size} comps for this housing type."
            )
        } else {
            // 2) Fallback absoluto (tu requisito #3 y #4): 9,500
            SuggestedPrice(
                value = 9500.0,
                low = 9500.0,
                high = 9500.0,
                compsCount = 0,
                note = "Default recommendation."
            )
        }
    }

    private fun percentile(sorted: List<Double>, q: Double): Double {
        if (sorted.isEmpty()) return 9500.0
        val pos = (sorted.size - 1) * q
        val i = pos.toInt()
        val frac = pos - i
        return if (i + 1 < sorted.size) sorted[i] * (1 - frac) + sorted[i + 1] * frac else sorted[i].toDouble()
    }
}
