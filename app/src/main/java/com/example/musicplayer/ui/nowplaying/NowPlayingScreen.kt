package com.example.musicplayer.ui.nowplaying

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Lyrics
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.musicplayer.domain.model.RepeatMode
import com.example.musicplayer.player.PlayerState
import com.example.musicplayer.ui.components.NowPlayingCoverArt
import com.example.musicplayer.ui.theme.AppColors
import kotlin.math.roundToLong

/**
 * Full Now Playing screen: the strongest, most polished screen in the
 * app per the Phase 3 brief. Large cinematic artwork, title/artist,
 * seekable progress tied to real Media3 position, and the primary
 * transport controls with the purple play/pause button as the visual
 * focal point.
 *
 * All state comes from [NowPlayingViewModel], which forwards straight
 * to [com.example.musicplayer.player.MusicPlayerController] — this
 * screen has no playback logic of its own.
 */
@Composable
fun NowPlayingScreen(
    onBack: () -> Unit,
    onOpenQueue: () -> Unit,
    onOpenLyrics: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: NowPlayingViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val isFavorite by viewModel.isCurrentTrackFavorite.collectAsState()
    val track = state.currentTrack

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppColors.Background)
            .padding(horizontal = 20.dp)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ChevronLeft, contentDescription = "Back", tint = AppColors.TextPrimary)
            }
            Text(
                text = "NOW PLAYING",
                color = AppColors.TextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.sp
            )
            IconButton(onClick = onOpenQueue) {
                Icon(Icons.Filled.MoreVert, contentDescription = "More", tint = AppColors.TextPrimary)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Artwork
        NowPlayingCoverArt(
            artworkUri = track?.artworkUri,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(28.dp))

        // Track info
        Text(
            text = track?.title ?: "Nothing playing",
            color = AppColors.TextPrimary,
            fontSize = 23.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = track?.artist ?: "Select a track to start listening",
            color = AppColors.TextSecondary,
            fontSize = 14.5.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(22.dp))

        ProgressSection(state = state, onSeek = viewModel::onSeek)

        Spacer(modifier = Modifier.height(18.dp))

        TransportControls(
            state = state,
            onTogglePlayPause = viewModel::onTogglePlayPause,
            onNext = viewModel::onNext,
            onPrevious = viewModel::onPrevious,
            onToggleShuffle = viewModel::onToggleShuffle,
            onCycleRepeat = viewModel::onCycleRepeat
        )

        Spacer(modifier = Modifier.height(18.dp))

        SecondaryActionsRow(
            isFavorite = isFavorite,
            onToggleFavorite = viewModel::onToggleFavorite,
            onOpenLyrics = onOpenLyrics,
            onOpenQueue = onOpenQueue
        )

        Spacer(modifier = Modifier.height(12.dp))
    }
}

@Composable
private fun ProgressSection(
    state: PlayerState,
    onSeek: (Long) -> Unit
) {
    // Local drag state so the thumb tracks the finger smoothly and
    // only commits a real seek on release, rather than seeking on
    // every pixel of drag (which would spam the player).
    var isDragging by remember { mutableStateOf(false) }
    var dragPositionMs by remember { mutableStateOf(0L) }

    val duration = state.durationMs.coerceAtLeast(1L)
    val displayedPositionMs = if (isDragging) dragPositionMs else state.currentPositionMs
    val sliderValue = (displayedPositionMs.toFloat() / duration.toFloat()).coerceIn(0f, 1f)

    Column {
        Slider(
            value = sliderValue,
            onValueChange = { fraction ->
                isDragging = true
                dragPositionMs = (fraction * duration).roundToLong()
            },
            onValueChangeFinished = {
                if (state.hasPlayableMedia) onSeek(dragPositionMs)
                isDragging = false
            },
            enabled = state.hasPlayableMedia,
            colors = SliderDefaults.colors(
                thumbColor = AppColors.Purple,
                activeTrackColor = AppColors.Purple,
                inactiveTrackColor = AppColors.SurfaceAlt,
                disabledThumbColor = AppColors.TextTertiary,
                disabledActiveTrackColor = AppColors.SurfaceAlt,
                disabledInactiveTrackColor = AppColors.SurfaceAlt
            ),
            modifier = Modifier.fillMaxWidth()
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = formatDuration(displayedPositionMs),
                color = AppColors.TextTertiary,
                fontSize = 11.5.sp
            )
            Text(
                text = if (state.hasPlayableMedia) formatDuration(state.durationMs) else "--:--",
                color = AppColors.TextTertiary,
                fontSize = 11.5.sp
            )
        }
    }
}

@Composable
private fun TransportControls(
    state: PlayerState,
    onTogglePlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onToggleShuffle: () -> Unit,
    onCycleRepeat: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onToggleShuffle) {
            Icon(
                Icons.Filled.Shuffle,
                contentDescription = "Shuffle",
                tint = if (state.shuffleEnabled) AppColors.Purple else AppColors.TextSecondary
            )
        }
        IconButton(onClick = onPrevious, modifier = Modifier.size(44.dp)) {
            Icon(
                Icons.Filled.SkipPrevious,
                contentDescription = "Previous",
                tint = AppColors.TextPrimary,
                modifier = Modifier.size(30.dp)
            )
        }

        // Play/Pause: the visual focal point of the screen.
        IconButton(
            onClick = onTogglePlayPause,
            modifier = Modifier
                .size(68.dp)
                .clip(CircleShape)
                .background(AppColors.Purple)
        ) {
            Icon(
                imageVector = if (state.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                contentDescription = if (state.isPlaying) "Pause" else "Play",
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
        }

        IconButton(onClick = onNext, modifier = Modifier.size(44.dp)) {
            Icon(
                Icons.Filled.SkipNext,
                contentDescription = "Next",
                tint = AppColors.TextPrimary,
                modifier = Modifier.size(30.dp)
            )
        }
        IconButton(onClick = onCycleRepeat) {
            Icon(
                imageVector = if (state.repeatMode == RepeatMode.ONE) Icons.Filled.RepeatOne else Icons.Filled.Repeat,
                contentDescription = "Repeat",
                tint = if (state.repeatMode != RepeatMode.OFF) AppColors.Purple else AppColors.TextSecondary
            )
        }
    }
}

@Composable
private fun SecondaryActionsRow(
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onOpenLyrics: () -> Unit,
    onOpenQueue: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onToggleFavorite) {
            Icon(
                imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                contentDescription = "Favorite",
                tint = if (isFavorite) AppColors.Pink else AppColors.TextSecondary
            )
        }
        IconButton(onClick = onOpenLyrics) {
            Icon(Icons.Filled.Lyrics, contentDescription = "Lyrics", tint = AppColors.TextSecondary)
        }
        IconButton(onClick = onOpenQueue) {
            Icon(Icons.Filled.QueueMusic, contentDescription = "Queue", tint = AppColors.TextSecondary)
        }
        IconButton(onClick = { /* additional actions: Phase 5+ */ }) {
            Icon(Icons.Filled.MoreVert, contentDescription = "More actions", tint = AppColors.TextSecondary)
        }
    }
}

private fun formatDuration(durationMs: Long): String {
    val totalSeconds = (durationMs / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}
