package com.example.mmapp.app4.work

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.mmapp.settings.data.repositories.ProcessSettings
import java.time.Duration
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit

class ProcessScheduler(
    private val context: Context,
) {
    fun sync(settings: List<ProcessSettings>) {
        settings.forEach { process ->
            if (process.enabled) {
                schedule(process)
            } else {
                cancel(process.processId)
            }
        }
    }

    private fun schedule(process: ProcessSettings) {
        val request = PeriodicWorkRequestBuilder<RunProcessWorker>(
            process.intervalDays.toLong(),
            TimeUnit.DAYS,
        )
            .setInitialDelay(initialDelay(process.hourOfDay))
            .setInputData(RunProcessWorker.inputData(process.processId))
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            uniqueWorkName(process.processId),
            ExistingPeriodicWorkPolicy.UPDATE,
            request,
        )
    }

    private fun cancel(processId: String) {
        WorkManager.getInstance(context).cancelUniqueWork(uniqueWorkName(processId))
    }

    private fun uniqueWorkName(processId: String): String = "process_work_$processId"

    private fun initialDelay(hourOfDay: Int): Duration {
        val now = ZonedDateTime.now(MADRID_ZONE)
        val nextRun = now.withHour(hourOfDay).withMinute(0).withSecond(0).withNano(0).let { scheduled ->
            if (now >= scheduled) scheduled.plusDays(1) else scheduled
        }
        return Duration.between(now, nextRun)
    }

    companion object {
        val MADRID_ZONE: ZoneId = ZoneId.of("Europe/Madrid")
    }
}
