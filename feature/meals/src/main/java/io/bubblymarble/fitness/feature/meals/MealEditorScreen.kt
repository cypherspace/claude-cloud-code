package io.bubblymarble.fitness.feature.meals

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.bubblymarble.fitness.core.ai.PhotoFoodSuggestion
import io.bubblymarble.fitness.core.data.model.Ingredient
import io.bubblymarble.fitness.core.data.model.MealType

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MealEditorScreen(
    onSaved: () -> Unit,
    viewModel: MealEditorViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val photoLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        val mime = context.contentResolver.getType(uri) ?: "image/jpeg"
        val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
        if (bytes != null) viewModel.analyzePhoto(bytes, mime)
    }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            if (state.mealId == null) "Log meal" else "Edit meal",
            style = MaterialTheme.typography.headlineSmall,
        )

        OutlinedTextField(
            value = state.name,
            onValueChange = viewModel::setName,
            label = { Text("Name") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )

        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MealType.values().forEach { type ->
                FilterChip(
                    selected = type == state.type,
                    onClick = { viewModel.setType(type) },
                    label = { Text(type.name.lowercase().replaceFirstChar(Char::uppercase)) },
                )
            }
        }

        HorizontalDivider()
        Text("Items", style = MaterialTheme.typography.titleMedium)
        if (state.items.isEmpty()) {
            Text("Search or photo-log to add ingredients.", style = MaterialTheme.typography.bodySmall)
        } else {
            state.items.forEachIndexed { idx, item ->
                ItemRow(
                    item = item,
                    onGramsChange = { viewModel.updateGrams(idx, it) },
                    onRemove = { viewModel.removeItem(idx) },
                )
            }
        }

        HorizontalDivider()
        Text("Search", style = MaterialTheme.typography.titleMedium)
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = viewModel::setSearchQuery,
                label = { Text("Find an ingredient") },
                modifier = Modifier.weight(1f),
                singleLine = true,
            )
            Button(onClick = viewModel::runSearch, enabled = !state.searching) { Text("Find") }
        }
        if (state.searchResults.isNotEmpty()) {
            LazyColumn(modifier = Modifier.height(220.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                items(state.searchResults, key = { it.id }) { ing ->
                    IngredientResultRow(ing) { viewModel.addIngredient(ing, ing.servingGrams) }
                }
            }
        }

        HorizontalDivider()
        Text("Photo log (Gemini)", style = MaterialTheme.typography.titleMedium)
        OutlinedButton(
            onClick = { photoLauncher.launch("image/*") },
            enabled = !state.analyzingPhoto,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(if (state.analyzingPhoto) "Analyzing…" else "Pick a photo")
        }
        if (state.photoSuggestions.isNotEmpty()) {
            LazyColumn(modifier = Modifier.height(220.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                itemsIndexed(state.photoSuggestions, key = { _, s -> s.name + s.grams }) { _, s ->
                    SuggestionRow(s) { viewModel.acceptSuggestion(s) }
                }
            }
        }

        HorizontalDivider()
        Button(
            onClick = { viewModel.save(onSaved) },
            enabled = state.items.isNotEmpty() && !state.saving,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(if (state.saving) "Saving…" else "Save meal")
        }
    }
}

@Composable
private fun ItemRow(item: EditorItem, onGramsChange: (Double) -> Unit, onRemove: () -> Unit) {
    Card(elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
        Row(
            Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(item.ingredient.name, style = MaterialTheme.typography.bodyLarge)
                Text(
                    "%.0f kcal · P %.0fg · C %.0fg · F %.0fg".format(
                        item.ingredient.kcalPer100g * item.grams / 100,
                        item.ingredient.proteinPer100g * item.grams / 100,
                        item.ingredient.carbsPer100g * item.grams / 100,
                        item.ingredient.fatPer100g * item.grams / 100,
                    ),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            val gramsText = remember(item.grams) { "%.0f".format(item.grams) }
            OutlinedTextField(
                value = gramsText,
                onValueChange = { v ->
                    v.filter { c -> c.isDigit() || c == '.' }.toDoubleOrNull()?.let(onGramsChange)
                },
                label = { Text("g") },
                modifier = Modifier.width(96.dp).padding(horizontal = 8.dp),
                singleLine = true,
            )
            IconButton(onClick = onRemove) { Icon(Icons.Default.Close, contentDescription = "Remove") }
        }
    }
}

@Composable
private fun IngredientResultRow(ingredient: Ingredient, onAdd: () -> Unit) {
    Card(elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
        Row(
            Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(ingredient.name, style = MaterialTheme.typography.bodyLarge)
                Text(
                    "%.0f kcal/100g · P %.1fg · C %.1fg · F %.1fg".format(
                        ingredient.kcalPer100g,
                        ingredient.proteinPer100g,
                        ingredient.carbsPer100g,
                        ingredient.fatPer100g,
                    ),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            IconButton(onClick = onAdd) { Icon(Icons.Default.Add, contentDescription = "Add") }
        }
    }
}

@Composable
private fun SuggestionRow(s: PhotoFoodSuggestion, onAccept: () -> Unit) {
    Card(elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
        Row(
            Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(s.name, style = MaterialTheme.typography.bodyLarge)
                Text(
                    "%.0fg · %.0f kcal · P %.0fg · C %.0fg · F %.0fg".format(s.grams, s.kcal, s.proteinG, s.carbsG, s.fatG),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            IconButton(onClick = onAccept) { Icon(Icons.Default.Add, contentDescription = "Accept") }
        }
    }
}

