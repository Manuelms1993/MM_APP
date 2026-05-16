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
    fun schedule() {
        val request = PeriodicWorkRequestBuilder<DailyReminderWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(initialDelayToNextRun())
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            UNIQUE_WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            request,
        )
    }

    private fun initialDelayToNextRun(): Duration {
        val now = ZonedDateTime.now(MADRID_ZONE)
        val nextRun = now.withHour(9).withMinute(0).withSecond(0).withNano(0).let { scheduled ->
            if (now >= scheduled) scheduled.plusDays(1) else scheduled
        }
        return Duration.between(now, nextRun)
    }

    companion object {
        const val UNIQUE_WORK_NAME = "daily_plant_reminder_work"
        val MADRID_ZONE: java.time.ZoneId = java.time.ZoneId.of("Europe/Madrid")
    }
}
