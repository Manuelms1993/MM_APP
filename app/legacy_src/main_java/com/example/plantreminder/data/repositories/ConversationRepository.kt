package com.example.plantreminder.data.repositories

import com.example.plantreminder.data.db.ConversationMessageDao
import com.example.plantreminder.data.mappers.toDomain
import com.example.plantreminder.data.mappers.toEntity
import com.example.plantreminder.domain.models.ConversationMessage
import com.example.plantreminder.domain.models.DailyMessage
import com.example.plantreminder.domain.models.MessageSource
import com.example.plantreminder.domain.models.MessageStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

class ConversationRepository(
    private val dao: ConversationMessageDao,
) : ConversationDataSource {
    override fun getConversationFlow(): Flow<List<ConversationMessage>> = dao.getAllOrdered().map { entities ->
        entities.map { it.toDomain() }
    }

    override suspend fun appendMessageForDate(
        dailyMessage: DailyMessage,
        source: MessageSource,
        status: MessageStatus,
        createdAt: Long,
    ): AppendResult {
        val insertedId = dao.insert(
            dailyMessage.toEntity(
                createdAt = createdAt,
                source = source,
                status = status,
            ),
        )
        if (insertedId == -1L) {
            return AppendResult.SkippedExisting
        }
        return AppendResult.Inserted(insertedId)
    }

    suspend fun appendGeneratedMessageForDate(
        dailyMessage: DailyMessage,
        source: MessageSource,
        createdAt: Long = System.currentTimeMillis(),
    ): AppendResult = appendMessageForDate(
        dailyMessage = dailyMessage,
        source = source,
        status = MessageStatus.GENERATED,
        createdAt = createdAt,
    )

    override suspend fun hasMessageForDate(date: LocalDate): Boolean = dao.existsForDate(date.toString())

    override suspend fun markError(date: LocalDate) {
        dao.updateStatusForDate(date.toString(), MessageStatus.ERROR.name)
    }

    override suspend fun deleteBetween(fromDate: LocalDate, toDate: LocalDate) {
        if (fromDate.isAfter(toDate)) return
        dao.deleteBetween(fromDate.toString(), toDate.toString())
    }

    override suspend fun deleteAll() {
        dao.deleteAll()
    }
}

sealed interface AppendResult {
    data class Inserted(val id: Long) : AppendResult
    data object SkippedExisting : AppendResult
}
