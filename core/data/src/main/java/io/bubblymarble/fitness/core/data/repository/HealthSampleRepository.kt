package io.bubblymarble.fitness.core.data.repository

import io.bubblymarble.fitness.core.data.db.dao.HealthSampleDao
import io.bubblymarble.fitness.core.data.db.entities.HealthSampleEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HealthSampleRepository @Inject constructor(
    private val dao: HealthSampleDao,
) {
    suspend fun storeAll(samples: List<HealthSampleEntity>) = dao.insertAll(samples)
    suspend fun samplesForSession(sessionId: Long) = dao.samplesForSession(sessionId)
    suspend fun samplesIn(type: String, startEpochMs: Long, endEpochMs: Long) =
        dao.samplesIn(type, startEpochMs, endEpochMs)
}
