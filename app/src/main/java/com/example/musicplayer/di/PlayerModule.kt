package com.example.musicplayer.di

import com.example.musicplayer.player.MusicPlayerController
import com.example.musicplayer.player.MusicPlayerControllerImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Binds the [MusicPlayerController] interface to its real,
 * Media3-backed implementation. Installed in [SingletonComponent] so
 * there is exactly one controller instance — and therefore exactly
 * one MediaController connection to [com.example.musicplayer.service.MusicPlaybackService]
 * — for the whole app process, which is what "one shared playback
 * source of truth" requires.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class PlayerModule {

    @Binds
    @Singleton
    abstract fun bindMusicPlayerController(
        impl: MusicPlayerControllerImpl
    ): MusicPlayerController
}
