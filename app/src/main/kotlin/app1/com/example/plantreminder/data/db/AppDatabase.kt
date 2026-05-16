package com.example.mmapp.app1.data.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [ConversationMessageEntity::class, WeatherDayEntity::class, AppPreferencesEntity::class],
    version = 3,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun conversationMessageDao(): ConversationMessageDao

    abstract fun weatherDayDao(): WeatherDayDao

    abstract fun appPreferencesDao(): AppPreferencesDao
}
