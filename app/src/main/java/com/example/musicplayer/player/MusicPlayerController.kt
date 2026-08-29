package com.example.musicplayer.player

import com.example.musicplayer.domain.model.RepeatMode
import com.example.musicplayer.domain.model.Track
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * The single API surface the UI layer is allowed to depend on for
 * playback. No ViewModel or Composable talks to ExoPlayer, Media3's
 * MediaController, or [com.example.musicplayer.service.MusicPlaybackService]
 * directly — everything goes through this interface.
 *
 * [MusicPlayerControllerImpl] is the real implementation, backed by a
 * Media3 MediaController bound to the playback service. This
 * separation is what makes it possible to preview/test screens
 * without a real player, and is the seam the Phase 2 PlaybackStateHolder
 * was standing in for before real playback existed.
 */
interface MusicPlayerController {

    /** Continuous, observable playback state. Single source of truth for the whole app. */
    val state: StateFlow<PlayerState>

    /** One-shot events (errors, unavailable media) — collect, don't treat as persistent state. */
    val events: SharedFlow<PlayerEvent>

    /**
     * Replaces the queue with [tracks] and starts playing the track at
     * [startIndex]. Used when a track is selected from Library/Albums/etc:
     * the whole visible track list becomes the new queue.
     */
    fun playQueue(tracks: List<Track>, startIndex: Int)

    /** Plays [track] as a single-item queue (used when there's no larger list context). */
    fun playTrack(track: Track)

    fun togglePlayPause()
    fun play()
    fun pause()
    fun skipToNext()
    fun skipToPrevious()

    /** Seeks within the current track. No-op if there is no playable media loaded. */
    fun seekTo(positionMs: Long)

    fun toggleShuffle()
    fun cycleRepeatMode()

    /**
     * Moves the queue item at [fromIndex] to [toIndex] (indices into
     * [PlayerState.queue]). Updates Media3's actual playlist ordering,
     * not just a UI-side copy — [PlayerState.queue] always reflects
     * what Media3 will really play next.
     */
    fun moveQueueItem(fromIndex: Int, toIndex: Int)

    /** Removes the queue item at [index]. No-op if it's the only item left. */
    fun removeFromQueue(index: Int)

    /** Clears every item from the queue except the currently playing one, and stops playback. */
    fun clearQueue()

    /** Jumps directly to the queue item at [index] and starts playing it. */
    fun playQueueItemAt(index: Int)

    /** Call from the owning Activity/Application to release the underlying controller connection. */
    fun release()
}
