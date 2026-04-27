package io.bubblymarble.fitness.feature.plans.editor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.bubblymarble.fitness.core.designsystem.components.TonalCard
import io.bubblymarble.fitness.core.designsystem.theme.Spacing

@Composable
fun PlanEditorScreen(
    onSaved: () -> Unit,
    viewModel: PlanEditorViewModel = hiltViewModel(),
) {
    val ui by viewModel.ui.collectAsStateWithLifecycle()

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(Spacing.lg),
        verticalArrangement = Arrangement.spacedBy(Spacing.md),
    ) {
        when {
            ui.loading -> CircularProgressIndicator()
            ui.notFound -> Text("Plan not found.", color = MaterialTheme.colorScheme.error)
            else -> {
                Text(ui.name, style = MaterialTheme.typography.headlineSmall)
                if (ui.description.isNotBlank()) {
                    Text(
                        ui.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                HorizontalDivider()
                ui.items.forEachIndexed { idx, item ->
                    EditorRow(
                        index = idx,
                        item = item,
                        onSetsChange = { viewModel.setSets(idx, it) },
                        onRepsChange = { viewModel.setReps(idx, it) },
                        onRestChange = { viewModel.setRestSec(idx, it) },
                        onWeightChange = { viewModel.setWeightKg(idx, it) },
                        onRemove = { viewModel.removeItem(idx) },
                    )
                }
                Spacer(Modifier.height(Spacing.sm))
                Button(
                    onClick = { viewModel.save(onSaved) },
                    enabled = !ui.saving && ui.items.isNotEmpty(),
                    modifier = Modifier.fillMaxWidth(),
                ) { Text(if (ui.saving) "Saving…" else "Save plan") }
            }
        }
    }
}

@Composable
private fun EditorRow(
    index: Int,
    item: EditableItem,
    onSetsChange: (Int) -> Unit,
    onRepsChange: (Int?) -> Unit,
    onRestChange: (Int) -> Unit,
    onWeightChange: (Double?) -> Unit,
    onRemove: () -> Unit,
) {
    TonalCard(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(Spacing.lg), verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "${index + 1}. ${item.exerciseName}",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.weight(1f),
                )
                IconButton(onClick = onRemove) {
                    Icon(Icons.Default.Close, contentDescription = "Remove")
                }
            }

            val setsText = remember(item.sets) { item.sets.toString() }
            val repsText = remember(item.reps) { item.reps?.toString().orEmpty() }
            val restText = remember(item.restSec) { item.restSec.toString() }
            val weightText = remember(item.weightKg) { item.weightKg?.let { "%.1f".format(it) }.orEmpty() }

            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                NumberField(
                    label = "Sets",
                    value = setsText,
                    onValueChange = { it.toIntOrNull()?.let(onSetsChange) },
                    modifier = Modifier.weight(1f),
                )
                NumberField(
                    label = "Reps",
                    value = repsText,
                    onValueChange = { onRepsChange(it.toIntOrNull()) },
                    modifier = Modifier.weight(1f),
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                NumberField(
                    label = "Rest (s)",
                    value = restText,
                    onValueChange = { it.toIntOrNull()?.let(onRestChange) },
                    modifier = Modifier.weight(1f),
                )
                DecimalField(
                    label = "Weight (kg)",
                    value = weightText,
                    onValueChange = { onWeightChange(it.toDoubleOrNull()) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun NumberField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = value,
        onValueChange = { v -> onValueChange(v.filter(Char::isDigit).take(4)) },
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = modifier,
    )
}

@Composable
private fun DecimalField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = value,
        onValueChange = { v -> onValueChange(v.filter { c -> c.isDigit() || c == '.' }.take(6)) },
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = modifier,
    )
}
