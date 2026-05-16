package com.example.plantreminder.domain

import com.example.plantreminder.domain.models.DailyMessage
import com.example.plantreminder.domain.models.DailyPlantAction
import com.example.plantreminder.domain.models.MessageType
import com.example.plantreminder.domain.models.PlantActionType
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
                    add("  - Regar")
                    wateringActions.flatMap { it.details }.forEach { detail ->
                        add("    · $detail")
                    }
                }

                val pestActions = plantActions.filter { it.actionType == PlantActionType.INSPECT_PESTS }
                if (pestActions.isNotEmpty()) {
                    add("  - Revisar plagas")
                    pestActions.flatMap { it.details }.forEach { detail ->
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
