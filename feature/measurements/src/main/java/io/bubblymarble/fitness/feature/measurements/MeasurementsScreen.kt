package io.bubblymarble.fitness.feature.measurements

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.bubblymarble.fitness.core.data.model.BodyMeasurement
import io.bubblymarble.fitness.core.data.model.MeasurementGroup
import io.bubblymarble.fitness.core.data.model.MeasurementType
import io.bubblymarble.fitness.core.designsystem.components.SectionHeader
import io.bubblymarble.fitness.core.designsystem.components.StatCard
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun MeasurementsScreen(viewModel: MeasurementsViewModel = hiltViewModel()) {
    val ui by viewModel.ui.collectAsStateWithLifecycle()
    val latest by viewModel.latest.collectAsStateWithLifecycle()
    val weightSeries by viewModel.weightSeries.collectAsStateWithLifecycle()

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Measurements", style = MaterialTheme.typography.headlineSmall)

        SyncBlock(
            available = ui.healthConnectAvailable,
            syncing = ui.syncing,
            status = ui.syncStatus,
            onSync = viewModel::syncFromHealthConnect,
            onDismiss = viewModel::dismissSyncStatus,
        )

        SectionHeader("Body composition")
        BodyCompGrid(latest)

        if (weightSeries.size >= 2) {
            SectionHeader("Weight trend (last ${weightSeries.size} entries)")
            WeightSparkline(weightSeries)
        }

        SectionHeader("Tape measurements")
        MeasurementType.values()
            .filter { it.groupedAs == MeasurementGroup.TAPE }
            .forEach { type ->
                TapeRow(
                    type = type,
                    latest = latest[type],
                    onLog = { value -> viewModel.record(type, value) },
                )
            }

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun SyncBlock(
    available: Boolean,
    syncing: Boolean,
    status: String?,
    onSync: () -> Unit,
    onDismiss: () -> Unit,
) {
    Card(elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Health Connect", style = MaterialTheme.typography.titleMedium)
            Text(
                if (available)
                    "Pulls weight, body fat, lean mass, body water, bone mass, height, and BMR " +
                        "from any source publishing to Health Connect (e.g. Renpho)."
                else
                    "Health Connect isn't installed. Install it from the Play Store to import " +
                        "scale data automatically.",
                style = MaterialTheme.typography.bodySmall,
            )
            Button(
                onClick = onSync,
                enabled = available && !syncing,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(if (syncing) "Syncing…" else "Sync last 30 days")
            }
            if (status != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(status, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                    TextButton(onClick = onDismiss) { Text("OK") }
                }
            }
        }
    }
}

@Composable
private fun BodyCompGrid(latest: Map<MeasurementType, BodyMeasurement>) {
    val types = MeasurementType.values().filter { it.groupedAs == MeasurementGroup.BODY_COMP }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        types.chunked(2).forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { type ->
                    val entry = latest[type]
                    StatCard(
                        label = type.label(),
                        value = entry?.let { "%.1f %s".format(it.value, type.unit) } ?: "—",
                        modifier = Modifier.weight(1f),
                    )
                }
                if (row.size == 1) Box(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun WeightSparkline(samples: List<BodyMeasurement>) {
    val values = samples.map { it.value.toFloat() }
    val min = values.min()
    val max = values.max()
    val range = (max - min).coerceAtLeast(0.01f)

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth()) {
                Text("Latest %.1f kg".format(samples.last().value), style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.weight(1f))
                Text("Range %.1f–%.1f kg".format(min, max), style = MaterialTheme.typography.bodySmall)
            }
            Spacer(Modifier.height(8.dp))
            androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxWidth().height(80.dp)) {
                if (values.size < 2) return@Canvas
                val stride = size.width / (values.size - 1)
                val points = values.mapIndexed { index, v ->
                    val x = index * stride
                    val y = size.height - ((v - min) / range) * size.height
                    androidx.compose.ui.geometry.Offset(x, y)
                }
                for (i in 1 until points.size) {
                    drawLine(
                        color = androidx.compose.ui.graphics.Color(0xFF0EA5E9),
                        start = points[i - 1],
                        end = points[i],
                        strokeWidth = 4f,
                    )
                }
            }
        }
    }
}

@Composable
private fun TapeRow(
    type: MeasurementType,
    latest: BodyMeasurement?,
    onLog: (Double) -> Unit,
) {
    var input by remember { mutableStateOf("") }
    val dateFmt = remember { DateTimeFormatter.ofPattern("d MMM").withZone(ZoneId.systemDefault()) }

    Card(elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
        Row(
            Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(type.label(), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                Text(
                    text = latest?.let { "%.1f %s · %s".format(it.value, type.unit, dateFmt.format(it.timestamp)) }
                        ?: "No entries yet",
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            OutlinedTextField(
                value = input,
                onValueChange = { input = it.filter { c -> c.isDigit() || c == '.' }.take(6) },
                singleLine = true,
                label = { Text("New") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.padding(horizontal = 8.dp).widthIn(min = 96.dp, max = 120.dp),
            )
            TextButton(
                enabled = input.toDoubleOrNull() != null,
                onClick = {
                    input.toDoubleOrNull()?.let {
                        onLog(it)
                        input = ""
                    }
                },
            ) { Text("Log") }
        }
    }
}

private fun MeasurementType.label(): String = name
    .removeSuffix("_KG").removeSuffix("_CM").removeSuffix("_PERCENT").removeSuffix("_KCAL")
    .replace('_', ' ')
    .lowercase()
    .replaceFirstChar(Char::uppercase)
