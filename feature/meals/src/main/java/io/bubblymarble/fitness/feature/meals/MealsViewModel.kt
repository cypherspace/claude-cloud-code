package io.bubblymarble.fitness.feature.meals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.bubblymarble.fitness.core.common.TimeSource
import io.bubblymarble.fitness.core.data.model.Meal
import io.bubblymarble.fitness.core.data.model.NutritionTotals
import io.bubblymarble.fitness.core.data.repository.MealRepository
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class DayUi(
    val date: LocalDate,
    val meals: List<Meal>,
    val totals: NutritionTotals,
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class MealsViewModel @Inject constructor(
    private val meals: MealRepository,
    private val time: TimeSource,
) : ViewModel() {

    private val day = MutableStateFlow(time.today())

    val ui: StateFlow<DayUi> = day
        .flatMapLatest { d ->
            meals.observeForDay(d).map { mealList ->
                DayUi(
                    date = d,
                    meals = mealList,
                    totals = mealList.fold(NutritionTotals.ZERO) { acc, m -> acc + m.nutrition },
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DayUi(time.today(), emptyList(), NutritionTotals.ZERO))

    fun previousDay() { day.value = day.value.minusDays(1) }
    fun nextDay() { day.value = day.value.plusDays(1) }
    fun today() { day.value = time.today() }

    fun deleteMeal(id: Long) {
        viewModelScope.launch { meals.delete(id) }
    }
}
