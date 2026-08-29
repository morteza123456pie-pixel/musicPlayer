package com.example.musicplayer.data.favorites

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository pattern wrapping [FavoriteDao], and the single favorites
 * state holder for the whole app — every screen that needs to know
 * "is track X favorited" or "what are my favorite tracks" reads
 * [favoriteTrackIds] / [favoriteTrackIdsOrdered] rather than querying
 * Room directly or keeping its own local favorite flag. This is what
 * makes favoriting a track from Now Playing show up instantly in
 * Search results, Library rows, and the Favorite Songs playlist
 * without an app restart.
 */
@Singleton
class FavoritesRepository @Inject constructor(
    private val favoriteDao: FavoriteDao
) {
    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /** Most-recently-favorited-first, for screens that render an ordered list (Favorite Songs playlist). */
    val favoriteTrackIdsOrdered: StateFlow<List<Long>> = favoriteDao.observeFavoriteTrackIds()
        .stateIn(
            scope = repositoryScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    /** Reactive set of favorited track ids, for cheap "is this track favorited" membership checks. */
    val favoriteTrackIds: StateFlow<Set<Long>> = favoriteDao.observeFavoriteTrackIds()
        .map { it.toSet() }
        .stateIn(
            scope = repositoryScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptySet()
        )

    suspend fun toggleFavorite(trackId: Long) {
        if (favoriteDao.isFavorite(trackId)) {
            favoriteDao.deleteByTrackId(trackId)
        } else {
            favoriteDao.insert(FavoriteEntity(trackId = trackId, favoritedAtMs = System.currentTimeMillis()))
        }
    }

    suspend fun setFavorite(trackId: Long, isFavorite: Boolean) {
        if (isFavorite) {
            favoriteDao.insert(FavoriteEntity(trackId = trackId, favoritedAtMs = System.currentTimeMillis()))
        } else {
            favoriteDao.deleteByTrackId(trackId)
        }
    }
}
