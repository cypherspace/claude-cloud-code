package io.bubblymarble.fitness.feature.workouts.runner

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun WorkoutRunnerScreen(
    sessionId: Long,
    onFinish: () -> Unit,
    viewModel: WorkoutRunnerViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.phase) {
        if (state.phase == RunnerPhase.COMPLETE) {
            kotlinx.coroutines.delay(1500)
            onFinish()
        }
    }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(state.templateName.ifBlank { "Workout" }, style = MaterialTheme.typography.headlineSmall)

        when (state.phase) {
            RunnerPhase.LOADING -> Text("Loading…")
            RunnerPhase.ERROR -> Text(state.errorMessage ?: "Error", color = MaterialTheme.colorScheme.error)
            RunnerPhase.COMPLETE -> {
                Text("Session complete", style = MaterialTheme.typography.titleLarge)
                Text("Streak updated. Nice work, #$sessionId.", style = MaterialTheme.typography.bodyMedium)
            }
            RunnerPhase.EXERCISE -> ExerciseBlock(state, viewModel)
            RunnerPhase.REST -> RestBlock(state, viewModel)
        }
    }
}

@Composable
private fun ExerciseBlock(state: RunnerState, vm: WorkoutRunnerViewModel) {
    val item = state.currentItem ?: return
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Exercise ${state.currentItemIndex + 1} of ${state.items.size}", style = MaterialTheme.typography.labelLarge)
        Text(item.exercise.name, style = MaterialTheme.typography.titleLarge)
        Text("Set ${state.currentSet} / ${state.totalSetsForCurrent}", style = MaterialTheme.typography.titleMedium)
        item.template.targetReps?.let { Text("Target: $it reps") }
        item.template.targetDurationSec?.let { Text("Target: ${it}s · ${state.secondsLeft}s left") }

        var reps by remember(state.currentItemIndex, state.currentSet) {
            mutableStateOf(item.template.targetReps?.toString().orEmpty())
        }
        var weight by remember(state.currentItemIndex, state.currentSet) { mutableStateOf("") }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = reps,
                onValueChange = { reps = it.filter(Char::isDigit).take(3) },
                label = { Text("Reps") },
                modifier = Modifier.weight(1f),
                singleLine = true,
            )
            OutlinedTextField(
                value = weight,
                onValueChange = { weight = it.filter { c -> c.isDigit() || c == '.' }.take(6) },
                label = { Text("Weight (kg)") },
                modifier = Modifier.weight(1f),
                singleLine = true,
            )
        }

        Spacer(Modifier.height(4.dp))
        Button(
            onClick = { vm.completeSet(repsActual = reps.toIntOrNull(), weightKg = weight.toDoubleOrNull()) },
            modifier = Modifier.fillMaxWidth(),
        ) { Text("Complete set") }
        OutlinedButton(onClick = { vm.cancel() }, modifier = Modifier.fillMaxWidth()) {
            Text("Cancel workout")
        }
    }
}

@Composable
private fun RestBlock(state: RunnerState, vm: WorkoutRunnerViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Rest", style = MaterialTheme.typography.titleLarge)
        Text("${state.secondsLeft}s remaining", style = MaterialTheme.typography.headlineMedium)
        Button(onClick = { vm.skipRest() }, modifier = Modifier.fillMaxWidth()) {
            Text("Skip rest")
        }
    }
}
