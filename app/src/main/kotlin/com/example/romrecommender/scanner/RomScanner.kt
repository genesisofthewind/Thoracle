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
        GameSystem("3DS", "3DS", listOf("3ds", "cia", "cci", "cxi", "app", "3dsx")),
        GameSystem(
            "Nintendo Switch",
            "Switch",
            listOf("nsp", "xci", "nsz", "xcz"),
            listOf("Switch", "Nintendo Switch", "NSW")
        ),
        GameSystem("PSP", "PSP", listOf("iso", "cso", "pbp")),
        GameSystem(
            "PS1",
            "PS1",
            listOf("bin", "cue", "iso", "chd", "pbp", "m3u", "img", "ccd"),
            listOf("PS1", "PSX", "PlayStation", "PlayStation 1")
        ),
        GameSystem("PS2", "PS2", listOf("iso", "chd", "gz")),
        GameSystem(
            "GameCube",
            "GC",
            listOf("iso", "gcm", "rvz", "ciso"),
            listOf("GameCube", "GC", "NGC", "Nintendo GameCube")
        ),
        GameSystem(
            "Wii",
            "Wii",
            listOf("iso", "wbfs", "rvz", "wad"),
            listOf("Wii", "Nintendo Wii")
        ),
        GameSystem(
            "WiiWare",
            "WiiWare",
            listOf("wad"),
            listOf("WiiWare", "WAD", "WADs")
        ),
        GameSystem(
            "Genesis",
            "Genesis",
            listOf("md", "gen", "smd", "bin"),
            listOf("Genesis", "Sega Genesis", "Mega Drive", "MegaDrive", "MD")
        ),
        GameSystem(
            "Master System",
            "Master System",
            listOf("sms"),
            listOf("Master System", "Sega Master System", "SMS")
        ),
        GameSystem(
            "Game Gear",
            "Game Gear",
            listOf("gg"),
            listOf("Game Gear", "Sega Game Gear", "GG")
        ),
        GameSystem(
            "Saturn",
            "Saturn",
            listOf("cue", "bin", "iso", "chd", "m3u"),
            listOf("Saturn", "Sega Saturn")
        ),
        GameSystem(
            "Dreamcast",
            "DC",
            listOf("cdi", "gdi", "chd"),
            listOf("Dreamcast", "Sega Dreamcast", "DC")
        )
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
                val system = supportedSystems.find { it.matchesFolderName(file.name) }
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
        val files = folder.listFiles().sortedBy { if (it.isPreferredGameFolder(system)) 0 else 1 }

        files.forEach { file ->
            if (file.isFile) {
                val extension = file.name?.substringAfterLast('.', "")?.lowercase() ?: ""
                if (system.extensions.contains(extension)) {
                    val cleanName = cleanGameName(file.name ?: "Unknown")
                    roms.add(RomGame(file.name ?: "Unknown", cleanName, system, file.uri.toString()))
                }
            } else if (file.isDirectory && !file.isIgnoredGameFolder()) {
                roms.addAll(scanSystemFolder(file, system))
            }
        }
        return roms
    }

    private fun GameSystem.matchesFolderName(folderName: String?): Boolean {
        return folderAliases.any { it.equals(folderName, ignoreCase = true) }
    }

    private fun DocumentFile.isPreferredGameFolder(system: GameSystem): Boolean {
        val folderName = name ?: return false
        return preferredGameFolderNames.any { it.equals(folderName, ignoreCase = true) } ||
            system.matchesFolderName(folderName)
    }

    private fun DocumentFile.isIgnoredGameFolder(): Boolean {
        val folderName = name ?: return false
        return ignoredGameFolderNames.any { it.equals(folderName, ignoreCase = true) }
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

    companion object {
        private val preferredGameFolderNames = listOf("Roms", "ROMs", "Games")
        private val ignoredGameFolderNames = listOf("DLC", "Updates", "Update", "User", "Saves", "Save Data")
    }
}
