package com.example.mmapp.settings

import android.app.Application
import androidx.room.Room
import com.example.mmapp.settings.data.db.AppSettingsDatabase
import com.example.mmapp.settings.data.repositories.AppSettingsRepository

class AppContainerFactory(
    private val application: Application,
) {
    fun create(): AppContainer {
        val database = Room.databaseBuilder(
            application,
            AppSettingsDatabase::class.java,
            "mm-app-settings.db",
        ).addMigrations(
            AppSettingsDatabase.MIGRATION_1_2,
            AppSettingsDatabase.MIGRATION_2_3,
            AppSettingsDatabase.MIGRATION_3_4,
        )
            .build()

        return AppContainer(
            appSettingsRepository = AppSettingsRepository(database.appSettingsDao()),
        )
    }
}
