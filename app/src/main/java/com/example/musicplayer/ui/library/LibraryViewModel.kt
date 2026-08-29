package com.example.musicplayer.ui.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicplayer.data.favorites.FavoritesRepository
import com.example.musicplayer.data.sample.SampleMusicData
import com.example.musicplayer.domain.model.Track
import com.example.musicplayer.player.MusicPlayerController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val playerController: MusicPlayerController,
    private val favoritesRepository: FavoritesRepository
) : ViewModel() {

    private val selectedTab = MutableStateFlow(LibraryTab.SONGS)

    val uiState: StateFlow<LibraryUiState> = combine(
        selectedTab,
        playerController.state,
        favoritesRepository.favoriteTrackIds
    ) { tab, playback, favoriteIds ->
        LibraryUiState(
            selectedTab = tab,
            tracks = SampleMusicData.tracks,
            currentTrackId = playback.currentTrack?.id,
            isPlaying = playback.isPlaying,
            favoriteTrackIds = favoriteIds
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = LibraryUiState(tracks = SampleMusicData.tracks)
    )

    fun selectTab(tab: LibraryTab) {
        selectedTab.value = tab
    }

    /**
     * Selecting a track plays it through the real player layer, with
     * the full visible track list as the queue — this is now backed
     * by Media3/ExoPlayer via [MusicPlayerController] rather than the
     * Phase 2 visual-only PlaybackStateHolder. The mini-player (and
     * any other screen observing [MusicPlayerController.state]) picks
     * up the change automatically since there is exactly one shared
     * player state.
     */
    fun onTrackSelected(track: Track) {
        val queue = SampleMusicData.tracks
        val index = queue.indexOfFirst { it.id == track.id }.coerceAtLeast(0)
        playerController.playQueue(queue, index)
    }

    fun onToggleFavorite(trackId: Long) {
        viewModelScope.launch { favoritesRepository.toggleFavorite(trackId) }
    }
}
