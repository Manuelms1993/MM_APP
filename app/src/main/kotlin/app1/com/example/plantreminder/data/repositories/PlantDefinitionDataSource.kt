package com.example.mmapp.app1.data.repositories

import com.example.mmapp.app1.domain.models.PlantDefinition
import java.time.LocalDate

interface PlantDefinitionDataSource {
    suspend fun getAllPlants(): List<PlantDefinition>

    suspend fun getEarliestStartDate(): LocalDate?
}

