package com.example.musicplayer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.musicplayer.domain.model.Track
import com.example.musicplayer.ui.theme.AppColors

/**
 * Persistent mini-player shown above the bottom navigation on all
 * main screens. Tapping the row opens Now Playing; the trailing
 * button toggles play/pause without navigating away.
 */
@Composable
fun MiniPlayer(
    track: Track,
    isPlaying: Boolean,
    onTogglePlay: () -> Unit,
    onOpenNowPlaying: () -> Unit,
    onOpenQueue: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.verticalGradient(
                    listOf(AppColors.SurfaceElevated, AppColors.Surface)
                )
            )
            .border(1.dp, AppColors.BorderStrong, RoundedCornerShape(16.dp))
            .clickableNoRipple(onClick = onOpenNowPlaying)
            .padding(9.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        CoverArt(
            artworkUri = track.artworkUri,
            size = 38.dp,
            cornerRadius = 9.dp
        )

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = track.title,
                color = AppColors.TextPrimary,
                fontSize = 13.5.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = track.artist,
                color = AppColors.TextSecondary,
                fontSize = 11.5.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        if (onOpenQueue != null) {
            IconButton(onClick = onOpenQueue, modifier = Modifier.size(32.dp)) {
                Icon(
                    Icons.Filled.QueueMusic,
                    contentDescription = "Queue",
                    tint = AppColors.TextSecondary
                )
            }
        }

        IconButton(
            onClick = onTogglePlay,
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(AppColors.Purple)
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                contentDescription = if (isPlaying) "Pause" else "Play",
                tint = Color.White
            )
        }
    }
}
