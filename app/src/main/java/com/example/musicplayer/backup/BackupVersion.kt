package com.example.musicplayer.backup

/** Result of validating an imported backup file before showing the user a confirmation dialog. */
sealed class BackupValidationResult {
    data class Valid(val data: BackupData, val favoriteCount: Int) : BackupValidationResult()
    data object InvalidFormat : BackupValidationResult()
    data class UnsupportedVersion(val foundVersion: Int) : BackupValidationResult()
}
