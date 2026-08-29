package com.example.musicplayer.backup

import com.example.musicplayer.audio.AudioPreset
import com.example.musicplayer.data.settings.AppLanguage
import com.example.musicplayer.data.settings.AppearanceOption
import com.example.musicplayer.sleeptimer.SleepTimerEndBehavior
import org.json.JSONArray
import org.json.JSONObject

/**
 * Hand-rolled JSON (de)serialization for [BackupData] using the
 * `org.json` classes already bundled with the Android platform SDK —
 * no new Gson/Moshi/kotlinx.serialization dependency needed for a
 * schema this small and stable. [serialize] produces pretty-printed
 * JSON so an exported backup file is also human-readable/diffable if
 * a user opens it directly.
 */
object BackupSerializer {

    fun serialize(data: BackupData): String {
        val root = JSONObject()
        root.put("version", data.version)
        root.put("exportedAtMs", data.exportedAtMs)

        val favoritesArray = JSONArray()
        data.favorites.forEach { fav ->
            favoritesArray.put(
                JSONObject().apply {
                    put("trackId", fav.trackId)
                    put("favoritedAtMs", fav.favoritedAtMs)
                }
            )
        }
        root.put("favorites", favoritesArray)

        root.put(
            "equalizer",
            JSONObject().apply {
                put("enabled", data.equalizer.enabled)
                put("preset", data.equalizer.preset.name)
                put(
                    "bandLevels",
                    JSONObject().apply {
                        data.equalizer.bandLevels.forEach { (index, level) -> put(index.toString(), level) }
                    }
                )
                put("bassBoostEnabled", data.equalizer.bassBoostEnabled)
                put("bassBoostStrength", data.equalizer.bassBoostStrength)
            }
        )

        root.put(
            "appPreferences",
            JSONObject().apply {
                put("appearance", data.appPreferences.appearance.name)
                put("language", data.appPreferences.language.name)
            }
        )

        root.put(
            "playbackPreferences",
            JSONObject().apply {
                put("skipSilenceEnabled", data.playbackPreferences.skipSilenceEnabled)
                put("resumePlaybackEnabled", data.playbackPreferences.resumePlaybackEnabled)
            }
        )

        root.put(
            "sleepTimer",
            JSONObject().apply {
                put("defaultEndBehavior", data.sleepTimer.defaultEndBehavior.name)
            }
        )

        return root.toString(2)
    }

    /**
     * Returns null (rather than throwing) on any malformed/unrecognized
     * input, so callers can present a clean "invalid backup file"
     * message instead of crashing on a corrupted or foreign JSON file.
     */
    fun deserialize(json: String): BackupData? = runCatching {
        val root = JSONObject(json)
        val version = root.optInt("version", -1)
        if (version <= 0) return null

        val favoritesArray = root.optJSONArray("favorites") ?: JSONArray()
        val favorites = (0 until favoritesArray.length()).mapNotNull { i ->
            val obj = favoritesArray.optJSONObject(i) ?: return@mapNotNull null
            val trackId = obj.optLong("trackId", -1L)
            if (trackId < 0L) return@mapNotNull null
            FavoriteEntry(trackId = trackId, favoritedAtMs = obj.optLong("favoritedAtMs", 0L))
        }

        val eqObj = root.optJSONObject("equalizer") ?: JSONObject()
        val bandLevelsObj = eqObj.optJSONObject("bandLevels") ?: JSONObject()
        val bandLevels = bandLevelsObj.keys().asSequence().mapNotNull { key ->
            val index = key.toIntOrNull() ?: return@mapNotNull null
            index to bandLevelsObj.optInt(key, 0)
        }.toMap()

        val equalizer = EqualizerBackupSection(
            enabled = eqObj.optBoolean("enabled", false),
            preset = eqObj.optString("preset", AudioPreset.FLAT.name).toEnumOrDefault(AudioPreset.FLAT),
            bandLevels = bandLevels,
            bassBoostEnabled = eqObj.optBoolean("bassBoostEnabled", false),
            bassBoostStrength = eqObj.optInt("bassBoostStrength", 0)
        )

        val appPrefsObj = root.optJSONObject("appPreferences") ?: JSONObject()
        val appPreferences = AppPreferencesBackupSection(
            appearance = appPrefsObj.optString("appearance", AppearanceOption.DARK.name)
                .toEnumOrDefault(AppearanceOption.DARK),
            language = appPrefsObj.optString("language", AppLanguage.SYSTEM_DEFAULT.name)
                .toEnumOrDefault(AppLanguage.SYSTEM_DEFAULT)
        )

        val playbackObj = root.optJSONObject("playbackPreferences") ?: JSONObject()
        val playbackPreferences = PlaybackPreferencesBackupSection(
            skipSilenceEnabled = playbackObj.optBoolean("skipSilenceEnabled", false),
            resumePlaybackEnabled = playbackObj.optBoolean("resumePlaybackEnabled", true)
        )

        val sleepObj = root.optJSONObject("sleepTimer") ?: JSONObject()
        val sleepTimer = SleepTimerBackupSection(
            defaultEndBehavior = sleepObj.optString("defaultEndBehavior", SleepTimerEndBehavior.PAUSE.name)
                .toEnumOrDefault(SleepTimerEndBehavior.PAUSE)
        )

        BackupData(
            version = version,
            exportedAtMs = root.optLong("exportedAtMs", 0L),
            favorites = favorites,
            equalizer = equalizer,
            appPreferences = appPreferences,
            playbackPreferences = playbackPreferences,
            sleepTimer = sleepTimer
        )
    }.getOrNull()

    private inline fun <reified T : Enum<T>> String.toEnumOrDefault(default: T): T =
        runCatching { enumValueOf<T>(this) }.getOrDefault(default)

    private fun String.toIntOrNull(): Int? = try {
        this.toInt()
    } catch (e: NumberFormatException) {
        null
    }
}
