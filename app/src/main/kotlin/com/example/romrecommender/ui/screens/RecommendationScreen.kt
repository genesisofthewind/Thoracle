package com.example.romrecommender.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.romrecommender.model.Recommendation

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecommendationScreen(recommendations: List<Recommendation>) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Recommended for You", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text("Based on your library", fontSize = 14.sp, color = MaterialTheme.colorScheme.outline)
        Spacer(modifier = Modifier.height(16.dp))

        if (recommendations.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                Text("No recommendations found yet. Try scanning more games!")
            }
        } else {
            LazyColumn {
                items(recommendations) { rec ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(text = rec.gameName, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                Badge { Text(rec.systemName) }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = "Because you have ${rec.reasonOwnedGame}", fontSize = 12.sp, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = rec.description, fontSize = 14.sp)
                        }
                    }
                }
            }
        }
    }
}
