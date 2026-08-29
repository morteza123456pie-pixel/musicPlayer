package com.example.musicplayer.sleeptimer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.example.musicplayer.service.MusicPlaybackService

/**
 * Receives the [SleepTimerAlarmScheduler]-scheduled alarm. This can run
 * even when the app's normal process (and therefore [SleepTimerManager]'s
 * in-memory countdown) has been killed by the system, which is exactly
 * the case it exists to cover.
 *
 * Deliberately does not try to perform the pause/stop itself — it has
 * no guaranteed live [com.example.musicplayer.player.MusicPlayerController]
 * connection to act through. Instead it starts [MusicPlaybackService]
 * with an explicit action, and the service (which owns the real
 * ExoPlayer instance directly) performs the end behavior itself. If
 * the service process is already dead, starting it fresh is safe:
 * [MusicPlaybackService] re-reads the persisted timer state on create
 * and finds it already expired, so the net effect is identical to a
 * live in-process countdown finishing.
 */
class SleepTimerAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val serviceIntent = Intent(context, MusicPlaybackService::class.java).apply {
            action = MusicPlaybackService.ACTION_SLEEP_TIMER_EXPIRED
        }
        ContextCompat.startForegroundService(context, serviceIntent)
    }
}
