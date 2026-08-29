package com.example.musicplayer.ui.folders

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.musicplayer.data.sample.SampleMusicData
import com.example.musicplayer.domain.model.MusicFolder
import com.example.musicplayer.ui.components.HeaderIconButton
import com.example.musicplayer.ui.components.ScreenHeader
import com.example.musicplayer.ui.components.clickableNoRipple
import com.example.musicplayer.ui.theme.AppColors

/**
 * Real Folders screen (Phase 2): local music folder browser.
 * Folder rows currently have no drill-down destination (Phase 2 scope
 * does not include a folder-contents screen) — tapping is a no-op
 * hook left ready for a future phase.
 */
@Composable
fun FoldersScreen(
    onOpenSearch: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        ScreenHeader(
            title = "Folders",
            trailingContent = {
                HeaderIconButton(
                    icon = Icons.Filled.Search,
                    contentDescription = "Search",
                    onClick = onOpenSearch
                )
            }
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            items(SampleMusicData.folders, key = { it.path }) { folder ->
                FolderRow(folder = folder)
            }
            item { Spacer(modifier = Modifier.height(12.dp)) }
        }
    }
}

@Composable
private fun FolderRow(folder: MusicFolder) {
    val accentColor = remember(folder.accentColorHex) { parseHexColor(folder.accentColorHex) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickableNoRipple { /* Folder contents browsing is out of Phase 2 scope */ }
            .padding(horizontal = 6.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(RoundedCornerShape(13.dp))
                .background(AppColors.PurpleDim),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Folder,
                contentDescription = null,
                tint = accentColor
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = folder.name,
                color = AppColors.TextPrimary,
                fontSize = 14.5.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "${folder.itemCount} songs",
                color = AppColors.TextSecondary,
                fontSize = 12.5.sp
            )
        }
        Icon(
            imageVector = Icons.Filled.ChevronRight,
            contentDescription = null,
            tint = AppColors.TextTertiary
        )
    }
}

private fun parseHexColor(hex: String): Color {
    return try {
        Color(android.graphics.Color.parseColor(hex))
    } catch (e: IllegalArgumentException) {
        AppColors.Purple
    }
}
