package com.example.musicplayer.ui.search

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.graphics.SolidColor
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.musicplayer.domain.model.Track
import com.example.musicplayer.ui.components.CoverArt
import com.example.musicplayer.ui.components.SelectableChip
import com.example.musicplayer.ui.components.clickableNoRipple
import com.example.musicplayer.ui.theme.AppColors

/**
 * Full Search screen (Phase 4). Reachable from the bottom nav Search
 * tab and Search buttons elsewhere in the app (both pass [onBack] so
 * the header can optionally show a back arrow when pushed on top of
 * another screen vs. used as a bottom-nav tab).
 *
 * Layout stays intentionally LTR (inherited from the app-wide
 * `LocalLayoutDirection` override in Theme.kt) regardless of typed
 * text direction — the search field itself uses Compose's default
 * text input, which already renders Persian/mixed-language text with
 * correct bidi shaping without needing any extra handling here.
 */
@Composable
fun SearchScreen(
    onBack: (() -> Unit)? = null,
    onOpenNowPlaying: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val keyboardController = LocalSoftwareKeyboardController.current

    Column(modifier = modifier.fillMaxSize()) {
        SearchHeader(
            query = uiState.query,
            onQueryChange = viewModel::onQueryChanged,
            onClear = viewModel::onClearQuery,
            onBack = onBack,
            onSubmit = {
                viewModel.commitSearch()
                keyboardController?.hide()
            }
        )

        if (uiState.query.isBlank()) {
            SearchInitialState(
                recentSearches = uiState.recentSearches,
                suggestionChips = uiState.suggestionChips,
                onRecentSelected = viewModel::onSuggestionSelected,
                onSuggestionSelected = viewModel::onSuggestionSelected,
                onClearHistory = viewModel::onClearHistory
            )
        } else if (uiState.results.isEmpty) {
            SearchEmptyState(query = uiState.query)
        } else {
            SearchResultsList(
                uiState = uiState,
                onTrackSelected = { track ->
                    viewModel.onTrackSelected(track)
                    onOpenNowPlaying()
                },
                onToggleFavorite = viewModel::onToggleFavorite
            )
        }
    }
}

@Composable
private fun SearchHeader(
    query: String,
    onQueryChange: (String) -> Unit,
    onClear: () -> Unit,
    onBack: (() -> Unit)?,
    onSubmit: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 20.dp, top = 18.dp, bottom = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (onBack != null) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(AppColors.Surface)
            ) {
                Icon(Icons.Filled.ChevronLeft, contentDescription = "Back", tint = AppColors.TextPrimary)
            }
        }

        Row(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(16.dp))
                .background(AppColors.Surface)
                .border(1.dp, AppColors.Border, RoundedCornerShape(16.dp))
                .padding(horizontal = 14.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                Icons.Filled.Search,
                contentDescription = null,
                tint = AppColors.TextSecondary,
                modifier = Modifier.size(18.dp)
            )
            Box(modifier = Modifier.weight(1f)) {
                if (query.isEmpty()) {
                    Text(
                        text = "Search songs, albums, artists...",
                        color = AppColors.TextTertiary,
                        fontSize = 14.sp
                    )
                }
                BasicTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    singleLine = true,
                    textStyle = TextStyle(color = AppColors.TextPrimary, fontSize = 14.sp),
                    cursorBrush = SolidColor(AppColors.Purple),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(
                        onSearch = { onSubmit() }
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            if (query.isNotEmpty()) {
                Icon(
                    Icons.Filled.Close,
                    contentDescription = "Clear",
                    tint = AppColors.TextSecondary,
                    modifier = Modifier
                        .size(18.dp)
                        .clickableNoRipple(onClear)
                )
            }
        }
    }
}

