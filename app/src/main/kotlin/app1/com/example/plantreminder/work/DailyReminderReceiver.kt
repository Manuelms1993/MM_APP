package com.example.mmapp.app1.work

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.mmapp.MMAppApplication
import com.example.mmapp.settings.data.repositories.AppSettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DailyReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != DailyReminderScheduler.ACTION_NOTIFY_PLANTS) return
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                DailyReminderRunner.run(context)
            } finally {
                val application = context.applicationContext as MMAppApplication
                val settings = application.settingsContainer.appSettingsRepository.getNotificationSettings()
                val plants = settings.findById(AppSettingsRepository.PLANTS_NOTIFICATION_ID)
                if (plants?.enabled != false) {
                    DailyReminderScheduler(context).schedule(
                        intervalDays = plants?.intervalDays ?: 1,
                        hourOfDay = plants?.hourOfDay ?: 9,
                    )
                }
                pendingResult.finish()
            }
        }
    }
}
