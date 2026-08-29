package com.example.musicplayer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.musicplayer.ui.navigation.BOTTOM_NAV_ITEMS
import com.example.musicplayer.ui.theme.AppColors

/**
 * Custom bottom navigation bar — deliberately not the default Material3
 * NavigationBar, to match the reference's premium custom look:
 * transparent unselected icons, subtle purple accent on the active tab,
 * no ripple/elevation artifacts.
 *
 * Phase 3.5 fix: on a real device the icons rendered too small (21dp)
 * with a cramped touch target (short vertical padding, no minimum
 * height on the bar itself) and a barely-visible single-pixel top
 * border, making the whole bar look like a thin, unclear strip rather
 * than a clear navigation surface. This version:
 *  - increases icons to 26dp and gives each item a proper 48dp minimum
 *    touch target height (Android's accessibility minimum)
 *  - gives the bar itself a fixed comfortable height instead of
 *    relying purely on content + padding to size it
 *  - keeps the same purple-accent-on-selected, custom (non-Material)
 *    look — no default NavigationBar component is used
 */
@Composable
fun AppBottomNavigation(
    currentRoute: String?,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 72.dp)
            .background(AppColors.Background)
            .border(width = 1.dp, color = AppColors.Border)
            .padding(top = 10.dp, bottom = 10.dp, start = 4.dp, end = 4.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        BOTTOM_NAV_ITEMS.forEach { item ->
            val isActive = currentRoute == item.route
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 48.dp)
                    .clickableNoRipple { onNavigate(item.route) }
                    .padding(vertical = 6.dp),
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = item.label,
                    tint = if (isActive) AppColors.Purple else AppColors.TextSecondary,
                    modifier = Modifier.size(26.dp)
                )
                Text(
                    text = item.label,
                    color = if (isActive) AppColors.Purple else AppColors.TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Medium
                )
            }
        }
    }
}
