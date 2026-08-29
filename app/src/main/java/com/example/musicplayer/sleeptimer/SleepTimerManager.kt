package com.example.musicplayer.sleeptimer

import com.example.musicplayer.data.settings.SleepTimerPreferences
import com.example.musicplayer.player.MusicPlayerController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.max

/**
 * Sleep timer that lives for the whole app process (Hilt singleton,
 * created once in [com.example.musicplayer.MusicPlayerApplication]'s
 * dependency graph) rather than being owned by the Sleep Timer screen
 * or its ViewModel. This is what "managed independently from screen
 * lifecycle" means in practice: the countdown coroutine keeps running
 * in [scope] (a process-lifetime [CoroutineScope], not `viewModelScope`)
 * whether or not the Sleep Timer screen — or any screen — is currently
 * visible, and the real pause/stop action fires against
 * [MusicPlayerController] regardless of UI state.
 *
 * Correctness across backgrounding/process death comes from
 * [SleepTimerPreferences] storing an absolute end timestamp: on every
 * tick (and once immediately on init), remaining time is recomputed as
 * `endTimestampMs - now`, so a suspended/killed-and-restarted process
 * picks up exactly where the wall clock says it should, including
 * firing the end action immediately if the timer already expired while
 * the process was gone.
 */
@Singleton
class SleepTimerManager @Inject constructor(
    private val preferences: SleepTimerPreferences,
    private val playerController: MusicPlayerController,
    private val alarmScheduler: SleepTimerAlarmScheduler
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private val _state = MutableStateFlow(SleepTimerUiState())
    val state: StateFlow<SleepTimerUiState> = _state.asStateFlow()

    private var countdownJob: kotlinx.coroutines.Job? = null

    init {
        // Recompute from persisted absolute state as soon as this
        // singleton is first created (app process start), so an
        // in-progress or already-expired timer from a previous process
        // is handled correctly rather than silently forgotten.
        scope.launch { restoreFromPersistedState() }
    }

    private suspend fun restoreFromPersistedState() {
        val saved = preferences.current()
        val endTimestamp = saved.endTimestampMs
        if (endTimestamp == null) {
            _state.value = SleepTimerUiState(endBehavior = saved.endBehavior)
            return
        }

        val remaining = endTimestamp - System.currentTimeMillis()
        if (remaining <= 0L) {
            // Timer expired while the app/process was gone. The
            // AlarmManager backstop (see SleepTimerAlarmScheduler)
            // should already have fired the service action for this
            // case, but performing it again here is a safe no-op
            // (pause/clearQueue are idempotent) and covers the case
            // where the app is reopened by the user before the alarm
            // broadcast is processed.
            performEndAction(saved.endBehavior)
            preferences.clearTimer()
            alarmScheduler.cancel()
            _state.value = SleepTimerUiState(endBehavior = saved.endBehavior)
            return
        }

        startCountdown(endTimestamp, saved.endBehavior, saved.lastDurationMs)
    }

    fun startTimer(durationMs: Long, behavior: SleepTimerEndBehavior) {
        scope.launch {
            preferences.startTimer(durationMs, behavior)
            val endTimestamp = System.currentTimeMillis() + durationMs
            alarmScheduler.schedule(endTimestamp)
            startCountdown(endTimestamp, behavior, durationMs)
        }
    }

    fun cancelTimer() {
        countdownJob?.cancel()
        countdownJob = null
        alarmScheduler.cancel()
        scope.launch { preferences.clearTimer() }
        _state.update { it.copy(isActive = false, remainingMs = 0L) }
    }

    private fun startCountdown(endTimestampMs: Long, behavior: SleepTimerEndBehavior, totalDurationMs: Long) {
        countdownJob?.cancel()
        countdownJob = scope.launch {
            while (isActive) {
                val remaining = endTimestampMs - System.currentTimeMillis()
                if (remaining <= 0L) {
                    performEndAction(behavior)
                    preferences.clearTimer()
                    alarmScheduler.cancel()
                    _state.update { it.copy(isActive = false, remainingMs = 0L) }
                    break
                }
                _state.update {
                    it.copy(
                        isActive = true,
                        remainingMs = remaining,
                        totalDurationMs = max(totalDurationMs, remaining),
                        endBehavior = behavior
                    )
                }
                kotlinx.coroutines.delay(500)
            }
        }
    }

    private fun performEndAction(behavior: SleepTimerEndBehavior) {
        when (behavior) {
            SleepTimerEndBehavior.PAUSE -> playerController.pause()
            SleepTimerEndBehavior.STOP_AND_CLEAR -> {
                playerController.pause()
                playerController.clearQueue()
            }
        }
    }
}
