package com.example.mmapp.settings.data.db

import androidx.room.Database
import androidx.room.RoomDatabase

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [AppSettingsEntity::class],
    version = 4,
    exportSchema = false,
)
abstract class AppSettingsDatabase : RoomDatabase() {
    abstract fun appSettingsDao(): AppSettingsDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    ALTER TABLE `app_settings`
                    ADD COLUMN `lacuponeraProcessEnabled` INTEGER NOT NULL DEFAULT 0
                    """.trimIndent(),
                )
                db.execSQL(
                    """
                    ALTER TABLE `app_settings`
                    ADD COLUMN `lacuponeraProcessIntervalDays` INTEGER NOT NULL DEFAULT 3
                    """.trimIndent(),
                )
                db.execSQL(
                    """
                    ALTER TABLE `app_settings`
                    ADD COLUMN `lacuponeraProcessHourOfDay` INTEGER NOT NULL DEFAULT 13
                    """.trimIndent(),
                )
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    ALTER TABLE `app_settings`
                    ADD COLUMN `plantNotificationIntervalDays` INTEGER NOT NULL DEFAULT 1
                    """.trimIndent(),
                )
                db.execSQL(
                    """
                    ALTER TABLE `app_settings`
                    ADD COLUMN `plantNotificationHourOfDay` INTEGER NOT NULL DEFAULT 9
                    """.trimIndent(),
                )
                db.execSQL(
                    """
                    ALTER TABLE `app_settings`
                    ADD COLUMN `foodNotificationIntervalDays` INTEGER NOT NULL DEFAULT 1
                    """.trimIndent(),
                )
                db.execSQL(
                    """
                    ALTER TABLE `app_settings`
                    ADD COLUMN `foodNotificationHourOfDay` INTEGER NOT NULL DEFAULT 10
                    """.trimIndent(),
                )
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    ALTER TABLE `app_settings`
                    ADD COLUMN `lunchNotificationsEnabled` INTEGER NOT NULL DEFAULT 1
                    """.trimIndent(),
                )
                db.execSQL(
                    """
                    ALTER TABLE `app_settings`
                    ADD COLUMN `lunchNotificationIntervalDays` INTEGER NOT NULL DEFAULT 1
                    """.trimIndent(),
                )
                db.execSQL(
                    """
                    ALTER TABLE `app_settings`
                    ADD COLUMN `lunchNotificationHourOfDay` INTEGER NOT NULL DEFAULT 10
                    """.trimIndent(),
                )
                db.execSQL(
                    """
                    ALTER TABLE `app_settings`
                    ADD COLUMN `dinnerNotificationsEnabled` INTEGER NOT NULL DEFAULT 1
                    """.trimIndent(),
                )
                db.execSQL(
                    """
                    ALTER TABLE `app_settings`
                    ADD COLUMN `dinnerNotificationIntervalDays` INTEGER NOT NULL DEFAULT 1
                    """.trimIndent(),
                )
                db.execSQL(
                    """
                    ALTER TABLE `app_settings`
                    ADD COLUMN `dinnerNotificationHourOfDay` INTEGER NOT NULL DEFAULT 18
                    """.trimIndent(),
                )
                db.execSQL(
                    """
                    UPDATE `app_settings`
                    SET
                        `lunchNotificationsEnabled` = `foodNotificationsEnabled`,
                        `lunchNotificationIntervalDays` = `foodNotificationIntervalDays`,
                        `lunchNotificationHourOfDay` = `foodNotificationHourOfDay`,
                        `dinnerNotificationsEnabled` = `foodNotificationsEnabled`,
                        `dinnerNotificationIntervalDays` = `foodNotificationIntervalDays`,
                        `dinnerNotificationHourOfDay` = ((`foodNotificationHourOfDay` + 8) % 24)
                    """.trimIndent(),
                )
            }
        }
    }
}
