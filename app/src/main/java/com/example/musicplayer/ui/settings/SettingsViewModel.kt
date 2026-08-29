package com.example.musicplayer.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicplayer.audio.EqualizerRepository
import com.example.musicplayer.data.settings.AppLanguage
import com.example.musicplayer.data.settings.AppPreferences
import com.example.musicplayer.data.settings.AppearanceOption
import com.example.musicplayer.sleeptimer.SleepTimerManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsScreenState(
    val appearance: AppearanceOption = AppearanceOption.DARK,
    val language: AppLanguage = AppLanguage.SYSTEM_DEFAULT,
    val isEqualizerEnabled: Boolean = false,
    val isSleepTimerActive: Boolean = false,
    val sleepTimerRemainingLabel: String? = null
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val appPreferences: AppPreferences,
    equalizerRepository: EqualizerRepository,
    sleepTimerManager: SleepTimerManager
) : ViewModel() {

    init {
        viewModelScope.launch { equalizerRepository.refresh() }
    }

    val state: StateFlow<SettingsScreenState> = combine(
        appPreferences.appearance,
        appPreferences.language,
        equalizerRepository.state,
        sleepTimerManager.state
    ) { appearance, language, eqState, timerState ->
        SettingsScreenState(
            appearance = appearance,
            language = language,
            isEqualizerEnabled = eqState.isEnabled,
            isSleepTimerActive = timerState.isActive,
            sleepTimerRemainingLabel = if (timerState.isActive) {
                val minutes = (timerState.remainingMs / 60_000L).coerceAtLeast(0)
                if (minutes < 1) "Ends in under 1 min" else "Ends in $minutes min"
            } else null
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsScreenState())
}
