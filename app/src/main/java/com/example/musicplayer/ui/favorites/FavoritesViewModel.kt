package com.example.musicplayer.ui.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicplayer.data.favorites.FavoritesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Thin, reusable wrapper around [FavoritesRepository] for screens that
 * only need to toggle/read favorite state inline (e.g. Now Playing's
 * heart icon) without needing a full favorites list. Screens that
 * display the favorites list itself (Favorite Songs playlist detail)
 * read [FavoritesRepository.favoriteTrackIds] directly alongside
 * SampleMusicData rather than duplicating that logic here.
 */
@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val favoritesRepository: FavoritesRepository
) : ViewModel() {

    val favoriteTrackIds = favoritesRepository.favoriteTrackIds

    fun toggleFavorite(trackId: Long) {
        viewModelScope.launch { favoritesRepository.toggleFavorite(trackId) }
    }
}
