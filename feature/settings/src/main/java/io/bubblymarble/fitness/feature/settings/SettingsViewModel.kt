package io.bubblymarble.fitness.feature.settings

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import io.bubblymarble.fitness.core.data.prefs.SecurePrefs
import io.bubblymarble.fitness.core.notifications.ReminderScheduler
import java.time.LocalTime
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class SettingsUi(
    val maskedKey: String = "",
    val reminderHour: Int = 18,
    val reminderMinute: Int = 0,
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val securePrefs: SecurePrefs,
    private val reminders: ReminderScheduler,
) : ViewModel() {

    private val _ui = MutableStateFlow(SettingsUi(maskedKey = mask(securePrefs.geminiKey())))
    val ui: StateFlow<SettingsUi> = _ui.asStateFlow()

    fun setApiKey(value: String) {
        securePrefs.setGeminiKey(value.takeIf { it.isNotBlank() })
        _ui.update { it.copy(maskedKey = mask(value)) }
    }

    fun setReminder(hour: Int, minute: Int) {
        _ui.update { it.copy(reminderHour = hour, reminderMinute = minute) }
        reminders.scheduleDailyReminder(LocalTime.of(hour.coerceIn(0, 23), minute.coerceIn(0, 59)))
    }

    fun cancelReminders() = reminders.cancel()

    private fun mask(key: String?): String =
        if (key.isNullOrBlank()) "" else "•".repeat(key.length.coerceAtMost(8)) + key.takeLast(4)
}
