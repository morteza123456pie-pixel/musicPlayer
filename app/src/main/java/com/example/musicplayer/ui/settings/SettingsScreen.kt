package com.example.musicplayer.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.musicplayer.data.settings.AppLanguage
import com.example.musicplayer.data.settings.AppearanceOption
import com.example.musicplayer.ui.components.ScreenHeader
import com.example.musicplayer.ui.theme.AppColors

/**
 * Full functional Settings screen — every row here either navigates to
 * a fully working destination or (for anything not yet fully
 * implemented, like Light theme) is simply not shown, per the "prefer
 * not to show incomplete options" requirement.
 */
@Composable
fun SettingsScreen(
    onNavigateToAppearance: () -> Unit,
    onNavigateToLanguage: () -> Unit,
    onNavigateToEqualizer: () -> Unit,
    onNavigateToPlaybackSettings: () -> Unit,
    onNavigateToSleepTimer: () -> Unit,
    onNavigateToBackupRestore: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.Background)
    ) {
        ScreenHeader(title = "Settings")

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Column {
                SettingsSectionLabel("GENERAL")
                SettingsGroupCard {
                    SettingsRow(
                        title = "Appearance",
                        trailingText = state.appearance.label(),
                        onClick = onNavigateToAppearance
                    )
                    SettingsDivider()
                    SettingsRow(
                        title = "Language",
                        trailingText = state.language.label(),
                        onClick = onNavigateToLanguage
                    )
                }
            }

            Column {
                SettingsSectionLabel("PLAYBACK")
                SettingsGroupCard {
                    SettingsRow(
                        title = "Equalizer",
                        trailingText = if (state.isEqualizerEnabled) "On" else "Off",
                        onClick = onNavigateToEqualizer
                    )
                    SettingsDivider()
                    SettingsRow(
                        title = "Playback Settings",
                        onClick = onNavigateToPlaybackSettings
                    )
                }
            }

            Column {
                SettingsSectionLabel("OTHER")
                SettingsGroupCard {
                    SettingsRow(
                        title = "Sleep Timer",
                        trailingText = state.sleepTimerRemainingLabel ?: "Off",
                        onClick = onNavigateToSleepTimer
                    )
                    SettingsDivider()
                    SettingsRow(
                        title = "Backup & Restore",
                        onClick = onNavigateToBackupRestore
                    )
                }
            }

            Spacer(Modifier.height(12.dp))
        }
    }
}

private fun AppearanceOption.label(): String = when (this) {
    AppearanceOption.SYSTEM_DEFAULT -> "System Default"
    AppearanceOption.DARK -> "Dark"
    AppearanceOption.LIGHT -> "Light"
}

private fun AppLanguage.label(): String = when (this) {
    AppLanguage.SYSTEM_DEFAULT -> "System Default"
    AppLanguage.ENGLISH -> "English"
    AppLanguage.PERSIAN -> "فارسی"
}
