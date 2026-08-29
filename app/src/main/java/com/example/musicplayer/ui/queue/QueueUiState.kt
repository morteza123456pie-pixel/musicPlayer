package com.example.musicplayer.ui.queue

import com.example.musicplayer.domain.model.Track

/**
 * Everything the Queue screen needs. [queue]/[currentIndex] mirror
 * [com.example.musicplayer.player.PlayerState] directly — this is a
 * thin read-through, not a separate copy of playback state.
 */
data class QueueUiState(
    val queue: List<Track> = emptyList(),
    val currentIndex: Int = 0,
    val isPlaying: Boolean = false
)
