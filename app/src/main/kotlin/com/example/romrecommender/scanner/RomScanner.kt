package com.example.romrecommender.scanner

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.example.romrecommender.model.GameSystem
import com.example.romrecommender.model.RomGame
import com.example.romrecommender.model.ScanResult

/**
 * Handles scanning the selected folder for ROM files.
 */
class RomScanner(private val context: Context) {

    private val supportedSystems = listOf(
        GameSystem("NES", "NES", listOf("nes")),
        GameSystem("SNES", "SNES", listOf("smc", "sfc")),
        GameSystem("N64", "N64", listOf("n64", "z64")),
        GameSystem("GBA", "GBA", listOf("gba")),
        GameSystem("DS", "DS", listOf("nds")),
        GameSystem("3DS", "3DS", listOf("3ds")),
        GameSystem("PSP", "PSP", listOf("iso", "cso")),
        GameSystem("PS1", "PS1", listOf("bin", "cue", "iso")),
        GameSystem("PS2", "PS2", listOf("iso")),
        GameSystem("GameCube", "GC", listOf("iso", "gcm")),
        GameSystem("Wii", "Wii", listOf("iso", "wbfs")),
        GameSystem("Dreamcast", "DC", listOf("cdi", "gdi")),
        GameSystem("Genesis", "Genesis", listOf("md", "gen")),
        GameSystem("Saturn", "Saturn", listOf("iso", "bin"))
    )

    fun scanFolder(rootUri: Uri): ScanResult {
        val rootDir = DocumentFile.fromTreeUri(context, rootUri) ?: return ScanResult(emptyList(), emptyMap(), 0)
        
        val gamesBySystem = mutableMapOf<String, MutableList<RomGame>>()
        val systemsFound = mutableSetOf<GameSystem>()
        var totalGames = 0

        // Iterate through subfolders
        rootDir.listFiles().forEach { file ->
            if (file.isDirectory) {
                // Check if folder name matches a system
                val system = supportedSystems.find { it.folderName.equals(file.name, ignoreCase = true) }
                if (system != null) {
                    systemsFound.add(system)
                    val games = scanSystemFolder(file, system)
                    if (games.isNotEmpty()) {
                        gamesBySystem.getOrPut(system.name) { mutableListOf() }.addAll(games)
                        totalGames += games.size
                    }
                }
            }
        }

        return ScanResult(systemsFound.toList(), gamesBySystem, totalGames)
    }

    private fun scanSystemFolder(folder: DocumentFile, system: GameSystem): List<RomGame> {
        val roms = mutableListOf<RomGame>()
        folder.listFiles().forEach { file ->
            if (file.isFile) {
                val extension = file.name?.substringAfterLast('.', "")?.lowercase() ?: ""
                if (system.extensions.contains(extension)) {
                    val cleanName = cleanGameName(file.name ?: "Unknown")
                    roms.add(RomGame(file.name ?: "Unknown", cleanName, system, file.uri.toString()))
                }
            }
        }
        return roms
    }

    /**
     * Cleans up common ROM naming conventions.
     * Example: "Pokemon Emerald (USA).gba" -> "Pokemon Emerald"
     */
    private fun cleanGameName(fileName: String): String {
        var name = fileName.substringBeforeLast('.')
        
        // Remove region tags like (USA), (Japan), (Europe)
        name = name.replace(Regex("\\s?\\([^)]*\\)"), "")
        
        // Remove revision tags like [v1.1], [Rev 1]
        name = name.replace(Regex("\\s?\\[[^]]*\\]"), "")
        
        // Remove Disc info like (Disc 1)
        name = name.replace(Regex("\\s?\\(Disc\\s?\\d+\\)", RegexOption.IGNORE_CASE), "")
        
        // Remove common junk symbols
        name = name.replace("_", " ")
        
        return name.trim()
    }
}
