package com.example.mmapp.app1

import com.example.mmapp.app1.domain.DailyActionCalculator
import com.example.mmapp.app1.domain.models.FertilizerDoseMode
import com.example.mmapp.app1.domain.models.FertilizerRule
import com.example.mmapp.app1.domain.models.PlantActionType
import com.example.mmapp.app1.domain.models.PlantDefinition
import com.example.mmapp.app1.domain.models.Season
import com.example.mmapp.app1.domain.models.SeasonalFrequency
import com.example.mmapp.app1.domain.models.WateringRule
import com.example.mmapp.app1.domain.models.WeatherDay
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.time.LocalDate

class DailyActionCalculatorTest {
    private val calculator = DailyActionCalculator()

    @Test
    fun calculatesWateringBySeason() {
        val plant = plantDefinition(
            nombre = "Romero",
            start = LocalDate.parse("2026-05-01"),
            watering = mapOf(Season.PRIMAVERA to 5),
        )

        val result = calculator.calculate(LocalDate.parse("2026-05-06"), listOf(plant))

        assertThat(result).hasSize(1)
        assertThat(result.first().actionType).isEqualTo(PlantActionType.WATER)
    }

    @Test
    fun calculatesFertilizerBySeason() {
        val plant = plantDefinition(
            nombre = "Romero",
            start = LocalDate.parse("2026-05-01"),
            fertilizerBySeason = mapOf(
                Season.PRIMAVERA to listOf(
                    FertilizerRule(
                        tipo = "humus_lombriz",
                        cadaDias = 5,
                        cantidad = "superficie ligera",
                        dosis = FertilizerDoseMode.NORMAL,
                        dosisTextoOriginal = null,
                        recordatorio = null,
                    ),
                ),
            ),
        )

        val result = calculator.calculate(LocalDate.parse("2026-05-06"), listOf(plant))

        assertThat(result.any { it.title == "aplicar humus de lombriz" }).isTrue()
    }

    @Test
    fun includesHalfDoseReminderForLiquidFertilizer() {
        val plant = plantDefinition(
            nombre = "Romero",
            start = LocalDate.parse("2026-05-01"),
            fertilizerBySeason = mapOf(
                Season.PRIMAVERA to listOf(
                    FertilizerRule(
                        tipo = "fertilizante_liquido_mediterraneas",
                        cadaDias = 5,
                        cantidad = null,
                        dosis = FertilizerDoseMode.FABRICANTE_DIVIDIDO_2,
                        dosisTextoOriginal = "fabricante_dividido_2",
                        recordatorio = "Mejor quedarse corto que sobrefertilizar.",
                    ),
                ),
            ),
        )

        val result = calculator.calculate(LocalDate.parse("2026-05-06"), listOf(plant))

        assertThat(result.single().details).contains("media dosis")
    }

    @Test
    fun ignoresInactivePlantsBeforeStartDate() {
        val plant = plantDefinition(
            nombre = "Romero",
            start = LocalDate.parse("2026-05-10"),
            watering = mapOf(Season.PRIMAVERA to 1),
        )

        val result = calculator.calculate(LocalDate.parse("2026-05-06"), listOf(plant))

        assertThat(result).isEmpty()
    }

    @Test
    fun ignoresPlantsAfterEndDate() {
        val plant = plantDefinition(
            nombre = "Albahaca",
            start = LocalDate.parse("2026-04-01"),
            end = LocalDate.parse("2026-05-05"),
            watering = mapOf(Season.PRIMAVERA to 1),
        )

        val result = calculator.calculate(LocalDate.parse("2026-05-06"), listOf(plant))

        assertThat(result).isEmpty()
    }

    @Test
    fun ignoresDisabledPlants() {
        val plant = plantDefinition(
            nombre = "Rabano",
            start = LocalDate.parse("2026-05-15"),
            watering = mapOf(Season.PRIMAVERA to 1),
        ).copy(activada = false)

        val result = calculator.calculate(LocalDate.parse("2026-05-16"), listOf(plant))

        assertThat(result).isEmpty()
    }

    @Test
    fun triggersWateringExactlyFromStartDateCadence() {
        val plant = plantDefinition(
            nombre = "Tomate Cherry",
            start = LocalDate.parse("2026-04-20"),
            watering = mapOf(Season.PRIMAVERA to 2),
        )

        val noMatch = calculator.calculate(LocalDate.parse("2026-04-21"), listOf(plant))
        val match = calculator.calculate(LocalDate.parse("2026-04-22"), listOf(plant))

        assertThat(noMatch).isEmpty()
        assertThat(match.map { it.actionType }).containsExactly(PlantActionType.WATER)
    }

