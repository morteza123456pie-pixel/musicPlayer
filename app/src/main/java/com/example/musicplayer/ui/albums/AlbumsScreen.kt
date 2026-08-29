package com.example.musicplayer.ui.albums

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.musicplayer.data.sample.SampleMusicData
import com.example.musicplayer.domain.model.Album
import com.example.musicplayer.ui.components.HeaderIconButton
import com.example.musicplayer.ui.components.ScreenHeader
import com.example.musicplayer.ui.components.clickableNoRipple
import com.example.musicplayer.ui.theme.AppColors

/**
 * Real Albums screen (Phase 2): header with search action, responsive
 * two-column grid of album cards. No ViewModel needed yet — album
 * data is static sample data with no per-row interactive state beyond
 * navigation, so a ViewModel would be pure ceremony at this stage.
 */
@Composable
fun AlbumsScreen(
    onOpenSearch: () -> Unit,
    onOpenAlbumDetail: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        ScreenHeader(
            title = "Albums",
            trailingContent = {
                HeaderIconButton(
                    icon = Icons.Filled.Search,
                    contentDescription = "Search",
                    onClick = onOpenSearch
                )
            }
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(SampleMusicData.albums, key = { it.id }) { album ->
                AlbumCard(
                    album = album,
                    onClick = { onOpenAlbumDetail(album.id) }
                )
            }
            item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(2) }) {
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun AlbumCard(
    album: Album,
    onClick: () -> Unit
) {
    // Deterministic per-album accent so the grid has visual variety
    // without introducing bright, off-theme colors — cycles through
    // the same restrained accent palette used by playlists/folders.
    val accents = listOf(AppColors.Purple, AppColors.Blue, AppColors.AccentPink, AppColors.AccentGreen)
    val accent = accents[(album.id % accents.size).toInt()]

    Column(modifier = Modifier.clickableNoRipple(onClick)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(16.dp))
                .background(
                    Brush.linearGradient(
                        listOf(accent, Color(0xFF12101F))
                    )
                )
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = album.name,
            color = AppColors.TextPrimary,
            fontSize = 13.5.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = album.artist,
            color = AppColors.TextSecondary,
            fontSize = 12.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
