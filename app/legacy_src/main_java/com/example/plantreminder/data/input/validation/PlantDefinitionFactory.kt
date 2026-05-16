package com.example.plantreminder.data.input.validation

import com.example.plantreminder.domain.models.PlantDefinition

data class PlantDefinitionFactoryResult(
    val plantDefinition: PlantDefinition?,
    val warnings: List<String>,
)

interface PlantDefinitionFactory {
    fun create(rawJson: String, sourceName: String): PlantDefinitionFactoryResult
}
