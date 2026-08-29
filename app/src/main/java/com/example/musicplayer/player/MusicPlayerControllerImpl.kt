package com.example.musicplayer.player

import android.content.Context
import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import com.example.musicplayer.domain.model.RepeatMode
import com.example.musicplayer.domain.model.Track
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Real [MusicPlayerController] implementation. Owns the Media3
 * [MediaController] connection (via [PlayerServiceConnection]) and is
 * the only class in the app that touches Media3 player callbacks —
 * every screen and ViewModel observes [state] instead.
 *
 * Sample tracks (Phase 2 data) use placeholder `sample://track/N`
 * URIs that ExoPlayer cannot resolve. Rather than crash or silently
 * do nothing, [playQueue]/[playTrack] detect non-resolvable URIs up
 * front and emit [PlayerEvent.PlaybackUnavailable] instead of handing
 * them to the player — [state.hasPlayableMedia] stays false and the
 * Now Playing screen renders the track's title/artist/artwork without
 * a moving progress bar. Once real device audio URIs are wired in
 * (MediaStore, Phase 4+), the same code path plays them for real with
 * no further changes needed here.
 */
@Singleton
class MusicPlayerControllerImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : MusicPlayerController {

    private val connection = PlayerServiceConnection(context)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private var controller: MediaController? = null
    private var positionPollingJob: kotlinx.coroutines.Job? = null

    private val _state = MutableStateFlow(PlayerState())
    override val state: StateFlow<PlayerState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<PlayerEvent>(extraBufferCapacity = 4)
    override val events: SharedFlow<PlayerEvent> = _events

    // Track metadata for whatever is currently loaded, keyed by media
    // item index, so Player.Listener callbacks (which only give us
    // Media3 types) can be translated back into our domain Track.
    private var currentQueueTracks: List<Track> = emptyList()

