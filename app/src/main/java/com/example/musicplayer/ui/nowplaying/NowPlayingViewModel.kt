package com.example.musicplayer.ui.nowplaying

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicplayer.data.favorites.FavoritesRepository
import com.example.musicplayer.player.MusicPlayerController
import com.example.musicplayer.player.PlayerState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Thin ViewModel for the Now Playing screen. Every action here is a
 * direct forward to [MusicPlayerController] — the screen has no
 * playback logic of its own, and observes the same shared [state]
 * the MiniPlayer and Library screen observe, so seeking/pausing here
 * is instantly reflected everywhere else too.
 */
@HiltViewModel
class NowPlayingViewModel @Inject constructor(
    private val playerController: MusicPlayerController,
    private val favoritesRepository: FavoritesRepository
) : ViewModel() {

    val state: StateFlow<PlayerState> = playerController.state.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = PlayerState()
    )

    /** True when the currently playing track is favorited. Derived from the same shared repository every other screen reads. */
    val isCurrentTrackFavorite: StateFlow<Boolean> = combine(
        state,
        favoritesRepository.favoriteTrackIds
    ) { playback, favoriteIds ->
        playback.currentTrack?.id != null && playback.currentTrack.id in favoriteIds
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = false
    )

    fun onTogglePlayPause() = playerController.togglePlayPause()
    fun onNext() = playerController.skipToNext()
    fun onPrevious() = playerController.skipToPrevious()
    fun onSeek(positionMs: Long) = playerController.seekTo(positionMs)
    fun onToggleShuffle() = playerController.toggleShuffle()
    fun onCycleRepeat() = playerController.cycleRepeatMode()

    fun onToggleFavorite() {
        val trackId = state.value.currentTrack?.id ?: return
        viewModelScope.launch { favoritesRepository.toggleFavorite(trackId) }
    }
}
