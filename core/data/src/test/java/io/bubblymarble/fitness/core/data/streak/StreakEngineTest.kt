package io.bubblymarble.fitness.core.data.streak

import com.google.common.truth.Truth.assertThat
import io.bubblymarble.fitness.core.data.model.StreakRule
import io.bubblymarble.fitness.core.data.model.StreakState
import java.time.LocalDate
import org.junit.Test

class StreakEngineTest {

    private val baseDaily = StreakState(
        lastCompleted = null,
        currentLength = 0,
        longestLength = 0,
        rule = StreakRule.DAILY,
        weeklyTarget = 3,
    )

    @Test fun firstWorkout_startsAtOne() {
        val out = StreakEngine.onWorkoutCompleted(baseDaily, LocalDate.of(2026, 1, 5))
        assertThat(out.currentLength).isEqualTo(1)
        assertThat(out.longestLength).isEqualTo(1)
        assertThat(out.lastCompleted).isEqualTo(LocalDate.of(2026, 1, 5))
    }

    @Test fun consecutiveDays_increment() {
        var s = StreakEngine.onWorkoutCompleted(baseDaily, LocalDate.of(2026, 1, 5))
        s = StreakEngine.onWorkoutCompleted(s, LocalDate.of(2026, 1, 6))
        s = StreakEngine.onWorkoutCompleted(s, LocalDate.of(2026, 1, 7))
        assertThat(s.currentLength).isEqualTo(3)
        assertThat(s.longestLength).isEqualTo(3)
    }

    @Test fun sameDayWorkouts_doNotIncrement() {
        var s = StreakEngine.onWorkoutCompleted(baseDaily, LocalDate.of(2026, 1, 5))
        s = StreakEngine.onWorkoutCompleted(s, LocalDate.of(2026, 1, 5))
        assertThat(s.currentLength).isEqualTo(1)
    }

    @Test fun gapResetsStreak() {
        var s = StreakEngine.onWorkoutCompleted(baseDaily, LocalDate.of(2026, 1, 5))
        s = StreakEngine.onWorkoutCompleted(s, LocalDate.of(2026, 1, 6))
        s = StreakEngine.onWorkoutCompleted(s, LocalDate.of(2026, 1, 9)) // 2-day gap
        assertThat(s.currentLength).isEqualTo(1)
        assertThat(s.longestLength).isEqualTo(2)
    }

    @Test fun rolloverExpiresStreakWhenIdle() {
        val s = baseDaily.copy(lastCompleted = LocalDate.of(2026, 1, 5), currentLength = 4, longestLength = 7)
        val out = StreakEngine.onDayRollover(s, LocalDate.of(2026, 1, 8))
        assertThat(out.currentLength).isEqualTo(0)
        assertThat(out.longestLength).isEqualTo(7)
    }

    @Test fun rolloverDoesNothingWhenStreakStillValid() {
        val s = baseDaily.copy(lastCompleted = LocalDate.of(2026, 1, 5), currentLength = 4)
        val out = StreakEngine.onDayRollover(s, LocalDate.of(2026, 1, 6))
        assertThat(out.currentLength).isEqualTo(4)
    }

    @Test fun weeklyRule_allowsUpToSevenDayGap() {
        val s = baseDaily.copy(rule = StreakRule.WEEKLY_TARGET, weeklyTarget = 3)
        val day1 = StreakEngine.onWorkoutCompleted(s, LocalDate.of(2026, 1, 5))
        val day8 = StreakEngine.onWorkoutCompleted(day1, LocalDate.of(2026, 1, 12))
        assertThat(day8.currentLength).isEqualTo(2)
    }
}
