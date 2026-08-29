package com.example.musicplayer.player

/**
 * One-shot events from the player layer that the UI should react to
 * once (e.g. show a snackbar) rather than continuously observe as
 * state. Kept separate from [PlayerState] so a transient error
 * doesn't get "stuck" in a StateFlow and re-fire on recomposition.
 */
sealed interface PlayerEvent {
    /** A track could not be played (e.g. sample/placeholder URI with no real media). */
    data class PlaybackUnavailable(val trackTitle: String) : PlayerEvent

    /** A real ExoPlayer error occurred (network, decode, etc). */
    data class PlaybackError(val message: String) : PlayerEvent
}
