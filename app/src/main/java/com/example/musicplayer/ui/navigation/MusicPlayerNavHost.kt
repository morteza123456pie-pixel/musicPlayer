package com.example.musicplayer.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.musicplayer.ui.albums.AlbumsScreen
import com.example.musicplayer.ui.components.PlaceholderScreen
import com.example.musicplayer.ui.equalizer.EqualizerScreen
import com.example.musicplayer.ui.folders.FoldersScreen
import com.example.musicplayer.ui.library.LibraryScreen
import com.example.musicplayer.ui.lyrics.LyricsScreen
import com.example.musicplayer.ui.nowplaying.NowPlayingScreen
import com.example.musicplayer.ui.playlistdetail.PlaylistDetailScreen
import com.example.musicplayer.ui.playlists.PlaylistsScreen
import com.example.musicplayer.ui.queue.QueueScreen
import com.example.musicplayer.ui.search.SearchScreen
import com.example.musicplayer.ui.settings.AppearanceScreen
import com.example.musicplayer.ui.settings.LanguageScreen
import com.example.musicplayer.ui.settings.SettingsScreen
import com.example.musicplayer.ui.settings.backup.BackupRestoreScreen
import com.example.musicplayer.ui.settings.playback.PlaybackSettingsScreen
import com.example.musicplayer.ui.sleeptimer.SleepTimerScreen

/**
 * Root navigation graph.
 *
 * Status as of Phase 4:
 *  - Library, Albums, Playlists, Folders, Now Playing are real screens
 *    (Phase 2/3).
 *  - Search, Queue, Lyrics, and Playlist Detail (including the
 *    built-in "Favorite Songs" playlist) are real screens (Phase 4).
 *  - Home renders the same LibraryScreen as Library (see Destination.kt
 *    for why these are two routes rather than one shared route).
 *  - AlbumDetail remains a placeholder scaffold — full album detail is
 *    still out of scope.
 *  - Equalizer, Settings, SleepTimer remain PlaceholderScreen — those
 *    belong to Phase 5.
 *
 * Kept intentionally thin: each route wires to exactly one screen
 * composable, no business logic lives here.
 */
@Composable
fun MusicPlayerNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Destination.Home.route,
        modifier = modifier
    ) {
        composable(Destination.Home.route) {
            LibraryScreen(
                onOpenSearch = { navController.navigate(Destination.Search.route) },
                onOpenAlbums = { navController.navigate(Destination.Albums.route) },
                onOpenPlaylists = { navController.navigate(Destination.Playlists.route) }
            )
        }
        composable(Destination.Library.route) {
            LibraryScreen(
                onOpenSearch = { navController.navigate(Destination.Search.route) },
                onOpenAlbums = { navController.navigate(Destination.Albums.route) },
                onOpenPlaylists = { navController.navigate(Destination.Playlists.route) }
            )
        }
        composable(Destination.Search.route) {
            // Search is also a bottom-nav tab, so it's reached both as
            // a top-level destination (no back arrow needed — the
            // bottom nav itself is the way back) and via onOpenSearch
            // from other screens (also fine without a back arrow here,
            // since it's a single registered destination either way;
            // the system back button still pops correctly).
            SearchScreen(
                onBack = null,
                onOpenNowPlaying = { navController.navigate(Destination.NowPlaying.route) }
            )
        }
        composable(Destination.Playlists.route) {
            PlaylistsScreen(
                onOpenPlaylistDetail = { playlistId ->
                    navController.navigate(Destination.PlaylistDetail.createRoute(playlistId))
                }
            )
        }
        composable(Destination.Albums.route) {
            AlbumsScreen(
                onOpenSearch = { navController.navigate(Destination.Search.route) },
                onOpenAlbumDetail = { albumId ->
                    navController.navigate(Destination.AlbumDetail.createRoute(albumId))
                }
            )
        }
        composable(Destination.Folders.route) {
            FoldersScreen(
                onOpenSearch = { navController.navigate(Destination.Search.route) }
            )
        }
        composable(
            route = Destination.AlbumDetail.route,
            arguments = listOf(navArgument("albumId") { type = NavType.LongType })
        ) {
            // Minimal scaffold only — full album detail (track list for
            // that album, play-all action, etc.) is out of scope so far.
            PlaceholderScreen(title = "Album Detail")
        }
        composable(
            route = Destination.PlaylistDetail.route,
            arguments = listOf(navArgument("playlistId") { type = NavType.LongType })
        ) {
            PlaylistDetailScreen(
                onBack = { navController.popBackStack() },
                onOpenNowPlaying = { navController.navigate(Destination.NowPlaying.route) }
            )
        }
        composable(Destination.NowPlaying.route) {
            NowPlayingScreen(
                onBack = { navController.popBackStack() },
                onOpenQueue = { navController.navigate(Destination.Queue.route) },
                onOpenLyrics = { navController.navigate(Destination.Lyrics.route) }
            )
        }
        composable(Destination.Queue.route) {
            QueueScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable(Destination.Lyrics.route) {
            LyricsScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable(Destination.Equalizer.route) {
            EqualizerScreen(onBack = { navController.popBackStack() })
        }
        composable(Destination.Settings.route) {
            SettingsScreen(
                onNavigateToAppearance = { navController.navigate(Destination.Appearance.route) },
                onNavigateToLanguage = { navController.navigate(Destination.Language.route) },
                onNavigateToEqualizer = { navController.navigate(Destination.Equalizer.route) },
                onNavigateToPlaybackSettings = { navController.navigate(Destination.PlaybackSettings.route) },
                onNavigateToSleepTimer = { navController.navigate(Destination.SleepTimer.route) },
                onNavigateToBackupRestore = { navController.navigate(Destination.BackupRestore.route) }
            )
        }
        composable(Destination.SleepTimer.route) {
            SleepTimerScreen(onBack = { navController.popBackStack() })
        }
        composable(Destination.Appearance.route) {
            AppearanceScreen(onBack = { navController.popBackStack() })
        }
        composable(Destination.Language.route) {
            LanguageScreen(onBack = { navController.popBackStack() })
        }
        composable(Destination.PlaybackSettings.route) {
            PlaybackSettingsScreen(onBack = { navController.popBackStack() })
        }
        composable(Destination.BackupRestore.route) {
            BackupRestoreScreen(onBack = { navController.popBackStack() })
        }
    }
}
