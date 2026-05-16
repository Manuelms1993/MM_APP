package com.example.mmapp.app1.ui.usecases

import com.example.mmapp.app1.data.repositories.PlantDefinitionDataSource
import com.example.mmapp.app1.domain.models.PlantDefinition

class LoadPlantsUseCase(
    private val plantDefinitionDataSource: PlantDefinitionDataSource,
) {
    suspend operator fun invoke(): List<PlantDefinition> = plantDefinitionDataSource.getAllPlants()
}
