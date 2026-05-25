package com.example.mmapp.app2.notifications

import android.Manifest
import android.app.PendingIntent
import android.graphics.BitmapFactory
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.mmapp.MainActivity
import com.example.mmapp.MMAppApplication
import com.example.mmapp.app2.AppContainerFactory
import com.example.mmapp.R
import com.example.mmapp.settings.data.repositories.AppSettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MenuNotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            val mealType = mealTypeFromIntent(intent)
            try {
                val application = context.applicationContext as MMAppApplication
                val settings = application.settingsContainer.appSettingsRepository.getNotificationSettings()
                val foodSettings = settings.findById(notificationIdFor(mealType))
                if (foodSettings?.enabled == false) {
                    return@launch
                }
                val catalog = AppContainerFactory(context.applicationContext as android.app.Application)
                    .create()
                    .menuCatalogDataSource
                    .getCatalog()
                val selection = catalog.selectionForDate(
                    java.time.LocalDate.now(MenuNotificationScheduler.MADRID_ZONE_ID),
                )
                val mealNames = when (mealType) {
                    MealType.LUNCH -> selection.lunchOptions
                    MealType.DINNER -> selection.dinnerOptions
                }.map { it.name }
                val title = if (mealType == MealType.LUNCH) "Comida" else "Cena"
                val content = mealNames.ifEmpty { listOf("Sin opciones") }.joinToString(" · ")

                val launchIntent = Intent(context, MainActivity::class.java)
                val contentIntent = PendingIntent.getActivity(
                    context,
                    if (mealType == MealType.LUNCH) 2001 else 2002,
                    launchIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
                )

                val notification = NotificationCompat.Builder(context, MenuNotificationScheduler.CHANNEL_ID)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setLargeIcon(BitmapFactory.decodeResource(context.resources, R.mipmap.ic_launcher))
                    .setContentTitle(title)
                    .setContentText(content)
                    .setStyle(NotificationCompat.BigTextStyle().bigText(content))
                    .setContentIntent(contentIntent)
                    .setAutoCancel(true)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .build()

                if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                    NotificationManagerCompat.from(context).notify(
                        if (mealType == MealType.LUNCH) 3001 else 3002,
                        notification,
                    )
                }
            } finally {
                val application = context.applicationContext as MMAppApplication
                val settings = application.settingsContainer.appSettingsRepository.getNotificationSettings()
                val foodSettings = settings.findById(notificationIdFor(mealType))
                if (foodSettings?.enabled != false) {
                    MenuNotificationScheduler(context).scheduleNotification(
                        mealType = mealType,
                        intervalDays = foodSettings?.intervalDays ?: 1,
                        hourOfDay = foodSettings?.hourOfDay ?: MenuNotificationScheduler.defaultHourOfDay(mealType),
                    )
                }
                pendingResult.finish()
            }
        }
    }

    private fun mealTypeFromIntent(intent: Intent): MealType = intent.getStringExtra(
        MenuNotificationScheduler.EXTRA_MEAL_TYPE,
    )?.let(MealType::valueOf) ?: MealType.LUNCH

    private fun notificationIdFor(mealType: MealType): String = when (mealType) {
        MealType.LUNCH -> AppSettingsRepository.FOOD_LUNCH_NOTIFICATION_ID
        MealType.DINNER -> AppSettingsRepository.FOOD_DINNER_NOTIFICATION_ID
    }
}
