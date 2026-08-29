package com.example.musicplayer.domain.model

data class MusicFolder(
    val path: String,
    val name: String,
    val itemCount: Int,
    val accentColorHex: String = "#8A6AE8"
)
