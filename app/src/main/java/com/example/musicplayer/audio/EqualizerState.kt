package com.example.musicplayer.audio

/**
 * One equalizer frequency band, as reported by the platform
 * [android.media.audiofx.Equalizer] for the active audio session.
 * Never hardcoded — [AudioEffectsManager] populates this list from
 * whatever the real device/session actually exposes, so devices with
 * 5 bands, 6 bands, or a different frequency spread all render
 * correctly rather than assuming a fixed layout.
 */
data class EqualizerBand(
    val index: Int,
    val centerFrequencyHz: Int,
    val minLevelMillibel: Int,
    val maxLevelMillibel: Int,
    val levelMillibel: Int
)

/** Named preset. [nativePresetIndex] is set when a real platform preset backs it, null when we map it to a custom band configuration instead. */
enum class AudioPreset(val displayName: String, val nativePresetIndex: Int?) {
    FLAT("Flat", null),
    POP("Pop", null),
    ROCK("Rock", null),
    JAZZ("Jazz", null),
    CLASSICAL("Classic", null),
    CUSTOM("Custom", null)
}

/**
 * Single source of truth for equalizer + bass boost UI state. Built
 * fresh by [AudioEffectsManager]/[EqualizerController] from the real
 * platform effect plus persisted preferences — the UI layer only ever
 * reads this, never an [android.media.audiofx.Equalizer] instance.
 */
data class EqualizerUiSnapshot(
    val isSupported: Boolean = false,
    val isEnabled: Boolean = false,
    val bands: List<EqualizerBand> = emptyList(),
    val selectedPreset: AudioPreset = AudioPreset.FLAT,
    val isBassBoostSupported: Boolean = false,
    val isBassBoostEnabled: Boolean = false,
    /** 0-1000 per [android.media.audiofx.BassBoost.setStrength]. */
    val bassBoostStrength: Int = 0,
    val isReady: Boolean = false
)
