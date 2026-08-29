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
import com.example.musicplayer.data.settings.AppPreferences
import com.example.musicplayer.data.settings.AppearanceOption
import com.example.musicplayer.ui.components.AppCard
import com.example.musicplayer.ui.components.ScreenHeader
import com.example.musicplayer.ui.components.clickableNoRipple
import com.example.musicplayer.ui.theme.AppColors
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppearanceViewModel @Inject constructor(
    private val appPreferences: AppPreferences
) : ViewModel() {
    val appearance = appPreferences.appearance

    fun select(option: AppearanceOption) {
        viewModelScope.launch { appPreferences.setAppearance(option) }
    }
}

@Composable
fun AppearanceScreen(
    onBack: () -> Unit,
    viewModel: AppearanceViewModel = hiltViewModel()
) {
    val selected by viewModel.appearance.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.Background)
    ) {
        ScreenHeader(title = "Appearance", onBack = onBack)

        Column(
            modifier = Modifier.padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            SettingsGroupCard {
                AppearanceOptionRow(
                    title = "System Default",
                    subtitle = "Match your device setting",
                    selected = selected == AppearanceOption.SYSTEM_DEFAULT,
                    onClick = { viewModel.select(AppearanceOption.SYSTEM_DEFAULT) }
                )
                SettingsDivider()
                AppearanceOptionRow(
                    title = "Dark",
                    subtitle = "The app's premium dark design",
                    selected = selected == AppearanceOption.DARK,
                    onClick = { viewModel.select(AppearanceOption.DARK) }
                )
                SettingsDivider()
                AppearanceOptionRow(
                    title = "Light",
                    subtitle = "Coming in a future update",
                    selected = false,
                    enabled = false,
                    onClick = {}
                )
            }

            Text(
                "Light theme isn't available yet — enabling it before it meets the same design quality as Dark would mean a broken experience, so it stays hidden from selection until it's ready.",
                color = AppColors.TextTertiary,
                fontSize = 12.sp,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }
    }
}

@Composable
private fun AppearanceOptionRow(
    title: String,
    subtitle: String,
    selected: Boolean,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (enabled) Modifier.clickableNoRipple(onClick) else Modifier)
            .padding(horizontal = 18.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                title,
                color = if (enabled) AppColors.TextPrimary else AppColors.TextTertiary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            )
            Text(subtitle, color = AppColors.TextTertiary, fontSize = 12.sp, modifier = Modifier.padding(top = 2.dp))
        }
        if (selected) {
            Icon(Icons.Filled.Check, contentDescription = "Selected", tint = AppColors.Purple)
        }
    }
}
