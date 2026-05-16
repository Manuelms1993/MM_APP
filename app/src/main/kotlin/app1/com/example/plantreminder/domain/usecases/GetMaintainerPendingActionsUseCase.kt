package com.example.mmapp.app1.domain.usecases

import com.example.mmapp.app1.data.repositories.PlantDefinitionDataSource
import com.example.mmapp.app1.data.repositories.WeatherDataSource
import com.example.mmapp.app1.domain.DailyActionCalculator
import com.example.mmapp.app1.domain.models.DailyPlantAction
import java.time.LocalDate

class GetMaintainerPendingActionsUseCase(
    private val plantDefinitionRepository: PlantDefinitionDataSource,
    private val weatherDataSource: WeatherDataSource,
    private val dailyActionCalculator: DailyActionCalculator,
) {
    suspend operator fun invoke(
        maintainer: String,
        date: LocalDate = LocalDate.now(),
    ): List<DailyPlantAction> {
        val normalizedMaintainer = maintainer.uppercase()
        val plants = plantDefinitionRepository.getAllPlants()
        val weatherDays = weatherDataSource.getWeatherDays()
        return dailyActionCalculator.calculate(date, plants, weatherDays)
            .filter { action ->
                plants.firstOrNull { it.id == action.plantId }?.responsable?.uppercase() == normalizedMaintainer
            }
    }
}
