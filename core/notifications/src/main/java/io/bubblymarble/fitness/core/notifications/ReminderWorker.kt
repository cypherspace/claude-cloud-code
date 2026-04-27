package io.bubblymarble.fitness.core.notifications

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class ReminderWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        if (!hasNotificationPermission()) return Result.success()

        val launch = applicationContext.packageManager.getLaunchIntentForPackage(applicationContext.packageName)
        val pi = launch?.let {
            PendingIntent.getActivity(
                applicationContext, 0, it,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
            )
        }

        val n = NotificationCompat.Builder(applicationContext, NotificationChannels.WORKOUT_REMINDER)
            .setContentTitle("Time to train")
            .setContentText("Tap to start today's workout.")
            .setSmallIcon(android.R.drawable.ic_menu_today)
            .setAutoCancel(true)
            .setContentIntent(pi)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .build()

        NotificationManagerCompat.from(applicationContext).notify(REMINDER_NOTIFICATION_ID, n)
        return Result.success()
    }

    private fun hasNotificationPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            applicationContext, Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    }

    companion object {
        const val REMINDER_NOTIFICATION_ID = 4243
    }
}
