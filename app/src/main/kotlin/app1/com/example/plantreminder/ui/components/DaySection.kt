package com.example.mmapp.app1.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.mmapp.app1.domain.models.ConversationMessage
import java.time.LocalDate

@Composable
fun DaySection(
    date: LocalDate,
    messages: List<ConversationMessage>,
    activeMaintainer: String,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = formatDateHeader(date),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 4.dp),
        )
        messages.forEach { message ->
            MessageBubble(
                message = message,
                maintainerFilter = activeMaintainer,
            )
        }
    }
}

private fun formatDateHeader(date: LocalDate): String {
    val today = LocalDate.now()
    return when (date) {
        today -> "Hoy · $date"
        today.minusDays(1) -> "Ayer · $date"
        else -> date.toString()
    }
}
