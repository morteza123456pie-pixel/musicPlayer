package com.example.musicplayer.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Shape language from the reference: smooth rounded corners everywhere,
 * larger radii for album art and cards, fully circular for the main
 * playback button and mini-player play/pause control.
 */
val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(26.dp)
)

object AppRadii {
    val ListItemCover = 11.dp
    val MiniPlayerCover = 9.dp
    val GridAlbumCover = 16.dp
    val NowPlayingCover = 26.dp
    val Card = 16.dp
    val Chip = 20.dp
    val IconButton = 12.dp
    val Full = 999.dp
}
