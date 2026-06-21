package com.example.mmapp.app1.work

import android.content.Context
import com.example.mmapp.MMAppApplication
import com.example.mmapp.app1.domain.models.MessageSource
import com.example.mmapp.settings.data.repositories.AppSettingsRepository
import java.time.LocalDate

internal object DailyReminderRunner {
    suspend fun run(context: Context) {
        val application = context.applicationContext as MMAppApplication
        val settings = application.settingsContainer.appSettingsRepository.getNotificationSettings()
        if (settings.findById(AppSettingsRepository.PLANTS_NOTIFICATION_ID)?.enabled == false) {
            return
        }
        val container = application.plantsContainer
        container.generatePendingCareUseCase(source = MessageSource.WORKER)
        val maintainer = container.appPreferencesDataSource.getActiveMaintainer()
        val actions = container.getMaintainerPendingActionsUseCase(
            maintainer = maintainer,
            date = LocalDate.now(DailyReminderScheduler.MADRID_ZONE),
        )
        if (actions.isNotEmpty()) {
            DailyReminderNotifier(context).show(
                maintainer = maintainer,
                taskCount = actions.size,
            )
        }
    }
}
