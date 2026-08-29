package com.example.musicplayer.player

import com.example.musicplayer.domain.model.RepeatMode
import com.example.musicplayer.domain.model.Track

/**
 * Reactive snapshot of the real player, backed by Media3/ExoPlayer via
 * [MusicPlayerController]. This is the Phase 3 replacement for the
 * temporary Phase 2 PlaybackStateHolder — same shape of information
 * (current track, queue, position, play state, shuffle, repeat), but
 * now sourced from an actual ExoPlayer instance running inside
 * [com.example.musicplayer.service.MusicPlaybackService] instead of
 * being mutated directly by the UI.
 *
 * [currentPositionMs] and [durationMs] only advance/reflect real
 * values when [hasPlayableMedia] is true. Sample tracks that don't
 * resolve to a real audio URI keep [hasPlayableMedia] false so the
 * UI can render safely (0:00 / --:--) without crashing or faking a
 * moving progress bar.
 */
data class PlayerState(
    val currentTrack: Track? = null,
    val queue: List<Track> = emptyList(),
    val queueIndex: Int = 0,
    val isPlaying: Boolean = false,
    val currentPositionMs: Long = 0L,
    val durationMs: Long = 0L,
    val shuffleEnabled: Boolean = false,
    val repeatMode: RepeatMode = RepeatMode.OFF,
    val isBuffering: Boolean = false,
    val hasPlayableMedia: Boolean = false
)
