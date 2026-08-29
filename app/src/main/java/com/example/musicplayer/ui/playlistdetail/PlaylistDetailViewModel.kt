package com.example.musicplayer.ui.playlistdetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicplayer.data.favorites.FavoritesRepository
import com.example.musicplayer.data.sample.FAVORITES_PLAYLIST_ID
import com.example.musicplayer.data.sample.SampleMusicData
import com.example.musicplayer.domain.model.Playlist
import com.example.musicplayer.domain.model.Track
import com.example.musicplayer.player.MusicPlayerController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PlaylistDetailUiState(
    val playlist: Playlist? = null,
    val tracks: List<Track> = emptyList(),
    val currentTrackId: Long? = null,
    val favoriteTrackIds: Set<Long> = emptySet(),
    val isFavoritesPlaylist: Boolean = false
)

/**
 * Backs the Playlist Detail screen for every playlist, including the
 * built-in "Favorite Songs" playlist (id == [FAVORITES_PLAYLIST_ID]),
 * which is special-cased to read its track list from
 * [FavoritesRepository] in real time instead of the static
 * [Playlist.trackIds] every other sample playlist uses. Un/favoriting
 * a track anywhere in the app updates this screen immediately with no
 * restart, since both read the same repository StateFlow.
 */
@HiltViewModel
class PlaylistDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val playerController: MusicPlayerController,
    private val favoritesRepository: FavoritesRepository
) : ViewModel() {

    private val playlistId: Long = checkNotNull(savedStateHandle["playlistId"])

    val uiState: StateFlow<PlaylistDetailUiState> = combine(
        playerController.state,
        favoritesRepository.favoriteTrackIdsOrdered,
        favoritesRepository.favoriteTrackIds
    ) { playback, orderedFavoriteIds, favoriteIds ->
        val playlist = SampleMusicData.playlists.find { it.id == playlistId }
        val isFavorites = playlistId == FAVORITES_PLAYLIST_ID

        val tracks = if (isFavorites) {
            orderedFavoriteIds.mapNotNull { id -> SampleMusicData.tracks.find { it.id == id } }
        } else {
            playlist?.trackIds?.mapNotNull { id -> SampleMusicData.tracks.find { it.id == id } }.orEmpty()
        }

        PlaylistDetailUiState(
            playlist = playlist,
            tracks = tracks,
            currentTrackId = playback.currentTrack?.id,
            favoriteTrackIds = favoriteIds,
            isFavoritesPlaylist = isFavorites
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = PlaylistDetailUiState()
    )

    fun onTrackSelected(track: Track) {
        val queue = uiState.value.tracks
        val index = queue.indexOfFirst { it.id == track.id }.coerceAtLeast(0)
        playerController.playQueue(queue, index)
    }

    fun onToggleFavorite(trackId: Long) {
        viewModelScope.launch { favoritesRepository.toggleFavorite(trackId) }
    }
}
