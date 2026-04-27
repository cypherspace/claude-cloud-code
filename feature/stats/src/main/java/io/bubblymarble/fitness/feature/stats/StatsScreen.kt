package io.bubblymarble.fitness.feature.stats

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.bubblymarble.fitness.core.designsystem.components.StatCard

@Composable
fun StatsScreen(viewModel: StatsViewModel = hiltViewModel()) {
    val ui by viewModel.ui.collectAsStateWithLifecycle()
    Column(
        Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Stats", style = MaterialTheme.typography.headlineSmall)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard(label = "Current streak", value = "${ui.streak?.currentLength ?: 0} d", modifier = Modifier.weight(1f))
            StatCard(label = "Longest", value = "${ui.streak?.longestLength ?: 0} d", modifier = Modifier.weight(1f))
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard(label = "Sessions", value = ui.totalSessions.toString(), modifier = Modifier.weight(1f))
            StatCard(label = "Last 7 d", value = ui.sessionsLast7d.toString(), modifier = Modifier.weight(1f))
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard(label = "Total sets", value = ui.totalSets.toString(), modifier = Modifier.weight(1f))
            StatCard(label = "Volume (kg)", value = "%.0f".format(ui.totalVolumeKg), modifier = Modifier.weight(1f))
        }
    }
}
