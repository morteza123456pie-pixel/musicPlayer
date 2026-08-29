package com.example.musicplayer.audio

import android.os.Bundle
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionResult
import com.example.musicplayer.data.settings.PlaybackPreferences
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * [MediaSession.Callback] that authorizes the custom audio-effects
 * commands (so [androidx.media3.session.MediaController.sendCustomCommand]
 * calls from the UI process are accepted rather than rejected — see
 * the "Controller isn't allowed to call custom session command"
 * failure mode this avoids) and dispatches them into
 * [AudioEffectsManager].
 *
 * Deliberately holds no reference to the ExoPlayer instance itself —
 * every command is a plain (index, value) tuple forwarded to the
 * manager, which is the only thing that touches real platform
 * AudioEffect objects. This keeps [com.example.musicplayer.service.MusicPlaybackService]
 * free of audio-effects logic beyond wiring this callback in.
 */
class AudioEffectsSessionCallback(
    private val audioEffectsManager: AudioEffectsManager,
    private val player: ExoPlayer,
    private val playbackPreferences: PlaybackPreferences,
    private val serviceScope: CoroutineScope
) : MediaSession.Callback {

    override fun onConnect(
        session: MediaSession,
        controller: MediaSession.ControllerInfo
    ): MediaSession.ConnectionResult {
        val availableCommands = MediaSession.ConnectionResult.DEFAULT_SESSION_AND_LIBRARY_COMMANDS
            .buildUpon()
            .apply { AudioEffectsSessionCommands.allCommands.forEach(::add) }
            .build()
        return MediaSession.ConnectionResult.AcceptedResultBuilder(session)
            .setAvailableSessionCommands(availableCommands)
            .build()
    }

    override fun onCustomCommand(
        session: MediaSession,
        controller: MediaSession.ControllerInfo,
        customCommand: SessionCommand,
        args: Bundle
    ): ListenableFuture<SessionResult> {
        when (customCommand.customAction) {
            AudioEffectsSessionCommands.ACTION_GET_STATE -> {
                // No-op: current state is read below and returned for every branch.
            }
            AudioEffectsSessionCommands.ACTION_SET_ENABLED -> {
                audioEffectsManager.setEnabled(args.getBoolean(AudioEffectsSessionCommands.KEY_ENABLED))
            }
            AudioEffectsSessionCommands.ACTION_SET_BAND_LEVEL -> {
                audioEffectsManager.setBandLevel(
                    bandIndex = args.getInt(AudioEffectsSessionCommands.KEY_BAND_INDEX),
                    levelMillibel = args.getInt(AudioEffectsSessionCommands.KEY_LEVEL_MILLIBEL)
                )
            }
            AudioEffectsSessionCommands.ACTION_APPLY_PRESET -> {
                val presetName = args.getString(AudioEffectsSessionCommands.KEY_PRESET_NAME)
                val preset = presetName?.let { runCatching { AudioPreset.valueOf(it) }.getOrNull() }
                if (preset != null) audioEffectsManager.applyPreset(preset)
            }
            AudioEffectsSessionCommands.ACTION_SET_BASS_BOOST_ENABLED -> {
                audioEffectsManager.setBassBoostEnabled(args.getBoolean(AudioEffectsSessionCommands.KEY_ENABLED))
            }
            AudioEffectsSessionCommands.ACTION_SET_BASS_BOOST_STRENGTH -> {
                audioEffectsManager.setBassBoostStrength(args.getInt(AudioEffectsSessionCommands.KEY_STRENGTH))
            }
            AudioEffectsSessionCommands.ACTION_SET_SKIP_SILENCE -> {
                val enabled = args.getBoolean(AudioEffectsSessionCommands.KEY_ENABLED)
                // Real ExoPlayer feature (SilenceSkippingAudioProcessor),
                // applied directly to the live player — not simulated.
                runCatching { player.skipSilenceEnabled = enabled }
                serviceScope.launch { playbackPreferences.setSkipSilenceEnabled(enabled) }
            }
            else -> {
                return Futures.immediateFuture(SessionResult(SessionResult.RESULT_ERROR_NOT_SUPPORTED))
            }
        }

        val resultBundle = audioEffectsManager.state.value.toResultBundle()
        return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS, resultBundle))
    }
}
