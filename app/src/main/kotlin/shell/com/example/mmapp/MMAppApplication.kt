package com.example.mmapp

import android.app.Application
import com.example.mmapp.app1.AppContainerFactory as PlantsAppContainerFactory
import com.example.mmapp.app1.work.DailyReminderScheduler
import com.example.mmapp.app2.AppContainerFactory as FoodAppContainerFactory
import com.example.mmapp.app2.notifications.MenuNotificationScheduler
import com.example.mmapp.app3.AppContainerFactory as TravelAppContainerFactory

class MMAppApplication : Application() {
    val plantsContainer by lazy {
        PlantsAppContainerFactory(this).create()
    }

    val foodContainer by lazy {
        FoodAppContainerFactory(this).create()
    }

    val travelContainer by lazy {
        TravelAppContainerFactory(this).create()
    }

    override fun onCreate() {
        super.onCreate()
        DailyReminderScheduler(this).schedule()
        MenuNotificationScheduler(this).ensureChannels()
        MenuNotificationScheduler(this).scheduleDailyNotifications()
    }
}
