package com.example.musicplayer.data.settings

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.musicplayer.sleeptimer.SleepTimerEndBehavior
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Persists the sleep timer as an absolute end-of-day timestamp
 * ([System.currentTimeMillis] epoch millis), never a countdown of
 * remaining seconds. Remaining time is always recomputed as
 * `endTimestampMs - currentTimeMillis()` wherever it's needed — that
 * arithmetic is what keeps the timer correct across app backgrounding,
 * configuration changes, and process recreation, since an
 * elapsed-seconds counter would silently freeze whenever the process
 * hosting it was suspended.
 */
@Singleton
class SleepTimerPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Keys {
        val END_TIMESTAMP_MS = longPreferencesKey("sleep_timer_end_timestamp_ms")
        val END_BEHAVIOR = stringPreferencesKey("sleep_timer_end_behavior")
        val LAST_DURATION_MS = longPreferencesKey("sleep_timer_last_duration_ms")
    }

    data class Snapshot(
        val endTimestampMs: Long?,
        val endBehavior: SleepTimerEndBehavior,
        val lastDurationMs: Long
    )

    val snapshot: Flow<Snapshot> = context.settingsDataStore.data.map { prefs ->
        Snapshot(
            endTimestampMs = prefs[Keys.END_TIMESTAMP_MS]?.takeIf { it > 0L },
            endBehavior = prefs[Keys.END_BEHAVIOR]?.let { name ->
                runCatching { SleepTimerEndBehavior.valueOf(name) }.getOrNull()
            } ?: SleepTimerEndBehavior.PAUSE,
            lastDurationMs = prefs[Keys.LAST_DURATION_MS] ?: (15 * 60_000L)
        )
    }

    suspend fun current(): Snapshot = snapshot.first()

    suspend fun startTimer(durationMs: Long, behavior: SleepTimerEndBehavior) {
        context.settingsDataStore.edit { prefs ->
            prefs[Keys.END_TIMESTAMP_MS] = System.currentTimeMillis() + durationMs
            prefs[Keys.END_BEHAVIOR] = behavior.name
            prefs[Keys.LAST_DURATION_MS] = durationMs
        }
    }

    suspend fun clearTimer() {
        context.settingsDataStore.edit { prefs ->
            prefs[Keys.END_TIMESTAMP_MS] = 0L
        }
    }
}
