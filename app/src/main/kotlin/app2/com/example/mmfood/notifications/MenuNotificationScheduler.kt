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

    fun scheduleDailyNotifications() {
        scheduleNotification(MealType.LUNCH)
        scheduleNotification(MealType.DINNER)
    }

    fun scheduleNotification(mealType: MealType) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        when (mealType) {
            MealType.LUNCH -> schedule(
                alarmManager = alarmManager,
                requestCode = REQUEST_CODE_LUNCH,
                mealType = mealType,
                triggerTime = LocalTime.of(10, 0),
            )

            MealType.DINNER -> schedule(
                alarmManager = alarmManager,
                requestCode = REQUEST_CODE_DINNER,
                mealType = mealType,
                triggerTime = LocalTime.of(18, 0),
            )
        }
    }

    private fun schedule(
        alarmManager: AlarmManager,
        requestCode: Int,
        mealType: MealType,
        triggerTime: LocalTime,
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
        val triggerAtMillis = nextTriggerMillis(triggerTime)
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

    private fun nextTriggerMillis(triggerTime: LocalTime): Long {
        return nextTriggerAt(
            now = ZonedDateTime.now(MADRID_ZONE_ID),
            triggerTime = triggerTime,
        ).toInstant().toEpochMilli()
    }

    companion object {
        const val CHANNEL_ID = "daily_menu_channel"
        const val ACTION_NOTIFY_MENU = "com.example.mmapp.app2.NOTIFY_MENU"
        const val EXTRA_MEAL_TYPE = "meal_type"
        val MADRID_ZONE_ID: ZoneId = ZoneId.of("Europe/Madrid")
        private const val REQUEST_CODE_LUNCH = 1001
        private const val REQUEST_CODE_DINNER = 1002

        internal fun nextTriggerAt(
            now: ZonedDateTime,
            triggerTime: LocalTime,
        ): ZonedDateTime {
            val madridNow = now.withZoneSameInstant(MADRID_ZONE_ID)
            val todayAtTime = madridNow.toLocalDate().atTime(triggerTime).atZone(MADRID_ZONE_ID)
            return if (todayAtTime.isAfter(madridNow)) todayAtTime else todayAtTime.plusDays(1)
        }
    }
}

enum class MealType {
    LUNCH,
    DINNER,
}
