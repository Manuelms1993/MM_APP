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
                else -> current
            },
        )
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
    )

    companion object {
        const val PLANTS_NOTIFICATION_ID = "plants_notifications"
        const val FOOD_LUNCH_NOTIFICATION_ID = "food_lunch_notifications"
        const val FOOD_DINNER_NOTIFICATION_ID = "food_dinner_notifications"
        const val LACUPONERA_PROCESS_ID = "lacuponera_free_products"
    }
}
