package io.bubblymarble.fitness.feature.plans.editor

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.bubblymarble.fitness.core.data.model.GoalType
import io.bubblymarble.fitness.core.data.model.TemplateItem
import io.bubblymarble.fitness.core.data.model.WorkoutTemplate
import io.bubblymarble.fitness.core.data.repository.ExerciseRepository
import io.bubblymarble.fitness.core.data.repository.WorkoutTemplateRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EditableItem(
    val exerciseId: String,
    val exerciseName: String,
    val sets: Int,
    val reps: Int?,
    val durationSec: Int?,
    val restSec: Int,
    val weightKg: Double?,
)

data class EditorUi(
    val templateId: Long? = null,
    val name: String = "",
    val description: String = "",
    val items: List<EditableItem> = emptyList(),
    val loading: Boolean = true,
    val saving: Boolean = false,
    val notFound: Boolean = false,
)

@HiltViewModel
class PlanEditorViewModel @Inject constructor(
    savedState: SavedStateHandle,
    private val templates: WorkoutTemplateRepository,
    private val exercises: ExerciseRepository,
) : ViewModel() {

    private val templateIdArg: Long = savedState.get<String>("templateId")?.toLongOrNull() ?: 0L

    private val _ui = MutableStateFlow(EditorUi())
    val ui: StateFlow<EditorUi> = _ui.asStateFlow()

    init {
        viewModelScope.launch {
            val tpl = templates.byId(templateIdArg)
            if (tpl == null) {
                _ui.update { it.copy(loading = false, notFound = true) }
                return@launch
            }
            val ex = exercises.byIds(tpl.items.map { it.exerciseId }).associateBy { it.id }
            _ui.update {
                EditorUi(
                    templateId = tpl.id,
                    name = tpl.name,
                    description = tpl.description,
                    loading = false,
                    items = tpl.items.sortedBy { ti -> ti.orderIndex }.map { ti ->
                        EditableItem(
                            exerciseId = ti.exerciseId,
                            exerciseName = ex[ti.exerciseId]?.name ?: ti.exerciseId,
                            sets = ti.targetSets,
                            reps = ti.targetReps,
                            durationSec = ti.targetDurationSec,
                            restSec = ti.targetRestSec,
                            weightKg = ti.targetWeightKg,
                        )
                    },
                )
            }
        }
    }

    fun setSets(index: Int, value: Int) = updateItem(index) { it.copy(sets = value.coerceIn(1, 20)) }
    fun setReps(index: Int, value: Int?) = updateItem(index) { it.copy(reps = value) }
    fun setRestSec(index: Int, value: Int) = updateItem(index) { it.copy(restSec = value.coerceAtLeast(0)) }
    fun setWeightKg(index: Int, value: Double?) = updateItem(index) { it.copy(weightKg = value) }
    fun setDurationSec(index: Int, value: Int?) = updateItem(index) { it.copy(durationSec = value) }

    fun removeItem(index: Int) {
        _ui.update { state ->
            state.copy(items = state.items.toMutableList().also { if (index in it.indices) it.removeAt(index) })
        }
    }

    private fun updateItem(index: Int, transform: (EditableItem) -> EditableItem) {
        _ui.update { state ->
            val updated = state.items.toMutableList()
            if (index in updated.indices) updated[index] = transform(updated[index])
            state.copy(items = updated)
        }
    }

    fun save(onDone: () -> Unit) {
        val s = _ui.value
        if (s.templateId == null || s.saving) return
        _ui.update { it.copy(saving = true) }
        viewModelScope.launch {
            val original = templates.byId(s.templateId) ?: return@launch
            templates.save(
                WorkoutTemplate(
                    id = original.id,
                    name = s.name.ifBlank { original.name },
                    description = s.description,
                    goal = original.goal.let { runCatching { GoalType.valueOf(it.name) }.getOrDefault(it) },
                    generatedByAi = original.generatedByAi,
                    createdAt = original.createdAt,
                    items = s.items.mapIndexed { idx, item ->
                        TemplateItem(
                            exerciseId = item.exerciseId,
                            orderIndex = idx,
                            targetSets = item.sets,
                            targetReps = item.reps,
                            targetDurationSec = item.durationSec,
                            targetRestSec = item.restSec,
                            targetWeightKg = item.weightKg,
                            notes = null,
                        )
                    },
                ),
            )
            _ui.update { it.copy(saving = false) }
            onDone()
        }
    }
}
