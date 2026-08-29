package com.example.musicplayer.service

import android.content.Context
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession

/**
 * Builds the ExoPlayer instance and the MediaSession that wraps it.
 * Kept separate from [MusicPlaybackService] so the service class stays
 * focused on Android service lifecycle, not player/session
 * configuration.
 *
 * [AudioAttributes] + `setHandleAudioBecomingNoisy(true)` gives us
 * correct audio-focus behavior and automatic pause on headset
 * disconnect "for free" from Media3, satisfying the brief's audio
 * focus handling requirement without custom AudioManager code.
 */
class MediaSessionManager(private val context: Context) {

    fun buildPlayer(): ExoPlayer {
        val audioAttributes = AudioAttributes.Builder()
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .setUsage(C.USAGE_MEDIA)
            .build()

        return ExoPlayer.Builder(context)
            .setAudioAttributes(audioAttributes, /* handleAudioFocus = */ true)
            .setHandleAudioBecomingNoisy(true)
            .build()
    }

    /**
     * [callback] handles custom session commands (Phase 5: audio
     * effects control from [com.example.musicplayer.audio.AudioEffectsManager]).
     * Kept as a parameter rather than hardcoded here so this class
     * stays focused on player/session construction only.
     */
    fun buildSession(player: ExoPlayer, callback: MediaSession.Callback? = null): MediaSession {
        val builder = MediaSession.Builder(context, player)
        if (callback != null) builder.setCallback(callback)
        return builder.build()
    }
}
