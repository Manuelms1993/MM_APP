package com.example.mmapp

import android.app.Application
import com.example.mmapp.app1.AppContainerFactory as PlantsAppContainerFactory
import com.example.mmapp.app2.AppContainerFactory as FoodAppContainerFactory
import com.example.mmapp.app3.AppContainerFactory as TravelAppContainerFactory
import com.example.mmapp.app4.AppContainerFactory as ScriptingAppContainerFactory
import com.example.mmapp.settings.AppContainerFactory as SettingsAppContainerFactory
import com.example.mmapp.settings.NotificationSettingsCoordinator
import com.example.mmapp.settings.ProcessSettingsCoordinator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class MMAppApplication : Application() {
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    val plantsContainer by lazy {
        PlantsAppContainerFactory(this).create()
    }

    val foodContainer by lazy {
        FoodAppContainerFactory(this).create()
    }

    val travelContainer by lazy {
        TravelAppContainerFactory(this).create()
    }

    val scriptingContainer by lazy {
        ScriptingAppContainerFactory(this).create(
            appSettingsRepository = settingsContainer.appSettingsRepository,
        )
    }

    val settingsContainer by lazy {
        SettingsAppContainerFactory(this).create()
    }

    override fun onCreate() {
        super.onCreate()
        applicationScope.launch {
            val settings = settingsContainer.appSettingsRepository.getNotificationSettings()
            NotificationSettingsCoordinator(this@MMAppApplication).apply(settings)
            val processSettings = settingsContainer.appSettingsRepository.getProcessSettings()
            ProcessSettingsCoordinator(this@MMAppApplication).apply(processSettings)
        }
    }
}
