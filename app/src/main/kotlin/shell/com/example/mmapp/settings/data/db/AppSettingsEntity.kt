package com.example.mmapp.settings.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_settings")
data class AppSettingsEntity(
    @PrimaryKey val id: Int = SINGLE_ROW_ID,
    val plantNotificationsEnabled: Boolean = true,
    val plantNotificationIntervalDays: Int = 1,
    val plantNotificationHourOfDay: Int = 9,
    val foodNotificationsEnabled: Boolean = true,
    val foodNotificationIntervalDays: Int = 1,
    val foodNotificationHourOfDay: Int = 10,
    val lunchNotificationsEnabled: Boolean = true,
    val lunchNotificationIntervalDays: Int = 1,
    val lunchNotificationHourOfDay: Int = 10,
    val dinnerNotificationsEnabled: Boolean = true,
    val dinnerNotificationIntervalDays: Int = 1,
    val dinnerNotificationHourOfDay: Int = 18,
    val lacuponeraProcessEnabled: Boolean = false,
    val lacuponeraProcessIntervalDays: Int = 3,
    val lacuponeraProcessHourOfDay: Int = 13,
    val schoolDriveTimeProcessEnabled: Boolean = false,
    val schoolDriveTimeProcessIntervalDays: Int = 7,
    val schoolDriveTimeProcessHourOfDay: Int = 13,
    val schoolDriveTimeOriginLatitude: String = "",
    val schoolDriveTimeOriginLongitude: String = "",
    val schoolDriveTimeOutputFileName: String = "centros_final.csv",
) {
    companion object {
        const val SINGLE_ROW_ID = 1
    }
}
