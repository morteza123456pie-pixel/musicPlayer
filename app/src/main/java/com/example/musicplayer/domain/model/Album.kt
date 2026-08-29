package com.example.musicplayer.domain.model

data class Album(
    val id: Long,
    val name: String,
    val artist: String,
    val artworkUri: String? = null,
    val trackCount: Int = 0,
    val year: Int? = null
)
