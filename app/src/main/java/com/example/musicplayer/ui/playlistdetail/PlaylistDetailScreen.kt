package com.example.musicplayer.ui.playlistdetail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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

/**
 * Playlist detail screen used for every playlist, including the
 * built-in "Favorite Songs" playlist. For Favorite Songs, the track
 * list is the real, reactive favorites list — favoriting/unfavoriting
 * a track anywhere in the app updates this screen immediately.
 */
@Composable
fun PlaylistDetailScreen(
    onBack: () -> Unit,
    onOpenNowPlaying: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PlaylistDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(modifier = modifier.fillMaxSize()) {
        ScreenHeader(
            title = uiState.playlist?.name ?: "Playlist",
            onBack = onBack
        )

        if (uiState.tracks.isEmpty()) {
            EmptyPlaylistState(isFavorites = uiState.isFavoritesPlaylist)
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                items(uiState.tracks, key = { it.id }) { track ->
                    PlaylistTrackRow(
                        track = track,
                        isCurrent = track.id == uiState.currentTrackId,
                        isFavorite = track.id in uiState.favoriteTrackIds,
                        onClick = {
                            viewModel.onTrackSelected(track)
                            onOpenNowPlaying()
                        },
                        onToggleFavorite = { viewModel.onToggleFavorite(track.id) }
                    )
                }
                item { Spacer(modifier = Modifier.height(12.dp)) }
            }
        }
    }
}

@Composable
private fun PlaylistTrackRow(
    track: Track,
    isCurrent: Boolean,
    isFavorite: Boolean,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (isCurrent) AppColors.PurpleDim else Color.Transparent)
            .clickableNoRipple(onClick)
            .padding(horizontal = 6.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        CoverArt(artworkUri = track.artworkUri, size = 46.dp, cornerRadius = 11.dp)
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
        Icon(
            imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
            contentDescription = "Favorite",
            tint = if (isFavorite) AppColors.Pink else AppColors.TextTertiary,
            modifier = Modifier
                .size(18.dp)
                .clickableNoRipple(onToggleFavorite)
        )
    }
}

@Composable
private fun EmptyPlaylistState(isFavorites: Boolean) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = if (isFavorites) {
                "No favorites yet — tap the heart on any track to add it here."
            } else {
                "This playlist is empty."
            },
            color = AppColors.TextSecondary,
            fontSize = 13.5.sp,
            modifier = Modifier.padding(horizontal = 40.dp)
        )
    }
}
