package com.example.plantreminder.data.repositories

import com.example.plantreminder.domain.models.PlantDefinition
import java.time.LocalDate

interface PlantDefinitionDataSource {
    suspend fun getAllPlants(): List<PlantDefinition>

    suspend fun getEarliestStartDate(): LocalDate?
}