    @Test
    fun triggersFertilizerExactlyFromStartDateCadence() {
        val plant = plantDefinition(
            nombre = "Albahaca",
            start = LocalDate.parse("2026-04-20"),
            fertilizerBySeason = mapOf(
                Season.PRIMAVERA to listOf(
                    FertilizerRule(
                        tipo = "compost_suave_hierbas",
                        cadaDias = 30,
                        cantidad = "capa fina",
                        dosis = FertilizerDoseMode.NORMAL,
                        dosisTextoOriginal = null,
                        recordatorio = null,
                    ),
                ),
            ),
        )

        val noMatch = calculator.calculate(LocalDate.parse("2026-05-19"), listOf(plant))
        val match = calculator.calculate(LocalDate.parse("2026-05-20"), listOf(plant))

        assertThat(noMatch).isEmpty()
        assertThat(match.map { it.title }).containsExactly("aplicar compost suave hierbas")
    }

    @Test
    fun usesCurrentSeasonFrequencyForSamePlant() {
        val plant = plantDefinition(
            nombre = "Romero",
            start = LocalDate.parse("2026-05-01"),
            watering = mapOf(
                Season.PRIMAVERA to 5,
                Season.VERANO to 3,
            ),
        )

        val springMatch = calculator.calculate(LocalDate.parse("2026-05-06"), listOf(plant))
        val summerMatch = calculator.calculate(LocalDate.parse("2026-06-03"), listOf(plant))
        val summerNoMatch = calculator.calculate(LocalDate.parse("2026-06-04"), listOf(plant))

        assertThat(springMatch.map { it.actionType }).containsExactly(PlantActionType.WATER)
        assertThat(summerMatch.map { it.actionType }).containsExactly(PlantActionType.WATER)
        assertThat(summerNoMatch).isEmpty()
    }

    @Test
    fun skipsOutdoorWateringWhenRelevantRainFellYesterday() {
        val plant = plantDefinition(
            nombre = "Romero",
            start = LocalDate.parse("2026-05-01"),
            watering = mapOf(Season.PRIMAVERA to 5),
        )

        val result = calculator.calculate(
            date = LocalDate.parse("2026-05-06"),
            plants = listOf(plant),
            weatherDays = listOf(
                weatherDay(date = LocalDate.parse("2026-05-05"), precipitationMm = 4.0),
            ),
        )

        assertThat(result).isEmpty()
    }

    @Test
    fun keepsWateringForIndoorPlantDespiteRecentRain() {
        val plant = plantDefinition(
            nombre = "Albahaca",
            start = LocalDate.parse("2026-05-01"),
            watering = mapOf(Season.PRIMAVERA to 5),
            interior = true,
        )

        val result = calculator.calculate(
            date = LocalDate.parse("2026-05-06"),
            plants = listOf(plant),
            weatherDays = listOf(
                weatherDay(date = LocalDate.parse("2026-05-05"), precipitationMm = 4.0),
            ),
        )

        assertThat(result.map { it.actionType }).containsExactly(PlantActionType.WATER)
    }

    @Test
    fun keepsWateringForRoofedPlantDespiteRecentRain() {
        val plant = plantDefinition(
            nombre = "Olivo",
            start = LocalDate.parse("2026-05-01"),
            watering = mapOf(Season.PRIMAVERA to 5),
            metadata = mapOf("techado" to "true"),
        )

        val result = calculator.calculate(
            date = LocalDate.parse("2026-05-06"),
            plants = listOf(plant),
            weatherDays = listOf(
                weatherDay(date = LocalDate.parse("2026-05-04"), precipitationMm = 4.0),
            ),
        )

        assertThat(result.map { it.actionType }).containsExactly(PlantActionType.WATER)
    }

}

private fun plantDefinition(
    nombre: String,
    start: LocalDate,
    end: LocalDate? = null,
    watering: Map<Season, Int> = emptyMap(),
    fertilizerBySeason: Map<Season, List<FertilizerRule>> = emptyMap(),
    interior: Boolean = false,
    metadata: Map<String, String> = emptyMap(),
): PlantDefinition = PlantDefinition(
    id = nombre.lowercase(),
    nombre = nombre,
    especie = null,
    activada = true,
    fechaInicio = start,
    fechaFin = end,
    responsable = "L",
    mostrarEnSiembra = false,
    mesesSiembra = emptyList(),
    mesesRecoleccion = emptyList(),
    riego = WateringRule(SeasonalFrequency(watering), emptyList()),
    abono = fertilizerBySeason,
    exposicionSolar = null,
    interior = interior,
    notas = emptyList(),
    composicionMaceta = null,
    fuenteInformacionUrl = null,
    fuenteSustratoUrl = null,
    metadata = metadata,
    rawPayload = "{}",
)

private fun weatherDay(
    date: LocalDate,
    precipitationMm: Double,
): WeatherDay = WeatherDay(
    date = date,
    locationName = "Rafelbunyol",
    latitude = 39.59,
    longitude = -0.33,
    rainMm = precipitationMm,
    precipitationMm = precipitationMm,
    precipitationProbabilityMax = 100,
    precipitationHours = 4.0,
    provider = "open-meteo",
    fetchedAt = 0L,
)
