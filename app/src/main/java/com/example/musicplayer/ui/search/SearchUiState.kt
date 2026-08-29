package com.example.musicplayer.ui.search

import com.example.musicplayer.data.search.SearchResults

/**
 * Everything the Search screen needs to render. When [query] is blank,
 * the screen shows [recentSearches] + [suggestionChips] instead of
 * [results] — see SearchScreen.kt's branch on `uiState.query.isBlank()`.
 */
data class SearchUiState(
    val query: String = "",
    val results: SearchResults = SearchResults(),
    val recentSearches: List<String> = emptyList(),
    val suggestionChips: List<String> = DEFAULT_SUGGESTIONS,
    val currentTrackId: Long? = null,
    val favoriteTrackIds: Set<Long> = emptySet()
)

/** Realistic starter suggestions shown before the user has any search history. */
val DEFAULT_SUGGESTIONS = listOf("Pop", "Rock", "Chill", "Workout", "Imagine Dragons", "Delacey")
