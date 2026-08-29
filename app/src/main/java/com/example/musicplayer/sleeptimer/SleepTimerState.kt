package com.example.musicplayer.sleeptimer

/** What happens to playback when the sleep timer reaches zero. */
enum class SleepTimerEndBehavior {
    PAUSE,
    STOP_AND_CLEAR
}

/**
 * Live sleep timer state, derived from an absolute end timestamp
 * rather than a ticking counter — see [SleepTimerManager] for why.
 */
data class SleepTimerUiState(
    val isActive: Boolean = false,
    val remainingMs: Long = 0L,
    val totalDurationMs: Long = 0L,
    val endBehavior: SleepTimerEndBehavior = SleepTimerEndBehavior.PAUSE
)

/** Common quick-select durations shown on the Sleep Timer screen, in minutes. */
val SLEEP_TIMER_QUICK_DURATIONS_MINUTES = listOf(5, 10, 15, 30, 45, 60)
