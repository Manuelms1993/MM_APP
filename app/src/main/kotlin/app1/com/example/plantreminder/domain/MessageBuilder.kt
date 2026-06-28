package com.example.mmapp.app1.domain

import com.example.mmapp.app1.domain.models.DailyMessage
import com.example.mmapp.app1.domain.models.DailyPlantAction
import com.example.mmapp.app1.domain.models.MessageType
import com.example.mmapp.app1.domain.models.PlantActionType
import java.time.LocalDate

class MessageBuilder {
    fun build(date: LocalDate, actions: List<DailyPlantAction>): DailyMessage {
        if (actions.isEmpty()) {
            return DailyMessage(
                date = date,
                text = "Sin acciones para este día.",
                messageType = MessageType.NO_ACTIONS,
                rawPayload = "[]",
            )
        }

        val grouped = actions.groupBy { it.plantName }.toSortedMap(String.CASE_INSENSITIVE_ORDER)
        val lines = buildList {
            grouped.forEach { (plantName, plantActions) ->
                add("• $plantName")
                val wateringActions = plantActions.filter { it.actionType == PlantActionType.WATER }
                if (wateringActions.isNotEmpty()) {
                    val wateringTitle = wateringActions.first().title
                        .replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
                    add("  - $wateringTitle")
                    wateringActions.flatMap { it.details }.forEach { detail ->
                        add("    · $detail")
                    }
                }

                val fertilizerActions = plantActions.filter { it.actionType == PlantActionType.FERTILIZE }
                if (fertilizerActions.isNotEmpty()) {
                    add("  - Abono")
                    fertilizerActions.forEach { action ->
                        add("    · ${action.title.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }}")
                        action.details.forEach { detail ->
                            add("    · $detail")
                        }
                    }
                }
                add("")
            }
        }.dropLastWhile { it.isBlank() }

        return DailyMessage(
            date = date,
            text = lines.joinToString("\n"),
            messageType = MessageType.ACTIONS,
            rawPayload = actions.joinToString(prefix = "[", postfix = "]", separator = ",") { it.rawPayload ?: "{}" },
        )
    }
}
