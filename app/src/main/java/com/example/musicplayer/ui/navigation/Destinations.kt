package com.example.musicplayer.ui.navigation

/**
 * Every screen in the app, matching the reference 1:1.
 * Bottom-nav destinations are the top-level tabs; the rest are
 * pushed on top (Now Playing, Queue, Lyrics, Equalizer, Sleep Timer, etc).
 */
sealed class Destination(val route: String) {
    // --- Bottom navigation tabs -------------------------------------
    // Fixed in Phase 2: previously "Home" and "Library" both pointed
    // at Destination.Library.route, which meant two bottom-nav items
    // shared one route (breaks selection-state highlighting and back
    // stack behavior). Home is now its own route. It renders the same
    // LibraryScreen composable as Library (both are music-browsing
    // entry points in the reference), but as a separate destination
    // so the bottom nav can tell them apart.
    data object Home : Destination("home")
    data object Search : Destination("search")
    data object Library : Destination("library")
    data object Playlists : Destination("playlists")

    // "More" fixed in Phase 2: it was a placeholder route ("more")
    // that rendered a *different* placeholder than Settings, even
    // though the reference treats "More" as the entry point into
    // Settings. There is now only one real screen — Settings — and
    // the "More" bottom-nav item navigates straight to it. No
    // separate "more" route is registered in the NavHost.
    data object Settings : Destination("settings")

    // Reached from within Library/Home tab
    data object Albums : Destination("albums")
    data object Folders : Destination("folders")

    // Full-screen / pushed destinations (no bottom nav, has back button)
    data object NowPlaying : Destination("now_playing")
    data object Queue : Destination("queue")
    data object Lyrics : Destination("lyrics")
    data object Equalizer : Destination("equalizer")
    data object SleepTimer : Destination("sleep_timer")

    // Phase 5: Settings sub-screens
    data object Appearance : Destination("settings/appearance")
    data object Language : Destination("settings/language")
    data object PlaybackSettings : Destination("settings/playback")
    data object BackupRestore : Destination("settings/backup_restore")

    data object PlaylistDetail : Destination("playlist_detail/{playlistId}") {
        fun createRoute(playlistId: Long) = "playlist_detail/$playlistId"
    }

    data object AlbumDetail : Destination("album_detail/{albumId}") {
        fun createRoute(albumId: Long) = "album_detail/$albumId"
    }
}

/** Destinations that show the custom bottom navigation bar. */
val BOTTOM_NAV_ROUTES = setOf(
    Destination.Home.route,
    Destination.Search.route,
    Destination.Library.route,
    Destination.Playlists.route,
    Destination.Settings.route
)

/** Destinations that show the persistent mini-player above the bottom nav / content. */
val MINI_PLAYER_HIDDEN_ROUTES = setOf(
    Destination.NowPlaying.route,
    Destination.Lyrics.route
)
