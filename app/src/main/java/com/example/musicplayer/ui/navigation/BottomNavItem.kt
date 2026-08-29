package com.example.musicplayer.ui.navigation

import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.PlaylistPlay
import androidx.compose.material.icons.filled.MoreHoriz

data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector
)

// Fixed in Phase 2: "Home" and "Library" previously both pointed at
// Destination.Library.route (a duplicate-route bug that broke bottom
// nav selection highlighting). Home now uses its own distinct route.
// "More" now points directly at Settings — there is no separate
// "more" placeholder destination anymore.
val BOTTOM_NAV_ITEMS = listOf(
    BottomNavItem(Destination.Home.route, "Home", Icons.Filled.Home),
    BottomNavItem(Destination.Search.route, "Search", Icons.Filled.Search),
    BottomNavItem(Destination.Library.route, "Library", Icons.Filled.LibraryMusic),
    BottomNavItem(Destination.Playlists.route, "Playlist", Icons.Filled.PlaylistPlay),
    BottomNavItem(Destination.Settings.route, "More", Icons.Filled.MoreHoriz)
)
