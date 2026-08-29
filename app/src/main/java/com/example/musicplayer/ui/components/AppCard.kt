package com.example.musicplayer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.musicplayer.ui.theme.AppColors

/**
 * Base surface used throughout: rounded dark card with a thin,
 * low-opacity blue-gray border — the reference's signature "glass-like
 * dark surface" look, without literal glassmorphism/blur.
 */
@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    cornerRadius: androidx.compose.ui.unit.Dp = 16.dp,
    content: @Composable () -> Unit
) {
    androidx.compose.foundation.layout.Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(AppColors.Surface)
            .border(1.dp, AppColors.Border, RoundedCornerShape(cornerRadius))
    ) {
        content()
    }
}
