package io.bubblymarble.fitness.feature.workouts.runner

import io.bubblymarble.fitness.core.data.model.Exercise
import io.bubblymarble.fitness.core.data.model.TemplateItem

enum class RunnerPhase { LOADING, EXERCISE, REST, COMPLETE, ERROR }

data class ResolvedItem(val template: TemplateItem, val exercise: Exercise)

data class RunnerState(
    val phase: RunnerPhase = RunnerPhase.LOADING,
    val templateName: String = "",
    val items: List<ResolvedItem> = emptyList(),
    val currentItemIndex: Int = 0,
    val currentSet: Int = 1,
    val totalSetsForCurrent: Int = 0,
    val secondsLeft: Int = 0,
    val errorMessage: String? = null,
) {
    val currentItem: ResolvedItem?
        get() = items.getOrNull(currentItemIndex)
}
