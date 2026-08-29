package com.example.musicplayer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.musicplayer.ui.theme.AppColors

/**
 * Renders album/track artwork. If [artworkUri] is null (sample data /
 * no embedded art yet), falls back to a flat dark gradient placeholder
 * with a subtle music-note glyph — a deliberately calm, professional
 * "no artwork" treatment rather than an attention-grabbing visual.
 *
 * Phase 3.5 fix: the previous placeholder used a radial gradient with
 * an unbounded default radius plus a separately-aligned "moon" circle
 * inside a second aspectRatio Box. On a real device that combination
 * rendered as a glowing purple blob with a bright dot escaping the
 * card's clipped bounds (visible in the Phase 3 device screenshot).
 * This version clips once, at the outermost Box, and every child
 * (gradient + icon) is a plain fillMaxSize/centered child of that same
 * clipped Box — nothing can render outside the card's rounded bounds.
 */
@Composable
fun CoverArt(
    artworkUri: String?,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    cornerRadius: Dp = 11.dp,
    seedColor: Color = AppColors.Purple
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(cornerRadius))
    ) {
        if (artworkUri != null) {
            AsyncImage(
                model = artworkUri,
                contentDescription = null,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            CoverArtFallback(seedColor = seedColor, modifier = Modifier.fillMaxSize())
        }
    }
}

/** Square cinematic cover for the Now Playing screen (large, atmospheric). */
@Composable
fun NowPlayingCoverArt(
    artworkUri: String?,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 26.dp,
    seedColor: Color = AppColors.Purple
) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(cornerRadius))
    ) {
        if (artworkUri != null) {
            AsyncImage(
                model = artworkUri,
                contentDescription = null,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            CoverArtFallback(seedColor = seedColor, modifier = Modifier.fillMaxSize(), large = true)
        }
    }
}

/**
 * Flat, deliberately understated fallback: a diagonal dark gradient
 * (top-left seed color fading to near-black, no radial glow, no
 * unbounded-radius artifacts) with a centered, low-opacity music-note
 * glyph. Every element here is a direct, fillMaxSize/centered child of
 * the single Box the caller already clips — there is no nested
 * aspectRatio Box and no independently-aligned circle that could
 * escape the clip bounds.
 */
@Composable
private fun CoverArtFallback(
    seedColor: Color,
    modifier: Modifier = Modifier,
    large: Boolean = false
) {
    Box(
        modifier = modifier.background(
            Brush.linearGradient(
                colors = listOf(
                    seedColor.copy(alpha = 0.55f),
                    Color(0xFF1B1D2E),
                    Color(0xFF0D0A1F)
                )
            )
        ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Filled.MusicNote,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.35f),
            modifier = Modifier.size(if (large) 56.dp else 18.dp)
        )
    }
}
