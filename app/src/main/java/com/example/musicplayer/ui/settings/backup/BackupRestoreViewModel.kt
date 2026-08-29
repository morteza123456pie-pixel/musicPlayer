package com.example.musicplayer.ui.settings.backup

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicplayer.backup.BackupData
import com.example.musicplayer.backup.BackupManager
import com.example.musicplayer.backup.BackupValidationResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class BackupRestoreUiState {
    data object Idle : BackupRestoreUiState()
    data object ExportSuccess : BackupRestoreUiState()
    data object ExportFailed : BackupRestoreUiState()
    data object ImportInvalid : BackupRestoreUiState()
    data class ImportUnsupportedVersion(val foundVersion: Int) : BackupRestoreUiState()
    /** Backup parsed successfully; waiting on the user's explicit merge/replace confirmation before anything is applied. */
    data class AwaitingImportConfirmation(val data: BackupData, val favoriteCount: Int) : BackupRestoreUiState()
    data object ImportSuccess : BackupRestoreUiState()
}

@HiltViewModel
class BackupRestoreViewModel @Inject constructor(
    private val backupManager: BackupManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<BackupRestoreUiState>(BackupRestoreUiState.Idle)
    val uiState: StateFlow<BackupRestoreUiState> = _uiState.asStateFlow()

    fun exportTo(destination: Uri) {
        viewModelScope.launch {
            val success = backupManager.exportTo(destination)
            _uiState.value = if (success) BackupRestoreUiState.ExportSuccess else BackupRestoreUiState.ExportFailed
        }
    }

    /** Reads and validates only — never applies anything until [confirmImport] is called. */
    fun readAndValidate(source: Uri) {
        viewModelScope.launch {
            when (val result = backupManager.readAndValidate(source)) {
                is BackupValidationResult.Valid ->
                    _uiState.value = BackupRestoreUiState.AwaitingImportConfirmation(result.data, result.favoriteCount)
                is BackupValidationResult.UnsupportedVersion ->
                    _uiState.value = BackupRestoreUiState.ImportUnsupportedVersion(result.foundVersion)
                BackupValidationResult.InvalidFormat ->
                    _uiState.value = BackupRestoreUiState.ImportInvalid
            }
        }
    }

    fun confirmImport(data: BackupData, replaceFavorites: Boolean) {
        viewModelScope.launch {
            backupManager.applyBackup(data, replaceFavorites)
            _uiState.value = BackupRestoreUiState.ImportSuccess
        }
    }

    fun dismissMessage() {
        _uiState.value = BackupRestoreUiState.Idle
    }
}
