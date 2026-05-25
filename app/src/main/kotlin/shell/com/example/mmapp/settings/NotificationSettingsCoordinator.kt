package com.example.mmapp.settings

import android.content.Context
import com.example.mmapp.app1.work.DailyReminderScheduler
import com.example.mmapp.app2.notifications.MealType
import com.example.mmapp.app2.notifications.MenuNotificationScheduler
import com.example.mmapp.settings.data.repositories.NotificationSettings
import com.example.mmapp.settings.data.repositories.AppSettingsRepository

class NotificationSettingsCoordinator(
    private val context: Context,
) {
    fun apply(settings: NotificationSettings) {
        val plants = settings.findById(AppSettingsRepository.PLANTS_NOTIFICATION_ID)
        val lunch = settings.findById(AppSettingsRepository.FOOD_LUNCH_NOTIFICATION_ID)
        val dinner = settings.findById(AppSettingsRepository.FOOD_DINNER_NOTIFICATION_ID)

        DailyReminderScheduler(context).sync(
            enabled = plants?.enabled ?: true,
            intervalDays = plants?.intervalDays ?: 1,
            hourOfDay = plants?.hourOfDay ?: 9,
        )
        val menuScheduler = MenuNotificationScheduler(context)
        if (lunch?.enabled != false || dinner?.enabled != false) {
            menuScheduler.ensureChannels()
        }
        menuScheduler.syncNotification(
            mealType = MealType.LUNCH,
            enabled = lunch?.enabled ?: true,
            intervalDays = lunch?.intervalDays ?: 1,
            hourOfDay = lunch?.hourOfDay ?: 10,
        )
        menuScheduler.syncNotification(
            mealType = MealType.DINNER,
            enabled = dinner?.enabled ?: true,
            intervalDays = dinner?.intervalDays ?: 1,
            hourOfDay = dinner?.hourOfDay ?: 18,
        )
    }
}
