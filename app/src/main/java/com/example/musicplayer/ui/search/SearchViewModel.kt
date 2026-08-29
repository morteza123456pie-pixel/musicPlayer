package com.example.musicplayer.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicplayer.data.favorites.FavoritesRepository
import com.example.musicplayer.data.sample.SampleMusicData
import com.example.musicplayer.data.search.RecentSearchRepository
import com.example.musicplayer.data.search.SearchResults
import com.example.musicplayer.domain.model.Track
import com.example.musicplayer.player.MusicPlayerController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Search over the app's current in-memory sample data (Track/Album/
 * Artist/Playlist) — per the Phase 4 brief, no separate fake search
 * dataset, and results feed straight into the same shared
 * [MusicPlayerController] the rest of the app uses.
 *
 * The query itself updates state immediately (so the text field never
 * feels laggy); actual result computation is debounced slightly so
 * fast typing doesn't recompute four list filters on every keystroke.
 */
@OptIn(FlowPreview::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val playerController: MusicPlayerController,
    private val recentSearchRepository: RecentSearchRepository,
    private val favoritesRepository: FavoritesRepository
) : ViewModel() {

    private val queryFlow = MutableStateFlow("")

    // Debounced separately from queryFlow so the text field binds to
    // the un-debounced value (no visible input lag) while result
    // computation only re-runs ~200ms after typing pauses.
    private val debouncedQuery = queryFlow.debounce(200)

    val uiState: StateFlow<SearchUiState> = combine(
        queryFlow,
        debouncedQuery,
        recentSearchRepository.recentSearches,
        playerController.state,
        favoritesRepository.favoriteTrackIds
    ) { query, debounced, recents, playback, favoriteIds ->
        SearchUiState(
            query = query,
            results = if (debounced.isBlank()) SearchResults() else search(debounced),
            recentSearches = recents,
            currentTrackId = playback.currentTrack?.id,
            favoriteTrackIds = favoriteIds
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SearchUiState()
    )

    fun onQueryChanged(newQuery: String) {
        queryFlow.value = newQuery
    }

    fun onClearQuery() {
        queryFlow.value = ""
    }

    /** Commits the current query into recent-search history (call on submit/track-tap, not every keystroke). */
    fun commitSearch(query: String = queryFlow.value) {
        if (query.isBlank()) return
        viewModelScope.launch { recentSearchRepository.addSearch(query) }
    }

    fun onSuggestionSelected(suggestion: String) {
        queryFlow.value = suggestion
        commitSearch(suggestion)
    }

    fun onClearHistory() {
        viewModelScope.launch { recentSearchRepository.clearHistory() }
    }

    /**
     * Selecting a track from results plays it through the same shared
     * player every other screen uses. The result list itself (already
     * filtered to matching tracks) becomes the queue, mirroring how
     * Library treats its visible list as the queue.
     */
    fun onTrackSelected(track: Track) {
        commitSearch()
        val queue = uiState.value.results.tracks
        val index = queue.indexOfFirst { it.id == track.id }.coerceAtLeast(0)
        playerController.playQueue(queue, index)
    }

    fun onToggleFavorite(trackId: Long) {
        viewModelScope.launch { favoritesRepository.toggleFavorite(trackId) }
    }

    private fun search(rawQuery: String): SearchResults {
        val q = rawQuery.trim()
        if (q.isEmpty()) return SearchResults()

        // Case-insensitive substring match across English/Persian/mixed
        // text — Kotlin's default String.contains with ignoreCase
        // works directly on the Unicode text, no locale-specific
        // handling required for simple substring search.
        fun matches(vararg fields: String) = fields.any { it.contains(q, ignoreCase = true) }

        val tracks = SampleMusicData.tracks.filter { matches(it.title, it.artist, it.album) }
        val albums = SampleMusicData.albums.filter { matches(it.name, it.artist) }
        val artists = SampleMusicData.artists.filter { matches(it.name) }
        val playlists = SampleMusicData.playlists.filter { matches(it.name) }

        return SearchResults(tracks = tracks, albums = albums, artists = artists, playlists = playlists)
    }
}
