package com.example.mmapp.app1.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.mmapp.MMAppApplication
import com.example.mmapp.settings.data.repositories.AppSettingsRepository
import java.time.LocalDate

class DailyReminderWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        val application = applicationContext as MMAppApplication
        val settings = application.settingsContainer.appSettingsRepository.getNotificationSettings()
        if (settings.findById(AppSettingsRepository.PLANTS_NOTIFICATION_ID)?.enabled == false) {
            return Result.success()
        }
        val container = application.plantsContainer
        return runCatching {
            container.generatePendingCareUseCase(source = com.example.mmapp.app1.domain.models.MessageSource.WORKER)
            val maintainer = container.appPreferencesDataSource.getActiveMaintainer()
            val actions = container.getMaintainerPendingActionsUseCase(
                maintainer = maintainer,
                date = LocalDate.now(DailyReminderScheduler.MADRID_ZONE),
            )
            if (actions.isNotEmpty()) {
                DailyReminderNotifier(applicationContext).show(
                    maintainer = maintainer,
                    taskCount = actions.size,
                )
            }
        }.fold(
            onSuccess = { Result.success() },
            onFailure = { Result.retry() },
        )
    }
}
