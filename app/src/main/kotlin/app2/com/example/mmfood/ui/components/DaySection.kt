package com.example.mmapp.app2.ui.components

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
import com.example.mmapp.app2.domain.models.ConversationMessage
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun DaySection(
    date: LocalDate,
    messages: List<ConversationMessage>,
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
            MessageBubble(message = message)
        }
    }
}

private fun formatDateHeader(date: LocalDate): String {
    val today = LocalDate.now()
    val locale = Locale("es", "ES")
    val dayName = date.dayOfWeek.getDisplayName(TextStyle.FULL, locale)
        .replaceFirstChar { if (it.isLowerCase()) it.titlecase(locale) else it.toString() }
    val dateText = date.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))
    return when (date) {
        today -> "Hoy · $dayName $dateText"
        today.plusDays(1) -> "Mañana · $dayName $dateText"
        else -> "$dayName · $dateText"
    }
}
