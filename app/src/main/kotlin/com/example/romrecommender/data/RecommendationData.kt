package com.example.romrecommender.data

import com.example.romrecommender.model.Recommendation

/**
 * Static dataset for MVP recommendations.
 * In a future version, this could be loaded from a JSON file or an online API.
 */
object RecommendationData {
    val library: Map<String, List<Recommendation>> = mapOf(
        "Pokemon Emerald" to listOf(
            Recommendation("Pokemon FireRed", "GBA", "Pokemon Emerald", "A classic remake of the first generation."),
            Recommendation("Golden Sun", "GBA", "Pokemon Emerald", "One of the best traditional RPGs on the GBA."),
            Recommendation("Mario & Luigi: Superstar Saga", "GBA", "Pokemon Emerald", "A hilarious and innovative RPG experience.")
        ),
        "Castlevania: Aria of Sorrow" to listOf(
            Recommendation("Castlevania: Dawn of Sorrow", "DS", "Castlevania: Aria of Sorrow", "The direct sequel to Aria of Sorrow."),
            Recommendation("Metroid Fusion", "GBA", "Castlevania: Aria of Sorrow", "A premier 'Metroidvania' experience with a sci-fi twist."),
            Recommendation("Castlevania: Circle of the Moon", "GBA", "Castlevania: Aria of Sorrow", "Another excellent GBA Castlevania title.")
        ),
        "Sonic Advance" to listOf(
            Recommendation("Sonic Advance 2", "GBA", "Sonic Advance", "Faster and more refined sequel."),
            Recommendation("Sonic Battle", "GBA", "Sonic Advance", "A unique 3D fighting game with Sonic characters."),
            Recommendation("Mega Man Zero", "GBA", "Sonic Advance", "High-speed action platforming.")
        ),
        "Super Mario World" to listOf(
            Recommendation("Super Mario All-Stars", "SNES", "Super Mario World", "Collection of classic Mario titles."),
            Recommendation("Donkey Kong Country", "SNES", "Super Mario World", "Pre-rendered graphics and tight platforming.")
        )
    )

    fun getRecommendationsFor(gameName: String): List<Recommendation> {
        // Try exact match or contains
        return library.entries.find { it.key.lowercase().contains(gameName.lowercase()) || gameName.lowercase().contains(it.key.lowercase()) }?.value ?: emptyList()
    }
}
