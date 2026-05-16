package com.example.mmapp.app1.domain.models

import java.time.LocalDate

data class ConversationMessage(
    val id: Long,
    val date: LocalDate,
    val createdAt: Long,
    val messageText: String,
    val messageType: MessageType,
    val status: MessageStatus,
    val source: MessageSource,
    val rawPayload: String? = null,
)

