package io.bubblymarble.fitness.feature.plans

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
fun PlansScreen(viewModel: PlansViewModel = hiltViewModel()) {
    val templates by viewModel.templatesFlow.collectAsStateWithLifecycle()
    val state by viewModel.state.collectAsStateWithLifecycle()

    Column(
        Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Button(
            enabled = !state.generating,
            onClick = { viewModel.generate(sessionsPerWeek = 4, minutesPerSession = 45) },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(if (state.generating) "Generating…" else "Generate plan")
        }
        state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        Spacer(Modifier.height(8.dp))
        if (templates.isEmpty()) {
            Text("No plans yet. Generate one to begin.", style = MaterialTheme.typography.bodyMedium)
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(templates, key = { it.id }) { tpl ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text(tpl.name, style = MaterialTheme.typography.titleMedium)
                            Text(
                                "${tpl.items.size} exercises · ${if (tpl.generatedByAi) "AI" else "Template"}",
                                style = MaterialTheme.typography.bodySmall,
                            )
                            if (tpl.description.isNotBlank()) {
                                Text(tpl.description, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        }
    }
}
