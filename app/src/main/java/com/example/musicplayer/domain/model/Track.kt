package com.example.musicplayer.domain.model

/**
 * Core domain representation of a playable song.
 * [uri] points to a local content:// or file:// URI once real
 * device audio is wired in; sample data can use a placeholder string.
 */
data class Track(
    val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val durationMs: Long,
    val uri: String,
    val artworkUri: String? = null,
    val albumId: Long? = null,
    val trackNumber: Int? = null
)
