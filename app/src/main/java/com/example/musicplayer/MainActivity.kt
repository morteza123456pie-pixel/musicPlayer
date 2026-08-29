package com.example.musicplayer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.musicplayer.ui.components.AppBottomNavigation
import com.example.musicplayer.ui.components.MiniPlayer
import com.example.musicplayer.ui.navigation.BOTTOM_NAV_ROUTES
import com.example.musicplayer.ui.navigation.Destination
import com.example.musicplayer.ui.navigation.MINI_PLAYER_HIDDEN_ROUTES
import com.example.musicplayer.ui.navigation.MusicPlayerNavHost
import com.example.musicplayer.ui.theme.AppColors
import com.example.musicplayer.ui.theme.MusicPlayerTheme
import com.example.musicplayer.ui.theme.ThemeViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeViewModel: ThemeViewModel = hiltViewModel()
            val isDarkTheme by themeViewModel.isDarkTheme.collectAsState()
            MusicPlayerTheme(darkTheme = isDarkTheme) {
                MusicPlayerAppShell()
            }
        }
    }
}

/**
 * App shell: wraps the NavHost with the custom bottom navigation bar
 * and the persistent MiniPlayer.
 *
 * Phase 3: the MiniPlayer is now wired to [MainViewModel], which
 * exposes the real Media3-backed [com.example.musicplayer.player.MusicPlayerController]
 * state. Selecting a track anywhere (Library, and eventually Albums/
 * Playlists/Folders) plays it through the same shared player, and the
 * MiniPlayer reflects that automatically — there is exactly one
 * playback source of truth for the whole app. The MiniPlayer is
 * hidden on routes in [MINI_PLAYER_HIDDEN_ROUTES] (Now Playing /
 * Lyrics) so it doesn't visually duplicate the full player screen.
 *
 * Phase 3.5 fix: `enableEdgeToEdge()` in [MainActivity] draws content
 * behind the system status/navigation bars, but nothing was consuming
 * those insets, so screen headers rendered underneath (or overlapping)
 * the status bar, and the bottom nav sat flush against — or behind —
 * the gesture navigation area on real devices. This shell now applies
 * `WindowInsets.statusBars` padding once, at the top of the scrollable
 * content area, and `WindowInsets.navigationBars` padding once, around
 * the whole bottom (mini-player + bottom nav) group. Individual
 * screens do NOT need their own inset handling — this is the single
 * place it's applied for the entire app.
 */
@Composable
fun MusicPlayerAppShell(
    mainViewModel: MainViewModel = hiltViewModel()
) {
    val navController: NavHostController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val playerState by mainViewModel.playerState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.Background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Status bar inset: pushes every screen's content below the
            // system status bar instead of drawing underneath it. Applied
            // once here rather than per-screen (e.g. inside ScreenHeader)
            // so every current and future screen gets it automatically.
            MusicPlayerNavHost(
                navController = navController,
                modifier = Modifier
                    .weight(1f)
                    .windowInsetsPadding(WindowInsets.statusBars)
            )

            // Bottom group (mini-player + bottom nav) gets the navigation
            // bar / gesture inset applied around it as a whole, so it
            // sits fully above the gesture area on devices that use
            // gesture navigation instead of 3-button navigation.
            Column(
                modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                val showMiniPlayer = playerState.currentTrack != null &&
                    currentRoute !in MINI_PLAYER_HIDDEN_ROUTES

                if (showMiniPlayer) {
                    MiniPlayer(
                        track = playerState.currentTrack!!,
                        isPlaying = playerState.isPlaying,
                        onTogglePlay = mainViewModel::onTogglePlayPause,
                        onOpenNowPlaying = {
                            navController.navigate(Destination.NowPlaying.route)
                        },
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                if (currentRoute in BOTTOM_NAV_ROUTES) {
                    AppBottomNavigation(
                        currentRoute = currentRoute,
                        onNavigate = { route ->
                            navController.navigate(route) {
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    }
}
