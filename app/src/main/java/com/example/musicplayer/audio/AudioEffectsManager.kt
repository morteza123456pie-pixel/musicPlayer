package com.example.musicplayer.audio

import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.util.Log
import com.example.musicplayer.data.settings.EqualizerPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "AudioEffectsManager"

/**
 * Owns the real [Equalizer] and [BassBoost] platform audio effects and
 * attaches them to whichever ExoPlayer audio session is currently
 * active.
 *
 * Lives for the lifetime of [com.example.musicplayer.service.MusicPlaybackService]
 * (it's a Hilt singleton, but the effects it holds are only ever
 * constructed/attached while the service — and therefore the real
 * AudioTrack session — is alive). The UI never touches this class
 * directly: [com.example.musicplayer.player.MusicPlayerController]
 * forwards commands to the service, which calls into this manager, and
 * [state] is what the Equalizer screen actually observes (via the
 * repository layer).
 *
 * Session handling: ExoPlayer assigns a new native audio session id
 * whenever the player is reset or, on some devices, when output
 * routing changes. [attachToSession] is safe to call repeatedly with
 * the same id (no-op) or a new one (old effects are released first,
 * then recreated on the new session) — this is what "safely recreated
 * or reattached" means in practice for platform AudioEffects, which
 * are hard-tied to a single session id at construction time.
 */
