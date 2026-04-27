package io.bubblymarble.fitness.feature.workouts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun WorkoutsScreen(
    onStartSession: (Long) -> Unit,
    viewModel: WorkoutsViewModel = hiltViewModel(),
) {
    val templates by viewModel.templatesFlow.collectAsStateWithLifecycle()
    Column(
        Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Today's workouts", style = MaterialTheme.typography.headlineSmall)
        if (templates.isEmpty()) {
            Text("No workouts yet. Generate a plan first.", style = MaterialTheme.typography.bodyMedium)
        } else LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(templates, key = { it.id }) { tpl ->
                Card(
                    Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(tpl.name, style = MaterialTheme.typography.titleMedium)
                        Text("${tpl.items.size} exercises", style = MaterialTheme.typography.bodySmall)
                        Button(
                            onClick = { viewModel.startSession(tpl.id, onStartSession) },
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        ) { Text("Start") }
                    }
                }
            }
        }
    }
}
