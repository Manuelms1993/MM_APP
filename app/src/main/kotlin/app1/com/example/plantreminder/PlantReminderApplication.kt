package com.example.mmapp.app1

import android.app.Application
import com.example.mmapp.app1.work.DailyReminderScheduler

class PlantReminderApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainerFactory(this).create()
        DailyReminderScheduler(this).schedule()
    }
}
