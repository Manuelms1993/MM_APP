package com.example.mmapp.app1.work

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.time.Duration
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit

class DailyReminderScheduler(
    private val context: Context,
) {
    fun schedule(
        intervalDays: Int = 1,
        hourOfDay: Int = 9,
    ) {
        val request = PeriodicWorkRequestBuilder<DailyReminderWorker>(intervalDays.toLong(), TimeUnit.DAYS)
            .setInitialDelay(initialDelayToNextRun(hourOfDay, intervalDays))
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            UNIQUE_WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            request,
        )
    }

    fun cancel() {
        WorkManager.getInstance(context).cancelUniqueWork(UNIQUE_WORK_NAME)
    }

    fun sync(
        enabled: Boolean,
        intervalDays: Int = 1,
        hourOfDay: Int = 9,
    ) {
        if (enabled) schedule(intervalDays, hourOfDay) else cancel()
    }

    private fun initialDelayToNextRun(hourOfDay: Int, intervalDays: Int): Duration {
        val now = ZonedDateTime.now(MADRID_ZONE)
        val normalizedHour = hourOfDay.coerceIn(0, 23)
        val normalizedIntervalDays = intervalDays.coerceIn(1, 30)
        val nextRun = now.withHour(normalizedHour).withMinute(0).withSecond(0).withNano(0).let { scheduled ->
            if (now >= scheduled) scheduled.plusDays(normalizedIntervalDays.toLong()) else scheduled
        }
        return Duration.between(now, nextRun)
    }

    companion object {
        const val UNIQUE_WORK_NAME = "daily_plant_reminder_work"
        val MADRID_ZONE: java.time.ZoneId = java.time.ZoneId.of("Europe/Madrid")
    }
}
