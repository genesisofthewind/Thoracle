package com.example.romrecommender.model

/**
 * Represents a gaming system (e.g., GBA, SNES).
 */
data class GameSystem(
    val name: String,
    val folderName: String,
    val extensions: List<String>
)

/**
 * Represents a single ROM file found on the device.
 */
data class RomGame(
    val fileName: String,
    val cleanName: String,
    val system: GameSystem,
    val path: String
)

/**
 * Represents a recommendation for a game the user does not have.
 */
data class Recommendation(
    val gameName: String,
    val systemName: String,
    val reasonOwnedGame: String,
    val description: String
)

/**
 * The final result of a folder scan.
 */
data class ScanResult(
    val systemsFound: List<GameSystem>,
    val gamesBySystem: Map<String, List<RomGame>>,
    val totalGames: Int
)
