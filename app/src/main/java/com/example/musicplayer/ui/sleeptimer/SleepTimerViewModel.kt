package com.example.musicplayer.ui.sleeptimer

import androidx.lifecycle.ViewModel
import com.example.musicplayer.sleeptimer.SleepTimerEndBehavior
import com.example.musicplayer.sleeptimer.SleepTimerManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * Thin wrapper around [SleepTimerManager], which is the actual process-
 * lifetime singleton owning the countdown. This ViewModel exists only
 * to give the Sleep Timer screen a lifecycle-aware, hiltViewModel()-
 * injectable handle — closing/reopening this screen never
 * starts/stops/affects the underlying timer, matching the "managed
 * independently from screen lifecycle" requirement.
 */
@HiltViewModel
class SleepTimerViewModel @Inject constructor(
    private val manager: SleepTimerManager
) : ViewModel() {

    val state = manager.state

    fun startTimer(durationMs: Long, behavior: SleepTimerEndBehavior) = manager.startTimer(durationMs, behavior)

    fun cancelTimer() = manager.cancelTimer()
}
