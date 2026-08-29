package com.example.musicplayer.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Color tokens derived directly from the reference image.
 * Dark navy/near-black background, purple primary accent,
 * soft blue secondary accent, low-opacity blue-gray borders.
 */
object AppColors {
    // Backgrounds
    val Background = Color(0xFF0A0B14)
    val BackgroundGradientTop = Color(0xFF12101F)
    val Surface = Color(0xFF161826)
    val SurfaceAlt = Color(0xFF1B1D2E)
    val SurfaceElevated = Color(0xFF1D1F33)

    // Borders
    val Border = Color(0x248C96C8)        // low-opacity blue-gray
    val BorderStrong = Color(0x388C96C8)

    // Accents
    val Purple = Color(0xFF8B6AE8)
    val PurpleDim = Color(0x298B6AE8)
    val PurpleDark = Color(0xFF6A4FC8)
    val Blue = Color(0xFF5AA6E8)
    val Pink = Color(0xFFE85A8A)

    // Text
    val TextPrimary = Color(0xFFF2F1F8)
    val TextSecondary = Color(0xFF8B8CA3)
    val TextTertiary = Color(0xFF5C5D75)

    // Playlist / folder accent palette (distinct but theme-consistent)
    val AccentPink = Color(0xFFE85A8A)
    val AccentGreen = Color(0xFF3AD98A)
    val AccentPurple = Color(0xFF8A6AE8)
    val AccentRed = Color(0xFFE85A5A)
    val AccentBlue = Color(0xFF4AA6E8)
}
