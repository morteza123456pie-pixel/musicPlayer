package com.example.musicplayer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicplayer.player.MusicPlayerController
import com.example.musicplayer.player.PlayerState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * App-shell-level ViewModel. Its only job is exposing the shared
 * [MusicPlayerController] state to [MainActivity] so the persistent
 * MiniPlayer (which lives in the shell, outside any single screen)
 * can render the current track and forward play/pause taps back into
 * the same real player every screen observes.
 *
 * Phase 3: this now wraps the real Media3-backed controller instead
 * of the Phase 2 PlaybackStateHolder. The shape of what the shell
 * needs (current track, is-playing) hasn't changed — only where it
 * comes from.
 */
@HiltViewModel
class MainViewModel @Inject constructor(
    private val playerController: MusicPlayerController
) : ViewModel() {

    val playerState: StateFlow<PlayerState> = playerController.state.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = PlayerState()
    )

    fun onTogglePlayPause() {
        playerController.togglePlayPause()
    }
}
