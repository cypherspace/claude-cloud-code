package io.bubblymarble.fitness.core.data.repository

import io.bubblymarble.fitness.core.data.db.dao.ExerciseDao
import io.bubblymarble.fitness.core.data.model.Exercise
import io.bubblymarble.fitness.core.data.model.toDomain
import io.bubblymarble.fitness.core.data.model.toEntity
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class ExerciseRepository @Inject constructor(
    private val dao: ExerciseDao,
) {
    fun observeAll(): Flow<List<Exercise>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    fun search(query: String, muscle: String, equipment: String): Flow<List<Exercise>> =
        dao.search(query, muscle, equipment).map { list -> list.map { it.toDomain() } }

    suspend fun byIds(ids: List<String>): List<Exercise> =
        dao.byIds(ids).map { it.toDomain() }

    suspend fun byId(id: String): Exercise? = dao.byId(id)?.toDomain()

    suspend fun count(): Int = dao.count()

    suspend fun upsert(exercises: List<Exercise>) {
        dao.upsertAll(exercises.map { it.toEntity() })
    }
}
