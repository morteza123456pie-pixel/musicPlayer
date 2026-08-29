package com.example.musicplayer.domain.model

/**
 * A single timed lyric line. [startTimeMs] and [endTimeMs] are the
 * window (relative to track position) during which this line is the
 * "active" one on [com.example.musicplayer.ui.lyrics.LyricsScreen].
 *
 * This is intentionally minimal so it's easy to source from different
 * places later without changing the UI layer: sample data now
 * (Phase 4), parsed .lrc files, locally-embedded lyrics, or an online
 * provider in the future all just need to produce a
 * `List<LyricLine>`.
 */
data class LyricLine(
    val startTimeMs: Long,
    val endTimeMs: Long,
    val text: String
)
