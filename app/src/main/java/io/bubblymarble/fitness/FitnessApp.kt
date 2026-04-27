package io.bubblymarble.fitness

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import io.bubblymarble.fitness.core.notifications.NotificationChannels
import javax.inject.Inject

@HiltAndroidApp
class FitnessApp : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        registerNotificationChannels()
    }

    private fun registerNotificationChannels() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val nm = getSystemService(NotificationManager::class.java) ?: return
        nm.createNotificationChannel(
            NotificationChannel(
                NotificationChannels.WORKOUT_ACTIVE,
                getString(R.string.workout_notification_channel_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.workout_notification_channel_description)
                setSound(null, null)
            }
        )
        nm.createNotificationChannel(
            NotificationChannel(
                NotificationChannels.WORKOUT_REMINDER,
                getString(R.string.reminder_notification_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = getString(R.string.reminder_notification_channel_description)
            }
        )
    }
}
