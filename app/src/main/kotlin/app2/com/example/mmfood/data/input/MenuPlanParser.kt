package com.example.mmapp.app2.data.input

import com.example.mmapp.app2.data.input.validation.MenuPlanFactory
import com.example.mmapp.app2.data.input.validation.MenuPlanFactoryResult
import com.example.mmapp.app2.domain.models.MenuDay
import com.example.mmapp.app2.domain.models.MenuOption
import com.example.mmapp.app2.domain.models.MenuPlan
import com.example.mmapp.app2.domain.models.MenuWeek
import java.time.LocalDate
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class MenuPlanParser(
    private val json: Json = Json { ignoreUnknownKeys = true },
) : MenuPlanFactory {
    override fun create(rawJson: String, sourceName: String): MenuPlanFactoryResult {
        val dto = runCatching { json.decodeFromString<MenuPlanDto>(rawJson) }.getOrElse {
            return MenuPlanFactoryResult(
                menuPlan = null,
                warnings = listOf("No se pudo parsear $sourceName."),
            )
        }

        val startDate = runCatching { LocalDate.parse(dto.startDate) }.getOrNull()
        if (startDate == null) {
            return MenuPlanFactoryResult(
                menuPlan = null,
                warnings = listOf("fechaInicio inválida en $sourceName."),
            )
        }

        val plan = MenuPlan(
            planType = dto.planType.trim().ifBlank { sourceName.substringBefore(".json") },
            startDate = startDate,
            weeks = dto.weeks.map { week ->
                MenuWeek(
                    weekNumber = week.weekNumber,
                    days = week.days.map { day ->
                        MenuDay(
                            dayLabel = day.dayOfWeek,
                            options = day.options.map { option ->
                                MenuOption(
                                    name = option.name,
                                    ingredients = option.ingredients.filter { it.isNotBlank() },
                                )
                            },
                        )
                    },
                )
            },
        )

        return MenuPlanFactoryResult(menuPlan = plan, warnings = emptyList())
    }
}

@Serializable
private data class MenuPlanDto(
    val schemaVersion: Int = 1,
    val planType: String,
    @SerialName("fechaInicio") val startDate: String,
    @SerialName("semanas") val weeks: List<MenuWeekDto>,
)

@Serializable
private data class MenuWeekDto(
    @SerialName("numeroSemana") val weekNumber: Int,
    @SerialName("dias") val days: List<MenuDayDto>,
)

@Serializable
private data class MenuDayDto(
    @SerialName("diaSemana") val dayOfWeek: String,
    @SerialName("opciones") val options: List<MenuOptionDto>,
)

@Serializable
private data class MenuOptionDto(
    @SerialName("nombre") val name: String,
    @SerialName("ingredientes") val ingredients: List<String>,
)
