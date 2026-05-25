package com.example.mmapp.app1.data.input

import com.example.mmapp.app1.data.input.validation.PlantDefinitionFactory
import com.example.mmapp.app1.data.input.validation.PlantDefinitionFactoryResult
import com.example.mmapp.app1.domain.models.FertilizerDoseMode
import com.example.mmapp.app1.domain.models.FertilizerRule
import com.example.mmapp.app1.domain.models.PlantDefinition
import com.example.mmapp.app1.domain.models.PottingMix
import com.example.mmapp.app1.domain.models.PottingMixComponent
import com.example.mmapp.app1.domain.models.Season
import com.example.mmapp.app1.domain.models.SeasonalFrequency
import com.example.mmapp.app1.domain.models.WateringRule
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonPrimitive
import java.time.LocalDate

@OptIn(ExperimentalSerializationApi::class)
class FlexibleTaskParser(
    private val json: Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        explicitNulls = false
    },
) : PlantDefinitionFactory {
    fun parse(rawJson: String, sourceName: String = "<memory>"): PlantDefinitionFactoryResult = create(
        rawJson = rawJson,
        sourceName = sourceName,
    )

    override fun create(rawJson: String, sourceName: String): PlantDefinitionFactoryResult {
        val root = runCatching { json.parseToJsonElement(rawJson) }.getOrElse {
            return PlantDefinitionFactoryResult(
                plantDefinition = null,
                warnings = listOf("No se pudo parsear $sourceName: JSON inválido."),
            )
        }

        val obj = root as? JsonObject ?: return PlantDefinitionFactoryResult(
            plantDefinition = null,
            warnings = listOf("Se ignoró $sourceName: la raíz no es un objeto."),
        )

        val plant = obj["planta"] as? JsonObject ?: return PlantDefinitionFactoryResult(
            plantDefinition = null,
            warnings = listOf("Se ignoró $sourceName: falta el nodo 'planta'."),
        )

        val warnings = mutableListOf<String>()
        val startDateText = plant.string("fechaInicio")
        val startDate = startDateText?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
        if (startDate == null) {
            warnings += "Se ignoró $sourceName: fechaInicio inválida o ausente."
            return PlantDefinitionFactoryResult(null, warnings)
        }

        val endDate = plant.string("fechaFin")?.takeUnless { it.equals("null", ignoreCase = true) }?.let {
            runCatching { LocalDate.parse(it) }.getOrNull().also { parsed ->
                if (parsed == null) warnings += "fechaFin inválida en $sourceName."
            }
        }

        val plantDefinition = PlantDefinition(
            id = plant.string("id").orEmpty().ifBlank { sourceName.substringBeforeLast(".json") },
            nombre = plant.string("nombre").orEmpty().ifBlank { "Planta" },
            especie = plant.string("especie"),
            activada = plant["activada"]?.jsonPrimitive?.booleanOrNull ?: true,
            fechaInicio = startDate,
            fechaFin = endDate,
            responsable = plant.string("responsable").orEmpty().uppercase().takeIf { it == "L" || it == "R" } ?: "L",
            mostrarEnSiembra = plant["mostrarEnSiembra"]?.jsonPrimitive?.booleanOrNull ?: false,
            mesesSiembra = plant.intList("mesesSiembra"),
            mesesRecoleccion = plant.intList("mesesRecoleccion"),
            riego = parseWateringRule(plant["riego"], warnings, sourceName),
            abono = parseFertilizerRulesBySeason(plant["abono"], warnings, sourceName),
            exposicionSolar = plant.string("exposicionSolar"),
            interior = plant["interior"]?.jsonPrimitive?.booleanOrNull ?: false,
            notas = plant.stringList("notas"),
            composicionMaceta = parsePottingMix(plant["composicionMaceta"], warnings, sourceName),
            fuenteInformacionUrl = plant.string("fuenteInformacionUrl"),
            fuenteSustratoUrl = plant.string("fuenteSustratoUrl"),
            metadata = plant.stringMap("metadata"),
            rawPayload = rawJson,
        )

        return PlantDefinitionFactoryResult(plantDefinition, warnings)
    }

    private fun parseWateringRule(
        element: JsonElement?,
        warnings: MutableList<String>,
        sourceName: String,
    ): WateringRule? {
        val obj = element as? JsonObject ?: return null
        val seasonal = parseSeasonalFrequency(obj["cadaDias"], warnings, sourceName)
        val notes = obj.stringList("notas")
        return seasonal?.let { WateringRule(it, notes) }
    }

    private fun parseSeasonalFrequency(
        element: JsonElement?,
        warnings: MutableList<String>,
        sourceName: String,
    ): SeasonalFrequency? {
        val obj = element as? JsonObject ?: return null
        val frequencies = buildMap {
            Season.entries.forEach { season ->
                val key = season.jsonKey()
                val value = obj[key]?.jsonPrimitive?.intOrNull
                if (value != null && value > 0) {
                    put(season, value)
                } else if (obj.containsKey(key)) {
                    warnings += "Frecuencia inválida para $key en $sourceName."
                }
            }
        }
        return SeasonalFrequency(frequencies)
    }

    private fun parseFertilizerRulesBySeason(
        element: JsonElement?,
        warnings: MutableList<String>,
        sourceName: String,
    ): Map<Season, List<FertilizerRule>> {
        val obj = element as? JsonObject ?: return emptyMap()
        return Season.entries.associateWith { season ->
            (obj[season.jsonKey()] as? JsonArray)?.mapNotNull { item ->
                parseFertilizerRule(item, warnings, sourceName)
            }.orEmpty()
        }
    }

    private fun parseFertilizerRule(
        element: JsonElement,
        warnings: MutableList<String>,
        sourceName: String,
    ): FertilizerRule? {
        val obj = element as? JsonObject ?: return null
        val cadaDias = obj["cadaDias"]?.jsonPrimitive?.intOrNull
        if (cadaDias == null || cadaDias <= 0) {
            warnings += "Regla de abono inválida en $sourceName: cadaDias ausente o no válido."
            return null
        }
        val rawDose = obj.string("dosis")
        return FertilizerRule(
            tipo = obj.string("tipo").orEmpty().ifBlank { "fertilizante" },
            cadaDias = cadaDias,
            cantidad = obj.string("cantidad"),
            dosis = rawDose.toDoseMode(),
            dosisTextoOriginal = rawDose,
            recordatorio = obj.string("recordatorio"),
        )
    }

    private fun parsePottingMix(
        element: JsonElement?,
        warnings: MutableList<String>,
        sourceName: String,
    ): PottingMix? {
        val obj = element as? JsonObject ?: return null
        val components = (obj["componentes"] as? JsonArray)?.mapNotNull { item ->
            val component = item as? JsonObject ?: return@mapNotNull null
            val material = component.string("material")
            val percentage = component["porcentaje"]?.jsonPrimitive?.intOrNull
            if (material.isNullOrBlank() || percentage == null || percentage !in 1..100) {
                warnings += "Componente de sustrato inválido en $sourceName."
                null
            } else {
                PottingMixComponent(material = material, percentage = percentage)
            }
        }.orEmpty()

        if (components.isEmpty()) return null
        val totalPercentage = components.sumOf { it.percentage }
        if (totalPercentage != 100) {
            warnings += "La composición de maceta en $sourceName no suma 100%."
        }

        return PottingMix(
            components = components,
            notes = obj.stringList("notas"),
        )
    }

    private fun String?.toDoseMode(): FertilizerDoseMode = when {
        this == null -> FertilizerDoseMode.NORMAL
        equals("fabricante_dividido_2", ignoreCase = true) -> FertilizerDoseMode.FABRICANTE_DIVIDIDO_2
        isBlank() || equals("normal", ignoreCase = true) -> FertilizerDoseMode.NORMAL
        else -> FertilizerDoseMode.PERSONALIZADO
    }

    private fun Season.jsonKey(): String = when (this) {
        Season.PRIMAVERA -> "primavera"
        Season.VERANO -> "verano"
        Season.OTONO -> "otono"
        Season.INVIERNO -> "invierno"
    }

    private fun JsonObject.string(key: String): String? = this[key]?.jsonPrimitive?.contentOrNull

    private fun JsonObject.stringList(key: String): List<String> =
        (this[key] as? JsonArray)?.mapNotNull { it.jsonPrimitive.contentOrNull } ?: emptyList()

    private fun JsonObject.intList(key: String): List<Int> =
        (this[key] as? JsonArray)?.mapNotNull { it.jsonPrimitive.intOrNull } ?: emptyList()

    private fun JsonObject.stringMap(key: String): Map<String, String> =
        (this[key] as? JsonObject)?.mapNotNull { (mapKey, value) ->
            value.jsonPrimitive.contentOrNull?.let { mapKey to it }
        }?.toMap().orEmpty()
}
