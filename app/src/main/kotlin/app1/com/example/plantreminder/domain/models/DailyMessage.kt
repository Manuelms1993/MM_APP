package com.example.mmapp.app1.domain.models

import java.time.LocalDate

data class DailyMessage(
    val date: LocalDate,
    val text: String,
    val messageType: MessageType,
    val rawPayload: String? = null,
)

