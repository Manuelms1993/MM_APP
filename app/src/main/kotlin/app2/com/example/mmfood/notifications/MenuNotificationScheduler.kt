package com.example.mmapp.app2.notifications

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

class MenuNotificationScheduler(
    private val context: Context,
) {
    fun ensureChannels() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Menú diario",
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            description = "Recordatorios de comida y cena"
        }
        notificationManager.createNotificationChannel(channel)
    }

    fun cancelDailyNotifications() {
        cancelNotification(REQUEST_CODE_LUNCH, MealType.LUNCH)
        cancelNotification(REQUEST_CODE_DINNER, MealType.DINNER)
    }

    fun syncNotification(
        mealType: MealType,
        enabled: Boolean,
        intervalDays: Int = 1,
        hourOfDay: Int = defaultHourOfDay(mealType),
    ) {
        if (enabled) {
            scheduleNotification(mealType, intervalDays, hourOfDay)
        } else {
            cancelNotification(requestCodeFor(mealType), mealType)
        }
    }

    fun scheduleNotification(
        mealType: MealType,
        intervalDays: Int = 1,
        hourOfDay: Int = defaultHourOfDay(mealType),
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        schedule(
            alarmManager = alarmManager,
            requestCode = requestCodeFor(mealType),
            mealType = mealType,
            triggerTime = LocalTime.of(hourOfDay.coerceIn(0, 23), 0),
            intervalDays = intervalDays,
        )
    }

    private fun cancelNotification(
        requestCode: Int,
        mealType: MealType,
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            Intent(context, MenuNotificationReceiver::class.java).apply {
                action = ACTION_NOTIFY_MENU
                putExtra(EXTRA_MEAL_TYPE, mealType.name)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
    }

    private fun schedule(
        alarmManager: AlarmManager,
        requestCode: Int,
        mealType: MealType,
        triggerTime: LocalTime,
        intervalDays: Int,
    ) {
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            Intent(context, MenuNotificationReceiver::class.java).apply {
                action = ACTION_NOTIFY_MENU
                putExtra(EXTRA_MEAL_TYPE, mealType.name)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        alarmManager.cancel(pendingIntent)
        val triggerAtMillis = nextTriggerMillis(triggerTime, intervalDays)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent,
            )
        } else {
            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent,
            )
        }
    }

    private fun nextTriggerMillis(triggerTime: LocalTime, intervalDays: Int): Long {
        return nextTriggerAt(
            now = ZonedDateTime.now(MADRID_ZONE_ID),
            triggerTime = triggerTime,
            intervalDays = intervalDays,
        ).toInstant().toEpochMilli()
    }

    companion object {
        const val CHANNEL_ID = "daily_menu_channel"
        const val ACTION_NOTIFY_MENU = "com.example.mmapp.app2.NOTIFY_MENU"
        const val EXTRA_MEAL_TYPE = "meal_type"
        val MADRID_ZONE_ID: ZoneId = ZoneId.of("Europe/Madrid")
        private const val REQUEST_CODE_LUNCH = 1001
        private const val REQUEST_CODE_DINNER = 1002

        fun defaultHourOfDay(mealType: MealType): Int = when (mealType) {
            MealType.LUNCH -> 10
            MealType.DINNER -> 18
        }

        private fun requestCodeFor(mealType: MealType): Int = when (mealType) {
            MealType.LUNCH -> REQUEST_CODE_LUNCH
            MealType.DINNER -> REQUEST_CODE_DINNER
        }

        internal fun nextTriggerAt(
            now: ZonedDateTime,
            triggerTime: LocalTime,
            intervalDays: Int,
        ): ZonedDateTime {
            val madridNow = now.withZoneSameInstant(MADRID_ZONE_ID)
            val todayAtTime = madridNow.toLocalDate().atTime(triggerTime).atZone(MADRID_ZONE_ID)
            val normalizedIntervalDays = intervalDays.coerceIn(1, 30)
            return if (todayAtTime.isAfter(madridNow)) todayAtTime else todayAtTime.plusDays(normalizedIntervalDays.toLong())
        }
    }
}

enum class MealType {
    LUNCH,
    DINNER,
}
