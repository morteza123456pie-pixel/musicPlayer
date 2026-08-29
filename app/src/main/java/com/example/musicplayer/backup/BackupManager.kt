package com.example.musicplayer.backup

import android.content.Context
import android.net.Uri
import com.example.musicplayer.data.favorites.FavoriteEntity
import com.example.musicplayer.data.favorites.FavoritesRepository
import com.example.musicplayer.data.settings.AppPreferences
import com.example.musicplayer.data.settings.EqualizerPreferences
import com.example.musicplayer.data.settings.PlaybackPreferences
import com.example.musicplayer.data.settings.SleepTimerPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Foundation for user-settings backup/restore (Phase 5 scope): gathers
 * favorites + preferences into [BackupData], serializes/deserializes
 * via [BackupSerializer], and reads/writes through the [Uri]s handed
 * back by the Android system file picker (Storage Access Framework) —
 * see the Settings > Backup & Restore screen, which launches
 * `ACTION_CREATE_DOCUMENT`/`ACTION_OPEN_DOCUMENT` for this.
 *
 * Never touches audio file bytes: [gatherCurrentState] only reads the
 * favorite table's track ids (see [FavoriteEntity]) and preference
 * values, never anything from device media storage.
 */
@Singleton
class BackupManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val favoritesRepository: FavoritesRepository,
    private val equalizerPreferences: EqualizerPreferences,
    private val appPreferences: AppPreferences,
    private val playbackPreferences: PlaybackPreferences,
    private val sleepTimerPreferences: SleepTimerPreferences
) {
    suspend fun gatherCurrentState(): BackupData {
        val favoriteIds = favoritesRepository.favoriteTrackIdsOrdered.first()
        val eq = equalizerPreferences.current()
        val playback = playbackPreferences.snapshot.first()
        val sleepTimer = sleepTimerPreferences.current()

        return BackupData(
            exportedAtMs = System.currentTimeMillis(),
            favorites = favoriteIds.map { FavoriteEntry(trackId = it, favoritedAtMs = System.currentTimeMillis()) },
            equalizer = EqualizerBackupSection(
                enabled = eq.enabled,
                preset = eq.preset,
                bandLevels = eq.bandLevels,
                bassBoostEnabled = eq.bassBoostEnabled,
                bassBoostStrength = eq.bassBoostStrength
            ),
            appPreferences = AppPreferencesBackupSection(
                appearance = appPreferences.appearance.value,
                language = appPreferences.language.value
            ),
            playbackPreferences = PlaybackPreferencesBackupSection(
                skipSilenceEnabled = playback.skipSilenceEnabled,
                resumePlaybackEnabled = playback.resumePlaybackEnabled
            ),
            sleepTimer = SleepTimerBackupSection(defaultEndBehavior = sleepTimer.endBehavior)
        )
    }

    /** Writes the current app state as JSON to [destination] (a Uri from ACTION_CREATE_DOCUMENT). Returns true on success. */
    suspend fun exportTo(destination: Uri): Boolean = runCatching {
        val data = gatherCurrentState()
        val json = BackupSerializer.serialize(data)
        context.contentResolver.openOutputStream(destination)?.use { stream ->
            stream.write(json.toByteArray(Charsets.UTF_8))
        } ?: return false
        true
    }.getOrDefault(false)

    /** Reads and validates a backup file at [source] (a Uri from ACTION_OPEN_DOCUMENT) without applying anything yet. */
    suspend fun readAndValidate(source: Uri): BackupValidationResult = runCatching {
        val json = context.contentResolver.openInputStream(source)?.use { stream ->
            stream.readBytes().toString(Charsets.UTF_8)
        } ?: return BackupValidationResult.InvalidFormat

        val data = BackupSerializer.deserialize(json) ?: return BackupValidationResult.InvalidFormat
        if (data.version > CURRENT_BACKUP_VERSION) {
            return BackupValidationResult.UnsupportedVersion(data.version)
        }
        BackupValidationResult.Valid(data, favoriteCount = data.favorites.size)
    }.getOrDefault(BackupValidationResult.InvalidFormat)

    /**
     * Applies a validated [BackupData]. Only ever called after the
     * user has explicitly confirmed the import in the UI — this class
     * has no "auto-apply on read" path, matching the "do not overwrite
     * current data silently" requirement.
     *
     * [replaceFavorites]: true clears existing favorites first
     * (replace), false merges (keeps existing + adds imported).
     */
    suspend fun applyBackup(data: BackupData, replaceFavorites: Boolean) {
        if (replaceFavorites) {
            val currentIds = favoritesRepository.favoriteTrackIdsOrdered.first()
            currentIds.forEach { favoritesRepository.setFavorite(it, false) }
        }
        data.favorites.forEach { entry ->
            favoritesRepository.setFavorite(entry.trackId, true)
        }

        equalizerPreferences.setEnabled(data.equalizer.enabled)
        equalizerPreferences.setPreset(data.equalizer.preset)
        equalizerPreferences.setBandLevels(data.equalizer.bandLevels)
        equalizerPreferences.setBassBoostEnabled(data.equalizer.bassBoostEnabled)
        equalizerPreferences.setBassBoostStrength(data.equalizer.bassBoostStrength)

        appPreferences.setAppearance(data.appPreferences.appearance)
        appPreferences.setLanguage(data.appPreferences.language)

        playbackPreferences.setSkipSilenceEnabled(data.playbackPreferences.skipSilenceEnabled)
        playbackPreferences.setResumePlaybackEnabled(data.playbackPreferences.resumePlaybackEnabled)

        // Only the default end-behavior preference is restored — see
        // SleepTimerBackupSection's docs for why an in-progress
        // countdown is never included/restored.
    }
}
