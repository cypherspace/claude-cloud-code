package io.bubblymarble.fitness.core.data.model

import io.bubblymarble.fitness.core.data.db.entities.BodyMeasurementEntity
import java.time.Instant

/**
 * The closed set of measurement types Bubblymarble understands. New types must be added
 * here so the manual-entry UI and Health Connect sync stay in sync.
 *
 * [groupedAs] determines which section of the screen the type appears in. [unit] is what we
 * persist to Room; presentation may format differently.
 */
enum class MeasurementType(val unit: String, val groupedAs: MeasurementGroup) {
    WEIGHT_KG("kg", MeasurementGroup.BODY_COMP),
    BODY_FAT_PERCENT("%", MeasurementGroup.BODY_COMP),
    LEAN_MASS_KG("kg", MeasurementGroup.BODY_COMP),
    BODY_WATER_KG("kg", MeasurementGroup.BODY_COMP),
    BONE_MASS_KG("kg", MeasurementGroup.BODY_COMP),
    BMR_KCAL("kcal/day", MeasurementGroup.BODY_COMP),
    HEIGHT_CM("cm", MeasurementGroup.BODY_COMP),
    NECK_CM("cm", MeasurementGroup.TAPE),
    SHOULDERS_CM("cm", MeasurementGroup.TAPE),
    CHEST_CM("cm", MeasurementGroup.TAPE),
    WAIST_CM("cm", MeasurementGroup.TAPE),
    HIPS_CM("cm", MeasurementGroup.TAPE),
    LEFT_BICEP_CM("cm", MeasurementGroup.TAPE),
    RIGHT_BICEP_CM("cm", MeasurementGroup.TAPE),
    LEFT_THIGH_CM("cm", MeasurementGroup.TAPE),
    RIGHT_THIGH_CM("cm", MeasurementGroup.TAPE),
    LEFT_CALF_CM("cm", MeasurementGroup.TAPE),
    RIGHT_CALF_CM("cm", MeasurementGroup.TAPE);

    companion object {
        fun fromName(value: String): MeasurementType? = runCatching { valueOf(value) }.getOrNull()
    }
}

enum class MeasurementGroup { BODY_COMP, TAPE }

data class BodyMeasurement(
    val id: Long,
    val type: MeasurementType,
    val timestamp: Instant,
    val value: Double,
    val source: String?,
)

fun BodyMeasurementEntity.toDomain(): BodyMeasurement? {
    val mt = MeasurementType.fromName(type) ?: return null
    return BodyMeasurement(
        id = id,
        type = mt,
        timestamp = Instant.ofEpochMilli(timestampEpochMs),
        value = value,
        source = source,
    )
}

fun BodyMeasurement.toEntity() = BodyMeasurementEntity(
    id = id,
    type = type.name,
    timestampEpochMs = timestamp.toEpochMilli(),
    value = value,
    unit = type.unit,
    source = source,
)
