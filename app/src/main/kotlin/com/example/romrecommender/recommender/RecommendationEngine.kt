package com.example.romrecommender.recommender

import com.example.romrecommender.data.RecommendationData
import com.example.romrecommender.model.Recommendation
import com.example.romrecommender.model.RomGame
import com.example.romrecommender.model.ScanResult

/**
 * Generates recommendations based on the scan result.
 */
class RecommendationEngine {

    fun generateRecommendations(scanResult: ScanResult): List<Recommendation> {
        val recommendations = mutableListOf<Recommendation>()
        val seenGames = mutableSetOf<String>()

        // For each game found, look up recommendations
        scanResult.gamesBySystem.values.flatten().forEach { game ->
            val recs = RecommendationData.getRecommendationsFor(game.cleanName)
            recs.forEach { rec ->
                // Avoid recommending games the user already owns or duplicate recommendations
                if (!isGameOwned(rec.gameName, scanResult) && !seenGames.contains(rec.gameName)) {
                    recommendations.add(rec)
                    seenGames.add(rec.gameName)
                }
            }
        }

        return recommendations.sortedBy { it.gameName }
    }

    private fun isGameOwned(gameName: String, scanResult: ScanResult): Boolean {
        return scanResult.gamesBySystem.values.flatten().any { 
            it.cleanName.lowercase().equals(gameName.lowercase(), ignoreCase = true) ||
            it.cleanName.lowercase().contains(gameName.lowercase()) 
        }
    }
}
