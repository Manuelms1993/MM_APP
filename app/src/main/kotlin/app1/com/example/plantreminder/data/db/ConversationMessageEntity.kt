package com.example.mmapp.app1.data.db

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "conversation_messages",
    indices = [Index(value = ["date"], unique = true)],
)
data class ConversationMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String,
    val createdAt: Long,
    val messageText: String,
    val messageType: String,
    val status: String,
    val source: String,
    val rawPayload: String? = null,
)

