package com.example.mmapp.app1.data.input.validation

import com.example.mmapp.app1.domain.models.PlantDefinition

data class PlantDefinitionFactoryResult(
    val plantDefinition: PlantDefinition?,
    val warnings: List<String>,
)

interface PlantDefinitionFactory {
    fun create(rawJson: String, sourceName: String): PlantDefinitionFactoryResult
}
