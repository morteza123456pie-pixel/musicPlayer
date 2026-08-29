package com.example.musicplayer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.musicplayer.ui.theme.AppColors

/**
 * Temporary stand-in so the navigation graph and app shell (Phase 1)
 * can be verified end-to-end before real screens exist.
 * Every usage here gets replaced in Phase 2+.
 */
@Composable
fun PlaceholderScreen(title: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.Background),
        contentAlignment = Alignment.Center
    ) {
        Text(text = "$title screen — coming in a later phase", color = AppColors.TextSecondary)
    }
}
