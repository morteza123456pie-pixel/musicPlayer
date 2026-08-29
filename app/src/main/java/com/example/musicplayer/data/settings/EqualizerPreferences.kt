package com.example.musicplayer.data.settings

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.musicplayer.audio.AudioPreset
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Single DataStore instance for all app preferences (equalizer,
 * playback, appearance, language, sleep timer). One file, many
 * typed accessor classes below — matches the app's "one Room database,
 * many DAOs" pattern for Room, just for preferences instead.
 */
val Context.settingsDataStore by preferencesDataStore(name = "musicplayer_settings")

/**
 * Persisted equalizer + bass boost state. This is what survives app
 * restart and process recreation — [com.example.musicplayer.audio.AudioEffectsManager]
 * reads it once on startup/session-attach to re-apply real audio
 * processing, and writes to it whenever the user changes something.
 *
 * Band levels are stored as a single serialized "index:millibel,..."
 * string rather than one key per band, since the number of bands is
 * device-dependent and DataStore keys must be declared statically.
 */
@Singleton
class EqualizerPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Keys {
        val ENABLED = booleanPreferencesKey("eq_enabled")
        val PRESET = stringPreferencesKey("eq_preset")
        val BAND_LEVELS = stringPreferencesKey("eq_band_levels")
        val BASS_BOOST_ENABLED = booleanPreferencesKey("eq_bass_boost_enabled")
        val BASS_BOOST_STRENGTH = intPreferencesKey("eq_bass_boost_strength")
    }

    data class Snapshot(
        val enabled: Boolean,
        val preset: AudioPreset,
        val bandLevels: Map<Int, Int>,
        val bassBoostEnabled: Boolean,
        val bassBoostStrength: Int
    )

    val snapshot: Flow<Snapshot> = context.settingsDataStore.data.map { prefs ->
        Snapshot(
            enabled = prefs[Keys.ENABLED] ?: false,
            preset = prefs[Keys.PRESET]?.let { name ->
                runCatching { AudioPreset.valueOf(name) }.getOrNull()
            } ?: AudioPreset.FLAT,
            bandLevels = prefs[Keys.BAND_LEVELS]?.let(::deserializeBandLevels) ?: emptyMap(),
            bassBoostEnabled = prefs[Keys.BASS_BOOST_ENABLED] ?: false,
            bassBoostStrength = prefs[Keys.BASS_BOOST_STRENGTH] ?: 0
        )
    }

    suspend fun current(): Snapshot = snapshot.first()

    suspend fun setEnabled(enabled: Boolean) {
        context.settingsDataStore.edit { it[Keys.ENABLED] = enabled }
    }

    suspend fun setPreset(preset: AudioPreset) {
        context.settingsDataStore.edit { it[Keys.PRESET] = preset.name }
    }

    suspend fun setBandLevels(levels: Map<Int, Int>) {
        context.settingsDataStore.edit { it[Keys.BAND_LEVELS] = serializeBandLevels(levels) }
    }

    suspend fun setBassBoostEnabled(enabled: Boolean) {
        context.settingsDataStore.edit { it[Keys.BASS_BOOST_ENABLED] = enabled }
    }

    suspend fun setBassBoostStrength(strength: Int) {
        context.settingsDataStore.edit { it[Keys.BASS_BOOST_STRENGTH] = strength }
    }

    private fun serializeBandLevels(levels: Map<Int, Int>): String =
        levels.entries.joinToString(",") { (index, level) -> "$index:$level" }

    private fun deserializeBandLevels(raw: String): Map<Int, Int> =
        raw.split(",")
            .mapNotNull { entry ->
                val parts = entry.split(":")
                if (parts.size == 2) {
                    val index = parts[0].toIntOrNull()
                    val level = parts[1].toIntOrNull()
                    if (index != null && level != null) index to level else null
                } else null
            }
            .toMap()
}
