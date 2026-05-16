package com.example.mmapp.app1.data.input.validation

import com.example.mmapp.app1.data.input.RawPlantInput
import com.example.mmapp.app1.domain.models.PlantDefinition

data class PlantCatalogValidationReport(
    val plants: List<PlantDefinition>,
    val warnings: List<String>,
)

class PlantCatalogValidator(
    private val plantDefinitionFactory: PlantDefinitionFactory,
    private val plantDefinitionValidator: PlantDefinitionValidator,
) {
    fun validate(inputs: List<RawPlantInput>): PlantCatalogValidationReport {
        val warnings = mutableListOf<String>()
        val plants = inputs.mapNotNull { input ->
            val result = plantDefinitionFactory.create(input.rawJson, input.fileName)
            warnings += result.warnings
            val plant = result.plantDefinition ?: return@mapNotNull null
            warnings += plantDefinitionValidator.validate(plant, input.fileName)
            plant
        }

        warnings += duplicateIdWarnings(plants)
        warnings += duplicateNameWarnings(plants)

        return PlantCatalogValidationReport(
            plants = plants.sortedWith(compareBy<PlantDefinition> { it.fechaInicio }.thenBy { it.nombre.lowercase() }),
            warnings = warnings,
        )
    }

    private fun duplicateIdWarnings(plants: List<PlantDefinition>): List<String> = plants
        .groupBy { it.id }
        .filterValues { it.size > 1 }
        .keys
        .sorted()
        .map { "ID duplicado en inputs: $it" }

    private fun duplicateNameWarnings(plants: List<PlantDefinition>): List<String> = plants
        .groupBy { it.nombre.lowercase() }
        .filterValues { it.size > 1 }
        .values
        .sortedBy { it.first().nombre.lowercase() }
        .map { "Nombre duplicado en inputs: ${it.first().nombre}" }
}
