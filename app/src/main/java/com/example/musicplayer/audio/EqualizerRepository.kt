package com.example.musicplayer.audio

import android.content.Context
import android.os.Bundle
import androidx.media3.session.MediaController
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionResult
import com.example.musicplayer.player.PlayerServiceConnection
import com.google.common.util.concurrent.MoreExecutors
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

/**
 * UI-facing entry point for equalizer/bass boost control. The
 * Equalizer screen's ViewModel depends on this, never on
 * [AudioEffectsManager] directly — that class only exists inside
 * [com.example.musicplayer.service.MusicPlaybackService]'s process
 * space, and the only supported way to reach it from here is a Media3
 * [MediaController] custom session command (see
 * [AudioEffectsSessionCommands]).
 *
 * Holds its own [PlayerServiceConnection] rather than sharing
 * [com.example.musicplayer.player.MusicPlayerControllerImpl]'s, so
 * this class stays a self-contained, independently testable seam and
 * adding it doesn't touch the existing playback controller at all.
 * Media3 supports multiple simultaneous controllers connected to the
 * same session.
 */
@Singleton
class EqualizerRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val connection = PlayerServiceConnection(context)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var controller: MediaController? = null

    private val _state = MutableStateFlow(EqualizerUiSnapshot())
    val state: StateFlow<EqualizerUiSnapshot> = _state.asStateFlow()

    private suspend fun ensureConnected(): MediaController {
        controller?.let { return it }
        val connected = connection.connect()
        controller = connected
        return connected
    }

    /** Fetches the latest state from the service. Call on screen entry and after any mutation. */
    suspend fun refresh() {
        sendCommand(AudioEffectsSessionCommands.ACTION_GET_STATE, Bundle.EMPTY)
    }

    fun setEnabled(enabled: Boolean) {
        scope.launch {
            sendCommand(
                AudioEffectsSessionCommands.ACTION_SET_ENABLED,
                Bundle().apply { putBoolean(AudioEffectsSessionCommands.KEY_ENABLED, enabled) }
            )
        }
    }

    fun setBandLevel(bandIndex: Int, levelMillibel: Int) {
        scope.launch {
            sendCommand(
                AudioEffectsSessionCommands.ACTION_SET_BAND_LEVEL,
                Bundle().apply {
                    putInt(AudioEffectsSessionCommands.KEY_BAND_INDEX, bandIndex)
                    putInt(AudioEffectsSessionCommands.KEY_LEVEL_MILLIBEL, levelMillibel)
                }
            )
        }
    }

    fun applyPreset(preset: AudioPreset) {
        scope.launch {
            sendCommand(
                AudioEffectsSessionCommands.ACTION_APPLY_PRESET,
                Bundle().apply { putString(AudioEffectsSessionCommands.KEY_PRESET_NAME, preset.name) }
            )
        }
    }

    fun setBassBoostEnabled(enabled: Boolean) {
        scope.launch {
            sendCommand(
                AudioEffectsSessionCommands.ACTION_SET_BASS_BOOST_ENABLED,
                Bundle().apply { putBoolean(AudioEffectsSessionCommands.KEY_ENABLED, enabled) }
            )
        }
    }

    fun setBassBoostStrength(strength: Int) {
        scope.launch {
            sendCommand(
                AudioEffectsSessionCommands.ACTION_SET_BASS_BOOST_STRENGTH,
                Bundle().apply { putInt(AudioEffectsSessionCommands.KEY_STRENGTH, strength) }
            )
        }
    }

    private suspend fun sendCommand(action: String, args: Bundle) {
        val mediaController = ensureConnected()
        val command = SessionCommand(action, Bundle.EMPTY)
        val result = runCatching { awaitSessionResult(mediaController, command, args) }.getOrNull()
        val resultBundle = result?.extras
        if (resultBundle != null) {
            _state.value = resultBundle.toEqualizerSnapshot()
        }
    }

    /**
     * Bridges Media3's Guava [com.google.common.util.concurrent.ListenableFuture]
     * result to a coroutine, matching the pattern already used by
     * [PlayerServiceConnection.connect] elsewhere in the app rather
     * than pulling in the separate kotlinx-coroutines-guava artifact
     * for a single call site.
     */
    private suspend fun awaitSessionResult(
        mediaController: MediaController,
        command: SessionCommand,
        args: Bundle
    ): SessionResult = suspendCancellableCoroutine { continuation ->
        val future = mediaController.sendCustomCommand(command, args)
        future.addListener(
            {
                try {
                    val result = future.get()
                    if (continuation.isActive) continuation.resume(result)
                } catch (t: Throwable) {
                    if (continuation.isActive) continuation.cancel(t)
                }
            },
            MoreExecutors.directExecutor()
        )
        continuation.invokeOnCancellation { future.cancel(false) }
    }

    fun release() {
        connection.release()
        controller = null
    }
}
