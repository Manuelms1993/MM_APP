package com.example.mmapp.app2

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.content.ContextCompat
import com.example.mmapp.app2.notifications.MenuNotificationScheduler
import com.example.mmapp.app2.ui.MMFoodApp

class MainActivity : ComponentActivity() {
    private val notificationsPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) {
            MenuNotificationScheduler(this).scheduleDailyNotifications()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestNotificationsPermissionIfNeeded()
        val container = (application as MMFoodApplication).container
        setContent {
            MMFoodApp(
                container = container,
                viewModelKey = "food-standalone",
            )
        }
    }

    private fun requestNotificationsPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            MenuNotificationScheduler(this).scheduleDailyNotifications()
            return
        }
        notificationsPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }
}
