package com.example.musicplayer.service

import android.content.Intent
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.analytics.AnalyticsListener
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.example.musicplayer.audio.AudioEffectsManager
import com.example.musicplayer.audio.AudioEffectsSessionCallback
import com.example.musicplayer.data.settings.PlaybackPreferences
import com.example.musicplayer.data.settings.SleepTimerPreferences
import com.example.musicplayer.sleeptimer.SleepTimerAlarmScheduler
import com.example.musicplayer.sleeptimer.SleepTimerEndBehavior
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Foreground service hosting the real ExoPlayer instance and its
 * MediaSession. Extending [MediaSessionService] (rather than a plain
 * Service) gives us, for free:
 *
 *  - A system media notification with play/pause/next/prev controls
 *  - Headset button / Bluetooth media button handling
 *  - Android Auto / Wear OS / Assistant "play music" compatibility
 *  - Correct foreground-service lifecycle tied to playback state
 *
 * The UI never binds to this service directly — [com.example.musicplayer.player.MusicPlayerControllerImpl]
 * connects to it via a Media3 [androidx.media3.session.MediaController],
 * which is the supported way to talk to a MediaSessionService from the
 * app process. This keeps the service itself free of any UI-facing API
 * beyond the custom session commands added in Phase 5 for audio
 * effects (see [AudioEffectsSessionCallback]).
 *
 * Phase 5 additions:
 *  - [AudioEffectsManager] is attached to the player's real audio
 *    session as soon as it's known (and reattached if it ever changes)
 *    so the Equalizer/Bass Boost operate on real playback, not a
 *    simulated audio path.
 *  - [ACTION_SLEEP_TIMER_EXPIRED] lets [com.example.musicplayer.sleeptimer.SleepTimerAlarmReceiver]
 *    (fired by a real [android.app.AlarmManager] alarm, which survives
 *    app-process death) tell this service to perform the sleep timer's
 *    end behavior directly against the live [ExoPlayer] instance, with
 *    no dependency on a UI-side MediaController connection existing.
 */
@AndroidEntryPoint
class MusicPlaybackService : MediaSessionService() {

    @Inject lateinit var audioEffectsManager: AudioEffectsManager
    @Inject lateinit var sleepTimerPreferences: SleepTimerPreferences
    @Inject lateinit var alarmScheduler: SleepTimerAlarmScheduler
    @Inject lateinit var playbackPreferences: PlaybackPreferences

    private var mediaSession: MediaSession? = null
    private lateinit var player: ExoPlayer
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private val analyticsListener = object : AnalyticsListener {
        override fun onAudioSessionIdChanged(eventTime: AnalyticsListener.EventTime, audioSessionId: Int) {
            audioEffectsManager.attachToSession(audioSessionId)
        }
    }

    override fun onCreate() {
        super.onCreate()
        val sessionManager = MediaSessionManager(this)
        player = sessionManager.buildPlayer()
        player.addAnalyticsListener(analyticsListener)
        // The audio session id may already be assigned by the time
        // we get here (it's created eagerly by ExoPlayer's internal
        // audio track), so attach proactively rather than only ever
        // reacting to a future change.
        if (player.audioSessionId != androidx.media3.common.C.AUDIO_SESSION_ID_UNSET) {
            audioEffectsManager.attachToSession(player.audioSessionId)
        }

        mediaSession = sessionManager.buildSession(
            player = player,
            callback = AudioEffectsSessionCallback(
                audioEffectsManager = audioEffectsManager,
                player = player,
                playbackPreferences = playbackPreferences,
                serviceScope = serviceScope
            )
        )

        // Apply the persisted skip-silence preference to the real
        // player as soon as it's created, so it's active from the
        // very first playback after an app/process restart, not only
        // after the user re-toggles it in Settings.
        serviceScope.launch {
            val saved = playbackPreferences.snapshot.first()
            runCatching { player.skipSilenceEnabled = saved.skipSilenceEnabled }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_SLEEP_TIMER_EXPIRED) {
            handleSleepTimerExpired()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun handleSleepTimerExpired() {
        serviceScope.launch {
            val saved = sleepTimerPreferences.current()
            // Only act if a timer is actually still pending (guards
            // against a stale/duplicate alarm firing after the user
            // already cancelled from the UI, since AlarmManager.cancel
            // is best-effort against already-in-flight broadcasts).
            val endTimestamp = saved.endTimestampMs ?: return@launch
            if (endTimestamp - System.currentTimeMillis() > 0L) return@launch

            when (saved.endBehavior) {
                SleepTimerEndBehavior.PAUSE -> player.pause()
                SleepTimerEndBehavior.STOP_AND_CLEAR -> {
                    player.pause()
                    player.stop()
                    player.clearMediaItems()
                }
            }
            sleepTimerPreferences.clearTimer()
            alarmScheduler.cancel()
        }
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    override fun onDestroy() {
        // Release audio effects, then player, then session — effects
        // are tied to the player's audio session and must not outlive
        // it; the player must not outlive the session that wraps it.
        audioEffectsManager.release()
        player.removeAnalyticsListener(analyticsListener)
        player.release()
        mediaSession?.run {
            release()
            mediaSession = null
        }
        super.onDestroy()
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        // Stop playback and the service when the app task is swiped
        // away and nothing is actively playing — standard Media3
        // guidance to avoid an orphaned foreground service.
        val player = mediaSession?.player ?: return
        if (!player.playWhenReady || player.mediaItemCount == 0 || player.playbackState == Player.STATE_ENDED) {
            stopSelf()
        }
    }

    companion object {
        const val ACTION_SLEEP_TIMER_EXPIRED = "com.example.musicplayer.action.SLEEP_TIMER_EXPIRED"
    }
}
