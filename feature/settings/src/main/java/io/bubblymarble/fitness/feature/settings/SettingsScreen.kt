package io.bubblymarble.fitness.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.bubblymarble.fitness.core.designsystem.components.SectionHeader

@Composable
fun SettingsScreen(viewModel: SettingsViewModel = hiltViewModel()) {
    val ui by viewModel.ui.collectAsStateWithLifecycle()

    var apiKey by remember { mutableStateOf("") }
    var hour by remember { mutableStateOf(ui.reminderHour.toString()) }
    var minute by remember { mutableStateOf(ui.reminderMinute.toString().padStart(2, '0')) }

    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {

        SectionHeader("Gemini API key")
        Text(if (ui.maskedKey.isBlank()) "Not set" else ui.maskedKey, style = MaterialTheme.typography.bodySmall)
        OutlinedTextField(
            value = apiKey,
            onValueChange = { apiKey = it.trim() },
            label = { Text("Paste new key") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = { viewModel.setApiKey(apiKey); apiKey = "" },
                enabled = apiKey.isNotBlank(),
                modifier = Modifier.weight(1f),
            ) { Text("Save key") }
            OutlinedButton(
                onClick = { viewModel.setApiKey("") },
                modifier = Modifier.weight(1f),
            ) { Text("Clear") }
        }

        SectionHeader("Daily reminder")
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = hour,
                onValueChange = { hour = it.filter(Char::isDigit).take(2) },
                label = { Text("Hour (0-23)") },
                modifier = Modifier.weight(1f),
                singleLine = true,
            )
            OutlinedTextField(
                value = minute,
                onValueChange = { minute = it.filter(Char::isDigit).take(2) },
                label = { Text("Minute") },
                modifier = Modifier.weight(1f),
                singleLine = true,
            )
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = {
                    viewModel.setReminder(
                        hour.toIntOrNull() ?: 18,
                        minute.toIntOrNull() ?: 0,
                    )
                },
                modifier = Modifier.weight(1f),
            ) { Text("Schedule") }
            OutlinedButton(
                onClick = { viewModel.cancelReminders() },
                modifier = Modifier.weight(1f),
            ) { Text("Cancel reminders") }
        }
    }
}
