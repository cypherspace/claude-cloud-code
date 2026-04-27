package io.bubblymarble.fitness.core.data.repository

import io.bubblymarble.fitness.core.data.db.dao.WorkoutTemplateDao
import io.bubblymarble.fitness.core.data.model.WorkoutTemplate
import io.bubblymarble.fitness.core.data.model.toDomain
import io.bubblymarble.fitness.core.data.model.toEntity
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class WorkoutTemplateRepository @Inject constructor(
    private val dao: WorkoutTemplateDao,
) {
    fun observeAll(): Flow<List<WorkoutTemplate>> =
        dao.observeTemplates().map { list ->
            list.map { tpl ->
                val items = dao.exercisesForTemplate(tpl.id)
                tpl.toDomain(items)
            }
        }

    suspend fun byId(id: Long): WorkoutTemplate? {
        val tpl = dao.templateById(id) ?: return null
        return tpl.toDomain(dao.exercisesForTemplate(id))
    }

    suspend fun save(template: WorkoutTemplate): Long {
        val id = dao.replaceTemplate(
            template.toEntity(),
            template.items.map { it.toEntity(template.id) },
        )
        return id
    }

    suspend fun delete(id: Long) = dao.delete(id)
}
