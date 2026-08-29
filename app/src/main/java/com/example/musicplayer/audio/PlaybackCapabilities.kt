package com.example.musicplayer.audio

/**
 * Declares which Playback Settings toggles are backed by real,
 * verified player behavior on the current Media3/ExoPlayer version
 * this app is built against, versus features that would require
 * further player-architecture work to implement correctly.
 *
 * Consulted by the Playback Settings screen so it never renders a
 * switch that silently does nothing — per Phase 5 scope, unavailable
 * features are shown as clearly disabled/unavailable rather than
 * hidden entirely or faked.
 */
object PlaybackCapabilities {
    /**
     * True crossfade needs two overlapping concurrently-playing
     * ExoPlayer instances (or a custom MediaSource/AudioProcessor
     * mixing pipeline) — the app's single shared ExoPlayer instance
     * (see [com.example.musicplayer.service.MusicPlaybackService])
     * has no built-in crossfade API to hook into. Implementing it
     * reliably is future player-architecture work, not a Phase 5-sized
     * change, so it stays disabled with a clear "unavailable" state
     * rather than a fake or unstable approximation.
     */
    const val CROSSFADE_SUPPORTED = false

    /**
     * Backed by ExoPlayer's real [androidx.media3.exoplayer.ExoPlayer.setSkipSilenceEnabled]
     * (SilenceSkippingAudioProcessor), applied directly to the live
     * player in [com.example.musicplayer.audio.AudioEffectsSessionCallback].
     * This is genuine, not simulated.
     */
    const val SKIP_SILENCE_SUPPORTED = true
}
