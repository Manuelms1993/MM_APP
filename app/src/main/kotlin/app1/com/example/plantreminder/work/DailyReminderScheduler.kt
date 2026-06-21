package com.example.mmapp.app1.work

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.work.WorkManager
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

class DailyReminderScheduler(
    private val context: Context,
) {
    fun schedule(
        intervalDays: Int = 1,
        hourOfDay: Int = 9,
    ) {
        WorkManager.getInstance(context).cancelUniqueWork(UNIQUE_WORK_NAME)
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pendingIntent = pendingIntent()
        alarmManager.cancel(pendingIntent)
        val triggerAtMillis = nextTriggerMillis(
            triggerTime = LocalTime.of(hourOfDay.coerceIn(0, 23), 0),
            intervalDays = intervalDays,
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
        }
    }

    fun cancel() {
        WorkManager.getInstance(context).cancelUniqueWork(UNIQUE_WORK_NAME)
        val pendingIntent = pendingIntent()
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
    }

    fun sync(
        enabled: Boolean,
        intervalDays: Int = 1,
        hourOfDay: Int = 9,
    ) {
        if (enabled) schedule(intervalDays, hourOfDay) else cancel()
    }

    private fun pendingIntent(): PendingIntent = PendingIntent.getBroadcast(
        context,
        REQUEST_CODE,
        Intent(context, DailyReminderReceiver::class.java).apply {
            action = ACTION_NOTIFY_PLANTS
        },
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )

    private fun nextTriggerMillis(triggerTime: LocalTime, intervalDays: Int): Long {
        return nextTriggerAt(
            now = ZonedDateTime.now(MADRID_ZONE),
            triggerTime = triggerTime,
            intervalDays = intervalDays,
        ).toInstant().toEpochMilli()
    }

    companion object {
        const val UNIQUE_WORK_NAME = "daily_plant_reminder_work"
        const val ACTION_NOTIFY_PLANTS = "com.example.mmapp.app1.NOTIFY_PLANTS"
        private const val REQUEST_CODE = 1101
        val MADRID_ZONE: ZoneId = ZoneId.of("Europe/Madrid")

        internal fun nextTriggerAt(
            now: ZonedDateTime,
            triggerTime: LocalTime,
            intervalDays: Int,
        ): ZonedDateTime {
            val madridNow = now.withZoneSameInstant(MADRID_ZONE)
            val todayAtTime = madridNow.toLocalDate().atTime(triggerTime).atZone(MADRID_ZONE)
            val normalizedIntervalDays = intervalDays.coerceIn(1, 30)
            return if (todayAtTime.isAfter(madridNow)) {
                todayAtTime
            } else {
                todayAtTime.plusDays(normalizedIntervalDays.toLong())
            }
        }
    }
}
