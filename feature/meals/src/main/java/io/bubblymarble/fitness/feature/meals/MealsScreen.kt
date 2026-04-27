package io.bubblymarble.fitness.feature.meals

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.bubblymarble.fitness.core.data.model.Meal
import io.bubblymarble.fitness.core.data.model.NutritionTotals
import io.bubblymarble.fitness.core.designsystem.components.StatCard
import java.time.format.DateTimeFormatter

@Composable
fun MealsScreen(
    onAddMeal: () -> Unit,
    onEditMeal: (Long) -> Unit,
    viewModel: MealsViewModel = hiltViewModel(),
) {
    val ui by viewModel.ui.collectAsStateWithLifecycle()
    val dateFmt = DateTimeFormatter.ofPattern("EEE, d MMM")

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(onClick = onAddMeal, text = { Text("Log meal") }, icon = {})
        },
    ) { padding ->
        Column(
            Modifier.padding(padding).fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            DayHeader(
                title = ui.date.format(dateFmt),
                onPrev = viewModel::previousDay,
                onNext = viewModel::nextDay,
                onToday = viewModel::today,
            )
            TotalsRow(ui.totals)
            if (ui.meals.isEmpty()) {
                Text("No meals logged yet.", style = MaterialTheme.typography.bodyMedium)
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(ui.meals, key = { it.id }) { meal ->
                        MealCard(
                            meal = meal,
                            onEdit = { onEditMeal(meal.id) },
                            onDelete = { viewModel.deleteMeal(meal.id) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DayHeader(title: String, onPrev: () -> Unit, onNext: () -> Unit, onToday: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        IconButton(onClick = onPrev) { Icon(Icons.Default.ChevronLeft, contentDescription = "Previous day") }
        Text(title, style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
        TextButton(onClick = onToday) { Text("Today") }
        IconButton(onClick = onNext) { Icon(Icons.Default.ChevronRight, contentDescription = "Next day") }
    }
}

@Composable
private fun TotalsRow(t: NutritionTotals) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        StatCard("kcal", "%.0f".format(t.kcal), Modifier.weight(1f))
        StatCard("Protein", "%.0fg".format(t.proteinG), Modifier.weight(1f))
        StatCard("Carbs", "%.0fg".format(t.carbsG), Modifier.weight(1f))
        StatCard("Fat", "%.0fg".format(t.fatG), Modifier.weight(1f))
    }
}

@Composable
private fun MealCard(meal: Meal, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(
        Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(meal.name, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                Text(meal.mealType.name.lowercase().replaceFirstChar(Char::uppercase),
                    style = MaterialTheme.typography.labelMedium)
            }
            Text(
                "${"%.0f".format(meal.nutrition.kcal)} kcal · " +
                    "P ${"%.0f".format(meal.nutrition.proteinG)}g · " +
                    "C ${"%.0f".format(meal.nutrition.carbsG)}g · " +
                    "F ${"%.0f".format(meal.nutrition.fatG)}g",
                style = MaterialTheme.typography.bodySmall,
            )
            if (meal.items.isNotEmpty()) {
                Text(
                    meal.items.joinToString { "${it.ingredient.name} (${"%.0f".format(it.grams)}g)" },
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onEdit) { Text("Edit") }
                IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, contentDescription = "Delete meal") }
            }
        }
    }
}
