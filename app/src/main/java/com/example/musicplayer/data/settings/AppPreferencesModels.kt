package com.example.musicplayer.data.settings

/** Appearance / theme preference. Light is intentionally excluded from being "fully supported" per Phase 5 scope — see AppearanceOption docs. */
enum class AppearanceOption {
    SYSTEM_DEFAULT,
    DARK,
    LIGHT
}

/**
 * App display-language preference. This only controls text/string
 * localization — it must never affect the music UI's layout direction.
 * See [com.example.musicplayer.ui.theme.MusicPlayerTheme]'s hardcoded
 * `LayoutDirection.Ltr` override, which this preference does not (and
 * must not) touch.
 */
enum class AppLanguage(val tag: String) {
    SYSTEM_DEFAULT("system"),
    ENGLISH("en"),
    PERSIAN("fa")
}

enum class SleepTimerDefaultBehaviorOption {
    PAUSE,
    STOP_AND_CLEAR
}

/**
 * Playback preferences. [crossfadeEnabled] and [skipSilenceEnabled]
 * are exposed only when the underlying player support is verified —
 * see [PlaybackCapabilities] — so the Settings screen never shows a
 * switch that doesn't actually do anything.
 */
data class PlaybackPreferencesSnapshot(
    val crossfadeEnabled: Boolean,
    val crossfadeDurationMs: Long,
    val skipSilenceEnabled: Boolean,
    val resumePlaybackEnabled: Boolean
)

data class AppPreferencesSnapshot(
    val appearance: AppearanceOption,
    val language: AppLanguage
)
