package com.example.musicplayer.ui.playlists

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.musicplayer.data.sample.FAVORITES_PLAYLIST_ID
import com.example.musicplayer.data.sample.SampleMusicData
import com.example.musicplayer.domain.model.Playlist
import com.example.musicplayer.ui.components.ScreenHeader
import com.example.musicplayer.ui.components.clickableNoRipple
import com.example.musicplayer.ui.favorites.FavoritesViewModel
import com.example.musicplayer.ui.theme.AppColors

/**
 * Real Playlists screen (Phase 2): vertical list of playlists, each
 * with a colored rounded-square icon, name, song count, and chevron.
 * Accent colors come from the existing Playlist.accentColorHex field
 * on the sample data (already restrained per AppColors, no new bright
 * colors introduced).
 *
 * Phase 4: the "Favorite Songs" row's song count now reflects the
 * real, reactive favorites count from [FavoritesViewModel] instead of
 * the static (and now always-empty) [Playlist.trackIds] placeholder
 * on that one playlist.
 */
@Composable
fun PlaylistsScreen(
    onOpenPlaylistDetail: (Long) -> Unit,
    modifier: Modifier = Modifier,
    favoritesViewModel: FavoritesViewModel = hiltViewModel()
) {
    val favoriteTrackIds by favoritesViewModel.favoriteTrackIds.collectAsState()

    Column(modifier = modifier.fillMaxSize()) {
        ScreenHeader(title = "Playlists")

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            items(SampleMusicData.playlists, key = { it.id }) { playlist ->
                val songCount = if (playlist.id == FAVORITES_PLAYLIST_ID) favoriteTrackIds.size else playlist.trackCount
                PlaylistRow(
                    playlist = playlist,
                    songCount = songCount,
                    onClick = { onOpenPlaylistDetail(playlist.id) }
                )
            }
            item { Spacer(modifier = Modifier.height(12.dp)) }
        }
    }
}

@Composable
private fun PlaylistRow(
    playlist: Playlist,
    songCount: Int,
    onClick: () -> Unit
) {
    val accentColor = remember(playlist.accentColorHex) { parseHexColor(playlist.accentColorHex) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickableNoRipple(onClick)
            .padding(horizontal = 6.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(RoundedCornerShape(13.dp))
                .background(accentColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.MusicNote,
                contentDescription = null,
                tint = Color.White
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = playlist.name,
                color = AppColors.TextPrimary,
                fontSize = 14.5.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "$songCount songs",
                color = AppColors.TextSecondary,
                fontSize = 12.5.sp
            )
        }
        Icon(
            imageVector = Icons.Filled.ChevronRight,
            contentDescription = null,
            tint = AppColors.TextTertiary
        )
    }
}

private fun parseHexColor(hex: String): Color {
    return try {
        Color(android.graphics.Color.parseColor(hex))
    } catch (e: IllegalArgumentException) {
        AppColors.Purple
    }
}
