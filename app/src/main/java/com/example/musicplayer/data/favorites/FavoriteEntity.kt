package com.example.musicplayer.data.favorites

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Persists only the fact that a track is favorited, keyed by the
 * track's existing domain id — not a redundant copy of the track's
 * title/artist/etc. [com.example.musicplayer.data.sample.SampleMusicData]
 * (and, later, a real MediaStore-backed track source) remains the
 * single source of truth for track metadata; this table is purely a
 * favorite/not-favorite flag with a timestamp for ordering.
 */
@Entity(tableName = "favorite_tracks")
data class FavoriteEntity(
    @PrimaryKey val trackId: Long,
    val favoritedAtMs: Long
)