@Singleton
class AudioEffectsManager @Inject constructor(
    private val preferences: EqualizerPreferences
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private var equalizer: Equalizer? = null
    private var bassBoost: BassBoost? = null
    private var currentSessionId: Int = 0

    private val _state = MutableStateFlow(EqualizerUiSnapshot())
    val state: StateFlow<EqualizerUiSnapshot> = _state.asStateFlow()

    // Custom (non-native) preset band offsets in millibel, applied on
    // top of a flat baseline and clamped to each band's real min/max.
    // Used when the platform doesn't expose a matching native preset,
    // or exposes a different number/order of presets than we want to
    // show. Keyed by approximate band position (low/low-mid/mid/high-mid/high)
    // rather than a fixed band count, and interpolated across however
    // many bands the device actually has.
    private val customPresetCurves: Map<AudioPreset, List<Int>> = mapOf(
        AudioPreset.FLAT to listOf(0, 0, 0, 0, 0),
        AudioPreset.POP to listOf(-100, 200, 300, 100, -100),
        AudioPreset.ROCK to listOf(400, 200, -100, 200, 300),
        AudioPreset.JAZZ to listOf(200, 100, -100, 100, 200),
        AudioPreset.CLASSICAL to listOf(300, 200, 0, 200, 300)
    )

    /**
     * Attaches (or reattaches, if [sessionId] changed) real audio
     * effects to the given ExoPlayer audio session, then applies
     * whatever was last persisted so processing resumes exactly where
     * the user left it.
     */
    fun attachToSession(sessionId: Int) {
        if (sessionId == 0 || sessionId == C_AUDIO_SESSION_ID_UNSET) return
        if (sessionId == currentSessionId && equalizer != null) return

        Log.d(TAG, "Attaching audio effects to session $sessionId (previous: $currentSessionId)")
        releaseEffects()
        currentSessionId = sessionId

        val newEqualizer = runCatching { Equalizer(0, sessionId) }.getOrNull()
        val newBassBoost = runCatching { BassBoost(0, sessionId) }.getOrNull()

        equalizer = newEqualizer
        bassBoost = newBassBoost

        scope.launch {
            val saved = preferences.current()
            applyPersistedState(saved)
            publishState()
        }
    }

    /** Call when the service/player is being torn down. Never leave effects attached past this point. */
    fun release() {
        releaseEffects()
        currentSessionId = 0
        _state.value = EqualizerUiSnapshot()
    }

    private fun releaseEffects() {
        equalizer?.let { eq ->
            runCatching { eq.enabled = false }
            runCatching { eq.release() }
        }
        bassBoost?.let { bb ->
            runCatching { bb.enabled = false }
            runCatching { bb.release() }
        }
        equalizer = null
        bassBoost = null
    }

    private fun applyPersistedState(saved: EqualizerPreferences.Snapshot) {
        val eq = equalizer
        if (eq != null) {
            runCatching { eq.enabled = saved.enabled }
            if (saved.bandLevels.isNotEmpty()) {
                saved.bandLevels.forEach { (index, level) ->
                    runCatching {
                        if (index < eq.numberOfBands) {
                            eq.setBandLevel(index.toShort(), level.toShort())
                        }
                    }
                }
            } else {
                applyPresetInternal(eq, saved.preset)
            }
        }
        bassBoost?.let { bb ->
            runCatching { bb.enabled = saved.bassBoostEnabled }
            runCatching { bb.setStrength(saved.bassBoostStrength.toShort()) }
        }
    }

    fun setEnabled(enabled: Boolean) {
        val eq = equalizer ?: return
        runCatching { eq.enabled = enabled }
        scope.launch { preferences.setEnabled(enabled) }
        publishState()
    }

    fun setBandLevel(bandIndex: Int, levelMillibel: Int) {
        val eq = equalizer ?: return
        val clamped = levelMillibel.coerceIn(
            eq.bandLevelRange.getOrNull(0)?.toInt() ?: Short.MIN_VALUE.toInt(),
            eq.bandLevelRange.getOrNull(1)?.toInt() ?: Short.MAX_VALUE.toInt()
        )
        runCatching { eq.setBandLevel(bandIndex.toShort(), clamped.toShort()) }
        scope.launch {
            val currentLevels = currentBandLevelsMap(eq)
            preferences.setBandLevels(currentLevels)
            // Manual band edit means we're no longer strictly on a
            // named preset's curve.
            preferences.setPreset(AudioPreset.CUSTOM)
        }
        publishState(forcedPreset = AudioPreset.CUSTOM)
    }

    fun applyPreset(preset: AudioPreset) {
        val eq = equalizer ?: return
        applyPresetInternal(eq, preset)
        scope.launch {
            preferences.setPreset(preset)
            preferences.setBandLevels(currentBandLevelsMap(eq))
        }
        publishState(forcedPreset = preset)
    }

    private fun applyPresetInternal(eq: Equalizer, preset: AudioPreset) {
        val nativeIndex = preset.nativePresetIndex
            ?: findMatchingNativePreset(eq, preset)

        if (nativeIndex != null) {
            runCatching { eq.usePreset(nativeIndex.toShort()) }
            return
        }

        // No native preset available/matching — fall back to our own
        // curve, interpolated across the device's real band count.
        val curve = customPresetCurves[preset] ?: customPresetCurves.getValue(AudioPreset.FLAT)
        val bandCount = runCatching { eq.numberOfBands.toInt() }.getOrDefault(0)
        for (band in 0 until bandCount) {
            val curveIndex = ((band.toFloat() / (bandCount - 1).coerceAtLeast(1)) * (curve.size - 1))
                .toInt()
                .coerceIn(0, curve.lastIndex)
            val range = runCatching { eq.bandLevelRange }.getOrNull()
            val min = range?.getOrNull(0)?.toInt() ?: -1500
            val max = range?.getOrNull(1)?.toInt() ?: 1500
            val level = curve[curveIndex].coerceIn(min, max)
            runCatching { eq.setBandLevel(band.toShort(), level.toShort()) }
        }
    }

    /** Best-effort: if the device exposes a native preset whose name matches, prefer real hardware/DSP processing over our approximation. */
    private fun findMatchingNativePreset(eq: Equalizer, preset: AudioPreset): Int? {
        val presetCount = runCatching { eq.numberOfPresets.toInt() }.getOrDefault(0)
        for (i in 0 until presetCount) {
            val name = runCatching { eq.getPresetName(i.toShort()) }.getOrNull() ?: continue
            if (name.equals(preset.displayName, ignoreCase = true)) return i
        }
        return null
    }

    fun setBassBoostEnabled(enabled: Boolean) {
        val bb = bassBoost ?: return
        runCatching { bb.enabled = enabled }
        scope.launch { preferences.setBassBoostEnabled(enabled) }
        publishState()
    }

    fun setBassBoostStrength(strength: Int) {
        val bb = bassBoost ?: return
        val clamped = strength.coerceIn(0, 1000)
        runCatching { bb.setStrength(clamped.toShort()) }
        scope.launch { preferences.setBassBoostStrength(clamped) }
        publishState()
    }

    private fun currentBandLevelsMap(eq: Equalizer): Map<Int, Int> {
        val bandCount = runCatching { eq.numberOfBands.toInt() }.getOrDefault(0)
        return (0 until bandCount).associateWith { band ->
            runCatching { eq.getBandLevel(band.toShort()).toInt() }.getOrDefault(0)
        }
    }

    private fun publishState(forcedPreset: AudioPreset? = null) {
        val eq = equalizer
        val bb = bassBoost

        val bands = if (eq != null) {
            val bandCount = runCatching { eq.numberOfBands.toInt() }.getOrDefault(0)
            (0 until bandCount).map { band ->
                val range = runCatching { eq.bandLevelRange }.getOrNull()
                EqualizerBand(
                    index = band,
                    centerFrequencyHz = runCatching { eq.getCenterFreq(band.toShort()) / 1000 }.getOrDefault(0),
                    minLevelMillibel = range?.getOrNull(0)?.toInt() ?: -1500,
                    maxLevelMillibel = range?.getOrNull(1)?.toInt() ?: 1500,
                    levelMillibel = runCatching { eq.getBandLevel(band.toShort()).toInt() }.getOrDefault(0)
                )
            }
        } else emptyList()

        _state.update {
            it.copy(
                isSupported = eq != null,
                isEnabled = runCatching { eq?.enabled }.getOrNull() ?: false,
                bands = bands,
                selectedPreset = forcedPreset ?: it.selectedPreset,
                isBassBoostSupported = bb != null && runCatching { bb.strengthSupported }.getOrDefault(false),
                isBassBoostEnabled = runCatching { bb?.enabled }.getOrNull() ?: false,
                bassBoostStrength = runCatching { bb?.roundedStrength?.toInt() }.getOrNull() ?: 0,
                isReady = true
            )
        }
    }

    companion object {
        private const val C_AUDIO_SESSION_ID_UNSET = 0
    }
}
