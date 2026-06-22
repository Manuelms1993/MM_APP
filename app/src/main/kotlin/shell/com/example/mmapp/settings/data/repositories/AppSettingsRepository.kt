package com.example.mmapp.settings.data.repositories

import com.example.mmapp.settings.data.db.AppSettingsDao
import com.example.mmapp.settings.data.db.AppSettingsEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

data class NotificationSettings(
    val appNotifications: List<AppNotificationSettings>,
) {
    val anyEnabled: Boolean
        get() = appNotifications.any { it.enabled }

    fun findById(appId: String): AppNotificationSettings? = appNotifications.firstOrNull { it.appId == appId }
}

data class AppNotificationSettings(
    val appId: String,
    val title: String,
    val enabled: Boolean,
    val intervalDays: Int,
    val hourOfDay: Int,
)

data class ProcessSettings(
    val processId: String,
    val title: String,
    val enabled: Boolean,
    val intervalDays: Int,
    val hourOfDay: Int,
    val latitude: String? = null,
    val longitude: String? = null,
    val outputFileName: String? = null,
)

class AppSettingsRepository(
    private val dao: AppSettingsDao,
) {
    fun observeNotificationSettings(): Flow<NotificationSettings> = dao.observe().map { entity ->
        entity?.toNotificationSettings() ?: defaultSettings()
    }

    suspend fun getNotificationSettings(): NotificationSettings =
        dao.get()?.toNotificationSettings() ?: defaultSettings()

    suspend fun setPlantNotificationsEnabled(enabled: Boolean) {
        val current = dao.get() ?: AppSettingsEntity()
        dao.upsert(current.copy(plantNotificationsEnabled = enabled))
    }

    suspend fun setPlantNotificationIntervalDays(intervalDays: Int) {
        val current = dao.get() ?: AppSettingsEntity()
        dao.upsert(current.copy(plantNotificationIntervalDays = intervalDays.coerceIn(1, 30)))
    }

    suspend fun setPlantNotificationHourOfDay(hourOfDay: Int) {
        val current = dao.get() ?: AppSettingsEntity()
        dao.upsert(current.copy(plantNotificationHourOfDay = hourOfDay.coerceIn(0, 23)))
    }

    suspend fun setFoodNotificationsEnabled(enabled: Boolean) {
        val current = dao.get() ?: AppSettingsEntity()
        dao.upsert(current.copy(foodNotificationsEnabled = enabled))
    }

    suspend fun setFoodNotificationIntervalDays(intervalDays: Int) {
        val current = dao.get() ?: AppSettingsEntity()
        dao.upsert(current.copy(foodNotificationIntervalDays = intervalDays.coerceIn(1, 30)))
    }

    suspend fun setFoodNotificationHourOfDay(hourOfDay: Int) {
        val current = dao.get() ?: AppSettingsEntity()
        dao.upsert(current.copy(foodNotificationHourOfDay = hourOfDay.coerceIn(0, 23)))
    }

    suspend fun setLunchNotificationsEnabled(enabled: Boolean) {
        val current = dao.get() ?: AppSettingsEntity()
        dao.upsert(current.copy(lunchNotificationsEnabled = enabled))
    }

    suspend fun setLunchNotificationIntervalDays(intervalDays: Int) {
        val current = dao.get() ?: AppSettingsEntity()
        dao.upsert(current.copy(lunchNotificationIntervalDays = intervalDays.coerceIn(1, 30)))
    }

    suspend fun setLunchNotificationHourOfDay(hourOfDay: Int) {
        val current = dao.get() ?: AppSettingsEntity()
        dao.upsert(current.copy(lunchNotificationHourOfDay = hourOfDay.coerceIn(0, 23)))
    }

    suspend fun setDinnerNotificationsEnabled(enabled: Boolean) {
        val current = dao.get() ?: AppSettingsEntity()
        dao.upsert(current.copy(dinnerNotificationsEnabled = enabled))
    }

    suspend fun setDinnerNotificationIntervalDays(intervalDays: Int) {
        val current = dao.get() ?: AppSettingsEntity()
        dao.upsert(current.copy(dinnerNotificationIntervalDays = intervalDays.coerceIn(1, 30)))
    }

    suspend fun setDinnerNotificationHourOfDay(hourOfDay: Int) {
        val current = dao.get() ?: AppSettingsEntity()
        dao.upsert(current.copy(dinnerNotificationHourOfDay = hourOfDay.coerceIn(0, 23)))
    }

    suspend fun saveNotificationSettings(settings: List<AppNotificationSettings>) {
        val current = dao.get() ?: AppSettingsEntity()
        val updated = settings.fold(current) { entity, setting ->
            val intervalDays = setting.intervalDays.coerceIn(1, 30)
            val hourOfDay = setting.hourOfDay.coerceIn(0, 23)
            when (setting.appId) {
                PLANTS_NOTIFICATION_ID -> entity.copy(
                    plantNotificationsEnabled = setting.enabled,
                    plantNotificationIntervalDays = intervalDays,
                    plantNotificationHourOfDay = hourOfDay,
                )
                FOOD_LUNCH_NOTIFICATION_ID -> entity.copy(
                    lunchNotificationsEnabled = setting.enabled,
                    lunchNotificationIntervalDays = intervalDays,
                    lunchNotificationHourOfDay = hourOfDay,
                )
                FOOD_DINNER_NOTIFICATION_ID -> entity.copy(
                    dinnerNotificationsEnabled = setting.enabled,
                    dinnerNotificationIntervalDays = intervalDays,
                    dinnerNotificationHourOfDay = hourOfDay,
                )
                else -> entity
            }
        }
        dao.upsert(updated)
    }

    fun observeProcessSettings(): Flow<List<ProcessSettings>> = dao.observe().map { entity ->
        (entity ?: AppSettingsEntity()).toProcessSettings()
    }

    suspend fun getProcessSettings(): List<ProcessSettings> =
        (dao.get() ?: AppSettingsEntity()).toProcessSettings()

    suspend fun setProcessEnabled(processId: String, enabled: Boolean) {
        val current = dao.get() ?: AppSettingsEntity()
        dao.upsert(
            when (processId) {
                LACUPONERA_PROCESS_ID -> current.copy(lacuponeraProcessEnabled = enabled)
                SCHOOL_DRIVE_TIME_PROCESS_ID -> current.copy(schoolDriveTimeProcessEnabled = enabled)
                else -> current
            },
        )
    }

    suspend fun setProcessIntervalDays(processId: String, intervalDays: Int) {
        val normalized = intervalDays.coerceIn(1, 30)
        val current = dao.get() ?: AppSettingsEntity()
        dao.upsert(
            when (processId) {
                LACUPONERA_PROCESS_ID -> current.copy(lacuponeraProcessIntervalDays = normalized)
                SCHOOL_DRIVE_TIME_PROCESS_ID -> current.copy(schoolDriveTimeProcessIntervalDays = normalized)
                else -> current
            },
        )
    }

    suspend fun setProcessHourOfDay(processId: String, hourOfDay: Int) {
        val normalized = hourOfDay.coerceIn(0, 23)
        val current = dao.get() ?: AppSettingsEntity()
        dao.upsert(
            when (processId) {
                LACUPONERA_PROCESS_ID -> current.copy(lacuponeraProcessHourOfDay = normalized)
                SCHOOL_DRIVE_TIME_PROCESS_ID -> current.copy(schoolDriveTimeProcessHourOfDay = normalized)
                else -> current
            },
        )
    }

    suspend fun saveProcessSettings(settings: List<ProcessSettings>) {
        val current = dao.get() ?: AppSettingsEntity()
        val updated = settings.fold(current) { entity, setting ->
            val intervalDays = setting.intervalDays.coerceIn(1, 30)
            val hourOfDay = setting.hourOfDay.coerceIn(0, 23)
            when (setting.processId) {
                LACUPONERA_PROCESS_ID -> entity.copy(
                    lacuponeraProcessEnabled = setting.enabled,
                    lacuponeraProcessIntervalDays = intervalDays,
                    lacuponeraProcessHourOfDay = hourOfDay,
                )
                SCHOOL_DRIVE_TIME_PROCESS_ID -> entity.copy(
                    schoolDriveTimeProcessEnabled = setting.enabled,
                    schoolDriveTimeProcessIntervalDays = intervalDays,
                    schoolDriveTimeProcessHourOfDay = hourOfDay,
                    schoolDriveTimeOriginLatitude = setting.latitude?.trim().orEmpty(),
                    schoolDriveTimeOriginLongitude = setting.longitude?.trim().orEmpty(),
                    schoolDriveTimeOutputFileName = setting.outputFileName?.trim().orEmpty().ifBlank { "centros_final.csv" },
                )
                else -> entity
            }
        }
        dao.upsert(updated)
    }

    private fun AppSettingsEntity.toNotificationSettings(): NotificationSettings = NotificationSettings(
        appNotifications = listOf(
            AppNotificationSettings(
                appId = PLANTS_NOTIFICATION_ID,
                title = "Plantas",
                enabled = plantNotificationsEnabled,
                intervalDays = plantNotificationIntervalDays.coerceIn(1, 30),
                hourOfDay = plantNotificationHourOfDay.coerceIn(0, 23),
            ),
            AppNotificationSettings(
                appId = FOOD_LUNCH_NOTIFICATION_ID,
                title = "Comida",
                enabled = lunchNotificationsEnabled,
                intervalDays = lunchNotificationIntervalDays.coerceIn(1, 30),
                hourOfDay = lunchNotificationHourOfDay.coerceIn(0, 23),
            ),
            AppNotificationSettings(
                appId = FOOD_DINNER_NOTIFICATION_ID,
                title = "Cena",
                enabled = dinnerNotificationsEnabled,
                intervalDays = dinnerNotificationIntervalDays.coerceIn(1, 30),
                hourOfDay = dinnerNotificationHourOfDay.coerceIn(0, 23),
            ),
        ),
    )

    private fun defaultSettings(): NotificationSettings = NotificationSettings(
        appNotifications = listOf(
            AppNotificationSettings(
                appId = PLANTS_NOTIFICATION_ID,
                title = "Plantas",
                enabled = true,
                intervalDays = 1,
                hourOfDay = 9,
            ),
            AppNotificationSettings(
                appId = FOOD_LUNCH_NOTIFICATION_ID,
                title = "Comida",
                enabled = true,
                intervalDays = 1,
                hourOfDay = 10,
            ),
            AppNotificationSettings(
                appId = FOOD_DINNER_NOTIFICATION_ID,
                title = "Cena",
                enabled = true,
                intervalDays = 1,
                hourOfDay = 18,
            ),
        ),
    )

    private fun AppSettingsEntity.toProcessSettings(): List<ProcessSettings> = listOf(
        ProcessSettings(
            processId = LACUPONERA_PROCESS_ID,
            title = "La Cuponera",
            enabled = lacuponeraProcessEnabled,
            intervalDays = lacuponeraProcessIntervalDays.coerceIn(1, 30),
            hourOfDay = lacuponeraProcessHourOfDay.coerceIn(0, 23),
        ),
        ProcessSettings(
            processId = SCHOOL_DRIVE_TIME_PROCESS_ID,
            title = "Centros por tiempo en coche",
            enabled = schoolDriveTimeProcessEnabled,
            intervalDays = schoolDriveTimeProcessIntervalDays.coerceIn(1, 30),
            hourOfDay = schoolDriveTimeProcessHourOfDay.coerceIn(0, 23),
            latitude = schoolDriveTimeOriginLatitude,
            longitude = schoolDriveTimeOriginLongitude,
            outputFileName = schoolDriveTimeOutputFileName,
        ),
    )

    companion object {
        const val PLANTS_NOTIFICATION_ID = "plants_notifications"
        const val FOOD_LUNCH_NOTIFICATION_ID = "food_lunch_notifications"
        const val FOOD_DINNER_NOTIFICATION_ID = "food_dinner_notifications"
        const val LACUPONERA_PROCESS_ID = "lacuponera_free_products"
        const val SCHOOL_DRIVE_TIME_PROCESS_ID = "school_drive_times"
    }
}
