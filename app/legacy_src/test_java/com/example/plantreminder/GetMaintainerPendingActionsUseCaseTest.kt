package com.example.plantreminder

import com.example.plantreminder.data.repositories.PlantDefinitionDataSource
import com.example.plantreminder.data.repositories.WeatherDataSource
import com.example.plantreminder.data.repositories.WeatherRefreshResult
import com.example.plantreminder.domain.DailyActionCalculator
import com.example.plantreminder.domain.models.FertilizerRule
import com.example.plantreminder.domain.models.PlantDefinition
import com.example.plantreminder.domain.models.Season
import com.example.plantreminder.domain.models.SeasonalFrequency
import com.example.plantreminder.domain.models.WeatherDay
import com.example.plantreminder.domain.models.WateringRule
import com.example.plantreminder.domain.usecases.GetMaintainerPendingActionsUseCase
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.time.LocalDate

class GetMaintainerPendingActionsUseCaseTest {
    @Test
    fun returnsOnlyActionsForSelectedMaintainer() = runTest {
        val date = LocalDate.parse("2026-05-13")
        val useCase = GetMaintainerPendingActionsUseCase(
            plantDefinitionRepository = FakePlantsForMaintainerUseCase(
                listOf(
                    maintainerPlant("Romero", "L", date.minusDays(2)),
                    maintainerPlant("Ficus", "R", date.minusDays(2)),
                ),
            ),
            weatherDataSource = FakeWeatherForMaintainerUseCase(),
            dailyActionCalculator = DailyActionCalculator(),
        )

        val lActions = useCase(maintainer = "L", date = date)
        val rActions = useCase(maintainer = "R", date = date)

        assertThat(lActions.map { it.plantName }).containsExactly("Romero")
        assertThat(rActions.map { it.plantName }).containsExactly("Ficus")
    }
}

private class FakePlantsForMaintainerUseCase(
    private val plants: List<PlantDefinition>,
) : PlantDefinitionDataSource {
    override suspend fun getAllPlants(): List<PlantDefinition> = plants

    override suspend fun getEarliestStartDate(): LocalDate? = plants.minOfOrNull { it.fechaInicio }
}

private class FakeWeatherForMaintainerUseCase : WeatherDataSource {
    override fun observeWeather(): Flow<List<WeatherDay>> = emptyFlow()

    override suspend fun getWeatherDays(): List<WeatherDay> = emptyList()

    override suspend fun refreshWeather(): WeatherRefreshResult = error("Not needed")
}

private fun maintainerPlant(
    nombre: String,
    responsable: String,
    start: LocalDate,
): PlantDefinition = PlantDefinition(
    id = nombre.lowercase(),
    nombre = nombre,
    especie = null,
    activada = true,
    fechaInicio = start,
    fechaFin = null,
    responsable = responsable,
    mostrarEnSiembra = false,
    mesesSiembra = emptyList(),
    mesesRecoleccion = emptyList(),
    riego = WateringRule(SeasonalFrequency(mapOf(Season.PRIMAVERA to 1)), emptyList()),
    abono = mapOf(Season.PRIMAVERA to listOf<FertilizerRule>()),
    exposicionSolar = null,
    interior = true,
    notas = emptyList(),
    composicionMaceta = null,
    fuenteInformacionUrl = null,
    fuenteSustratoUrl = null,
    plagas = null,
    metadata = emptyMap(),
    rawPayload = "{}",
)
