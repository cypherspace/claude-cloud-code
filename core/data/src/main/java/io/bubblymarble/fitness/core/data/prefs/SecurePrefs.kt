package io.bubblymarble.fitness.core.data.prefs

import android.content.SharedPreferences
import androidx.core.content.edit

class SecurePrefs(private val prefs: SharedPreferences) {
    fun geminiKey(): String? = prefs.getString(KEY_GEMINI, null)?.takeIf { it.isNotBlank() }
    fun setGeminiKey(value: String?) {
        prefs.edit { if (value.isNullOrBlank()) remove(KEY_GEMINI) else putString(KEY_GEMINI, value) }
    }

    companion object {
        private const val KEY_GEMINI = "gemini_api_key"
    }
}
