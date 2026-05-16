package com.example.mmapp.app2.data.mappers

import com.example.mmapp.app2.data.db.ConversationMessageEntity
import com.example.mmapp.app2.domain.models.ConversationMessage
import com.example.mmapp.app2.domain.models.DailyMessage
import com.example.mmapp.app2.domain.models.MessageSource
import com.example.mmapp.app2.domain.models.MessageStatus
import com.example.mmapp.app2.domain.models.MessageType
import java.time.LocalDate

fun ConversationMessageEntity.toDomain(): ConversationMessage = ConversationMessage(
    id = id,
    date = LocalDate.parse(date),
    createdAt = createdAt,
    messageText = messageText,
    messageType = MessageType.valueOf(messageType),
    status = status.toMessageStatus(),
    source = MessageSource.valueOf(source),
    rawPayload = rawPayload,
)

fun DailyMessage.toEntity(
    createdAt: Long,
    source: MessageSource,
    status: MessageStatus,
): ConversationMessageEntity = ConversationMessageEntity(
    date = date.toString(),
    createdAt = createdAt,
    messageText = text,
    messageType = messageType.name,
    status = status.name,
    source = source.name,
    rawPayload = rawPayload,
)

private fun String.toMessageStatus(): MessageStatus = runCatching {
    MessageStatus.valueOf(this)
}.getOrElse {
    MessageStatus.GENERATED
}
