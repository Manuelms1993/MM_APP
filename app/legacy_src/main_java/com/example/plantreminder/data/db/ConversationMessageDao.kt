package com.example.plantreminder.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ConversationMessageDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(entity: ConversationMessageEntity): Long

    @Query("SELECT * FROM conversation_messages ORDER BY date ASC, createdAt ASC")
    fun getAllOrdered(): Flow<List<ConversationMessageEntity>>

    @Query("SELECT * FROM conversation_messages WHERE date = :date ORDER BY createdAt ASC")
    suspend fun getMessagesForDate(date: String): List<ConversationMessageEntity>

    @Query("SELECT EXISTS(SELECT 1 FROM conversation_messages WHERE date = :date)")
    suspend fun existsForDate(date: String): Boolean

    @Query("UPDATE conversation_messages SET status = :status WHERE date = :date")
    suspend fun updateStatusForDate(date: String, status: String)

    @Query("DELETE FROM conversation_messages WHERE date BETWEEN :fromDate AND :toDate")
    suspend fun deleteBetween(fromDate: String, toDate: String)

    @Query("DELETE FROM conversation_messages")
    suspend fun deleteAll()
}
