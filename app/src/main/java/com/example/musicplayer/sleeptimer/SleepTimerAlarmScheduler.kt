package com.example.musicplayer.sleeptimer

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.content.getSystemService
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Schedules/cancels the [SleepTimerAlarmReceiver] alarm that backs the
 * sleep timer with a real [AlarmManager] entry.
 *
 * [SleepTimerManager]'s in-process coroutine countdown handles the
 * live "time remaining" UI while the app process is alive, but a
 * plain coroutine `delay()` cannot fire once Android has killed the
 * app process (e.g. the user backgrounds the app for a long time and
 * the system reclaims memory). This scheduler is the durability
 * backstop for that case: an [AlarmManager] entry survives process
 * death and Doze, and fires even if nothing in the app is currently
 * running. It prefers [AlarmManager.setExactAndAllowWhileIdle] for
 * precise timing, but falls back to [AlarmManager.setAndAllowWhileIdle]
 * when the SCHEDULE_EXACT_ALARM permission isn't granted (denied by
 * default on Android 13+ for apps that aren't calendar/alarm-clock
 * apps), so the timer still fires close to on time either way rather
 * than silently never firing.
 */
@Singleton
class SleepTimerAlarmScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val alarmManager: AlarmManager? = context.getSystemService()

    private val pendingIntent: PendingIntent
        get() {
            val intent = Intent(context, SleepTimerAlarmReceiver::class.java)
            return PendingIntent.getBroadcast(
                context,
                REQUEST_CODE,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }

    fun schedule(endTimestampMs: Long) {
        val manager = alarmManager ?: return
        runCatching {
            val canScheduleExact = android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.S ||
                manager.canScheduleExactAlarms()
            if (canScheduleExact) {
                manager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, endTimestampMs, pendingIntent)
            } else {
                // SCHEDULE_EXACT_ALARM is not granted (denied-by-default
                // on Android 13+ for non-alarm-clock apps — see
                // developer.android.com/about/versions/14/changes/schedule-exact-alarms).
                // Falling back to the inexact-but-Doze-aware variant
                // means the sleep timer still fires close to on time
                // even without that special permission, rather than
                // silently never firing at all.
                manager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, endTimestampMs, pendingIntent)
            }
        }
    }

    fun cancel() {
        val manager = alarmManager ?: return
        runCatching { manager.cancel(pendingIntent) }
    }

    private companion object {
        const val REQUEST_CODE = 9101
    }
}
