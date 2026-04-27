package io.bubblymarble.fitness.core.notifications

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Duration
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReminderScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    fun scheduleDailyReminder(localTime: LocalTime) {
        val now = ZonedDateTime.now(ZoneId.systemDefault())
        val target = now.with(localTime).let { if (it.isBefore(now)) it.plusDays(1) else it }
        val initialDelay = Duration.between(now, target)
        val request = PeriodicWorkRequestBuilder<ReminderWorker>(Duration.ofHours(24))
            .setInitialDelay(initialDelay)
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.NOT_REQUIRED).build())
            .addTag(REMINDER_TAG)
            .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            UNIQUE_REMINDER, ExistingPeriodicWorkPolicy.UPDATE, request,
        )
    }

    fun cancel() {
        WorkManager.getInstance(context).cancelUniqueWork(UNIQUE_REMINDER)
    }

    companion object {
        const val UNIQUE_REMINDER = "workout_reminder_daily"
        const val REMINDER_TAG = "workout-reminder"
    }
}
