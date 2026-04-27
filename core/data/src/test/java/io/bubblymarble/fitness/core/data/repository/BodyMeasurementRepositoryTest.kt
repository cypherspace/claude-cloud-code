package io.bubblymarble.fitness.core.data.repository

import com.google.common.truth.Truth.assertThat
import io.bubblymarble.fitness.core.common.SystemTimeSource
import io.bubblymarble.fitness.core.data.db.dao.BodyMeasurementDao
import io.bubblymarble.fitness.core.data.db.entities.BodyMeasurementEntity
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import org.junit.Test

class BodyMeasurementRepositoryTest {

    private val now = Instant.parse("2026-04-27T12:00:00Z")
    private val timeSource = SystemTimeSource(Clock.fixed(now, ZoneOffset.UTC))
    private val dao = FakeDao()
    private val repo = BodyMeasurementRepository(dao, timeSource)

    @Test fun importFromSource_dedupesByTypeTimestampSource() = runTest {
        val a = entity("WEIGHT_KG", 1000L, 75.0, "healthconnect:com.renpho")
        val b = entity("WEIGHT_KG", 2000L, 75.5, "healthconnect:com.renpho")
        val c = entity("WEIGHT_KG", 1000L, 75.0, "healthconnect:com.renpho") // dupe of a

        val first = repo.importFromSource(listOf(a, b))
        val second = repo.importFromSource(listOf(c, b))
        assertThat(first).isEqualTo(2)
        assertThat(second).isEqualTo(0)
        assertThat(dao.rows).hasSize(2)
    }

    @Test fun differentSources_areNotConsideredDupes() = runTest {
        val renpho = entity("WEIGHT_KG", 1000L, 75.0, "healthconnect:com.renpho")
        val manual = entity("WEIGHT_KG", 1000L, 75.0, "manual")
        val inserted = repo.importFromSource(listOf(renpho, manual))
        assertThat(inserted).isEqualTo(2)
    }

    private fun entity(type: String, ts: Long, value: Double, source: String) =
        BodyMeasurementEntity(
            type = type,
            timestampEpochMs = ts,
            value = value,
            unit = "kg",
            source = source,
        )

    private class FakeDao : BodyMeasurementDao {
        val rows = mutableListOf<BodyMeasurementEntity>()
        private val flow = MutableStateFlow<List<BodyMeasurementEntity>>(emptyList())

        override suspend fun insertAll(entries: List<BodyMeasurementEntity>) {
            rows += entries
            flow.value = rows.toList()
        }

        override suspend fun insert(entry: BodyMeasurementEntity): Long {
            rows += entry
            flow.value = rows.toList()
            return rows.size.toLong()
        }

        override fun observeAll(): Flow<List<BodyMeasurementEntity>> = flow

        override fun observeByType(type: String): Flow<List<BodyMeasurementEntity>> =
            flow.map { it.filter { row -> row.type == type } }

        override suspend fun latestByType(type: String): BodyMeasurementEntity? =
            rows.filter { it.type == type }.maxByOrNull { it.timestampEpochMs }

        override suspend fun seriesSince(type: String, sinceEpochMs: Long): List<BodyMeasurementEntity> =
            rows.filter { it.type == type && it.timestampEpochMs >= sinceEpochMs }
                .sortedBy { it.timestampEpochMs }

        override suspend fun delete(id: Long) {
            rows.removeAll { it.id == id }
            flow.value = rows.toList()
        }

        override suspend fun exists(type: String, timestampEpochMs: Long, source: String): Boolean =
            rows.any { it.type == type && it.timestampEpochMs == timestampEpochMs && (it.source ?: "") == source }
    }
}
