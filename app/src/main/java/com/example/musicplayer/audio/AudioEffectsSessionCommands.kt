package com.example.musicplayer.audio

import android.os.Bundle
import androidx.media3.session.SessionCommand

/**
 * Custom Media3 session commands used to control the real audio
 * effects that live inside [com.example.musicplayer.service.MusicPlaybackService].
 *
 * The UI process only ever holds a [androidx.media3.session.MediaController]
 * (see [com.example.musicplayer.player.PlayerServiceConnection]), which
 * has no direct access to platform [android.media.audiofx.Equalizer]/
 * [android.media.audiofx.BassBoost] objects — those must be constructed
 * in the same process as the real ExoPlayer audio session. Custom
 * session commands are Media3's supported bridge for exactly this:
 * the controller sends a named command + Bundle of args, the session's
 * [androidx.media3.session.MediaSession.Callback.onCustomCommand]
 * forwards it to [AudioEffectsManager], and the manager's resulting
 * [EqualizerUiSnapshot] is returned in the [androidx.media3.session.SessionResult]
 * Bundle so the caller gets a fresh snapshot back immediately (no
 * separate broadcast channel needed).
 */
object AudioEffectsSessionCommands {
    const val ACTION_GET_STATE = "com.example.musicplayer.audio.GET_STATE"
    const val ACTION_SET_ENABLED = "com.example.musicplayer.audio.SET_ENABLED"
    const val ACTION_SET_BAND_LEVEL = "com.example.musicplayer.audio.SET_BAND_LEVEL"
    const val ACTION_APPLY_PRESET = "com.example.musicplayer.audio.APPLY_PRESET"
    const val ACTION_SET_BASS_BOOST_ENABLED = "com.example.musicplayer.audio.SET_BASS_BOOST_ENABLED"
    const val ACTION_SET_BASS_BOOST_STRENGTH = "com.example.musicplayer.audio.SET_BASS_BOOST_STRENGTH"
    const val ACTION_SET_SKIP_SILENCE = "com.example.musicplayer.audio.SET_SKIP_SILENCE"

    const val KEY_ENABLED = "enabled"
    const val KEY_BAND_INDEX = "band_index"
    const val KEY_LEVEL_MILLIBEL = "level_millibel"
    const val KEY_PRESET_NAME = "preset_name"
    const val KEY_STRENGTH = "strength"

    // Result bundle keys describing an EqualizerUiSnapshot, since a
    // Bundle can't carry the data class directly across the session
    // boundary.
    const val RESULT_IS_SUPPORTED = "is_supported"
    const val RESULT_IS_ENABLED = "is_enabled"
    const val RESULT_SELECTED_PRESET = "selected_preset"
    const val RESULT_BASS_SUPPORTED = "bass_supported"
    const val RESULT_BASS_ENABLED = "bass_enabled"
    const val RESULT_BASS_STRENGTH = "bass_strength"
    const val RESULT_BAND_COUNT = "band_count"
    const val RESULT_BAND_INDEX_PREFIX = "band_index_"
    const val RESULT_BAND_FREQ_PREFIX = "band_freq_"
    const val RESULT_BAND_MIN_PREFIX = "band_min_"
    const val RESULT_BAND_MAX_PREFIX = "band_max_"
    const val RESULT_BAND_LEVEL_PREFIX = "band_level_"

    val allCommands: List<SessionCommand> = listOf(
        SessionCommand(ACTION_GET_STATE, Bundle.EMPTY),
        SessionCommand(ACTION_SET_ENABLED, Bundle.EMPTY),
        SessionCommand(ACTION_SET_BAND_LEVEL, Bundle.EMPTY),
        SessionCommand(ACTION_APPLY_PRESET, Bundle.EMPTY),
        SessionCommand(ACTION_SET_BASS_BOOST_ENABLED, Bundle.EMPTY),
        SessionCommand(ACTION_SET_BASS_BOOST_STRENGTH, Bundle.EMPTY),
        SessionCommand(ACTION_SET_SKIP_SILENCE, Bundle.EMPTY)
    )
}

