package com.example.mmapp

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.mmapp.ui.MMApp
import com.example.mmapp.settings.NotificationSettingsCoordinator
import com.example.mmapp.settings.ProcessSettingsCoordinator
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val notificationsPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) {
            applyNotificationSettings()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestNotificationsPermissionIfNeeded()

        val application = application as MMAppApplication
        setContent {
            MMApp(
                plantsContainer = application.plantsContainer,
                foodContainer = application.foodContainer,
                travelContainer = application.travelContainer,
                scriptingContainer = application.scriptingContainer,
                settingsContainer = application.settingsContainer,
                onNotificationSettingsChanged = ::applyNotificationSettings,
                onProcessSettingsChanged = ::applyProcessSettings,
            )
        }
    }

    private fun requestNotificationsPermissionIfNeeded() {
        lifecycleScope.launch {
            val application = application as MMAppApplication
            val settings = application.settingsContainer.appSettingsRepository.getNotificationSettings()
            if (settings.anyEnabled) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                    ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
                ) {
                    notificationsPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    return@launch
                }
            }
            applyNotificationSettings()
        }
    }

    private fun applyNotificationSettings() {
        lifecycleScope.launch {
            val application = application as MMAppApplication
            val settings = application.settingsContainer.appSettingsRepository.getNotificationSettings()
            if (settings.anyEnabled &&
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
            ) {
                notificationsPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                return@launch
            }
            NotificationSettingsCoordinator(this@MainActivity).apply(settings)
        }
    }

    private fun applyProcessSettings() {
        lifecycleScope.launch {
            val application = application as MMAppApplication
            val settings = application.settingsContainer.appSettingsRepository.getProcessSettings()
            ProcessSettingsCoordinator(this@MainActivity).apply(settings)
        }
    }
}
