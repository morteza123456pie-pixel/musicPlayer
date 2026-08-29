package com.example.musicplayer.data.settings

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Appearance + language preferences. Reactive (StateFlow) so switching
 * either one updates the UI immediately, matching [com.example.musicplayer.data.favorites.FavoritesRepository]'s
 * pattern of exposing a live StateFlow rather than a one-shot read.
 *
 * Deliberately does NOT expose anything that could influence layout
 * direction — [language] is a pure text-localization preference. The
 * app-wide LTR override lives in [com.example.musicplayer.ui.theme.MusicPlayerTheme]
 * and this class has no path to touch it.
 */
@Singleton
class AppPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Keys {
        val APPEARANCE = stringPreferencesKey("appearance_option")
        val LANGUAGE = stringPreferencesKey("app_language")
    }

    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val appearance: kotlinx.coroutines.flow.StateFlow<AppearanceOption> = context.settingsDataStore.data
        .map { prefs ->
            prefs[Keys.APPEARANCE]?.let { name -> runCatching { AppearanceOption.valueOf(name) }.getOrNull() }
                ?: AppearanceOption.DARK
        }
        .stateIn(repositoryScope, SharingStarted.WhileSubscribed(5_000), AppearanceOption.DARK)

    val language: kotlinx.coroutines.flow.StateFlow<AppLanguage> = context.settingsDataStore.data
        .map { prefs ->
            prefs[Keys.LANGUAGE]?.let { tag -> AppLanguage.entries.find { it.tag == tag } }
                ?: AppLanguage.SYSTEM_DEFAULT
        }
        .stateIn(repositoryScope, SharingStarted.WhileSubscribed(5_000), AppLanguage.SYSTEM_DEFAULT)

    suspend fun setAppearance(option: AppearanceOption) {
        context.settingsDataStore.edit { it[Keys.APPEARANCE] = option.name }
    }

    suspend fun setLanguage(language: AppLanguage) {
        context.settingsDataStore.edit { it[Keys.LANGUAGE] = language.tag }
    }
}

/**
 * Playback preferences. See [com.example.musicplayer.audio.PlaybackCapabilities]
 * for which of these actually do anything on the current player setup
 * — this class only persists the user's choice, it doesn't decide
 * whether the choice is meaningful.
 */
@Singleton
class PlaybackPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Keys {
        val CROSSFADE_ENABLED = booleanPreferencesKey("playback_crossfade_enabled")
        val CROSSFADE_DURATION_MS = longPreferencesKey("playback_crossfade_duration_ms")
        val SKIP_SILENCE_ENABLED = booleanPreferencesKey("playback_skip_silence_enabled")
        val RESUME_PLAYBACK_ENABLED = booleanPreferencesKey("playback_resume_enabled")
    }

    val snapshot: Flow<PlaybackPreferencesSnapshot> = context.settingsDataStore.data.map { prefs ->
        PlaybackPreferencesSnapshot(
            crossfadeEnabled = prefs[Keys.CROSSFADE_ENABLED] ?: false,
            crossfadeDurationMs = prefs[Keys.CROSSFADE_DURATION_MS] ?: 3000L,
            skipSilenceEnabled = prefs[Keys.SKIP_SILENCE_ENABLED] ?: false,
            resumePlaybackEnabled = prefs[Keys.RESUME_PLAYBACK_ENABLED] ?: true
        )
    }

    suspend fun setSkipSilenceEnabled(enabled: Boolean) {
        context.settingsDataStore.edit { it[Keys.SKIP_SILENCE_ENABLED] = enabled }
    }

    suspend fun setResumePlaybackEnabled(enabled: Boolean) {
        context.settingsDataStore.edit { it[Keys.RESUME_PLAYBACK_ENABLED] = enabled }
    }
}
