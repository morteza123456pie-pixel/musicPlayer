package com.example.musicplayer.ui.lyrics

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.Lyrics
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.musicplayer.domain.model.LyricLine
import com.example.musicplayer.ui.components.clickableNoRipple
import com.example.musicplayer.ui.theme.AppColors

/**
 * Immersive Lyrics screen (Phase 4), reached from Now Playing.
 * The active line derives purely from [LyricsViewModel.uiState], which
 * itself derives from the real shared player position — this screen
 * has no timer or animation loop of its own, only a smooth-scroll
 * effect that reacts to [LyricsUiState.activeLineIndex] changing.
 */
@Composable
fun LyricsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LyricsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()

    // Scroll toward the active line whenever it changes (play
    // progressing, seek, next/previous) — not on every position tick,
    // since activeLineIndex only changes when the active line itself
    // changes, keeping this cheap.
    LaunchedEffect(uiState.activeLineIndex) {
        if (uiState.activeLineIndex >= 0) {
            listState.animateScrollToItem(
                index = uiState.activeLineIndex,
                scrollOffset = -300
            )
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppColors.Background)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
                .padding(top = 16.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ChevronLeft, contentDescription = "Back", tint = AppColors.TextPrimary)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = uiState.currentTrack?.title ?: "Lyrics",
                    color = AppColors.TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1
                )
                Text(
                    text = uiState.currentTrack?.artist.orEmpty(),
                    color = AppColors.TextSecondary,
                    fontSize = 12.sp,
                    maxLines = 1
                )
            }
            Spacer(modifier = Modifier.size(38.dp))
        }

        Box(modifier = Modifier.weight(1f)) {
            when {
                uiState.currentTrack == null -> LyricsUnavailableState(
                    message = "Nothing playing right now."
                )
                uiState.showEmptyState -> LyricsUnavailableState(
                    message = "Lyrics are not available for this track."
                )
                else -> LyricsList(
                    lines = uiState.lyricLines,
                    activeIndex = uiState.activeLineIndex,
                    listState = listState,
                    onLineTapped = { line -> viewModel.onSeekToLine(line.startTimeMs) }
                )
            }
        }

        LyricsBottomControls(
            isPlaying = uiState.isPlaying,
            hasPlayableMedia = uiState.hasPlayableMedia,
            onTogglePlayPause = viewModel::onTogglePlayPause,
            onNext = viewModel::onNext,
            onPrevious = viewModel::onPrevious
        )
    }
}

@Composable
private fun LyricsList(
    lines: List<LyricLine>,
    activeIndex: Int,
    listState: LazyListState,
    onLineTapped: (LyricLine) -> Unit
) {
    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 120.dp, horizontal = 28.dp)
    ) {
        itemsIndexed(lines, key = { index, _ -> index }) { index, line ->
            LyricLineRow(
                text = line.text,
                state = when {
                    index == activeIndex -> LyricLineState.ACTIVE
                    index < activeIndex -> LyricLineState.PAST
                    else -> LyricLineState.UPCOMING
                },
                onClick = { onLineTapped(line) }
            )
        }
    }
}

private enum class LyricLineState { PAST, ACTIVE, UPCOMING }

@Composable
private fun LyricLineRow(
    text: String,
    state: LyricLineState,
    onClick: () -> Unit
) {
    val targetColor = when (state) {
        LyricLineState.PAST -> AppColors.TextTertiary
        LyricLineState.ACTIVE -> AppColors.Purple
        LyricLineState.UPCOMING -> AppColors.TextSecondary
    }
    val animatedColor by animateColorAsState(targetValue = targetColor, label = "lyricLineColor")

    Text(
        text = text,
        color = animatedColor,
        fontSize = if (state == LyricLineState.ACTIVE) 19.sp else 16.sp,
        fontWeight = if (state == LyricLineState.ACTIVE) FontWeight.Bold else FontWeight.Medium,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .clickableNoRipple(onClick)
            .padding(vertical = 10.dp)
    )
}

@Composable
private fun LyricsUnavailableState(message: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Filled.Lyrics,
                contentDescription = null,
                tint = AppColors.TextTertiary,
                modifier = Modifier.size(36.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = message,
                color = AppColors.TextSecondary,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            )
        }
    }
}

@Composable
private fun LyricsBottomControls(
    isPlaying: Boolean,
    hasPlayableMedia: Boolean,
    onTogglePlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 20.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPrevious, modifier = Modifier.size(40.dp)) {
            Icon(Icons.Filled.SkipPrevious, contentDescription = "Previous", tint = AppColors.TextPrimary, modifier = Modifier.size(26.dp))
        }
        IconButton(
            onClick = onTogglePlayPause,
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(AppColors.Purple)
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                contentDescription = if (isPlaying) "Pause" else "Play",
                tint = Color.White,
                modifier = Modifier.size(26.dp)
            )
        }
        IconButton(onClick = onNext, modifier = Modifier.size(40.dp)) {
            Icon(Icons.Filled.SkipNext, contentDescription = "Next", tint = AppColors.TextPrimary, modifier = Modifier.size(26.dp))
        }
    }
}
