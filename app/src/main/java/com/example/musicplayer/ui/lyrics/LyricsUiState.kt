package com.example.musicplayer.ui.lyrics

import com.example.musicplayer.domain.model.LyricLine
import com.example.musicplayer.domain.model.Track

data class LyricsUiState(
    val currentTrack: Track? = null,
    val lyricLines: List<LyricLine> = emptyList(),
    val activeLineIndex: Int = -1,
    val currentPositionMs: Long = 0L,
    val durationMs: Long = 0L,
    val isPlaying: Boolean = false,
    val hasPlayableMedia: Boolean = false
) {
    /** True when there is a track loaded but no timed lyrics exist for it. */
    val showEmptyState: Boolean get() = currentTrack != null && lyricLines.isEmpty()
}
