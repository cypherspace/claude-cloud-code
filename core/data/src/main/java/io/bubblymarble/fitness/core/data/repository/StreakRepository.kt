package io.bubblymarble.fitness.core.data.repository

import io.bubblymarble.fitness.core.data.db.dao.StreakDao
import io.bubblymarble.fitness.core.data.model.StreakRule
import io.bubblymarble.fitness.core.data.model.StreakState
import io.bubblymarble.fitness.core.data.model.toDomain
import io.bubblymarble.fitness.core.data.model.toEntity
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class StreakRepository @Inject constructor(
    private val dao: StreakDao,
) {
    fun observe(): Flow<StreakState> =
        dao.observe().map {
            it?.toDomain() ?: StreakState(null, 0, 0, StreakRule.DAILY, 3)
        }

    suspend fun get(): StreakState =
        dao.get()?.toDomain() ?: StreakState(null, 0, 0, StreakRule.DAILY, 3)

    suspend fun save(state: StreakState) {
        dao.upsert(state.toEntity())
    }
}
