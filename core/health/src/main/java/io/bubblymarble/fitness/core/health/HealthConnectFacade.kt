package io.bubblymarble.fitness.core.health

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.BasalMetabolicRateRecord
import androidx.health.connect.client.records.BodyFatRecord
import androidx.health.connect.client.records.BodyWaterMassRecord
import androidx.health.connect.client.records.BoneMassRecord
import androidx.health.connect.client.records.DistanceRecord
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.HeightRecord
import androidx.health.connect.client.records.LeanBodyMassRecord
import androidx.health.connect.client.records.OxygenSaturationRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.TotalCaloriesBurnedRecord
import androidx.health.connect.client.records.WeightRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import dagger.hilt.android.qualifiers.ApplicationContext
import io.bubblymarble.fitness.core.data.db.entities.BodyMeasurementEntity
import io.bubblymarble.fitness.core.data.db.entities.HealthSampleEntity
import io.bubblymarble.fitness.core.data.model.MeasurementType
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Wraps [HealthConnectClient] so the rest of the app does not depend on the AndroidX type directly.
 * The client is null when Health Connect is not installed; callers should treat that as a soft
 * failure (read empty, write no-op).
 */
@Singleton
class HealthConnectFacade @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val client: HealthConnectClient? by lazy {
        runCatching { HealthConnectClient.getOrCreate(context) }.getOrNull()
    }

    fun isAvailable(): Boolean =
        HealthConnectClient.getSdkStatus(context) == HealthConnectClient.SDK_AVAILABLE

    val readPermissions: Set<String> = setOf(
        HealthPermission.getReadPermission(HeartRateRecord::class),
        HealthPermission.getReadPermission(OxygenSaturationRecord::class),
        HealthPermission.getReadPermission(StepsRecord::class),
        HealthPermission.getReadPermission(DistanceRecord::class),
        HealthPermission.getReadPermission(TotalCaloriesBurnedRecord::class),
        HealthPermission.getReadPermission(ExerciseSessionRecord::class),
        HealthPermission.getReadPermission(WeightRecord::class),
        HealthPermission.getReadPermission(BodyFatRecord::class),
        HealthPermission.getReadPermission(LeanBodyMassRecord::class),
        HealthPermission.getReadPermission(BoneMassRecord::class),
        HealthPermission.getReadPermission(BodyWaterMassRecord::class),
        HealthPermission.getReadPermission(HeightRecord::class),
        HealthPermission.getReadPermission(BasalMetabolicRateRecord::class),
    )

    val writePermissions: Set<String> = setOf(
        HealthPermission.getWritePermission(HeartRateRecord::class),
        HealthPermission.getWritePermission(ExerciseSessionRecord::class),
    )

    val allPermissions: Set<String> = readPermissions + writePermissions

    suspend fun grantedPermissions(): Set<String> =
        client?.permissionController?.getGrantedPermissions().orEmpty()

    suspend fun readSessionSamples(
        sessionId: Long,
        startedAt: Instant,
        endedAt: Instant,
    ): List<HealthSampleEntity> {
        val c = client ?: return emptyList()
        val range = TimeRangeFilter.between(startedAt, endedAt)
        val out = mutableListOf<HealthSampleEntity>()
        runCatching {
            c.readRecords(ReadRecordsRequest(HeartRateRecord::class, range)).records.forEach { rec ->
                rec.samples.forEach { s ->
                    out += HealthSampleEntity(
                        type = HealthSampleType.HEART_RATE,
                        timestampEpochMs = s.time.toEpochMilli(),
                        value = s.beatsPerMinute.toDouble(),
                        unit = "bpm",
                        source = rec.metadata.dataOrigin.packageName,
                        sessionId = sessionId,
                    )
                }
            }
        }
        runCatching {
            c.readRecords(ReadRecordsRequest(OxygenSaturationRecord::class, range)).records.forEach { rec ->
                out += HealthSampleEntity(
                    type = HealthSampleType.SPO2,
                    timestampEpochMs = rec.time.toEpochMilli(),
                    value = rec.percentage.value,
                    unit = "%",
                    source = rec.metadata.dataOrigin.packageName,
                    sessionId = sessionId,
                )
            }
        }
        runCatching {
            c.readRecords(ReadRecordsRequest(DistanceRecord::class, range)).records.forEach { rec ->
                out += HealthSampleEntity(
                    type = HealthSampleType.DISTANCE,
                    timestampEpochMs = rec.endTime.toEpochMilli(),
                    value = rec.distance.inMeters,
                    unit = "m",
                    source = rec.metadata.dataOrigin.packageName,
                    sessionId = sessionId,
                )
            }
        }
        runCatching {
            c.readRecords(ReadRecordsRequest(TotalCaloriesBurnedRecord::class, range)).records.forEach { rec ->
                out += HealthSampleEntity(
                    type = HealthSampleType.CALORIES,
                    timestampEpochMs = rec.endTime.toEpochMilli(),
                    value = rec.energy.inKilocalories,
                    unit = "kcal",
                    source = rec.metadata.dataOrigin.packageName,
                    sessionId = sessionId,
                )
            }
        }
        runCatching {
            c.readRecords(ReadRecordsRequest(StepsRecord::class, range)).records.forEach { rec ->
                out += HealthSampleEntity(
                    type = HealthSampleType.STEPS,
                    timestampEpochMs = rec.endTime.toEpochMilli(),
                    value = rec.count.toDouble(),
                    unit = "steps",
                    source = rec.metadata.dataOrigin.packageName,
                    sessionId = sessionId,
                )
            }
        }
        return out
    }

    /**
     * Reads body-composition rows from Health Connect between [from] and [until]. Each row is a
     * separate measurement event. Renpho and other smart-scale apps publish here, so this is the
     * single integration point Bubblymarble needs for scale data. Returns rows ready to hand to
     * [BodyMeasurementRepository.importFromSource].
     */
    suspend fun readBodyMeasurements(from: Instant, until: Instant): List<BodyMeasurementEntity> {
        val c = client ?: return emptyList()
        val range = TimeRangeFilter.between(from, until)
        val out = mutableListOf<BodyMeasurementEntity>()

        runCatching {
            c.readRecords(ReadRecordsRequest(WeightRecord::class, range)).records.forEach { rec ->
                out += entry(MeasurementType.WEIGHT_KG, rec.time, rec.weight.inKilograms, rec.metadata.dataOrigin.packageName)
            }
        }
        runCatching {
            c.readRecords(ReadRecordsRequest(BodyFatRecord::class, range)).records.forEach { rec ->
                out += entry(MeasurementType.BODY_FAT_PERCENT, rec.time, rec.percentage.value, rec.metadata.dataOrigin.packageName)
            }
        }
        runCatching {
            c.readRecords(ReadRecordsRequest(LeanBodyMassRecord::class, range)).records.forEach { rec ->
                out += entry(MeasurementType.LEAN_MASS_KG, rec.time, rec.mass.inKilograms, rec.metadata.dataOrigin.packageName)
            }
        }
        runCatching {
            c.readRecords(ReadRecordsRequest(BoneMassRecord::class, range)).records.forEach { rec ->
                out += entry(MeasurementType.BONE_MASS_KG, rec.time, rec.mass.inKilograms, rec.metadata.dataOrigin.packageName)
            }
        }
        runCatching {
            c.readRecords(ReadRecordsRequest(BodyWaterMassRecord::class, range)).records.forEach { rec ->
                out += entry(MeasurementType.BODY_WATER_KG, rec.time, rec.mass.inKilograms, rec.metadata.dataOrigin.packageName)
            }
        }
        runCatching {
            c.readRecords(ReadRecordsRequest(HeightRecord::class, range)).records.forEach { rec ->
                out += entry(MeasurementType.HEIGHT_CM, rec.time, rec.height.inMeters * 100.0, rec.metadata.dataOrigin.packageName)
            }
        }
        runCatching {
            c.readRecords(ReadRecordsRequest(BasalMetabolicRateRecord::class, range)).records.forEach { rec ->
                out += entry(MeasurementType.BMR_KCAL, rec.time, rec.basalMetabolicRate.inKilocaloriesPerDay, rec.metadata.dataOrigin.packageName)
            }
        }
        return out
    }

    private fun entry(type: MeasurementType, time: Instant, value: Double, source: String): BodyMeasurementEntity =
        BodyMeasurementEntity(
            type = type.name,
            timestampEpochMs = time.toEpochMilli(),
            value = value,
            unit = type.unit,
            source = "healthconnect:$source",
        )

    suspend fun writeExerciseSession(
        title: String,
        startedAt: Instant,
        endedAt: Instant,
        notes: String?,
    ): String? {
        val c = client ?: return null
        val record = ExerciseSessionRecord(
            startTime = startedAt,
            startZoneOffset = null,
            endTime = endedAt,
            endZoneOffset = null,
            exerciseType = ExerciseSessionRecord.EXERCISE_TYPE_OTHER_WORKOUT,
            title = title,
            notes = notes,
        )
        return runCatching {
            c.insertRecords(listOf(record)).recordIdsList.firstOrNull()
        }.getOrNull()
    }
}
