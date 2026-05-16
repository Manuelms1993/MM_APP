package com.example.mmapp.app2

import android.app.Application
import com.example.mmapp.app2.notifications.MenuNotificationScheduler

class MMFoodApplication : Application() {
    val container: AppContainer by lazy {
        AppContainerFactory(this).create()
    }

    override fun onCreate() {
        super.onCreate()
        MenuNotificationScheduler(this).ensureChannels()
        MenuNotificationScheduler(this).scheduleDailyNotifications()
    }
}
