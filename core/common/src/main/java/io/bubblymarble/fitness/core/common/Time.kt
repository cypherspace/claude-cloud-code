package io.bubblymarble.fitness.core.common

import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

interface TimeSource {
    fun now(): Instant
    fun today(zone: ZoneId = ZoneId.systemDefault()): LocalDate
}

class SystemTimeSource(private val clock: Clock = Clock.systemDefaultZone()) : TimeSource {
    override fun now(): Instant = clock.instant()
    override fun today(zone: ZoneId): LocalDate = LocalDate.now(clock.withZone(zone))
}
