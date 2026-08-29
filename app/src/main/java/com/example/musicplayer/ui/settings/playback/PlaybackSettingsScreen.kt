package com.example.musicplayer.ui.settings.playback

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicplayer.audio.PlaybackCapabilities
import com.example.musicplayer.data.settings.PlaybackPreferences
import com.example.musicplayer.data.settings.PlaybackPreferencesSnapshot
import com.example.musicplayer.ui.components.ScreenHeader
import com.example.musicplayer.ui.settings.SettingsDivider
import com.example.musicplayer.ui.settings.SettingsGroupCard
import com.example.musicplayer.ui.theme.AppColors
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlaybackSettingsViewModel @Inject constructor(
    private val preferences: PlaybackPreferences
) : ViewModel() {

    val state: StateFlow<PlaybackPreferencesSnapshot> = preferences.snapshot.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        PlaybackPreferencesSnapshot(
            crossfadeEnabled = false,
            crossfadeDurationMs = 3000L,
            skipSilenceEnabled = false,
            resumePlaybackEnabled = true
        )
    )

    fun setSkipSilence(enabled: Boolean) {
        viewModelScope.launch { preferences.setSkipSilenceEnabled(enabled) }
    }

    fun setResumePlayback(enabled: Boolean) {
        viewModelScope.launch { preferences.setResumePlaybackEnabled(enabled) }
    }
}

@Composable
fun PlaybackSettingsScreen(
    onBack: () -> Unit,
    viewModel: PlaybackSettingsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.Background)
    ) {
        ScreenHeader(title = "Playback Settings", onBack = onBack)

        Column(
            modifier = Modifier.padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SettingsGroupCard {
                ToggleRow(
                    title = "Crossfade",
                    subtitle = "Not available yet — requires a future player-architecture update",
                    checked = false,
                    enabled = false,
                    onCheckedChange = {}
                )
                SettingsDivider()
                ToggleRow(
                    title = "Skip Silence",
                    subtitle = "Automatically speed through quiet passages",
                    checked = state.skipSilenceEnabled,
                    enabled = PlaybackCapabilities.SKIP_SILENCE_SUPPORTED,
                    onCheckedChange = viewModel::setSkipSilence
                )
                SettingsDivider()
                ToggleRow(
                    title = "Resume Playback",
                    subtitle = "Restore what was playing after restarting the app",
                    checked = state.resumePlaybackEnabled,
                    enabled = true,
                    onCheckedChange = viewModel::setResumePlayback
                )
            }
        }
    }
}

@Composable
private fun ToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    enabled: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
            Text(
                title,
                color = if (enabled) AppColors.TextPrimary else AppColors.TextTertiary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            )
            Text(subtitle, color = AppColors.TextTertiary, fontSize = 12.sp, modifier = Modifier.padding(top = 2.dp))
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = AppColors.Purple,
                uncheckedThumbColor = AppColors.TextSecondary,
                uncheckedTrackColor = AppColors.SurfaceAlt
            )
        )
    }
}