@Composable
private fun SearchInitialState(
    recentSearches: List<String>,
    suggestionChips: List<String>,
    onRecentSelected: (String) -> Unit,
    onSuggestionSelected: (String) -> Unit,
    onClearHistory: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {
        if (recentSearches.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Searches",
                    color = AppColors.TextPrimary,
                    fontSize = 14.5.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Clear",
                    color = AppColors.TextSecondary,
                    fontSize = 12.5.sp,
                    modifier = Modifier.clickableNoRipple(onClearHistory)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Column {
                recentSearches.forEach { recent ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickableNoRipple { onRecentSelected(recent) }
                            .padding(vertical = 9.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            Icons.Filled.History,
                            contentDescription = null,
                            tint = AppColors.TextTertiary,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(text = recent, color = AppColors.TextSecondary, fontSize = 13.5.sp)
                    }
                }
            }
            Spacer(modifier = Modifier.height(18.dp))
        }

        Text(
            text = "Suggestions",
            color = AppColors.TextPrimary,
            fontSize = 14.5.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            suggestionChips.forEach { chip ->
                SelectableChip(
                    label = chip,
                    selected = false,
                    onClick = { onSuggestionSelected(chip) }
                )
            }
        }
    }
}

@Composable
private fun SearchEmptyState(query: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Filled.Search,
                contentDescription = null,
                tint = AppColors.TextTertiary,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "No results for \u201C$query\u201D",
                color = AppColors.TextSecondary,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
private fun SearchResultsList(
    uiState: SearchUiState,
    onTrackSelected: (Track) -> Unit,
    onToggleFavorite: (Long) -> Unit
) {
    val results = uiState.results
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {
        if (results.tracks.isNotEmpty()) {
            item { SectionLabel("Songs") }
            items(results.tracks, key = { "track_${it.id}" }) { track ->
                SearchTrackRow(
                    track = track,
                    isPlaying = track.id == uiState.currentTrackId,
                    isFavorite = track.id in uiState.favoriteTrackIds,
                    onClick = { onTrackSelected(track) },
                    onToggleFavorite = { onToggleFavorite(track.id) }
                )
            }
            item { Spacer(modifier = Modifier.height(8.dp)) }
        }
        if (results.albums.isNotEmpty()) {
            item { SectionLabel("Albums") }
            items(results.albums, key = { "album_${it.id}" }) { album ->
                SearchSimpleRow(title = album.name, subtitle = album.artist)
            }
            item { Spacer(modifier = Modifier.height(8.dp)) }
        }
        if (results.artists.isNotEmpty()) {
            item { SectionLabel("Artists") }
            items(results.artists, key = { "artist_${it.id}" }) { artist ->
                SearchSimpleRow(title = artist.name, subtitle = "${artist.trackCount} songs")
            }
            item { Spacer(modifier = Modifier.height(8.dp)) }
        }
        if (results.playlists.isNotEmpty()) {
            item { SectionLabel("Playlists") }
            items(results.playlists, key = { "playlist_${it.id}" }) { playlist ->
                SearchSimpleRow(title = playlist.name, subtitle = "${playlist.trackCount} songs")
            }
        }
        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

@Composable
private fun SectionLabel(label: String) {
    Text(
        text = label,
        color = AppColors.TextSecondary,
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.5.sp,
        modifier = Modifier.padding(top = 14.dp, bottom = 8.dp)
    )
}

@Composable
private fun SearchTrackRow(
    track: Track,
    isPlaying: Boolean,
    isFavorite: Boolean,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (isPlaying) AppColors.PurpleDim else Color.Transparent)
            .clickableNoRipple(onClick)
            .padding(horizontal = 6.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        CoverArt(artworkUri = track.artworkUri, size = 46.dp, cornerRadius = 11.dp)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = track.title,
                color = if (isPlaying) AppColors.Purple else AppColors.TextPrimary,
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
        Text(text = formatDuration(track.durationMs), color = AppColors.TextTertiary, fontSize = 12.sp)
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
private fun SearchSimpleRow(title: String, subtitle: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 6.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(AppColors.SurfaceAlt),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Filled.MusicNote, contentDescription = null, tint = AppColors.TextTertiary, modifier = Modifier.size(16.dp))
        }
        Column {
            Text(text = title, color = AppColors.TextPrimary, fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(text = subtitle, color = AppColors.TextSecondary, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

private fun formatDuration(durationMs: Long): String {
    val totalSeconds = durationMs / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}
