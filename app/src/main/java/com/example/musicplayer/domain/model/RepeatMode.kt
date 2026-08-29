package com.example.musicplayer.domain.model

/**
 * Repeat behavior for the queue. Shared domain concept used by the
 * player layer's [com.example.musicplayer.player.PlayerState] and
 * mapped to/from Media3's `Player.REPEAT_MODE_*` constants in
 * [com.example.musicplayer.player.MusicPlayerControllerImpl].
 */
enum class RepeatMode { OFF, ALL, ONE }
