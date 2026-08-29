package com.example.musicplayer.ui.lyrics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicplayer.data.lyrics.LyricsRepository
import com.example.musicplayer.player.MusicPlayerController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * Derives the active lyric line from the real shared playback
 * position ([MusicPlayerController.state]) — there is no separate
 * timer here. Because [MusicPlayerController]'s position updates via
 * a ~500ms poll while playing (see MusicPlayerControllerImpl), the
 * active line naturally stays in sync with play/pause/seek/next/
 * previous without this screen needing to listen to those events
 * directly; it only ever reacts to position changes.
 */
@HiltViewModel
class LyricsViewModel @Inject constructor(
    private val playerController: MusicPlayerController,
    private val lyricsRepository: LyricsRepository
) : ViewModel() {

    val uiState: StateFlow<LyricsUiState> = playerController.state
        .map { playback ->
            val track = playback.currentTrack
            val lines = track?.let { lyricsRepository.getLyricsForTrack(it.id) }.orEmpty()
            val activeIndex = lines.indexOfLast { playback.currentPositionMs >= it.startTimeMs }

            LyricsUiState(
                currentTrack = track,
                lyricLines = lines,
                activeLineIndex = activeIndex,
                currentPositionMs = playback.currentPositionMs,
                durationMs = playback.durationMs,
                isPlaying = playback.isPlaying,
                hasPlayableMedia = playback.hasPlayableMedia
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = LyricsUiState()
        )

    fun onTogglePlayPause() = playerController.togglePlayPause()
    fun onNext() = playerController.skipToNext()
    fun onPrevious() = playerController.skipToPrevious()
    fun onSeekToLine(startTimeMs: Long) = playerController.seekTo(startTimeMs)
}
