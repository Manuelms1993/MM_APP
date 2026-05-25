package com.example.mmapp.app2.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.mmapp.MMAppApplication
import com.example.mmapp.settings.NotificationSettingsCoordinator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootCompletedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (
            intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == Intent.ACTION_MY_PACKAGE_REPLACED ||
            intent.action == Intent.ACTION_TIME_CHANGED ||
            intent.action == Intent.ACTION_TIMEZONE_CHANGED
        ) {
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val application = context.applicationContext as MMAppApplication
                    val settings = application.settingsContainer.appSettingsRepository.getNotificationSettings()
                    NotificationSettingsCoordinator(context).apply(settings)
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
