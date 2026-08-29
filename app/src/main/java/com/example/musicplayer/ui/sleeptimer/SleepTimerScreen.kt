package com.example.musicplayer.ui.sleeptimer

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.musicplayer.sleeptimer.SLEEP_TIMER_QUICK_DURATIONS_MINUTES
import com.example.musicplayer.sleeptimer.SleepTimerEndBehavior
import com.example.musicplayer.sleeptimer.SleepTimerUiState
import com.example.musicplayer.ui.components.AppCard
import com.example.musicplayer.ui.components.ScreenHeader
import com.example.musicplayer.ui.components.SelectableChip
import com.example.musicplayer.ui.components.clickableNoRipple
import com.example.musicplayer.ui.theme.AppColors
import kotlin.math.roundToInt

/**
 * Fully functional Sleep Timer screen. The countdown shown here is a
 * read-only reflection of [com.example.musicplayer.sleeptimer.SleepTimerManager]'s
 * process-lifetime state — this screen can be closed and reopened
 * freely without affecting the timer, and the value displayed here
 * always matches "real remaining time," recomputed from an absolute
 * end timestamp rather than a countdown that could drift or freeze.
 */
@Composable
fun SleepTimerScreen(
    onBack: () -> Unit,
    viewModel: SleepTimerViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var selectedMinutes by remember { mutableStateOf(15) }
    var customMinutesText by remember { mutableStateOf("") }
    var selectedBehavior by remember { mutableStateOf(SleepTimerEndBehavior.PAUSE) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.Background)
    ) {
        ScreenHeader(title = "Sleep Timer", onBack = onBack)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .windowInsetsPadding(WindowInsets.navigationBars),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(12.dp))
            CountdownRing(state = state)
            Spacer(Modifier.height(28.dp))

            if (state.isActive) {
                Text(
                    text = when (state.endBehavior) {
                        SleepTimerEndBehavior.PAUSE -> "Playback will pause when the timer ends"
                        SleepTimerEndBehavior.STOP_AND_CLEAR -> "Playback will stop and the queue will clear"
                    },
                    color = AppColors.TextSecondary,
                    fontSize = 13.sp
                )
                Spacer(Modifier.height(28.dp))
                Button(
                    onClick = viewModel::cancelTimer,
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.SurfaceAlt),
                    modifier = Modifier.fillMaxWidth().height(52.dp)
                ) {
                    Text("Cancel Timer", color = AppColors.TextPrimary, fontWeight = FontWeight.SemiBold)
                }
            } else {
                DurationGrid(
                    selectedMinutes = selectedMinutes,
                    onSelect = { selectedMinutes = it; customMinutesText = "" }
                )
                Spacer(Modifier.height(16.dp))
                CustomDurationField(
                    value = customMinutesText,
                    onValueChange = { text ->
                        customMinutesText = text
                        text.toIntOrNull()?.let { if (it > 0) selectedMinutes = it }
                    }
                )
                Spacer(Modifier.height(20.dp))
                EndBehaviorSelector(
                    selected = selectedBehavior,
                    onSelect = { selectedBehavior = it }
                )
                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = {
                        viewModel.startTimer(selectedMinutes * 60_000L, selectedBehavior)
                    },
                    enabled = selectedMinutes > 0,
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.Purple),
                    modifier = Modifier.fillMaxWidth().height(52.dp)
                ) {
                    Text("Start Timer", color = Color.White, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun CountdownRing(state: SleepTimerUiState) {
    val progress = if (state.isActive && state.totalDurationMs > 0) {
        (state.remainingMs.toFloat() / state.totalDurationMs.toFloat()).coerceIn(0f, 1f)
    } else 0f
    val animatedProgress by animateFloatAsState(targetValue = progress, label = "sleepTimerProgress")

    Box(modifier = Modifier.size(220.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 14.dp.toPx()
            drawArc(
                color = AppColors.SurfaceAlt,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                size = Size(size.width - strokeWidth, size.height - strokeWidth),
                topLeft = androidx.compose.ui.geometry.Offset(strokeWidth / 2, strokeWidth / 2)
            )
            if (state.isActive) {
                drawArc(
                    color = AppColors.Purple,
                    startAngle = -90f,
                    sweepAngle = 360f * animatedProgress,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                    size = Size(size.width - strokeWidth, size.height - strokeWidth),
                    topLeft = androidx.compose.ui.geometry.Offset(strokeWidth / 2, strokeWidth / 2)
                )
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (state.isActive) {
                val totalSeconds = (state.remainingMs / 1000).coerceAtLeast(0)
                val minutes = totalSeconds / 60
                val seconds = totalSeconds % 60
                Text(
                    text = "%02d:%02d".format(minutes, seconds),
                    color = AppColors.TextPrimary,
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Bold
                )
                Text("remaining", color = AppColors.TextSecondary, fontSize = 13.sp)
            } else {
                Text("Off", color = AppColors.TextSecondary, fontSize = 22.sp, fontWeight = FontWeight.SemiBold)
                Text("No timer running", color = AppColors.TextTertiary, fontSize = 13.sp)
            }
        }
    }
}

@Composable
private fun DurationGrid(selectedMinutes: Int, onSelect: (Int) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            "DURATION",
            color = AppColors.TextTertiary,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 10.dp, start = 2.dp)
        )
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.height(120.dp)
        ) {
            items(SLEEP_TIMER_QUICK_DURATIONS_MINUTES) { minutes ->
                AppCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(if (selectedMinutes == minutes) AppColors.PurpleDim else Color.Transparent)
                            .clickableNoRipple { onSelect(minutes) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "$minutes min",
                            color = if (selectedMinutes == minutes) AppColors.Purple else AppColors.TextPrimary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CustomDurationField(value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = { text -> if (text.all(Char::isDigit) && text.length <= 3) onValueChange(text) },
        label = { Text("Custom minutes", color = AppColors.TextSecondary) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = AppColors.Purple,
            unfocusedBorderColor = AppColors.Border,
            focusedTextColor = AppColors.TextPrimary,
            unfocusedTextColor = AppColors.TextPrimary,
            cursorColor = AppColors.Purple
        )
    )
}

@Composable
private fun EndBehaviorSelector(selected: SleepTimerEndBehavior, onSelect: (SleepTimerEndBehavior) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            "WHEN TIMER ENDS",
            color = AppColors.TextTertiary,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 10.dp, start = 2.dp)
        )
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            SelectableChip(
                label = "Pause playback",
                selected = selected == SleepTimerEndBehavior.PAUSE,
                onClick = { onSelect(SleepTimerEndBehavior.PAUSE) }
            )
            SelectableChip(
                label = "Stop & clear",
                selected = selected == SleepTimerEndBehavior.STOP_AND_CLEAR,
                onClick = { onSelect(SleepTimerEndBehavior.STOP_AND_CLEAR) }
            )
        }
    }
}
