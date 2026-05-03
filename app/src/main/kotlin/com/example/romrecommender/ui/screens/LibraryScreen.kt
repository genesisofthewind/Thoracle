package com.example.romrecommender.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.romrecommender.model.ScanResult

@Composable
fun LibraryScreen(scanResult: ScanResult?) {
    var selectedSystem by remember { mutableStateOf<String?>(null) }

    if (scanResult == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
            Text("Scan your library first!")
        }
        return
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Your Systems", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(scanResult.systemsFound) { system ->
                ListItem(
                    headlineContent = { Text(system.name) },
                    supportingContent = { Text("${scanResult.gamesBySystem[system.name]?.size ?: 0} games") },
                    modifier = Modifier.clickable { 
                        selectedSystem = if (selectedSystem == system.name) null else system.name 
                    }
                )
                
                if (selectedSystem == system.name) {
                    val games = scanResult.gamesBySystem[system.name] ?: emptyList()
                    games.forEach { game ->
                        Text(
                            text = "  • ${game.cleanName}",
                            fontSize = 14.sp,
                            modifier = Modifier.padding(start = 24.dp, bottom = 4.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                }
            }
        }
    }
}
