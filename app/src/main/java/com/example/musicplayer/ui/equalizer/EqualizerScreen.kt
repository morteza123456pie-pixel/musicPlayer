package com.example.musicplayer.ui.equalizer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.musicplayer.audio.AudioPreset
import com.example.musicplayer.audio.EqualizerBand
import com.example.musicplayer.ui.components.AppCard
import com.example.musicplayer.ui.components.ScreenHeader
import com.example.musicplayer.ui.components.SelectableChip
import com.example.musicplayer.ui.theme.AppColors

/**
 * Real functional Equalizer screen. Every control here reflects and
 * mutates the actual platform [android.media.audiofx.Equalizer]/
 * [android.media.audiofx.BassBoost] attached to the live playback
 * session — see [com.example.musicplayer.audio.AudioEffectsManager] —
 * not a visual-only mock. Band count and frequency labels come
 * directly from [EqualizerViewModel.state], which is itself populated
 * from the device's real reported band configuration, so this layout
 * adapts automatically rather than assuming a fixed number of bands.
 */
@Composable
fun EqualizerScreen(
    onBack: () -> Unit,
    viewModel: EqualizerViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.Background)
    ) {
        ScreenHeader(title = "Equalizer", onBack = onBack)

        if (!state.isReady) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AppColors.Purple)
            }
            return@Column
        }

        if (!state.isSupported) {
            UnsupportedState()
            return@Column
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .windowInsetsPadding(WindowInsets.navigationBars),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            EnableToggleCard(
                enabled = state.isEnabled,
                onToggle = viewModel::setEnabled
            )

            PresetSelector(
                selectedPreset = state.selectedPreset,
                enabled = state.isEnabled,
                onSelect = viewModel::applyPreset
            )

            BandsCard(
                bands = state.bands,
                enabled = state.isEnabled,
                onBandChange = viewModel::setBandLevel
            )

            if (state.isBassBoostSupported) {
                BassBoostCard(
                    enabled = state.isBassBoostEnabled,
                    controlsEnabled = state.isEnabled,
                    strength = state.bassBoostStrength,
                    onEnabledChange = viewModel::setBassBoostEnabled,
                    onStrengthChange = viewModel::setBassBoostStrength
                )
            } else {
                BassBoostUnsupportedCard()
            }

            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun UnsupportedState() {
    Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "Equalizer unavailable",
                color = AppColors.TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "This device does not expose a compatible audio equalizer for the current playback session.",
                color = AppColors.TextSecondary,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun EnableToggleCard(enabled: Boolean, onToggle: (Boolean) -> Unit) {
    AppCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Equalizer", color = AppColors.TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            Switch(
                checked = enabled,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = AppColors.Purple,
                    uncheckedThumbColor = AppColors.TextSecondary,
                    uncheckedTrackColor = AppColors.SurfaceAlt
                )
            )
        }
    }
}

@Composable
private fun PresetSelector(selectedPreset: AudioPreset, enabled: Boolean, onSelect: (AudioPreset) -> Unit) {
    val presets = listOf(
        AudioPreset.FLAT, AudioPreset.POP, AudioPreset.ROCK, AudioPreset.JAZZ, AudioPreset.CLASSICAL
    )
    Column {
        Text(
            "PRESETS",
            color = AppColors.TextTertiary,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 10.dp, start = 2.dp)
        )
        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            items(presets) { preset ->
                SelectableChip(
                    label = preset.displayName,
                    selected = selectedPreset == preset,
                    onClick = { if (enabled) onSelect(preset) }
                )
            }
            if (selectedPreset == AudioPreset.CUSTOM) {
                item {
                    SelectableChip(label = "Custom", selected = true, onClick = {})
                }
            }
        }
    }
}

@Composable
private fun BandsCard(bands: List<EqualizerBand>, enabled: Boolean, onBandChange: (Int, Int) -> Unit) {
    AppCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 22.dp)
                .height(220.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            bands.forEach { band ->
                BandSlider(
                    band = band,
                    enabled = enabled,
                    onLevelChange = { level -> onBandChange(band.index, level) }
                )
            }
        }
    }
}

@Composable
private fun RowScope.BandSlider(band: EqualizerBand, enabled: Boolean, onLevelChange: (Int) -> Unit) {
    var localLevel by remember(band.index, band.levelMillibel) { mutableStateOf(band.levelMillibel.toFloat()) }

    Column(
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        val gainDb = localLevel / 100f
        Text(
            text = (if (gainDb >= 0) "+" else "") + "%.1f".format(gainDb),
            color = if (enabled) AppColors.Purple else AppColors.TextTertiary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .width(36.dp),
            contentAlignment = Alignment.Center
        ) {
            Slider(
                value = localLevel,
                onValueChange = { localLevel = it },
                onValueChangeFinished = { onLevelChange(localLevel.toInt()) },
                valueRange = band.minLevelMillibel.toFloat()..band.maxLevelMillibel.toFloat(),
                enabled = enabled,
                modifier = Modifier
                    .rotate(-90f)
                    .width(180.dp),
                colors = SliderDefaults.colors(
                    thumbColor = AppColors.Purple,
                    activeTrackColor = AppColors.Purple,
                    inactiveTrackColor = AppColors.SurfaceAlt,
                    disabledThumbColor = AppColors.TextTertiary,
                    disabledActiveTrackColor = AppColors.TextTertiary,
                    disabledInactiveTrackColor = AppColors.SurfaceAlt
                )
            )
        }

        Text(
            text = formatFrequency(band.centerFrequencyHz),
            color = AppColors.TextSecondary,
            fontSize = 11.sp
        )
    }
}

private fun formatFrequency(hz: Int): String =
    if (hz >= 1000) "${hz / 1000}k" else "$hz"

@Composable
private fun BassBoostCard(
    enabled: Boolean,
    controlsEnabled: Boolean,
    strength: Int,
    onEnabledChange: (Boolean) -> Unit,
    onStrengthChange: (Int) -> Unit
) {
    var localStrength by remember(strength) { mutableStateOf(strength.toFloat()) }

    AppCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Bass Boost", color = AppColors.TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                Switch(
                    checked = enabled,
                    onCheckedChange = onEnabledChange,
                    enabled = controlsEnabled,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = AppColors.Purple,
                        uncheckedThumbColor = AppColors.TextSecondary,
                        uncheckedTrackColor = AppColors.SurfaceAlt
                    )
                )
            }
            Spacer(Modifier.height(8.dp))
            Text("Strength", color = AppColors.TextSecondary, fontSize = 13.sp)
            Slider(
                value = localStrength,
                onValueChange = { localStrength = it },
                onValueChangeFinished = { onStrengthChange(localStrength.toInt()) },
                valueRange = 0f..1000f,
                enabled = controlsEnabled && enabled,
                colors = SliderDefaults.colors(
                    thumbColor = AppColors.Purple,
                    activeTrackColor = AppColors.Purple,
                    inactiveTrackColor = AppColors.SurfaceAlt
                )
            )
        }
    }
}

@Composable
private fun BassBoostUnsupportedCard() {
    AppCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp)) {
            Text("Bass Boost", color = AppColors.TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(6.dp))
            Text(
                "Not supported on this device.",
                color = AppColors.TextTertiary,
                fontSize = 13.sp
            )
        }
    }
}
