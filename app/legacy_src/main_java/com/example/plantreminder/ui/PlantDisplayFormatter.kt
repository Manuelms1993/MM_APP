package com.example.plantreminder.ui

import com.example.plantreminder.domain.models.FertilizerDoseMode
import com.example.plantreminder.domain.models.FertilizerRule
import com.example.plantreminder.domain.models.PottingMix
import com.example.plantreminder.domain.models.PestMonitoringRule
import com.example.plantreminder.domain.models.Season
import com.example.plantreminder.domain.models.WateringRule
import java.time.Month
import java.time.format.TextStyle
import java.util.Locale

class PlantDisplayFormatter(
    private val locale: Locale = Locale("es", "ES"),
) {
    fun readableValue(value: String?): String {
        if (value.isNullOrBlank()) return "-"
        return value.split('_').joinToString(" ") { token ->
            token.replaceFirstChar { if (it.isLowerCase()) it.titlecase(locale) else it.toString() }
        }
    }

    fun monthSummary(months: List<Int>): String {
        if (months.isEmpty()) return "-"
        return months.sorted().joinToString(", ") { month ->
            Month.of(month).getDisplayName(TextStyle.SHORT, locale)
                .replaceFirstChar { if (it.isLowerCase()) it.titlecase(locale) else it.toString() }
        }
    }

    fun pottingMixLines(pottingMix: PottingMix?): List<String> {
        if (pottingMix == null) return listOf("-")
        val componentLines = pottingMix.components.map { component ->
            "• ${readableValue(component.material)} ${component.percentage}%"
        }
        return componentLines + pottingMix.notes.map { "• $it" }
    }

    fun wateringLines(wateringRule: WateringRule?): List<String> {
        if (wateringRule == null) return listOf("-")
        val frequencyLines = Season.entries.mapNotNull { season ->
            wateringRule.frequenciesBySeason.forSeason(season)?.let { everyDays ->
                "• ${seasonLabel(season)}: cada $everyDays días"
            }
        }
        return (frequencyLines + wateringRule.notes.map { "• $it" }).ifEmpty { listOf("-") }
    }

    fun fertilizerLines(fertilizerRules: Map<Season, List<FertilizerRule>>): List<String> {
        val lines = Season.entries.flatMap { season ->
            fertilizerRules[season].orEmpty().map { rule ->
                val details = buildList {
                    add("cada ${rule.cadaDias} días")
                    rule.cantidad?.takeIf { it.isNotBlank() }?.let { add("cantidad: $it") }
                    when (rule.dosis) {
                        FertilizerDoseMode.FABRICANTE_DIVIDIDO_2 -> add("media dosis")
                        FertilizerDoseMode.PERSONALIZADO -> rule.dosisTextoOriginal?.takeIf { it.isNotBlank() }?.let { add("dosis: ${readableValue(it)}") }
                        FertilizerDoseMode.NORMAL -> Unit
                    }
                    rule.recordatorio?.takeIf { it.isNotBlank() }?.let { add(it) }
                }
                "• ${seasonLabel(season)}: ${readableValue(rule.tipo)} (${details.joinToString(", ")})"
            }
        }
        return if (lines.isEmpty()) listOf("-") else lines
    }

    fun pestMonitoringLines(rule: PestMonitoringRule?): List<String> {
        if (rule == null) return listOf("-")
        val lines = buildList {
            rule.reviewEveryDays?.let { add("• Revisar cada $it días") }
            if (rule.commonIssues.isNotEmpty()) {
                add("• Vigilar: ${rule.commonIssues.joinToString(", ") { readableValue(it) }}")
            }
            addAll(rule.notes.map { "• $it" })
        }
        return if (lines.isEmpty()) listOf("-") else lines
    }

    fun seasonLabel(season: Season): String = season.name.lowercase()
        .replaceFirstChar { if (it.isLowerCase()) it.titlecase(locale) else it.toString() }
}
