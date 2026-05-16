package com.example.mmapp.app2.data.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [ConversationMessageEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun conversationMessageDao(): ConversationMessageDao
}

