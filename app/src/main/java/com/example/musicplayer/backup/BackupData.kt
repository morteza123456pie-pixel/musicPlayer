package com.example.musicplayer.backup

import com.example.musicplayer.audio.AudioPreset
import com.example.musicplayer.data.settings.AppLanguage
import com.example.musicplayer.data.settings.AppearanceOption
import com.example.musicplayer.sleeptimer.SleepTimerEndBehavior

/** Bumped whenever [BackupData]'s shape changes in a way older app versions couldn't parse. */
const val CURRENT_BACKUP_VERSION = 1

/**
 * Everything a Phase 5 backup contains. Deliberately excludes any
 * audio file bytes/paths beyond the [FavoriteEntry] track id — per
 * spec, backups never include copyrighted music files, only the
 * user's own preference/favorite data referencing tracks by id.
 */
data class BackupData(
    val version: Int = CURRENT_BACKUP_VERSION,
    val exportedAtMs: Long,
    val favorites: List<FavoriteEntry>,
    val equalizer: EqualizerBackupSection,
    val appPreferences: AppPreferencesBackupSection,
    val playbackPreferences: PlaybackPreferencesBackupSection,
    val sleepTimer: SleepTimerBackupSection
)

data class FavoriteEntry(
    val trackId: Long,
    val favoritedAtMs: Long
)

data class EqualizerBackupSection(
    val enabled: Boolean,
    val preset: AudioPreset,
    val bandLevels: Map<Int, Int>,
    val bassBoostEnabled: Boolean,
    val bassBoostStrength: Int
)

data class AppPreferencesBackupSection(
    val appearance: AppearanceOption,
    val language: AppLanguage
)

data class PlaybackPreferencesBackupSection(
    val skipSilenceEnabled: Boolean,
    val resumePlaybackEnabled: Boolean
)

/**
 * Only the user's *default* end-behavior preference is meaningfully
 * backup-worthy — an in-progress countdown is tied to "now" on the
 * exporting device and would be meaningless (or actively surprising)
 * replayed on a different day/device, so it is intentionally not
 * included here.
 */
data class SleepTimerBackupSection(
    val defaultEndBehavior: SleepTimerEndBehavior
)
