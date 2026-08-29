package com.example.musicplayer.domain.model

data class Playlist(
    val id: Long,
    val name: String,
    val trackIds: List<Long> = emptyList(),
    val accentColorHex: String = "#8A6AE8",
    val iconKey: String = "music"
) {
    val trackCount: Int get() = trackIds.size
}
