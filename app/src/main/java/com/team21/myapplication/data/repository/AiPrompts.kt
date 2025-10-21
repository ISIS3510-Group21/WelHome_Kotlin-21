package com.team21.myapplication.data.repository

object AiPrompts {

    /** Prompt para generar desde cero (cuando hay mainPhoto + tag + amenities) */
    fun makeCreatePrompt(
        city: String?,
        neighborhood: String?,
        housingTypeLabel: String,
        amenities: List<String>
    ): String = buildString {
        appendLine("You are a helpful real-estate copywriter for short rental listings.")
        appendLine("Write a short, catchy English description (~3 sentences) that makes people want to book, like Airbnb style.")
        appendLine("Be honest, avoid exaggeration, highlight location vibe and top amenities. No markdown, no headings.")
        appendLine()
        appendLine("Listing:")
        if (!city.isNullOrBlank()) appendLine("  City: $city")
        if (!neighborhood.isNullOrBlank()) appendLine("  Neighborhood: $neighborhood")
        appendLine("  HousingType: $housingTypeLabel")
        appendLine("  Amenities: ${amenities.joinToString()}")
        appendLine()
        appendLine("Return only the paragraph text.")
    }

    fun makeRewritePrompt(
        original: String
    ): String = buildString {
        appendLine("Rewrite the following listing description in English to be more attractive and commercial, around 3 short sentences.")
        appendLine("Keep facts, remove fluff, make it concise and compelling. No markdown.")
        appendLine()
        appendLine("Original:")
        appendLine(original.trim())
        appendLine()
        appendLine("Return only the improved paragraph.")
    }
}
