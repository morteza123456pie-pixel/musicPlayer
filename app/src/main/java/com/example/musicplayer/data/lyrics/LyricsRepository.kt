package com.example.musicplayer.data.lyrics

import com.example.musicplayer.domain.model.LyricLine
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Single point the UI layer asks for lyrics through. Backed by
 * [SampleLyricsData] for Phase 4.
 *
 * Deliberately just one function behind an interface-shaped class so
 * swapping in a real source later — parsed `.lrc` sidecar files,
 * locally embedded lyrics tags, or an online lyrics provider — only
 * means changing what happens inside [getLyricsForTrack], not any of
 * the call sites in [com.example.musicplayer.ui.lyrics.LyricsViewModel].
 * Not implemented yet, per the Phase 4 brief (no external lyric APIs).
 */
@Singleton
class LyricsRepository @Inject constructor() {

    fun getLyricsForTrack(trackId: Long): List<LyricLine>? {
        return SampleLyricsData.forTrackId(trackId)
    }
}
