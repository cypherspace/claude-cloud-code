package io.bubblymarble.fitness.core.data.repository

import io.bubblymarble.fitness.core.common.TimeSource
import io.bubblymarble.fitness.core.data.db.dao.BodyMeasurementDao
import io.bubblymarble.fitness.core.data.db.entities.BodyMeasurementEntity
import io.bubblymarble.fitness.core.data.model.BodyMeasurement
import io.bubblymarble.fitness.core.data.model.MeasurementType
import io.bubblymarble.fitness.core.data.model.toDomain
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class BodyMeasurementRepository @Inject constructor(
    private val dao: BodyMeasurementDao,
    private val time: TimeSource,
) {
    fun observeAll(): Flow<List<BodyMeasurement>> =
        dao.observeAll().map { list -> list.mapNotNull { it.toDomain() } }

    fun observeByType(type: MeasurementType): Flow<List<BodyMeasurement>> =
        dao.observeByType(type.name).map { list -> list.mapNotNull { it.toDomain() } }

    suspend fun latest(type: MeasurementType): BodyMeasurement? =
        dao.latestByType(type.name)?.toDomain()

    suspend fun seriesSince(type: MeasurementType, since: Instant): List<BodyMeasurement> =
        dao.seriesSince(type.name, since.toEpochMilli()).mapNotNull { it.toDomain() }

    suspend fun record(type: MeasurementType, value: Double, timestamp: Instant? = null, source: String? = "manual"): Long {
        val now = timestamp ?: time.now()
        return dao.insert(
            BodyMeasurementEntity(
                type = type.name,
                timestampEpochMs = now.toEpochMilli(),
                value = value,
                unit = type.unit,
                source = source,
            )
        )
    }

    /**
     * Stores rows from a sync source, deduping against existing (type, timestamp, source)
     * triples so re-running a sync is a no-op. Returns the count of newly inserted rows.
     */
    suspend fun importFromSource(rows: List<BodyMeasurementEntity>): Int {
        val toInsert = rows.filter { row ->
            !dao.exists(row.type, row.timestampEpochMs, row.source.orEmpty())
        }
        if (toInsert.isNotEmpty()) dao.insertAll(toInsert)
        return toInsert.size
    }

    suspend fun delete(id: Long) = dao.delete(id)
}
