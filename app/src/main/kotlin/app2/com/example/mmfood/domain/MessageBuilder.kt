package com.example.mmapp.app2.domain

import com.example.mmapp.app2.domain.models.DailyMessage
import com.example.mmapp.app2.domain.models.MenuDaySelection
import com.example.mmapp.app2.domain.models.MenuOption
import com.example.mmapp.app2.domain.models.MessageType

class MessageBuilder {
    fun build(selection: MenuDaySelection): DailyMessage {
        val lunchSection = buildMealSection("Comida", selection.lunchOptions)
        val dinnerSection = buildMealSection("Cena", selection.dinnerOptions)
        val messageText = listOf(lunchSection, dinnerSection).joinToString("\n")
        val hasOptions = selection.lunchOptions.isNotEmpty() || selection.dinnerOptions.isNotEmpty()

        return DailyMessage(
            date = selection.date,
            text = messageText,
            messageType = if (hasOptions) MessageType.MENU_DAY else MessageType.EMPTY_DAY,
            rawPayload = null,
        )
    }

    private fun buildMealSection(title: String, options: List<MenuOption>): String = buildString {
        append("$title:")
        if (options.isEmpty()) {
            append("\n  - Sin opciones")
            return@buildString
        }
        options.forEach { option ->
            append("\n  - ${option.name}")
        }
    }
}
