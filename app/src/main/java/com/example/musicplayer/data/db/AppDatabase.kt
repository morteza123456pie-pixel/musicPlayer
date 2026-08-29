package com.example.musicplayer.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.musicplayer.data.favorites.FavoriteDao
import com.example.musicplayer.data.favorites.FavoriteEntity

/**
 * Single Room database for the app. Phase 4 adds the first real table
 * ([FavoriteEntity]) — future persisted entities (downloaded track
 * cache, playlists once they're user-editable, etc.) belong here too
 * rather than spawning separate databases.
 */
@Database(
    entities = [FavoriteEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun favoriteDao(): FavoriteDao
}