    private val playerListener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _state.update { it.copy(isPlaying = isPlaying) }
            if (isPlaying) startPositionPolling() else stopPositionPolling()
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            _state.update {
                it.copy(
                    isBuffering = playbackState == Player.STATE_BUFFERING,
                    durationMs = controller?.duration?.takeIf { d -> d > 0 } ?: it.durationMs
                )
            }
        }

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            val index = controller?.currentMediaItemIndex ?: 0
            val track = currentQueueTracks.getOrNull(index)
            _state.update {
                it.copy(
                    currentTrack = track ?: it.currentTrack,
                    queueIndex = index,
                    currentPositionMs = 0L,
                    durationMs = 0L,
                    hasPlayableMedia = track != null
                )
            }
        }

        override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
            _state.update { it.copy(shuffleEnabled = shuffleModeEnabled) }
        }

        override fun onRepeatModeChanged(repeatMode: Int) {
            _state.update { it.copy(repeatMode = repeatMode.toDomainRepeatMode()) }
        }

        override fun onPlayerError(error: PlaybackException) {
            _events.tryEmit(PlayerEvent.PlaybackError(error.message ?: "Playback error"))
            _state.update { it.copy(isPlaying = false, isBuffering = false) }
        }
    }

    private fun ensureConnected(onReady: (MediaController) -> Unit) {
        val existing = controller
        if (existing != null) {
            onReady(existing)
            return
        }
        scope.launch {
            val connected = connection.connect()
            connected.addListener(playerListener)
            controller = connected
            onReady(connected)
        }
    }

    override fun playQueue(tracks: List<Track>, startIndex: Int) {
        if (tracks.isEmpty()) return
        val playableTracks = tracks.filter { it.hasResolvableUri() }

        if (playableTracks.isEmpty()) {
            // Nothing in this queue can actually be played (all sample
            // placeholder URIs). Surface the track info so the UI can
            // still show "now playing" context without a real player,
            // and tell the user why nothing is audible.
            val target = tracks.getOrNull(startIndex) ?: tracks.first()
            _state.update {
                it.copy(
                    currentTrack = target,
                    queue = tracks,
                    queueIndex = tracks.indexOf(target).coerceAtLeast(0),
                    isPlaying = false,
                    currentPositionMs = 0L,
                    durationMs = 0L,
                    hasPlayableMedia = false
                )
            }
            _events.tryEmit(PlayerEvent.PlaybackUnavailable(target.title))
            return
        }

        currentQueueTracks = playableTracks
        val resolvedStartIndex = playableTracks.indexOf(tracks.getOrNull(startIndex))
            .let { if (it >= 0) it else 0 }

        ensureConnected { mediaController ->
            val mediaItems = playableTracks.map { it.toMediaItem() }
            mediaController.setMediaItems(mediaItems, resolvedStartIndex, 0L)
            mediaController.prepare()
            mediaController.play()

            _state.update {
                it.copy(
                    currentTrack = playableTracks[resolvedStartIndex],
                    queue = tracks,
                    queueIndex = resolvedStartIndex,
                    hasPlayableMedia = true
                )
            }
        }
    }

    override fun playTrack(track: Track) {
        playQueue(listOf(track), 0)
    }

    override fun togglePlayPause() {
        val mediaController = controller ?: return
        if (!_state.value.hasPlayableMedia) return
        if (mediaController.isPlaying) mediaController.pause() else mediaController.play()
    }

    override fun play() {
        if (!_state.value.hasPlayableMedia) return
        controller?.play()
    }

    override fun pause() {
        controller?.pause()
    }

    override fun skipToNext() {
        val mediaController = controller ?: return
        if (mediaController.hasNextMediaItem()) mediaController.seekToNext()
    }

    override fun skipToPrevious() {
        val mediaController = controller ?: return
        if (mediaController.hasPreviousMediaItem()) mediaController.seekToPrevious()
    }

    override fun seekTo(positionMs: Long) {
        if (!_state.value.hasPlayableMedia) return
        controller?.seekTo(positionMs)
        _state.update { it.copy(currentPositionMs = positionMs) }
    }

    override fun toggleShuffle() {
        val mediaController = controller ?: return
        mediaController.shuffleModeEnabled = !mediaController.shuffleModeEnabled
    }

    override fun cycleRepeatMode() {
        val mediaController = controller ?: return
        val next = when (_state.value.repeatMode) {
            RepeatMode.OFF -> Player.REPEAT_MODE_ALL
            RepeatMode.ALL -> Player.REPEAT_MODE_ONE
            RepeatMode.ONE -> Player.REPEAT_MODE_OFF
        }
        mediaController.repeatMode = next
    }

    override fun moveQueueItem(fromIndex: Int, toIndex: Int) {
        val current = _state.value
        val queue = current.queue
        if (fromIndex !in queue.indices || toIndex !in queue.indices || fromIndex == toIndex) return

        val movedTrack = queue[fromIndex]
        val reordered = queue.toMutableList().apply {
            add(toIndex, removeAt(fromIndex))
        }

        // Keep the "currently playing" track pinned to its actual
        // track identity rather than its old index, since the move
        // shifts indices around it.
        val playingTrackId = current.currentTrack?.id
        val newIndex = reordered.indexOfFirst { it.id == playingTrackId }.let { if (it >= 0) it else current.queueIndex }

        if (current.hasPlayableMedia) {
            val mediaController = controller
            val fromPlayableIndex = currentQueueTracks.indexOfFirst { it.id == movedTrack.id }
            if (mediaController != null && fromPlayableIndex >= 0) {
                // Re-derive the target position purely in terms of the
                // playable subset, so Media3's real playlist and
                // currentQueueTracks stay index-consistent with each
                // other (they only ever contain playable tracks).
                val toPlayableIndex = reordered
                    .filter { t -> currentQueueTracks.any { it.id == t.id } }
                    .indexOfFirst { it.id == movedTrack.id }
                    .coerceIn(0, currentQueueTracks.lastIndex)

                mediaController.moveMediaItem(fromPlayableIndex, toPlayableIndex)
                currentQueueTracks = currentQueueTracks.toMutableList().apply {
                    add(toPlayableIndex, removeAt(fromPlayableIndex))
                }
            }
        }

        _state.update { it.copy(queue = reordered, queueIndex = newIndex) }
    }

    override fun removeFromQueue(index: Int) {
        val current = _state.value
        val queue = current.queue
        if (index !in queue.indices || queue.size <= 1) return

        val removedTrack = queue[index]
        val updatedQueue = queue.toMutableList().apply { removeAt(index) }

        if (current.hasPlayableMedia) {
            val mediaController = controller
            val playableIndex = currentQueueTracks.indexOfFirst { it.id == removedTrack.id }
            if (mediaController != null && playableIndex >= 0) {
                mediaController.removeMediaItem(playableIndex)
                currentQueueTracks = currentQueueTracks.toMutableList().apply { removeAt(playableIndex) }
            }
        }

        val playingTrackId = current.currentTrack?.id
        val newIndex = if (removedTrack.id == playingTrackId) {
            index.coerceAtMost(updatedQueue.lastIndex).coerceAtLeast(0)
        } else {
            updatedQueue.indexOfFirst { it.id == playingTrackId }.let { if (it >= 0) it else current.queueIndex }
        }
        val newCurrentTrack = if (removedTrack.id == playingTrackId) updatedQueue.getOrNull(newIndex) else current.currentTrack

        _state.update {
            it.copy(
                queue = updatedQueue,
                queueIndex = newIndex,
                currentTrack = newCurrentTrack ?: it.currentTrack
            )
        }
    }

    override fun clearQueue() {
        val current = _state.value
        val playing = current.currentTrack ?: return

        if (current.hasPlayableMedia) {
            controller?.let { mediaController ->
                val playableIndex = currentQueueTracks.indexOfFirst { it.id == playing.id }
                // Remove everything except the currently playing item so
                // playback of the current track is undisturbed.
                for (i in mediaController.mediaItemCount - 1 downTo 0) {
                    if (i != playableIndex) mediaController.removeMediaItem(i)
                }
            }
            currentQueueTracks = listOfNotNull(currentQueueTracks.find { it.id == playing.id })
        }

        _state.update { it.copy(queue = listOf(playing), queueIndex = 0) }
    }

    override fun playQueueItemAt(index: Int) {
        val queue = _state.value.queue
        if (index !in queue.indices) return
        // Re-run through playQueue so the unplayable/placeholder-URI
        // handling stays in exactly one place.
        playQueue(queue, index)
    }

    override fun release() {
        stopPositionPolling()
        controller?.removeListener(playerListener)
        connection.release()
        controller = null
    }

    private fun startPositionPolling() {
        stopPositionPolling()
        positionPollingJob = scope.launch {
            while (isActive) {
                val mediaController = controller
                if (mediaController != null && _state.value.hasPlayableMedia) {
                    _state.update {
                        it.copy(
                            currentPositionMs = mediaController.currentPosition.coerceAtLeast(0L),
                            durationMs = mediaController.duration.takeIf { d -> d > 0 } ?: it.durationMs
                        )
                    }
                }
                delay(500)
            }
        }
    }

    private fun stopPositionPolling() {
        positionPollingJob?.cancel()
        positionPollingJob = null
    }
}

/**
 * Sample/placeholder tracks use `sample://track/N` URIs that don't
 * point at real media. Real device audio (Phase 4+ MediaStore
 * integration) will use `content://` URIs, which this check accepts.
 */
private fun Track.hasResolvableUri(): Boolean {
    val parsed = runCatching { Uri.parse(uri) }.getOrNull() ?: return false
    return parsed.scheme == "content" || parsed.scheme == "file" || parsed.scheme == "http" || parsed.scheme == "https"
}

private fun Track.toMediaItem(): MediaItem {
    return MediaItem.Builder()
        .setUri(uri)
        .setMediaId(id.toString())
        .setMediaMetadata(
            MediaMetadata.Builder()
                .setTitle(title)
                .setArtist(artist)
                .setAlbumTitle(album)
                .setArtworkUri(artworkUri?.let { Uri.parse(it) })
                .build()
        )
        .build()
}

private fun Int.toDomainRepeatMode(): RepeatMode = when (this) {
    Player.REPEAT_MODE_ONE -> RepeatMode.ONE
    Player.REPEAT_MODE_ALL -> RepeatMode.ALL
    else -> RepeatMode.OFF
}
