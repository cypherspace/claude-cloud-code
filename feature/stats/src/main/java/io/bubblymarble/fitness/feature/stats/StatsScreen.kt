package io.bubblymarble.fitness.feature.stats

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.bubblymarble.fitness.core.designsystem.components.SectionHeader
import io.bubblymarble.fitness.core.designsystem.components.StatCard
import io.bubblymarble.fitness.core.designsystem.theme.LocalFeatureAccents
import io.bubblymarble.fitness.core.designsystem.theme.Spacing

@Composable
fun StatsScreen(viewModel: StatsViewModel = hiltViewModel()) {
    val ui by viewModel.ui.collectAsStateWithLifecycle()
    val accents = LocalFeatureAccents.current
    Column(
        Modifier.fillMaxSize().padding(Spacing.lg),
        verticalArrangement = Arrangement.spacedBy(Spacing.md),
    ) {
        SectionHeader("Streak")
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
            StatCard(
                label = "Current streak",
                value = "${ui.streak?.currentLength ?: 0} d",
                accent = accents.stats,
                modifier = Modifier.weight(1f),
            )
            StatCard(
                label = "Longest",
                value = "${ui.streak?.longestLength ?: 0} d",
                modifier = Modifier.weight(1f),
            )
        }

        SectionHeader("Sessions")
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
            StatCard(
                label = "Total",
                value = ui.totalSessions.toString(),
                accent = accents.workouts,
                modifier = Modifier.weight(1f),
            )
            StatCard(
                label = "Last 7 d",
                value = ui.sessionsLast7d.toString(),
                modifier = Modifier.weight(1f),
            )
        }

        SectionHeader("Volume")
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
            StatCard(
                label = "Sets",
                value = ui.totalSets.toString(),
                modifier = Modifier.weight(1f),
            )
            StatCard(
                label = "Total kg",
                value = "%.0f".format(ui.totalVolumeKg),
                modifier = Modifier.weight(1f),
            )
        }
    }
}
