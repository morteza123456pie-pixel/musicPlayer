package com.example.musicplayer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.musicplayer.ui.theme.AppColors

/**
 * Pill-shaped selectable chip: filled purple when selected, outlined
 * transparent otherwise. Used for Library category tabs, EQ presets,
 * and search suggestion chips.
 */
@Composable
fun SelectableChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Text(
        text = label,
        color = if (selected) Color.White else AppColors.TextSecondary,
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (selected) AppColors.Purple else Color.Transparent)
            .border(
                width = 1.dp,
                color = if (selected) Color.Transparent else AppColors.Border,
                shape = RoundedCornerShape(20.dp)
            )
            .clickableNoRipple(onClick)
            .padding(horizontal = 16.dp, vertical = 7.dp)
    )
}
