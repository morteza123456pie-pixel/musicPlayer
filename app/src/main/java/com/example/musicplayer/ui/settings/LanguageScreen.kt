package com.example.musicplayer.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicplayer.data.settings.AppLanguage
import com.example.musicplayer.data.settings.AppPreferences
import com.example.musicplayer.ui.components.ScreenHeader
import com.example.musicplayer.ui.components.clickableNoRipple
import com.example.musicplayer.ui.theme.AppColors
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Persists the app's text-localization language preference.
 *
 * IMPORTANT: this is preference *architecture*, not a claim that every
 * string in the app is fully translated yet — full string-resource
 * translation for Persian is future work. What this screen guarantees
 * today is: the preference is saved, is reactive, and — critically —
 * never touches the music UI's layout direction (see
 * [com.example.musicplayer.ui.theme.MusicPlayerTheme]'s hardcoded LTR
 * override, which this class has no path to affect).
 */
@HiltViewModel
class LanguageViewModel @Inject constructor(
    private val appPreferences: AppPreferences
) : ViewModel() {
    val language = appPreferences.language

    fun select(language: AppLanguage) {
        viewModelScope.launch { appPreferences.setLanguage(language) }
    }
}

@Composable
fun LanguageScreen(
    onBack: () -> Unit,
    viewModel: LanguageViewModel = hiltViewModel()
) {
    val selected by viewModel.language.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.Background)
    ) {
        ScreenHeader(title = "Language", onBack = onBack)

        Column(
            modifier = Modifier.padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            SettingsGroupCard {
                LanguageRow(
                    title = "System Default",
                    selected = selected == AppLanguage.SYSTEM_DEFAULT,
                    onClick = { viewModel.select(AppLanguage.SYSTEM_DEFAULT) }
                )
                SettingsDivider()
                LanguageRow(
                    title = "English",
                    selected = selected == AppLanguage.ENGLISH,
                    onClick = { viewModel.select(AppLanguage.ENGLISH) }
                )
                SettingsDivider()
                LanguageRow(
                    title = "فارسی (Persian)",
                    selected = selected == AppLanguage.PERSIAN,
                    onClick = { viewModel.select(AppLanguage.PERSIAN) }
                )
            }

            Text(
                "Playback controls, track rows, the mini-player, queue, and equalizer always stay left-to-right, regardless of language — only text follows the language you choose here.",
                color = AppColors.TextTertiary,
                fontSize = 12.sp,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }
    }
}

@Composable
private fun LanguageRow(title: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickableNoRipple(onClick)
            .padding(horizontal = 18.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, color = AppColors.TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Medium)
        if (selected) {
            Icon(Icons.Filled.Check, contentDescription = "Selected", tint = AppColors.Purple)
        }
    }
}
