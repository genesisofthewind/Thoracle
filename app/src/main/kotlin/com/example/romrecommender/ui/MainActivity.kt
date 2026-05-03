package com.example.romrecommender.ui

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.romrecommender.data.ScanResultStorage
import com.example.romrecommender.model.ScanResult
import com.example.romrecommender.recommender.RecommendationEngine
import com.example.romrecommender.scanner.RomScanner
import com.example.romrecommender.ui.screens.HomeScreen
import com.example.romrecommender.ui.screens.LibraryScreen
import com.example.romrecommender.ui.screens.RecommendationScreen
import com.example.romrecommender.ui.theme.ROMRecommenderTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ROMRecommenderTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    RomAppNavigation()
                }
            }
        }
    }
}

@Composable
fun RomAppNavigation() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val scanner = remember { RomScanner(context) }
    val recommender = remember { RecommendationEngine() }
    val scanResultStorage = remember { ScanResultStorage(context) }
    val savedScanState = remember { scanResultStorage.load() }
    val coroutineScope = rememberCoroutineScope()

    var selectedFolderUri by remember { mutableStateOf(savedScanState.selectedFolderUri) }
    var scanResult by remember { mutableStateOf(savedScanState.scanResult) }
    var isScanning by remember { mutableStateOf(false) }

    // Folder picker launcher
    val pickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        if (uri != null) {
            selectedFolderUri = uri

            // Persist permission
            context.contentResolver.takePersistableUriPermission(
                uri,
                android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        }
    }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Text("⌂") },
                    label = { Text("Home") },
                    selected = true, // Simplified for MVP
                    onClick = { navController.navigate("home") }
                )
                NavigationBarItem(
                    icon = { Text("☰") },
                    label = { Text("Library") },
                    selected = false,
                    onClick = { navController.navigate("library") }
                )
                NavigationBarItem(
                    icon = { Text("★") },
                    label = { Text("Recs") },
                    selected = false,
                    onClick = { navController.navigate("recommendations") }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("home") {
                HomeScreen(
                    selectedFolder = selectedFolderUri,
                    scanResult = scanResult,
                    isScanning = isScanning,
                    onSelectFolder = { pickerLauncher.launch(null) },
                    onScan = {
                        selectedFolderUri?.let { uri ->
                            coroutineScope.launch {
                                isScanning = true

                                try {
                                    val result = withContext(Dispatchers.IO) {
                                        val scannedResult = scanner.scanFolder(uri)
                                        scanResultStorage.save(uri, scannedResult)
                                        scannedResult
                                    }
                                    scanResult = result
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                } finally {
                                    isScanning = false
                                }
                            }
                        }
                    }
                )
            }
            composable("library") {
                LibraryScreen(scanResult = scanResult)
            }
            composable("recommendations") {
                val recs = scanResult?.let { recommender.generateRecommendations(it) } ?: emptyList()
                RecommendationScreen(recommendations = recs)
            }
        }
    }
}
