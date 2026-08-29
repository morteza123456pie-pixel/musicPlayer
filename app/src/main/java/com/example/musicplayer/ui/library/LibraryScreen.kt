package com.example.musicplayer.ui.library

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Search
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
import com.example.musicplayer.ui.components.HeaderIconButton
import com.example.musicplayer.ui.components.ScreenHeader
import com.example.musicplayer.ui.components.SelectableChip
import com.example.musicplayer.ui.components.clickableNoRipple
import com.example.musicplayer.ui.theme.AppColors

/**
 * Real Library screen (Phase 2): title, search action, category
 * tabs (Songs/Albums/Artists/Playlists — only Songs renders a list
 * for now, the others are covered by their own dedicated screens
 * reachable from elsewhere in Phase 2), and the scrollable track list.
 *
 * Layout intentionally leaves room at the bottom for the persistent
 * MiniPlayer, which the app shell overlays — this screen does not
 * render its own mini-player or bottom nav.
 */
@Composable
fun LibraryScreen(
    onOpenSearch: () -> Unit,
    onOpenAlbums: () -> Unit,
    onOpenPlaylists: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LibraryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(modifier = modifier.fillMaxSize()) {
        ScreenHeader(
            title = "Library",
            trailingContent = {
                HeaderIconButton(
                    icon = Icons.Filled.Search,
                    contentDescription = "Search",
                    onClick = onOpenSearch
                )
            }
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SelectableChip(
                label = "Songs",
                selected = uiState.selectedTab == LibraryTab.SONGS,
                onClick = { viewModel.selectTab(LibraryTab.SONGS) }
            )
            SelectableChip(
                label = "Albums",
                selected = uiState.selectedTab == LibraryTab.ALBUMS,
                onClick = {
                    viewModel.selectTab(LibraryTab.ALBUMS)
                    onOpenAlbums()
                }
            )
            SelectableChip(
                label = "Artists",
                selected = uiState.selectedTab == LibraryTab.ARTISTS,
                onClick = { viewModel.selectTab(LibraryTab.ARTISTS) }
            )
            SelectableChip(
                label = "Playlists",
                selected = uiState.selectedTab == LibraryTab.PLAYLISTS,
                onClick = {
                    viewModel.selectTab(LibraryTab.PLAYLISTS)
                    onOpenPlaylists()
                }
            )
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            items(uiState.tracks, key = { it.id }) { track ->
                TrackRow(
                    track = track,
                    isSelected = track.id == uiState.currentTrackId,
                    isFavorite = track.id in uiState.favoriteTrackIds,
                    onClick = { viewModel.onTrackSelected(track) },
                    onToggleFavorite = { viewModel.onToggleFavorite(track.id) }
                )
            }
            item { Spacer(modifier = Modifier.height(12.dp)) }
        }
    }
}

@Composable
private fun TrackRow(
    track: Track,
    isSelected: Boolean,
    isFavorite: Boolean,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            // Subtle highlight only — a dim purple wash, not a bright
            // fill, so the currently-selected row stays identifiable
            // without overpowering the list per the brief.
            .background(if (isSelected) AppColors.PurpleDim else Color.Transparent)
            .clickableNoRipple(onClick)
            .padding(horizontal = 6.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        CoverArt(
            artworkUri = track.artworkUri,
            size = 46.dp,
            cornerRadius = 11.dp
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = track.title,
                color = if (isSelected) AppColors.Purple else AppColors.TextPrimary,
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
        Text(
            text = formatDuration(track.durationMs),
            color = AppColors.TextTertiary,
            fontSize = 12.sp
        )
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

private fun formatDuration(durationMs: Long): String {
    val totalSeconds = durationMs / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}
