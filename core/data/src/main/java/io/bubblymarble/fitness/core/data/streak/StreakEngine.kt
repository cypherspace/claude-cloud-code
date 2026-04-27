package io.bubblymarble.fitness.core.data.streak

import io.bubblymarble.fitness.core.data.model.StreakRule
import io.bubblymarble.fitness.core.data.model.StreakState
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * Pure logic for advancing or expiring a streak. Lives in :core:data so it can be unit-tested
 * without Android dependencies and reused by the streak rollover worker.
 */
object StreakEngine {

    /** Called when a workout completes on [completedOn]. Returns the new streak state. */
    fun onWorkoutCompleted(state: StreakState, completedOn: LocalDate): StreakState {
        val last = state.lastCompleted
        val newLength = when {
            last == null -> 1
            last == completedOn -> state.currentLength.coerceAtLeast(1)
            isContiguous(state, last, completedOn) -> state.currentLength + 1
            else -> 1
        }
        return state.copy(
            lastCompleted = completedOn,
            currentLength = newLength,
            longestLength = maxOf(state.longestLength, newLength),
        )
    }

    /** Called by the daily rollover worker to expire a streak that's been missed. */
    fun onDayRollover(state: StreakState, today: LocalDate): StreakState {
        val last = state.lastCompleted ?: return state
        val expired = when (state.rule) {
            StreakRule.DAILY -> ChronoUnit.DAYS.between(last, today) > 1
            StreakRule.WEEKLY_TARGET -> ChronoUnit.DAYS.between(last, today) > 7
        }
        return if (expired) state.copy(currentLength = 0) else state
    }

    private fun isContiguous(state: StreakState, last: LocalDate, now: LocalDate): Boolean {
        return when (state.rule) {
            StreakRule.DAILY -> ChronoUnit.DAYS.between(last, now) == 1L
            StreakRule.WEEKLY_TARGET -> ChronoUnit.DAYS.between(last, now) <= 7
        }
    }
}
