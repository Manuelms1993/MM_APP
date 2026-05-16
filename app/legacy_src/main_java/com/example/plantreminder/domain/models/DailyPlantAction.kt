package com.example.plantreminder.domain.models

import java.time.LocalDate

data class DailyPlantAction(
    val date: LocalDate,
    val plantId: String,
    val plantName: String,
    val actionType: PlantActionType,
    val title: String,
    val details: List<String>,
    val rawPayload: String?,
)

enum class PlantActionType {
    WATER,
    FERTILIZE,
    INSPECT_PESTS,
}
