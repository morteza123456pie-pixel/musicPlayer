package com.example.musicplayer.ui.queue

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.musicplayer.domain.model.Track
import com.example.musicplayer.ui.components.CoverArt
import com.example.musicplayer.ui.components.ScreenHeader
import com.example.musicplayer.ui.components.clickableNoRipple
import com.example.musicplayer.ui.theme.AppColors
import kotlin.math.roundToInt

/**
 * Real playback queue screen (Phase 4). Shows exactly what
 * [com.example.musicplayer.player.MusicPlayerController] will play
 * next — nothing here is a separate UI-only list. Reorders/removals
 * are forwarded straight to the controller (which updates Media3's
 * real playlist), and the list here just reflects whatever the
 * controller reports back.
 */
@Composable
fun QueueScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: QueueViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()

    // Local optimistic drag offset while a drag is in progress, so the
    // dragged row moves smoothly frame-to-frame; the real commit to
    // MusicPlayerController happens once the drag ends. The list order
    // itself always comes straight from uiState.queue (the real
    // playback queue) — this screen never keeps its own copy.
    var draggingIndex by remember { mutableStateOf<Int?>(null) }
    var dragOffsetY by remember { mutableStateOf(0f) }
    val queue = uiState.queue

    Column(modifier = modifier.fillMaxSize()) {
        ScreenHeader(
            title = "Queue (${uiState.queue.size})",
            onBack = onBack,
            trailingContent = {
                if (uiState.queue.size > 1) {
                    Text(
                        text = "Clear",
                        color = AppColors.TextSecondary,
                        fontSize = 13.sp,
                        modifier = Modifier.clickableNoRipple(viewModel::onClearQueue)
                    )
                }
            }
        )

        if (uiState.queue.isEmpty()) {
            QueueEmptyState()
            return@Column
        }

        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            itemsIndexed(queue, key = { _, track -> track.id }) { index, track ->
                val isDraggingThis = draggingIndex == index
                QueueRow(
                    track = track,
                    isCurrent = index == uiState.currentIndex,
                    isPlaying = uiState.isPlaying && index == uiState.currentIndex,
                    modifier = Modifier
                        .graphicsLayer {
                            translationY = if (isDraggingThis) dragOffsetY else 0f
                            shadowElevation = if (isDraggingThis) 8f else 0f
                        }
                        .pointerInput(track.id) {
                            val rowHeightPx = size.height.toFloat().coerceAtLeast(1f)
                            detectDragGesturesAfterLongPress(
                                onDragStart = {
                                    draggingIndex = index
                                    dragOffsetY = 0f
                                },
                                onDragEnd = {
                                    val from = draggingIndex
                                    if (from != null) {
                                        val shift = (dragOffsetY / rowHeightPx).roundToInt()
                                        val to = (from + shift).coerceIn(0, queue.lastIndex)
                                        if (to != from) {
                                            viewModel.onMove(from, to)
                                        }
                                    }
                                    draggingIndex = null
                                    dragOffsetY = 0f
                                },
                                onDragCancel = {
                                    draggingIndex = null
                                    dragOffsetY = 0f
                                },
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    dragOffsetY += dragAmount.y
                                }
                            )
                        },
                    onClick = { viewModel.onTrackTapped(index) },
                    onRemove = { viewModel.onRemove(index) }
                )
            }
            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun QueueRow(
    track: Track,
    isCurrent: Boolean,
    isPlaying: Boolean,
    onClick: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            // Subtle purple wash for the currently-playing row only —
            // deliberately restrained per the brief, not a bright highlight.
            .background(if (isCurrent) AppColors.PurpleDim else Color.Transparent)
            .clickableNoRipple(onClick)
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        CoverArt(artworkUri = track.artworkUri, size = 44.dp, cornerRadius = 10.dp)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = track.title,
                color = if (isCurrent) AppColors.Purple else AppColors.TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = track.artist,
                color = AppColors.TextSecondary,
                fontSize = 12.5.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        if (isCurrent) {
            Icon(
                Icons.Filled.Equalizer,
                contentDescription = if (isPlaying) "Now playing" else "Current track",
                tint = AppColors.Purple,
                modifier = Modifier.size(16.dp)
            )
        }
        Icon(
            Icons.Filled.Close,
            contentDescription = "Remove from queue",
            tint = AppColors.TextTertiary,
            modifier = Modifier
                .size(16.dp)
                .clickableNoRipple(onRemove)
        )
        Icon(
            Icons.Filled.DragHandle,
            contentDescription = "Drag to reorder",
            tint = AppColors.TextTertiary,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
private fun QueueEmptyState() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = "Queue is empty", color = AppColors.TextSecondary, fontSize = 14.sp)
    }
}