/** Serializes an [EqualizerUiSnapshot] into a result [Bundle] for the [androidx.media3.session.SessionResult]. */
fun EqualizerUiSnapshot.toResultBundle(): Bundle = Bundle().apply {
    putBoolean(AudioEffectsSessionCommands.RESULT_IS_SUPPORTED, isSupported)
    putBoolean(AudioEffectsSessionCommands.RESULT_IS_ENABLED, isEnabled)
    putString(AudioEffectsSessionCommands.RESULT_SELECTED_PRESET, selectedPreset.name)
    putBoolean(AudioEffectsSessionCommands.RESULT_BASS_SUPPORTED, isBassBoostSupported)
    putBoolean(AudioEffectsSessionCommands.RESULT_BASS_ENABLED, isBassBoostEnabled)
    putInt(AudioEffectsSessionCommands.RESULT_BASS_STRENGTH, bassBoostStrength)
    putInt(AudioEffectsSessionCommands.RESULT_BAND_COUNT, bands.size)
    bands.forEachIndexed { i, band ->
        putInt("${AudioEffectsSessionCommands.RESULT_BAND_INDEX_PREFIX}$i", band.index)
        putInt("${AudioEffectsSessionCommands.RESULT_BAND_FREQ_PREFIX}$i", band.centerFrequencyHz)
        putInt("${AudioEffectsSessionCommands.RESULT_BAND_MIN_PREFIX}$i", band.minLevelMillibel)
        putInt("${AudioEffectsSessionCommands.RESULT_BAND_MAX_PREFIX}$i", band.maxLevelMillibel)
        putInt("${AudioEffectsSessionCommands.RESULT_BAND_LEVEL_PREFIX}$i", band.levelMillibel)
    }
}

/** Reconstructs an [EqualizerUiSnapshot] from a result [Bundle] produced by [toResultBundle]. */
fun Bundle.toEqualizerSnapshot(): EqualizerUiSnapshot {
    val bandCount = getInt(AudioEffectsSessionCommands.RESULT_BAND_COUNT, 0)
    val bands = (0 until bandCount).map { i ->
        EqualizerBand(
            index = getInt("${AudioEffectsSessionCommands.RESULT_BAND_INDEX_PREFIX}$i", i),
            centerFrequencyHz = getInt("${AudioEffectsSessionCommands.RESULT_BAND_FREQ_PREFIX}$i", 0),
            minLevelMillibel = getInt("${AudioEffectsSessionCommands.RESULT_BAND_MIN_PREFIX}$i", -1500),
            maxLevelMillibel = getInt("${AudioEffectsSessionCommands.RESULT_BAND_MAX_PREFIX}$i", 1500),
            levelMillibel = getInt("${AudioEffectsSessionCommands.RESULT_BAND_LEVEL_PREFIX}$i", 0)
        )
    }
    return EqualizerUiSnapshot(
        isSupported = getBoolean(AudioEffectsSessionCommands.RESULT_IS_SUPPORTED, false),
        isEnabled = getBoolean(AudioEffectsSessionCommands.RESULT_IS_ENABLED, false),
        bands = bands,
        selectedPreset = getString(AudioEffectsSessionCommands.RESULT_SELECTED_PRESET)?.let { name ->
            runCatching { AudioPreset.valueOf(name) }.getOrNull()
        } ?: AudioPreset.FLAT,
        isBassBoostSupported = getBoolean(AudioEffectsSessionCommands.RESULT_BASS_SUPPORTED, false),
        isBassBoostEnabled = getBoolean(AudioEffectsSessionCommands.RESULT_BASS_ENABLED, false),
        bassBoostStrength = getInt(AudioEffectsSessionCommands.RESULT_BASS_STRENGTH, 0),
        isReady = true
    )
}
