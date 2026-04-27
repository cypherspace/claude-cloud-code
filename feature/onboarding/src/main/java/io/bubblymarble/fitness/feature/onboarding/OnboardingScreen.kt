package io.bubblymarble.fitness.feature.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.bubblymarble.fitness.core.data.model.EquipmentAccess
import io.bubblymarble.fitness.core.data.model.ExperienceLevel
import io.bubblymarble.fitness.core.data.model.GoalType
import io.bubblymarble.fitness.core.designsystem.components.SectionHeader

@Composable
fun OnboardingScreen(
    onDone: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
    ) {
        Text("Welcome to OldFit", style = MaterialTheme.typography.headlineSmall)
        Text("Tell us a bit about yourself so we can build a plan.", style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = state.displayName,
            onValueChange = viewModel::setName,
            label = { Text("Your name") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )

        SectionHeader("Goal")
        ChipGroup(
            options = GoalType.values().toList(),
            selected = state.goal,
            label = { it.name.replace('_', ' ').lowercase().replaceFirstChar(Char::uppercase) },
            onSelect = viewModel::setGoal,
        )

        SectionHeader("Experience")
        ChipGroup(
            options = ExperienceLevel.values().toList(),
            selected = state.experience,
            label = { it.name.lowercase().replaceFirstChar(Char::uppercase) },
            onSelect = viewModel::setExperience,
        )

        SectionHeader("Equipment access")
        ChipGroup(
            options = EquipmentAccess.values().toList(),
            selected = state.equipment,
            label = { it.name.replace('_', ' ').lowercase().replaceFirstChar(Char::uppercase) },
            onSelect = viewModel::setEquipment,
        )

        SectionHeader("Sessions per week: ${state.weeklyTarget}")
        Slider(
            value = state.weeklyTarget.toFloat(),
            onValueChange = { viewModel.setWeeklyTarget(it.toInt()) },
            valueRange = 1f..7f,
            steps = 5,
        )

        Spacer(Modifier.height(24.dp))
        Button(
            onClick = { viewModel.finish(onDone) },
            enabled = state.displayName.isNotBlank() && !state.saving,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(if (state.saving) "Setting up…" else "Get started")
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun <T> ChipGroup(
    options: List<T>,
    selected: T,
    label: (T) -> String,
    onSelect: (T) -> Unit,
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        options.forEach { option ->
            FilterChip(
                selected = option == selected,
                onClick = { onSelect(option) },
                label = { Text(label(option)) },
            )
        }
    }
}
