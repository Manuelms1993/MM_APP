package com.example.plantreminder

import android.app.Application
import com.example.plantreminder.work.DailyReminderScheduler

class PlantReminderApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainerFactory(this).create()
        DailyReminderScheduler(this).schedule()
    }
}
