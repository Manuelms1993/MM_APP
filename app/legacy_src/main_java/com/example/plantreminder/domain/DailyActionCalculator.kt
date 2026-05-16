package com.example.plantreminder.domain

import com.example.plantreminder.domain.models.DailyPlantAction
import com.example.plantreminder.domain.models.FertilizerDoseMode
import com.example.plantreminder.domain.models.PlantActionType
import com.example.plantreminder.domain.models.PlantDefinition
import com.example.plantreminder.domain.models.Season
import com.example.plantreminder.domain.models.WeatherDay
import java.time.LocalDate
import java.time.temporal.ChronoUnit

class DailyActionCalculator(
    private val plantActionTextFactory: PlantActionTextFactory = PlantActionTextFactory(),
) {
    fun calculate(
        date: LocalDate,
        plants: List<PlantDefinition>,
        weatherDays: List<WeatherDay> = emptyList(),
    ): List<DailyPlantAction> {
        val season = Season.fromMonth(date.monthValue)
        return plants.asSequence()
            .filter { it.activada }
            .filter { isActiveOn(it, date) }
            .flatMap { plant ->
                val daysSinceStart = ChronoUnit.DAYS.between(plant.fechaInicio, date).toInt()
                sequence {
                    val wateringFrequency = plant.riego?.frequenciesBySeason?.forSeason(season)
                    if (
                        wateringFrequency != null &&
                        shouldTrigger(daysSinceStart, wateringFrequency) &&
                        shouldWaterPlant(date, plant, weatherDays)
                    ) {
                        yield(
                            DailyPlantAction(
                                date = date,
                                plantId = plant.id,
                                plantName = plant.nombre,
                                actionType = PlantActionType.WATER,
                                title = plantActionTextFactory.wateringTitle(),
                                details = plant.riego.notes,
                                rawPayload = plant.rawPayload,
                            ),
                        )
                    }

                    plant.plagas?.reviewEveryDays?.takeIf { shouldTrigger(daysSinceStart, it) }?.let {
                        yield(
                            DailyPlantAction(
                                date = date,
                                plantId = plant.id,
                                plantName = plant.nombre,
                                actionType = PlantActionType.INSPECT_PESTS,
                                title = plantActionTextFactory.pestInspectionTitle(),
                                details = plantActionTextFactory.pestInspectionDetails(plant.plagas),
                                rawPayload = plant.rawPayload,
                            ),
                        )
                    }

                    plant.abono[season].orEmpty().forEach { rule ->
                        if (shouldTrigger(daysSinceStart, rule.cadaDias)) {
                            yield(
                                DailyPlantAction(
                                    date = date,
                                    plantId = plant.id,
                                    plantName = plant.nombre,
                                    actionType = PlantActionType.FERTILIZE,
                                    title = plantActionTextFactory.fertilizerTitle(rule),
                                    details = plantActionTextFactory.fertilizerDetails(rule),
                                    rawPayload = plant.rawPayload,
                                ),
                            )
                        }
                    }
                }
            }
            .sortedWith(compareBy<DailyPlantAction> { it.plantName.lowercase() }.thenBy { it.actionType.name }.thenBy { it.title })
            .toList()
    }

    private fun isActiveOn(plant: PlantDefinition, date: LocalDate): Boolean {
        if (date.isBefore(plant.fechaInicio)) return false
        val endDate = plant.fechaFin
        return endDate == null || !date.isAfter(endDate)
    }

    private fun shouldTrigger(daysSinceStart: Int, everyDays: Int): Boolean =
        daysSinceStart >= 0 && everyDays > 0 && daysSinceStart % everyDays == 0

    private fun shouldWaterPlant(
        date: LocalDate,
        plant: PlantDefinition,
        weatherDays: List<WeatherDay>,
    ): Boolean {
        if (plant.interior || plant.isRainShielded()) return true
        return !hasRelevantRecentRain(date, weatherDays)
    }

    private fun hasRelevantRecentRain(
        date: LocalDate,
        weatherDays: List<WeatherDay>,
    ): Boolean = weatherDays.any { weatherDay ->
        (weatherDay.date == date.minusDays(1) || weatherDay.date == date.minusDays(2)) &&
            maxOf(weatherDay.rainMm, weatherDay.precipitationMm) >= RelevantRainMm
    }

    private fun PlantDefinition.isRainShielded(): Boolean =
        metadata["techado"].equals("true", ignoreCase = true) ||
            metadata["cubierto"].equals("true", ignoreCase = true)

    private companion object {
        const val RelevantRainMm = 2.0
    }
}
