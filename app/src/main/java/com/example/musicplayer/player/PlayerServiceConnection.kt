package com.example.musicplayer.player

import android.content.ComponentName
import android.content.Context
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.example.musicplayer.service.MusicPlaybackService
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Encapsulates connecting to [MusicPlaybackService] via Media3's
 * [MediaController], which is the supported client-side handle for
 * talking to a [androidx.media3.session.MediaSessionService].
 *
 * This is the only place in the app that constructs a [SessionToken]
 * or a raw [MediaController] — [MusicPlayerControllerImpl] depends on
 * this class rather than touching Media3's session APIs directly, so
 * the connection/teardown lifecycle lives in exactly one place.
 */
class PlayerServiceConnection(private val context: Context) {

    private var controllerFuture: ListenableFuture<MediaController>? = null

    /**
     * Suspends until a [MediaController] connected to
     * [MusicPlaybackService] is ready. Safe to call once and reuse the
     * result — callers (here, [MusicPlayerControllerImpl]) are
     * expected to hold onto the returned controller for the app's
     * lifetime and call [release] when done.
     */
    suspend fun connect(): MediaController = suspendCancellableCoroutine { continuation ->
        val sessionToken = SessionToken(context, ComponentName(context, MusicPlaybackService::class.java))
        val future = MediaController.Builder(context, sessionToken).buildAsync()
        controllerFuture = future

        future.addListener(
            {
                try {
                    val controller = future.get()
                    if (continuation.isActive) continuation.resume(controller)
                } catch (t: Throwable) {
                    if (continuation.isActive) continuation.resumeWithException(t)
                }
            },
            MoreExecutors.directExecutor()
        )

        continuation.invokeOnCancellation {
            controllerFuture?.let { MediaController.releaseFuture(it) }
        }
    }

    fun release() {
        controllerFuture?.let { MediaController.releaseFuture(it) }
        controllerFuture = null
    }
}
