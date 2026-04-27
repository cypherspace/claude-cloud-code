package io.bubblymarble.fitness.feature.measurements

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.bubblymarble.fitness.core.common.TimeSource
import io.bubblymarble.fitness.core.data.model.BodyMeasurement
import io.bubblymarble.fitness.core.data.model.MeasurementType
import io.bubblymarble.fitness.core.data.repository.BodyMeasurementRepository
import io.bubblymarble.fitness.core.health.HealthConnectFacade
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MeasurementsUi(
    val latestByType: Map<MeasurementType, BodyMeasurement> = emptyMap(),
    val weightSeries: List<BodyMeasurement> = emptyList(),
    val syncing: Boolean = false,
    val syncStatus: String? = null,
    val healthConnectAvailable: Boolean = true,
)

@HiltViewModel
class MeasurementsViewModel @Inject constructor(
    private val repo: BodyMeasurementRepository,
    private val health: HealthConnectFacade,
    private val time: TimeSource,
) : ViewModel() {

    private val _ui = MutableStateFlow(MeasurementsUi(healthConnectAvailable = health.isAvailable()))
    val ui: StateFlow<MeasurementsUi> = _ui.asStateFlow()

    /** All current measurements, keyed by type, derived from the live Room flow. */
    val latest: StateFlow<Map<MeasurementType, BodyMeasurement>> = repo.observeAll()
        .map { all ->
            all.groupBy { it.type }.mapValues { (_, list) -> list.maxBy { it.timestamp.toEpochMilli() } }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyMap())

    val weightSeries: StateFlow<List<BodyMeasurement>> = repo.observeByType(MeasurementType.WEIGHT_KG)
        .map { it.sortedBy { sample -> sample.timestamp.toEpochMilli() }.takeLast(60) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun record(type: MeasurementType, value: Double) {
        if (value <= 0 || value.isNaN()) return
        viewModelScope.launch { repo.record(type, value) }
    }

    fun delete(id: Long) {
        viewModelScope.launch { repo.delete(id) }
    }

    /** Pulls the last 30 days of body-composition rows from Health Connect. */
    fun syncFromHealthConnect() {
        if (_ui.value.syncing) return
        _ui.update { it.copy(syncing = true, syncStatus = null) }
        viewModelScope.launch {
            val until = time.now()
            val from = until.minus(30, ChronoUnit.DAYS)
            val rows = health.readBodyMeasurements(from, until)
            val inserted = repo.importFromSource(rows)
            _ui.update {
                it.copy(
                    syncing = false,
                    syncStatus = when {
                        rows.isEmpty() -> "No body-composition data found in Health Connect."
                        inserted == 0 -> "Already up to date."
                        else -> "Imported $inserted new measurement${if (inserted == 1) "" else "s"}."
                    },
                )
            }
        }
    }

    fun dismissSyncStatus() = _ui.update { it.copy(syncStatus = null) }
}
