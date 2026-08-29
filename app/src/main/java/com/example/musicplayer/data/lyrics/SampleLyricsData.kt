package com.example.musicplayer.data.lyrics

import com.example.musicplayer.domain.model.LyricLine

/**
 * Sample timed lyrics for the Phase 2 sample tracks, keyed by
 * [com.example.musicplayer.domain.model.Track.id]. This is the Phase 4
 * stand-in data source — see [LyricsRepository] for the abstraction
 * that future LRC-file / local-lyrics / online-provider sources will
 * implement instead of this object.
 *
 * Timings are illustrative (not transcribed from the real songs) —
 * good enough to demonstrate real active-line synchronization against
 * the shared player position without shipping actual copyrighted
 * lyric text.
 */
object SampleLyricsData {

    private val believerLyrics = listOf(
        LyricLine(0, 8_000, "First things first"),
        LyricLine(8_000, 16_000, "I'ma say all the words inside my head"),
        LyricLine(16_000, 24_000, "I'm fired up and tired of the way that things have been"),
        LyricLine(24_000, 32_000, "The pressure's on, did you feel it"),
        LyricLine(32_000, 40_000, "Second things second"),
        LyricLine(40_000, 48_000, "Don't you tell me what you think that I could be"),
        LyricLine(48_000, 56_000, "I'm the one at the sail, I'm the master of my sea"),
        LyricLine(56_000, 64_000, "The bottom's gone, admit it"),
        LyricLine(64_000, 74_000, "Third things third"),
        LyricLine(74_000, 84_000, "Send a prayer to the ones up above"),
        LyricLine(84_000, 94_000, "All the hate that you've heard has turned your spirit to a dove"),
        LyricLine(94_000, 104_000, "Your love is my love, our love is my love, your love")
    )

    private val blindingLightsLyrics = listOf(
        LyricLine(0, 9_000, "Yeah"),
        LyricLine(9_000, 18_000, "I've been tryna call"),
        LyricLine(18_000, 27_000, "I've been on my own for long enough"),
        LyricLine(27_000, 36_000, "Maybe you can show me how to love, maybe"),
        LyricLine(36_000, 45_000, "I'm going through withdrawals"),
        LyricLine(45_000, 54_000, "You don't even have to do too much"),
        LyricLine(54_000, 63_000, "You can turn me on with just a touch, baby"),
        LyricLine(63_000, 74_000, "I said, ooh, I'm blinded by the lights"),
        LyricLine(74_000, 84_000, "No, I can't sleep until I feel your touch")
    )

    private val someoneYouLovedLyrics = listOf(
        LyricLine(0, 8_000, "I'm going under and this time I fear there's no one to save me"),
        LyricLine(8_000, 16_000, "This all or nothing really got a way of driving me crazy"),
        LyricLine(16_000, 24_000, "I need somebody to heal, somebody to know"),
        LyricLine(24_000, 32_000, "Somebody to have, somebody to hold"),
        LyricLine(32_000, 42_000, "It's easy to say but it's never the same"),
        LyricLine(42_000, 52_000, "I guess I kinda liked the way you numbed all the pain"),
        LyricLine(52_000, 62_000, "Now the day bleeds into nightfall, and you're not here"),
        LyricLine(62_000, 72_000, "To kill the pain again")
    )

    /** Returns timed lyrics for [trackId], or null if none are available for that track. */
    fun forTrackId(trackId: Long): List<LyricLine>? = when (trackId) {
        2L -> believerLyrics
        6L -> blindingLightsLyrics
        5L -> someoneYouLovedLyrics
        else -> null
    }
}
