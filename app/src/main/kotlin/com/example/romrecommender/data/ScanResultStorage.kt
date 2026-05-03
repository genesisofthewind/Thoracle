package com.example.romrecommender.data

import android.content.Context
import android.net.Uri
import com.example.romrecommender.model.GameSystem
import com.example.romrecommender.model.RomGame
import com.example.romrecommender.model.ScanResult
import org.json.JSONArray
import org.json.JSONObject

data class SavedScanState(
    val selectedFolderUri: Uri?,
    val scanResult: ScanResult?
)

class ScanResultStorage(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun save(selectedFolderUri: Uri?, scanResult: ScanResult) {
        runCatching {
            prefs.edit()
                .putString(KEY_SELECTED_FOLDER_URI, selectedFolderUri?.toString())
                .putString(KEY_SCAN_RESULT, scanResult.toJson().toString())
                .apply()
        }
    }

    fun load(): SavedScanState {
        val selectedFolderUri = prefs.getString(KEY_SELECTED_FOLDER_URI, null)?.let(Uri::parse)
        val scanResult = prefs.getString(KEY_SCAN_RESULT, null)?.let { json ->
            runCatching { JSONObject(json).toScanResult() }.getOrNull()
        }

        return SavedScanState(selectedFolderUri, scanResult)
    }

    private fun ScanResult.toJson(): JSONObject {
        return JSONObject()
            .put("systemsFound", JSONArray(systemsFound.map { it.toJson() }))
            .put("gamesBySystem", gamesBySystem.toJson())
            .put("totalGames", totalGames)
    }

    private fun Map<String, List<RomGame>>.toJson(): JSONObject {
        val json = JSONObject()
        forEach { (systemName, games) ->
            json.put(systemName, JSONArray(games.map { it.toJson() }))
        }
        return json
    }

    private fun GameSystem.toJson(): JSONObject {
        return JSONObject()
            .put("name", name)
            .put("folderName", folderName)
            .put("extensions", JSONArray(extensions))
            .put("folderAliases", JSONArray(folderAliases))
    }

    private fun RomGame.toJson(): JSONObject {
        return JSONObject()
            .put("fileName", fileName)
            .put("cleanName", cleanName)
            .put("system", system.toJson())
            .put("path", path)
    }

    private fun JSONObject.toScanResult(): ScanResult {
        val systemsFound = getJSONArray("systemsFound").toGameSystems()
        val gamesBySystem = getJSONObject("gamesBySystem").toGamesBySystem()
        val totalGames = optInt("totalGames", gamesBySystem.values.sumOf { it.size })

        return ScanResult(systemsFound, gamesBySystem, totalGames)
    }

    private fun JSONObject.toGamesBySystem(): Map<String, List<RomGame>> {
        val gamesBySystem = mutableMapOf<String, List<RomGame>>()
        keys().forEach { systemName ->
            gamesBySystem[systemName] = getJSONArray(systemName).toRomGames()
        }
        return gamesBySystem
    }

    private fun JSONArray.toGameSystems(): List<GameSystem> {
        return List(length()) { index ->
            getJSONObject(index).toGameSystem()
        }
    }

    private fun JSONArray.toRomGames(): List<RomGame> {
        return List(length()) { index ->
            getJSONObject(index).toRomGame()
        }
    }

    private fun JSONObject.toGameSystem(): GameSystem {
        val extensionsJson = getJSONArray("extensions")
        val extensions = List(extensionsJson.length()) { index -> extensionsJson.getString(index) }
        val aliasesJson = optJSONArray("folderAliases")
        val folderName = getString("folderName")
        val folderAliases = if (aliasesJson != null) {
            List(aliasesJson.length()) { index -> aliasesJson.getString(index) }
        } else {
            listOf(folderName)
        }

        return GameSystem(
            name = getString("name"),
            folderName = folderName,
            extensions = extensions,
            folderAliases = folderAliases
        )
    }

    private fun JSONObject.toRomGame(): RomGame {
        return RomGame(
            fileName = getString("fileName"),
            cleanName = getString("cleanName"),
            system = getJSONObject("system").toGameSystem(),
            path = getString("path")
        )
    }

    companion object {
        private const val PREFS_NAME = "scan_result_storage"
        private const val KEY_SELECTED_FOLDER_URI = "selected_folder_uri"
        private const val KEY_SCAN_RESULT = "scan_result"
    }
}
