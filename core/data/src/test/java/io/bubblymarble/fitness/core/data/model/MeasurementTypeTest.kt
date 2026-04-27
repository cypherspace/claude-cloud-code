package io.bubblymarble.fitness.core.data.model

import com.google.common.truth.Truth.assertThat
import io.bubblymarble.fitness.core.data.db.entities.BodyMeasurementEntity
import org.junit.Test

class MeasurementTypeTest {

    @Test fun fromName_validValues_resolveCorrectly() {
        assertThat(MeasurementType.fromName("WEIGHT_KG")).isEqualTo(MeasurementType.WEIGHT_KG)
        assertThat(MeasurementType.fromName("LEFT_BICEP_CM")).isEqualTo(MeasurementType.LEFT_BICEP_CM)
        assertThat(MeasurementType.fromName("BMR_KCAL")).isEqualTo(MeasurementType.BMR_KCAL)
    }

    @Test fun fromName_invalidValue_returnsNull() {
        assertThat(MeasurementType.fromName("NOT_A_TYPE")).isNull()
        assertThat(MeasurementType.fromName("")).isNull()
        assertThat(MeasurementType.fromName("weight_kg")).isNull() // enum lookup is case-sensitive
    }

    @Test fun units_alignWithGroup() {
        MeasurementType.values().forEach { type ->
            assertThat(type.unit).isNotEmpty()
            when (type.groupedAs) {
                MeasurementGroup.TAPE -> assertThat(type.unit).isEqualTo("cm")
                MeasurementGroup.BODY_COMP -> assertThat(type.unit).isAnyOf("kg", "%", "kcal/day", "cm")
            }
        }
    }

    @Test fun entity_roundTripsThroughDomain() {
        val ts = 1_700_000_000_000L
        val entity = BodyMeasurementEntity(
            id = 7,
            type = MeasurementType.WAIST_CM.name,
            timestampEpochMs = ts,
            value = 84.5,
            unit = MeasurementType.WAIST_CM.unit,
            source = "manual",
        )
        val domain = entity.toDomain()!!
        assertThat(domain.type).isEqualTo(MeasurementType.WAIST_CM)
        assertThat(domain.value).isEqualTo(84.5)
        assertThat(domain.timestamp.toEpochMilli()).isEqualTo(ts)
        val back = domain.toEntity()
        assertThat(back).isEqualTo(entity)
    }

    @Test fun entity_unknownType_returnsNullDomain() {
        val entity = BodyMeasurementEntity(
            id = 1,
            type = "MADE_UP",
            timestampEpochMs = 0,
            value = 1.0,
            unit = "kg",
            source = null,
        )
        assertThat(entity.toDomain()).isNull()
    }
}
