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

    // Fallback OFFLINE: devuelve una descripción al azar con amenities incrustadas.
    fun makeOfflineTemplate(amenities: List<String>): String {
        val amenitiesList = if (amenities.isEmpty()) "—" else amenities.joinToString(", ")
        val t1 = """
        Step into sophistication and ease with this captivating residence, perfectly designed for the modern lifestyle. Enjoy seamless entertaining and comfortable daily living. A full list of prime amenities includes: $amenitiesList. Your dream home awaits your private tour.
    """.trimIndent()
        val t2 = """
        Discover a charming haven where comfort meets convenience in a sought-after neighborhood. This inviting property offers the perfect blend of warmth and practicality for your next chapter. Key features and essential amenities such as: $amenitiesList ensure a delightful living experience. Welcome home.
    """.trimIndent()
        val t3 = """
        An unmissable opportunity to secure a valuable piece of real estate with endless potential and inherent curb appeal. This property is ideal for both first-time buyers and seasoned investors looking for a solid foundation. Among the attractive amenities you will find: $amenitiesList. Don't delay—schedule your showing today!
    """.trimIndent()
        return listOf(t1, t2, t3).random()
    }

}
