package com.example.musicplayer.ui.library

import com.example.musicplayer.domain.model.Track

/** Which category chip is selected at the top of the Library screen. */
enum class LibraryTab { SONGS, ALBUMS, ARTISTS, PLAYLISTS }

/**
 * Everything the Library screen needs to render: the track list plus
 * which track is currently selected/playing (so the row can be
 * highlighted) and play state (so the row can show the right icon
 * later if needed).
 */
data class LibraryUiState(
    val selectedTab: LibraryTab = LibraryTab.SONGS,
    val tracks: List<Track> = emptyList(),
    val currentTrackId: Long? = null,
    val isPlaying: Boolean = false,
    val favoriteTrackIds: Set<Long> = emptySet()
)
