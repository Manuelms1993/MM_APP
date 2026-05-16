package com.example.plantreminder.ui.usecases

import com.example.plantreminder.data.repositories.PlantDefinitionDataSource
import com.example.plantreminder.domain.models.PlantDefinition

class LoadPlantsUseCase(
    private val plantDefinitionDataSource: PlantDefinitionDataSource,
) {
    suspend operator fun invoke(): List<PlantDefinition> = plantDefinitionDataSource.getAllPlants()
}
