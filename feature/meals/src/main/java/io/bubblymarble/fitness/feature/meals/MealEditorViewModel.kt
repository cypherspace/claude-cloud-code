package io.bubblymarble.fitness.feature.meals

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.bubblymarble.fitness.core.ai.FoodPhotoRecognizer
import io.bubblymarble.fitness.core.ai.PhotoFoodSuggestion
import io.bubblymarble.fitness.core.data.model.Ingredient
import io.bubblymarble.fitness.core.data.model.MealType
import io.bubblymarble.fitness.core.data.repository.IngredientRepository
import io.bubblymarble.fitness.core.data.repository.MealRepository
import io.bubblymarble.fitness.core.foodapi.OpenFoodFactsClient
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EditorItem(val ingredient: Ingredient, val grams: Double)

data class EditorState(
    val mealId: Long? = null,
    val name: String = "",
    val type: MealType = MealType.LUNCH,
    val items: List<EditorItem> = emptyList(),
    val searchQuery: String = "",
    val searchResults: List<Ingredient> = emptyList(),
    val searching: Boolean = false,
    val photoSuggestions: List<PhotoFoodSuggestion> = emptyList(),
    val analyzingPhoto: Boolean = false,
    val saving: Boolean = false,
)

@HiltViewModel
class MealEditorViewModel @Inject constructor(
    savedState: SavedStateHandle,
    private val meals: MealRepository,
    private val ingredients: IngredientRepository,
    private val foodApi: OpenFoodFactsClient,
    private val photoRecognizer: FoodPhotoRecognizer,
) : ViewModel() {

    private val mealIdArg: Long? = savedState.get<String>("mealId")?.toLongOrNull()?.takeIf { it > 0 }

    private val _state = MutableStateFlow(EditorState(mealId = mealIdArg))
    val state: StateFlow<EditorState> = _state.asStateFlow()

    init {
        if (mealIdArg != null) loadExisting(mealIdArg)
    }

    private fun loadExisting(id: Long) {
        viewModelScope.launch {
            val meal = meals.byId(id) ?: return@launch
            _state.update {
                it.copy(
                    mealId = meal.id,
                    name = meal.name,
                    type = meal.mealType,
                    items = meal.items.map { mi -> EditorItem(mi.ingredient, mi.grams) },
                )
            }
        }
    }

    fun setName(value: String) = _state.update { it.copy(name = value) }
    fun setType(value: MealType) = _state.update { it.copy(type = value) }

    fun setSearchQuery(value: String) {
        _state.update { it.copy(searchQuery = value) }
        if (value.isBlank()) {
            _state.update { it.copy(searchResults = emptyList(), searching = false) }
            return
        }
    }

    fun runSearch() {
        val q = _state.value.searchQuery.trim()
        if (q.isBlank()) return
        _state.update { it.copy(searching = true) }
        viewModelScope.launch {
            val local = ingredients.search(q)
            val remote = if (local.size < 5) foodApi.search(q, pageSize = 10) else emptyList()
            // cache remote results so subsequent local searches find them
            if (remote.isNotEmpty()) ingredients.upsertAll(remote)
            val combined = (local + remote).distinctBy { it.id }
            _state.update { it.copy(searching = false, searchResults = combined) }
        }
    }

    fun lookupBarcode(barcode: String) {
        viewModelScope.launch {
            val local = ingredients.byBarcode(barcode)
            val ing = local ?: foodApi.lookupByBarcode(barcode)?.also { ingredients.upsert(it) }
            if (ing != null) addIngredient(ing, ing.servingGrams)
        }
    }

    fun addIngredient(ingredient: Ingredient, grams: Double) {
        _state.update { state ->
            state.copy(items = state.items + EditorItem(ingredient, grams.coerceAtLeast(1.0)))
        }
    }

    fun updateGrams(index: Int, grams: Double) {
        _state.update { state ->
            val updated = state.items.toMutableList().also {
                if (index in it.indices) it[index] = it[index].copy(grams = grams.coerceAtLeast(1.0))
            }
            state.copy(items = updated)
        }
    }

    fun removeItem(index: Int) {
        _state.update { state ->
            val updated = state.items.toMutableList().also {
                if (index in it.indices) it.removeAt(index)
            }
            state.copy(items = updated)
        }
    }

    fun analyzePhoto(bytes: ByteArray, mimeType: String) {
        _state.update { it.copy(analyzingPhoto = true, photoSuggestions = emptyList()) }
        viewModelScope.launch {
            val suggestions = photoRecognizer.recognize(bytes, mimeType)
            _state.update { it.copy(analyzingPhoto = false, photoSuggestions = suggestions) }
        }
    }

    fun acceptSuggestion(suggestion: PhotoFoodSuggestion) {
        viewModelScope.launch {
            val ingredient = ingredientFromSuggestion(suggestion)
            ingredients.upsert(ingredient)
            addIngredient(ingredient, suggestion.grams)
            _state.update { state ->
                state.copy(photoSuggestions = state.photoSuggestions - suggestion)
            }
        }
    }

    fun save(onDone: () -> Unit) {
        val s = _state.value
        if (s.items.isEmpty()) return
        _state.update { it.copy(saving = true) }
        viewModelScope.launch {
            meals.saveMeal(
                id = s.mealId,
                name = s.name.ifBlank { defaultMealName(s.type) },
                type = s.type,
                eatenAt = null,
                notes = null,
                photoUri = null,
                items = s.items.map { it.ingredient.id to it.grams },
            )
            _state.update { it.copy(saving = false) }
            onDone()
        }
    }

    private fun defaultMealName(type: MealType) = when (type) {
        MealType.BREAKFAST -> "Breakfast"
        MealType.LUNCH -> "Lunch"
        MealType.DINNER -> "Dinner"
        MealType.SNACK -> "Snack"
    }

    private fun ingredientFromSuggestion(suggestion: PhotoFoodSuggestion): Ingredient {
        val factor = if (suggestion.grams > 0) 100.0 / suggestion.grams else 1.0
        val id = "ai:" + suggestion.name.lowercase().replace(' ', '_').take(40) + ":" +
            ((suggestion.kcal * 100).toInt())
        return Ingredient(
            id = id,
            name = suggestion.name,
            brand = null,
            barcode = null,
            source = "gemini-vision",
            servingGrams = suggestion.grams,
            kcalPer100g = suggestion.kcal * factor,
            proteinPer100g = suggestion.proteinG * factor,
            carbsPer100g = suggestion.carbsG * factor,
            fatPer100g = suggestion.fatG * factor,
            fibrePer100g = null,
        )
    }
}
