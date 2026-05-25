package com.example.mmapp.app4.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import com.example.mmapp.MMAppApplication

class RunProcessWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        val processId = inputData.getString(KEY_PROCESS_ID) ?: return Result.failure()
        val application = applicationContext as MMAppApplication
        val processSettings = application.settingsContainer.appSettingsRepository.getProcessSettings()
        val settings = processSettings.firstOrNull { it.processId == processId } ?: return Result.failure()
        if (!settings.enabled) return Result.success()

        val script = application.scriptingContainer.scripts.firstOrNull { it.definition.id == processId }
            ?: return Result.failure()

        return runCatching {
            script.execute()
        }.fold(
            onSuccess = { Result.success() },
            onFailure = { Result.retry() },
        )
    }

    companion object {
        private const val KEY_PROCESS_ID = "process_id"

        fun inputData(processId: String): Data = Data.Builder()
            .putString(KEY_PROCESS_ID, processId)
            .build()
    }
}
